package cache.doze.Activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cache.doze.Fragments.ContactsFragment;
import cache.doze.Fragments.AddNewReplyFragment;
import cache.doze.Fragments.DozeFragment;
import cache.doze.Fragments.HomeFragment;
import cache.doze.Model.Contact;
import cache.doze.Model.ReplyItem;
import cache.doze.MonitorSmsService;
import cache.doze.R;
import cache.doze.Tools.PermissionsHelper;
import cache.doze.Tools.ScreenUtil;
import cache.doze.Views.DozeSnackbar;
import cache.doze.Views.DozeToolbar;
import cache.doze.Views.FluidSearchView;
import cache.doze.Views.FunFab.FunFab;

/**
 * Created by Chris on 1/9/2018.
 */

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_REPLY_ITEMS = "reply_items";
    public static final int NOTIFICATION_ID = 95;
    public static final String CHANNEL_ID = "notif_channel";
    public static final String DEFAULT_PREFS = "default_prefs";
    public static final String PRESET_PREF = "preset";
    public static final String SERVICE_RUNNING = "its_running";
    public static final String REPLY_ALL = "reply_all";

    public static boolean active = false;
    public static String preset;

    NotificationManager nMN;
    Notification.Builder runningNotification;

    public FluidSearchView fluidSearchView;
    private DozeSnackbar dozeSnackbar;
    HomeFragment homeFragment;
    ContactsFragment contactsFragment;
    FunFab fab;


    DisplayMetrics displayMetrics;
    int maxTimesMessaged = 5;
    boolean welcomeScreen = true;
    boolean hasPermissions;
    public boolean isKeyboardShowing;

    /**
     * Timer to prevent accidental spam to recipient
     */
    private static Handler messageTimer = null;
    private static int messageSpamTime = 1000;

    public static ArrayList<ReplyItem> replyItems;
    public static ArrayList<Contact> contactList = new ArrayList<>();
    public static HashMap<String, Integer> messagedContacts = new HashMap<>();

    DozeToolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasPermissions = permissionsGranted())
            startApp();
        else {
            Intent intent = new Intent(MainActivity.this, PermissionsActivity.class);
            overridePendingTransition(R.anim.slide_in_top, 0);
            startActivityForResult(intent, 1);
        }
    }

    //Basic Lifecycle Methods
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.preset != null)
            MainActivity.preset = getPrefs().getString("preset", MainActivity.preset);
        if(hasPermissions) refreshReplyItems();
