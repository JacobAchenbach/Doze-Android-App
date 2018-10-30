package cache.doze.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.SaveEditText;

/**
 * Created by Chris on 10/4/2018.
 */

public class AddNewReplyFragment extends Fragment {

    MainActivity mainActivity;

    private View root;
    private SaveEditText inputTitle;
    public EditText inputPreset;

    ReplyItem currentReplyItem;

    boolean tryCheckSwitch = false;

    final static int STATE_ADD_NEW = 0;
    final static int STATE_EDITING = 1;

    private int currentState;


    public static MainRepliesFragment newInstance(int page, String title) {
        MainRepliesFragment mainRepliesFragment = new MainRepliesFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        mainRepliesFragment.setArguments(args);
        return mainRepliesFragment;
    }

    public void show(){
        if(root != null)root.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        root = inflater.inflate(R.layout.fragment_add_new, container, false);

        mainActivity = (MainActivity) getActivity();

        //Find views
        inputTitle = root.findViewById(R.id.input_title);
        inputPreset = root.findViewById(R.id.input_reply_message);
        //inputPreset.setText(MainActivity.preset);

        currentState = STATE_ADD_NEW;
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);
        //setupSwitchesAndOnClicks();

    }

    @Override
    public void onResume(){
        //presetInput.setText(MainActivity.preset);
        super.onResume();
    }

    private void setupSwitchesAndOnClicks(){
/*        //Start service with switch
        serviceSwitch.setChecked(mainActivity.isServiceRunning());
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){ //On --> Off
                    mainActivity.endSMSService();
                }else { //Off --> On
                    mainActivity.startSMSService();
                }


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
        });*/

    }

//    private void initViewPages(){
//        List<Fragment> fragments = new Vector<>();
//        fragments.add(Fragment.instantiate(this.getContext(), MainRepliesFragment.class.getName()));
//        fragments.add(Fragment.instantiate(this.getContext(), Page2.class.getName()));
//        viewPagerAdapter = new ViewPagerAdapter(super.get)
//    }

    public String getPresetText(){
        return inputPreset.getText().toString();
    }

    public void setPresetText(String input){inputPreset.setText(input);}

    public void setState(int state){
        currentState = state;
    }
    public int getState(){
        return currentState;
    }

    public void setReplyItem(ReplyItem replyItem){
        currentReplyItem = replyItem;
        refresh();
    }

    public ReplyItem getReplyItem(){
        int itemCount = currentState == STATE_ADD_NEW ? MainActivity.replyItems.size() + 1: MainActivity.replyItems.size();
        String title = inputTitle.getText().toString().isEmpty() ? "Reply " + (itemCount) : inputTitle.getText().toString();
        String reply = inputPreset.getText().toString();
        if(currentState == STATE_ADD_NEW) {
            currentReplyItem = new ReplyItem(title, reply);
        }else if(currentState == STATE_EDITING){
            currentReplyItem.setTitle(title);
            currentReplyItem.setReplyText(reply);
        }
        return currentReplyItem;
    }

    public void clear(){
        inputTitle.setText("");
        inputPreset.setText("");
        currentState = STATE_ADD_NEW;
    }

    private void refresh(){
        root.post(new Runnable() {
            @Override
            public void run() {
                inputTitle.setText(currentReplyItem.getTitle());
                inputPreset.setText(currentReplyItem.getReplyText());
            }
        });
    }
}
