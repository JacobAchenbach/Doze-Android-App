package cache.doze;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cache.doze.Activities.PermissionsActivity;
import cache.doze.Fragments.Page1;
import cache.doze.Fragments.Page2;
import cache.doze.Tools.PermissionsHelper;
import cache.doze.Tools.QuickTools;
import cache.doze.Views.FluidSearchView;

/**
 * Created by Chris on 1/9/2018.
 */

public class MainActivity extends AppCompatActivity{
    public static final String PREFS_CHECKED_CONTACTS = "checked_contacts";
    static final int NOTIFICATION_ID = 95;
    public static final String CHANNEL_ID = "notif_channel";
    public static final String DEFAULT_PREFS = "default_prefs";
    public static final String PRESET_PREF = "preset";
    public static final String SERVICE_RUNNING = "its_running";
    public static final String REPLY_ALL = "reply_all";

    public static boolean active = false;
    public static String preset;
    boolean welcomeScreen = true;

    private SharedPreferences prefs;
    NotificationManager nMN;
    Notification.Builder runningNotification;

    public FluidSearchView fluidSearchView;


    DisplayMetrics displayMetrics;
    int maxTimesMessaged = 1;

    /**
     * Timer to prevent accidental spam to recipient
     */
    private static Handler messageTimer = null;

    public static ArrayList<Contact> contactList = new ArrayList<>();
    public static HashMap<String, Integer> messagedContacts = new HashMap<>();

    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
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
        setContentView(R.layout.main_activity);

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