//        if(fluidSearchView != null && FluidSearchView.isDetached)
//            fluidSearchView.build();
    }

    @Override
    protected void onPause() {
        saveReplyItems();
        super.onPause();
//        if(fluidSearchView != null && !FluidSearchView.isDetached)
//            fluidSearchView.detach();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Other @Override methods
    @Override
    public void onBackPressed() {
        for (DozeFragment dozeFragment : DozeFragment.dozeFragments) {
            if (dozeFragment.isShown) {
                if (dozeFragment.onBackPressed()) return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.toolbar, menu);
        //menu.findItem(R.id.action_settings).getIcon().setTint(ContextCompat.getColor(getBaseContext(), R.color.black));
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

    private boolean permissionsGranted() {
        if (!PermissionsHelper.checkReadSMSPermission(getBaseContext()) || !PermissionsHelper.checkReadContactsPermission(getBaseContext())
                || !PermissionsHelper.checkReceiveSMSPermission(getBaseContext())) {
            return false;
        }
        return true;
    }

    private void startApp() {
        setContentView(R.layout.activity_main);

        MainActivity.preset = getPrefs().getString("preset", "");

        boolean firstTime = getPrefs().getBoolean("first_time", true);
        if (firstTime) {
            welcome();
            welcomeScreen = true;
        }

        setupApp();
    }

    private void setupApp() {
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


        setSupportActionBar(toolbar = findViewById(R.id.toolbar));

        replyItems = new ArrayList<>();
        fab = findViewById(R.id.fab);
        dozeSnackbar = findViewById(R.id.doze_snackbar);

        if (homeFragment == null) {
            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.replies_container, homeFragment, "Main_Replies").commitAllowingStateLoss();
            homeFragment.setFab(fab);
            homeFragment.isShown = true;
        }

        if (contactsFragment == null) {
            contactsFragment = new ContactsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.add_contacts_container, contactsFragment, "Add_Contacts").commitAllowingStateLoss();
            contactsFragment.isShown = false;
            contactsFragment.setFab(fab);
        }

        initSearchView();
        initKeyboardListener();

        refreshReplyItems();
    }

    public void showContactsFrag(ReplyItem replyItem) {
        homeFragment.slideOutLeft();
        contactsFragment.slideInRight();
        contactsFragment.setReplyItem(replyItem);
    }

    public void showMainRepliesFrag() {
        contactsFragment.slideOutRight();
        homeFragment.slideInLeft();
    }

    public FunFab getFab() {
        return fab == null ? findViewById(R.id.fab) : fab;
    }


    private void initSearchView() {
        if (fluidSearchView != null) return;

        fluidSearchView = new FluidSearchView(toolbar, this);
        //if(FluidSearchView.isDetached) fluidSearchView.build();
        //fluidSearchView.hide();
        //fluidSearchView.setOnQueryTextListener(qqqq);
    }

    private void initKeyboardListener(){
        ScreenUtil.setUpKeyboardListener(findViewById(R.id.main_activity), this);
    }

    public boolean isServiceRunning() {
        return getPrefs().getBoolean(SERVICE_RUNNING, false);
    }

    public ArrayList<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<Contact> newContactList) {
        contactList = new ArrayList<>(newContactList);
    }

    public void setToolbarPositive(String text) {
        toolbar.setTitle(text);
        animateToolbarColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary), ContextCompat.getColor(getBaseContext(), R.color.thatsGoodGreen));
    }

    private int savedToolbarColor;

    public void setToolbarColor(int from, int to) {
        animateToolbarColor(from, to);
    }

    public void setToolbarColor(int to) {
        animateToolbarColor(savedToolbarColor, to);
    }

    private void animateToolbarColor(int from, int to) {
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


    //Handling received messaged
    SmsManager smsManager = SmsManager.getDefault();

    public void messageReceived(String number) {
        for (ReplyItem replyItem : replyItems) {
            if (!replyItem.isChecked()) continue;

            if (replyItem.getContacts() == null || replyItem.getContacts().isEmpty()) {
                sendText(number, replyItem.getReplyText());
                continue;
            }

            if (replyItem.hasContact(number)) {
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

    private void sendText(String address, String msgBody) {
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
    private boolean canMessage(String address) {
        if (messagedContacts.containsKey(address)) {
            int timesMessaged = messagedContacts.get(address);
            if (timesMessaged < maxTimesMessaged) {
                messagedContacts.put(address, ++timesMessaged);
                return true;
            } else
                return false;
        }
        return true;
    }

    private boolean isContactSelected(String address) {
        Contact contact = new Contact("placeholder", "");
        for (Contact c : contactList) {
            if (c.getAddress().equals(address)) contact = c;
        }
        return contact.getSelected();
    }

    public void checkAll(boolean checked) {
        getPrefs().edit().putBoolean(REPLY_ALL, checked).apply();

        for (Contact c : contactList) {
            c.setSelected(checked);
        }

//        ContactsFragment page2 =
//                (ContactsFragment) viewPagerAdapter.getFragment(1);
//        page2.updateCheckStates();
    }

    public Snackbar startSMSService(){
        return startSMSService(true);
    }

    public Snackbar startSMSService(boolean showSnackBar) {
        if (checkRunning(1)) return null;

        dozeSnackbar.show("Reply Service Started!");

        MainActivity.active = true;
        getPrefs().edit().putBoolean(MainActivity.SERVICE_RUNNING, true).apply();

        Intent myService = new Intent(this, MonitorSmsService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(myService);
        } else {
            startService(myService);
        }

        saveReplyItems();

//        Snackbar snackbar = null;
//        if (showSnackBar) {
//            snackbar = Snackbar.make(toolbar.getRootView(), "Reply Service Started!", Snackbar.LENGTH_SHORT);
//            snackbar.show();
//        }
        return null;
    }

    public Snackbar endSMSService() {
        if (checkRunning(0)) return null;

        dozeSnackbar.show("Reply Service Dismissed");

        MainActivity.active = false;
        getPrefs().edit().putBoolean(MainActivity.SERVICE_RUNNING, false).apply();

        stopService(new Intent(this, MonitorSmsService.class));

        saveReplyItems();

//        Snackbar snackbar = Snackbar.make(toolbar.getRootView(), "Reply Service Dismissed", Snackbar.LENGTH_SHORT);
//        snackbar.show();
        return null;
    }

    private boolean checkRunning(int leniency) {
        leniency++;
        int count = 0;
        for (ReplyItem replyItem : replyItems) {
            if (replyItem.isChecked())
                count++;
            if (count >= leniency)
                return true;
        }
        return false;
    }


    public SharedPreferences getPrefs() {
        return getSharedPreferences(MainActivity.DEFAULT_PREFS, Context.MODE_PRIVATE);
    }

    //Handle from Permissions Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
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
        Typeface aye = Typeface.createFromAsset(getAssets(), "font/Pecita.otf");
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

        getPrefs().edit().putBoolean("first_time", false).putBoolean(SERVICE_RUNNING, false)
                .putBoolean(REPLY_ALL, true)
                .putString("preset", "Napping \uD83D\uDE34\nGet back to you soon!")
                .apply();

        MainActivity.preset = "Napping \uD83D\uDE34\nGet back to you soon!";
    }

    public DozeToolbar getToolbar() {
        return toolbar;
    }
    public DozeSnackbar getSnackbar() {
        return dozeSnackbar;
    }

    public void saveReplyItems() {
        JSONArray replyItemsJSON = new JSONArray();
        try {
            ArrayList<ReplyItem> replyItems = MainActivity.replyItems;
            if(replyItems == null) return;

            for (int i = 0; i < replyItems.size(); i++) {
                JSONObject replyJSON = new JSONObject();
                Gson gson = new Gson();
                String json = gson.toJson(replyItems.get(i));
                replyJSON.put(String.valueOf(i), json);
                replyItemsJSON.put(replyJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getPrefs().edit().putString(MainActivity.PREFS_REPLY_ITEMS, replyItemsJSON.toString()).apply();
    }

    private void refreshReplyItems() {
        ArrayList<ReplyItem> storedReplyItems = new ArrayList<>();
        String jsonString = getPrefs().getString(MainActivity.PREFS_REPLY_ITEMS, "");
        try {
            JSONArray replyItemsJSON = new JSONArray(jsonString);
            for (int i = 0; i < replyItemsJSON.length(); i++) {
                JSONObject replyJSON = (JSONObject) replyItemsJSON.get(i);
                Gson gson = new Gson();
                String json = (String) replyJSON.get(String.valueOf(i));
                ReplyItem replyItem = gson.fromJson(json, ReplyItem.class);
                storedReplyItems.add(replyItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (storedReplyItems.isEmpty()) return;

        replyItems.clear();
        replyItems.addAll(storedReplyItems);
        AddNewReplyFragment.updateUsedNumbers(replyItems);

        //Start the service if at least one item is already checked
        if (checkRunning(1)) {
            startSMSService(false);
        }
    }


}
