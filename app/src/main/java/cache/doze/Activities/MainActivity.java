package cache.doze.Activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cache.doze.Fragments.AddContactsFragment;
import cache.doze.Fragments.AddNewReplyFragment;
import cache.doze.Fragments.DozeFragment;
import cache.doze.Fragments.RepliesFragment;
import cache.doze.Model.Contact;
import cache.doze.Model.ReplyItem;
import cache.doze.MonitorSmsService;
import cache.doze.R;
import cache.doze.Tools.PermissionsHelper;
import cache.doze.Views.FluidSearchView;
import cache.doze.Views.FunFab.FunFab;

/**
 * Created by Chris on 1/9/2018.
 */

public class MainActivity extends AppCompatActivity{
    public static final String PREFS_REPLY_ITEMS = "reply_items";
    public static final int NOTIFICATION_ID = 95;
    public static final String CHANNEL_ID = "notif_channel";
    public static final String DEFAULT_PREFS = "default_prefs";
    public static final String PRESET_PREF = "preset";
    public static final String SERVICE_RUNNING = "its_running";
    public static final String REPLY_ALL = "reply_all";

    public static boolean active = false;
    public static String preset;

    private SharedPreferences prefs;
    NotificationManager nMN;
    Notification.Builder runningNotification;

    public FluidSearchView fluidSearchView;
    RepliesFragment repliesFragment;
    AddContactsFragment addContactsFragment;
    FunFab fab;


    DisplayMetrics displayMetrics;
    int maxTimesMessaged = 5;
    boolean welcomeScreen = true;

    /**
     * Timer to prevent accidental spam to recipient
     */
    private static Handler messageTimer = null;
    private static int messageSpamTime = 1000;