        initSearchView();
        initViewPager();
        //initKeyboardListener();

    }

    private void initViewPager(){
        viewPager = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getBaseContext(), getSupportFragmentManager(), tabLayout);
        viewPager.setAdapter(viewPagerAdapter);

        /*tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(tab == tabLayout.getTabAt(1)){
                    Page2 page2 = (Page2)viewPagerAdapter.getFragment(1);
                    if(page2 != null)page2.scrollRecyclerView(0);
                }
            }
        });
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                for(int n = 0; n < tabLayout.getTabCount(); n++){
                    TabLayout.Tab tab = tabLayout.getTabAt(n);
                    if(tab != null)tab.setIcon(getIcon(n));

                    ViewGroup child = (ViewGroup)((ViewGroup)tabLayout.getChildAt(0)).getChildAt(n);

                    if(child != null && child.getChildAt(1) instanceof AppCompatTextView){
                        TextView text = (TextView) child.getChildAt(1);
                        if(text != null) text.setTextSize(QuickTools.convertDpToPx(tabLayout.getContext(), 12));

                    }
                }
            }
            private Drawable getIcon(int index){
                Drawable drawable;
                switch (index) {
                    case 0:
                        drawable = ContextCompat.getDrawable(tabLayout.getContext(), R.drawable.baseline_home_black_36);
                        break;
                    case 1:
                        drawable = ContextCompat.getDrawable(tabLayout.getContext(), R.drawable.baseline_contacts_black_36);
                        break;
                    default:
                        drawable = ContextCompat.getDrawable(tabLayout.getContext(), R.drawable.baseline_home_black_36);
                }
                if(drawable != null) drawable.setTint(ContextCompat.getColor(tabLayout.getContext(), R.color.white));
                return drawable;
            }
        });*/

        viewPager.getCurrentItem();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            ImageView searchMag = fluidSearchView.getMagIcon();
            public void onPageScrollStateChanged(int state) {}
            int startMagX;
            int endMagX = -1;
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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
            }
            public void onPageSelected(int position) {
/*                if(position == 1)
                    if(!PermissionsHelper.checkReadContactsPermission())getPermissionToReadContacts();*/
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                Page1 page1 = (Page1)viewPagerAdapter.getFragment(0);
                if(page1 == null)return;
                EditText focusedText = page1.presetInput;
                if(imm != null && focusedText != null) imm.hideSoftInputFromWindow(focusedText.getWindowToken(), 0);

                if(position == 1){
                    Page2 page2 = (Page2)viewPagerAdapter.getFragment(1);
                    if(page2 == null)return;
                }
            }
        });

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                fluidSearchView.setOnQueryTextListener((Page2) viewPagerAdapter.getFragment(1));
            }
        });
    }

    private void initSearchView(){
        if(fluidSearchView != null) return;

        fluidSearchView = new FluidSearchView(toolbar, this);
        if(FluidSearchView.isDetached) fluidSearchView.build();
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


    //Basic Lifecycle Methods
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(MainActivity.preset != null) MainActivity.preset = prefs.getString("preset", MainActivity.preset);
//        if(fluidSearchView != null && FluidSearchView.isDetached)
//            fluidSearchView.build();
        if(viewPagerAdapter != null && viewPagerAdapter.getFragment(1) != null && fluidSearchView != null)
            fluidSearchView.setOnQueryTextListener((Page2) viewPagerAdapter.getFragment(1));
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(viewPagerAdapter == null || ((Page1)viewPagerAdapter.getFragment(0)) == null)return;
//        if(fluidSearchView != null && !FluidSearchView.isDetached)
//            fluidSearchView.detach();
        MainActivity.preset = ((Page1)viewPagerAdapter.getFragment(0)).getPresetText();
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

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            if(((Page1)viewPagerAdapter.getFragment(0)) != null)
                ((Page1)viewPagerAdapter.getFragment(0)).setEditTextStartPos(data.getIntExtra("start_position", 0));

            preset = data.getStringExtra("return_text");
            prefs.edit().putString("preset", preset).apply();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

    }*/

    //Other @Override methods
    @Override
    public void onBackPressed(){
        //if(viewPagerAdapter != null) Page1 tempPageInst = (Page1)viewPagerAdapter.getFragment(0);
        //if(tempPageInst.keyboardShowing)tempPageInst.revertZoom();
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
    protected void messageReceived(String address) {
        if (MainActivity.active && canMessage(address) && MainActivity.messageTimer == null) {
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
            }, 5000);
        }
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

        Page2 page2 =
                (Page2) viewPagerAdapter.getFragment(1);
        page2.updateCheckStates();
    }

    public void startSMSService(){
        MainActivity.active = true;
        prefs.edit().putBoolean(MainActivity.SERVICE_RUNNING, true).apply();

        Intent myService = new Intent(this, MonitorSmsService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(myService);
        } else {
            startService(myService);
        }
    }
    public void endSMSService(){
        MainActivity.active = false;
        prefs.edit().putBoolean(MainActivity.SERVICE_RUNNING, false).apply();

        stopService(new Intent(this, MonitorSmsService.class));
    }

    /**
     *  Handle showing/hiding Notification in user's StatusBar
     */
    public void showActiveNotif(Context context){
        if(active)return;
        active = true;
        Toast.makeText(context, "Service Started!", Toast.LENGTH_SHORT).show();
        Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(context,0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nMN = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        runningNotification = new Notification.Builder(context);
        runningNotification
                .setOngoing(true)
                .setContentTitle("Doze")
                .setContentText("Responding to your texts \uD83D\uDE34")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(intent)
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Reply Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            nMN.createNotificationChannel(channel);
            runningNotification.setChannelId(CHANNEL_ID);
        }

        nMN.notify(1, runningNotification.build());

        messagedContacts.clear();
        Log.i("Service status", "Service Started!");
    }
    public void removeActiveNotif(){
        if(active) {
            active = false;
            try{nMN.cancel(NOTIFICATION_ID);}catch (Exception e){Log.e("No notification", "No notification showing with ID " +NOTIFICATION_ID +" error:" +e.toString());}
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nMN.deleteNotificationChannel(CHANNEL_ID);
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && nMN.getNotificationChannel(CHANNEL_ID) != null){
                checkShowing(0);
            }


            Log.i("Service status", "Service Stopped");
        }
    }
    //Sometimes it isn't removed right away so this checks if it's gone, 3 times
    private void checkShowing(final int count){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void run() {
                if(count < 3)
                nMN.deleteNotificationChannel(CHANNEL_ID);
                if(nMN.getNotificationChannel(CHANNEL_ID) != null) checkShowing(count + 1);
            }
        }, 1000);
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
        ((RelativeLayout)findViewById(R.id.main_activity)).addView(welcome);
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

}
