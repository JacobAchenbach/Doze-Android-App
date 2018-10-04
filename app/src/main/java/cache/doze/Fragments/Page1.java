package cache.doze.Fragments;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import cache.doze.MainActivity;
import cache.doze.R;

/**
 * Created by Chris on 2/22/2018.
 */

public class Page1 extends Fragment {

    View v;

    ViewGroup viewGroup;
    RelativeLayout textViewGroup;

    public EditText presetInput;
    CardView expandedView;
    TextView text_start_service;
    Switch serviceSwitch;
    Switch replyAll;

    boolean tryCheckSwitch = false;
    public boolean keyboardShowing;

    MainActivity mainActivity;
    Animation currentAnimation;

    Rect startBounds = new Rect();
    Rect finalBounds = new Rect();
    Point globalOffset = new Point();
    float startScale;

    public static Page1 newInstance(int page, String title) {
        Page1 page1 = new Page1();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        page1.setArguments(args);
        return page1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.page_one, container, false);

        mainActivity = (MainActivity) getActivity();

        //Find views
        presetInput = v.findViewById(R.id.input_text);
        presetInput.setText(MainActivity.preset);
        expandedView = v.findViewById(R.id.expanded_view);
        serviceSwitch = v.findViewById(R.id.serviceSwitch);
        replyAll = v.findViewById(R.id.reply_all_switch);
        text_start_service = v.findViewById(R.id.text_service_options);
        viewGroup = v.findViewById(R.id.container);
        textViewGroup = v.findViewById(R.id.text_viewgroup);

        //final RelativeLayout listedContactsContainer = v.findViewById(R.id.container_listed_contacts);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);
        setupSwitchesAndOnClicks();

    }

    @Override
    public void onResume(){
        presetInput.setText(MainActivity.preset);
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(tryCheckSwitch){
            tryCheckSwitch = false;
            serviceSwitch.setChecked(true);
        }
    }

    private void setupSwitchesAndOnClicks(){
        //Start service with switch
        serviceSwitch.setChecked(mainActivity.isServiceRunning());
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){ //On --> Off
                    mainActivity.endSMSService();
                }else { //Off --> On
                    mainActivity.startSMSService();
                }

                String status = isChecked? "Reply Service Started!": "Reply Service Dismissed";
                Snackbar.make(v, status, Snackbar.LENGTH_SHORT).show();
            }
        });

        replyAll.setChecked(mainActivity.getReplyAll());
        replyAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){ //On --> Off
                    mainActivity.checkAll(false);
                }else { //Off --> On
                    mainActivity.checkAll(true);
                }
            }
        });

        presetInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        });

    }


    public void adjustContent(boolean show){
        if(show){
            v.findViewById(R.id.page1_content_two).setVisibility(View.VISIBLE);
//            v.findViewById(R.id.page1_content_two)
//                    .setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            textViewGroup.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            Animation scale = AnimationUtils.loadAnimation(getContext(), R.anim.scale_full_screen);
//            //textViewGroup.startAnimation(scale);
        }else{
            v.findViewById(R.id.page1_content_two).setVisibility(View.GONE);

        }

    }

//    private void initViewPages(){
//        List<Fragment> fragments = new Vector<>();
//        fragments.add(Fragment.instantiate(this.getContext(), Page1.class.getName()));
//        fragments.add(Fragment.instantiate(this.getContext(), Page2.class.getName()));
//        viewPagerAdapter = new ViewPagerAdapter(super.get)
//    }

    public String getPresetText(){
        return presetInput.getText().toString();
    }

    public void setPresetText(String input){presetInput.setText(input);}

}