    public static ArrayList<ReplyItem> replyItems;
    public static ArrayList<Contact> contactList = new ArrayList<>();
    public static HashMap<String, Integer> messagedContacts = new HashMap<>();

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(!permissionsGranted())return;
        startApp();
    }

    private boolean permissionsGranted(){
        if(!PermissionsHelper.checkReadSMSPermission(getBaseContext()) || !PermissionsHelper.checkReadContactsPermission(getBaseContext())
                || !PermissionsHelper.checkReceiveSMSPermission(getBaseContext())){
            Intent intent = new Intent(MainActivity.this, PermissionsActivity.class);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.slide_in_top, 0);
            return false;
        }
        return true;
    }

    private void startApp(){
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(MainActivity.DEFAULT_PREFS, Context.MODE_PRIVATE);
        MainActivity.preset = prefs.getString("preset", "");

        boolean firstTime = prefs.getBoolean("first_time", true);
        if(firstTime) {
            welcome();
            welcomeScreen = true;
        }

        setupApp();
    }

    private void setupApp(){
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


        setSupportActionBar(toolbar = (Toolbar)findViewById(R.id.toolbar));
        /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        */

        replyItems = new ArrayList<>();
        //replyItems.add(new ReplyItem("Work", "Currently out of the office, please leave a message!"));
        fab = findViewById(R.id.fab);

        if(repliesFragment == null){
            repliesFragment = new RepliesFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.replies_container, repliesFragment, "Main_Replies").commitAllowingStateLoss();
            repliesFragment.setFab(fab);
            repliesFragment.isShown = true;
        }

        if(addContactsFragment == null){
            addContactsFragment = new AddContactsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.add_contacts_container, addContactsFragment, "Add_Contacts").commitAllowingStateLoss();
            addContactsFragment.isShown = false;
            addContactsFragment.setFab(fab);
        }

        initSearchView();
        //initKeyboardListener();


    }

    public void showContactsFrag(ReplyItem replyItem){
        repliesFragment.slideOutLeft();
        addContactsFragment.slideInRight();
        addContactsFragment.setReplyItem(replyItem);
    }

    public void showMainRepliesFrag(){
        addContactsFragment.slideOutRight();
        repliesFragment.slideInLeft();
    }

    public FunFab getFab(){
        return fab == null? findViewById(R.id.fab): fab;
    }

    public void setFabExpanded(){

    }

    private void initViewPager(){
/*
                if(searchMag == null){
                    searchMag = fluidSearchView.getMagIcon();
                    return;
                }
                checkVariables();
                if(positionOffset < 1 && fluidSearchView.isOpened()){
                    fluidSearchView.staticOnClose();
                    return;
                }
                if(position == 0){
                    searchMag.setX(startMagX - (startMagX - endMagX) * positionOffset);
                    searchMag.setAlpha(positionOffset);
                    searchMag.setEnabled(false);
                    searchMag.setClickable(false);
                }else if(position == 1){
                    searchMag.setX(endMagX);
                    searchMag.setAlpha(1f);
                    searchMag.setEnabled(true);
                    searchMag.setClickable(true);
                }
            }
            private void checkVariables(){
                if(fluidSearchView.searchMagStartLeft != -1) endMagX = (int) fluidSearchView.searchMagStartLeft;
                if(endMagX != -1) {
                    startMagX = endMagX + QuickTools.convertDpToPx(searchMag.getContext(), 8);
                    searchMag.setX(startMagX);
                }
            }*/
    }

    private void initSearchView(){
        if(fluidSearchView != null) return;

        fluidSearchView = new FluidSearchView(toolbar, this);
        if(FluidSearchView.isDetached) fluidSearchView.build();
        fluidSearchView.hide();
        //fluidSearchView.setOnQueryTextListener(qqqq);
    }

    public boolean getReplyAll(){
        return prefs.getBoolean(REPLY_ALL, true);
    }

    public boolean isServiceRunning(){
        return prefs.getBoolean(SERVICE_RUNNING, false);
    }

    public ArrayList<Contact> getContactList(){
        return contactList;
    }

    public void setContactList(ArrayList<Contact> newContactList){contactList = new ArrayList<>(newContactList);}

    public void setToolbarPositive(String text){
        toolbar.setTitle(text);
        animateToolbarColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary), ContextCompat.getColor(getBaseContext(), R.color.thatsGoodGreen));
    }

    private int savedToolbarColor;
    public void setToolbarColor(int from, int to){
        animateToolbarColor(from, to);
    }
    public void setToolbarColor(int to){
        animateToolbarColor(savedToolbarColor, ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
    }

    private void animateToolbarColor(int from, int to){
        savedToolbarColor = to;
        ValueAnimator marginBottomAnimation = ValueAnimator.ofArgb(from, to).setDuration(250);
        marginBottomAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                toolbar.setBackgroundColor(color);
                getWindow().setStatusBarColor(color);
            }
        });
        marginBottomAnimation.start();
    }

    //Basic Lifecycle Methods
    @Override
    public void onStart() {
        super.onStart();
        //getPrefs().edit().putString(MainActivity.PREFS_REPLY_ITEMS, "").apply();

    }
    @Override
    protected void onResume(){
        super.onResume();
        if(MainActivity.preset != null) MainActivity.preset = prefs.getString("preset", MainActivity.preset);
        refreshReplyItems();
//        if(fluidSearchView != null && FluidSearchView.isDetached)
//            fluidSearchView.build();
    }
    @Override
    protected void onPause(){
        saveReplyItems();
        super.onPause();
//        if(fluidSearchView != null && !FluidSearchView.isDetached)
//            fluidSearchView.detach();
        //MainActivity.preset = ((RepliesFragment)viewPagerAdapter.getFragment(0)).getPresetText();
    }
    @Override
    protected void onStop(){
        super.onStop();
        //preset = presetInput.getText().toString();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    //Other @Override methods
    @Override
    public void onBackPressed(){
        for(DozeFragment dozeFragment: DozeFragment.dozeFragments){
            if(dozeFragment.isShown){
                if(dozeFragment.onBackPressed())return;
            }
        }
        super.onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        menu.findItem(R.id.action_settings).getIcon().setTint(ContextCompat.getColor(getBaseContext(), R.color.white));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "This is the settings option", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    //Handling received messaged
    SmsManager smsManager = SmsManager.getDefault();
    public void messageReceived(String number) {
        for(ReplyItem replyItem: replyItems){
            if(!replyItem.isChecked())continue;

            if(replyItem.getContacts() == null || replyItem.getContacts().isEmpty()){
                sendText(number, replyItem.getReplyText());
                continue;
            }

            if(replyItem.hasContact(number)){
                sendText(number, replyItem.getReplyText());
            }
        }
        /*
        if (MainActivity.active && canMessage(address) && !MainActivity.preset.isEmpty() && MainActivity.messageTimer == null) {
            if (!isContactSelected(address)) return;
            smsManager.sendTextMessage(address + "", null, MainActivity.preset + "", null, null);
            Log.i("Sent!", "Message: " + MainActivity.preset);
            MainActivity.messagedContacts.put(address, 0);
            messageTimer = new Handler();
            MainActivity.messageTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.messageTimer = null;
                }
            }, messageSpamTime);
        }
        */
    }

    private void sendText(String address, String msgBody){
        messageTimer = new Handler();
        MainActivity.messageTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.messageTimer = null;
            }
        }, messageSpamTime);

        smsManager.sendTextMessage(address + "", null, msgBody, null, null);
        Log.i("Sent!", "Message: " + msgBody);
    }

    //Make sure we aren't messaging someone too many times
    private boolean canMessage(String address){
        if(messagedContacts.containsKey(address)){
            int timesMessaged = messagedContacts.get(address);
            if(timesMessaged < maxTimesMessaged){
                messagedContacts.put(address, ++timesMessaged);
                return true;
            }else
                return false;
        }
        return true;
    }
    private boolean isContactSelected(String address){
        Contact contact = new Contact("placeholder", "");
        for(Contact c: contactList){
            if(c.getAddress().equals(address))contact = c;
        }
        return contact.getSelected();
    }
    public void checkAll(boolean checked){
        prefs.edit().putBoolean(REPLY_ALL, checked).apply();

        for(Contact c: contactList){
            c.setSelected(checked);
        }

//        AddContactsFragment page2 =
//                (AddContactsFragment) viewPagerAdapter.getFragment(1);
//        page2.updateCheckStates();
    }

    public Snackbar startSMSService(){
        if(checkRunning(1))return null;

        MainActivity.active = true;
        prefs.edit().putBoolean(MainActivity.SERVICE_RUNNING, true).apply();

        Intent myService = new Intent(this, MonitorSmsService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(myService);
        } else {
            startService(myService);
        }
        Snackbar snackbar = Snackbar.make(toolbar.getRootView(), "Reply Service Started!", Snackbar.LENGTH_SHORT);
        snackbar.show();
        return snackbar;
    }
    public Snackbar endSMSService(){
        if(checkRunning(0))return null;

        MainActivity.active = false;
        prefs.edit().putBoolean(MainActivity.SERVICE_RUNNING, false).apply();

        stopService(new Intent(this, MonitorSmsService.class));

        Snackbar snackbar = Snackbar.make(toolbar.getRootView(), "Reply Service Dismissed", Snackbar.LENGTH_SHORT);
        snackbar.show();
        return snackbar;
    }

    private boolean checkRunning(int leniency){
        leniency++;
        int count = 0;
        for(ReplyItem replyItem: replyItems){
            if(replyItem.isChecked())
                count++;
            if(count >= leniency)
                return true;
        }
        return false;
    }


    public SharedPreferences getPrefs(){
        return prefs;
    }

    //Handle from Permissions Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                startApp();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // TODO
            }
        }
    }

    //Startup
    private void welcome() {
        /*
        final TextView welcome = new TextView(getBaseContext());
        welcome.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        welcome.setText("Sleep Tight");
        Typeface aye = Typeface.createFromAsset(getAssets(), "fonts/Pecita.otf");
        welcome.setTypeface(aye);
        welcome.setTextColor(getResources().getColor(R.color.colorAccent));
        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        welcome.setGravity(Gravity.CENTER_HORIZONTAL);
        welcome.setLayoutParams(lp);
        ((RelativeLayout)findViewById(R.id.activity_main)).addView(welcome);
        welcome.setY((int)(Resources.getSystem().getDisplayMetrics().heightPixels));
        welcome.setY(welcome.getY() + welcome.getLayoutParams().height);

        final View fadeBG = findViewById(R.id.fadeBackground);
        fadeBG.setVisibility(View.VISIBLE);
        fadeBG.bringToFront();
        welcome.bringToFront();
        final float y = welcome.getY();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ObjectAnimator welcomeUp = ObjectAnimator.ofFloat(welcome, "y", y,
                metrics.heightPixels / 3);
        welcomeUp.setDuration(2000).setInterpolator(new DecelerateInterpolator());
        welcomeUp.setStartDelay(1000);
        welcomeUp.start();
        welcome.animate().alpha(0f).setStartDelay(2500).setDuration(1000);
        fadeBG.animate().alpha(0f).setStartDelay(2500).setDuration(1000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {welcomeScreen = false;}
        }, 3000);

        */

        prefs.edit().putBoolean("first_time", false).putBoolean(SERVICE_RUNNING, false)
                .putBoolean(REPLY_ALL, true)
                .putString("preset", "Napping \uD83D\uDE34\nGet back to you soon!")
                .apply();

        MainActivity.preset = "Napping \uD83D\uDE34\nGet back to you soon!";
    }

    public Toolbar getToolbar(){
        return toolbar;
    }

    public void saveReplyItems(){
        JSONArray replyItemsJSON = new JSONArray();
        try{
            ArrayList<ReplyItem> replyItems = MainActivity.replyItems;
            for (int i = 0; i < replyItems.size(); i++) {
                JSONObject replyJSON = new JSONObject();
                Gson gson = new Gson();
                String json = gson.toJson(replyItems.get(i));
                replyJSON.put(String.valueOf(i), json);
                replyItemsJSON.put(replyJSON);
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }

        getPrefs().edit().putString(MainActivity.PREFS_REPLY_ITEMS, replyItemsJSON.toString()).apply();
    }

    private void refreshReplyItems(){
        ArrayList<ReplyItem> storedReplyItems = new ArrayList<>();
        String jsonString = getPrefs().getString(MainActivity.PREFS_REPLY_ITEMS, "");
        try{
            JSONArray replyItemsJSON = new JSONArray(jsonString);
            for(int i = 0; i < replyItemsJSON.length(); i++){
                JSONObject replyJSON = (JSONObject) replyItemsJSON.get(i);
                Gson gson = new Gson();
                String json = (String) replyJSON.get(String.valueOf(i));
                ReplyItem replyItem = gson.fromJson(json, ReplyItem.class);
                storedReplyItems.add(replyItem);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        if(storedReplyItems.isEmpty())return;

        replyItems.clear();
        replyItems.addAll(storedReplyItems);
        AddNewReplyFragment.updateUsedNumbers(replyItems);
    }



}
