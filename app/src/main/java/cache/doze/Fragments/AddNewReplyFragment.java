package cache.doze.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.SaveEditText;
import cache.doze.Views.FunFab.FunFab;

/**
 * Created by Chris on 10/4/2018.
 */

public class AddNewReplyFragment extends Fragment {

    MainActivity mainActivity;
    ReplyItem currentReplyItem;

    private View root;
    private LinearLayout contactsButton;
    private SaveEditText inputTitle;
    public EditText inputPreset;

    private static ArrayList<Integer> usedNumbers = new ArrayList<>();

    //boolean tryCheckSwitch = false;

    private int currentState;

    final static int STATE_ADD_NEW = 0;
    final static int STATE_EDITING = 1;

    private OnContactsButtonPressedListener onContactsButtonPressedListener;



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
        mainActivity = (MainActivity) getActivity();
        currentState = STATE_ADD_NEW;
        root = inflater.inflate(R.layout.fragment_add_new, container, false);

        inputTitle = root.findViewById(R.id.input_title);
        inputPreset = root.findViewById(R.id.input_reply_message);
        contactsButton = root.findViewById(R.id.add_contacts_wrapper);

        setUpContactsButton();
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
//        fragments.add(Fragment.instantiate(this.getContext(), AddContactsFragment.class.getName()));
//        viewPagerAdapter = new ViewPagerAdapter(super.get)
//    }




    private void setUpContactsButton(){
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onContactsButtonPressedListener != null)onContactsButtonPressedListener.onPressed();
            }
        });
    }

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
        String title = inputTitle.getText().toString().isEmpty() ? "Reply " + (findUnusedNumber()) : inputTitle.getText().toString();
        String reply = inputPreset.getText().toString();
        if(currentState == STATE_ADD_NEW) {
            currentReplyItem = new ReplyItem(title, reply);
        }else if(currentState == STATE_EDITING){
            notifyTitleChanged(currentReplyItem.getTitle(), title);
            currentReplyItem.setTitle(title);
            currentReplyItem.setReplyText(reply);
        }
        return currentReplyItem;
    }

    public boolean canSubmit(){
        return !inputPreset.getText().toString().isEmpty();
    }

    private int findUnusedNumber(){
        for(int i = 1; i < usedNumbers.size() + 2; i++){
            if(usedNumbers.contains(i))continue;
            usedNumbers.add(i);
            return i;
        }

        return 1;
    }
    public static void updateUsedNumbers(ArrayList<ReplyItem> replyItems){
        usedNumbers.clear();
        for(int i = 0; i < replyItems.size(); i++){
            String title = replyItems.get(i).getTitle();
            String[] split;
            if(!(split = title.split(" "))[0].equalsIgnoreCase("reply"))return;

            int num;
            try{
                num = Integer.parseInt(split[1]);
            }catch (Exception e){
                return;
            }

            usedNumbers.add(num);
        }
    }

    public void notifyItemRemoved(String title){
        String[] split;
        if(!(split = title.split(" "))[0].equalsIgnoreCase("reply"))return;

        int num;
        try{
            num = Integer.parseInt(split[1]);
        }catch (Exception e){
            return;
        }

        for(int i = 0; i < usedNumbers.size(); i++){
            if(usedNumbers.get(i) == num)usedNumbers.remove(i);
        }
    }
    private void notifyTitleChanged(String oldTitle, String newTitle){
        String[] split1;
        String[] split2;
        if(!(split1 = oldTitle.split(" "))[0].equalsIgnoreCase("reply")
                || !(split2 = newTitle.split(" "))[0].equalsIgnoreCase("reply"))return;

        int oldNum;
        int newNum;
        try{
            oldNum = Integer.parseInt(split1[1]);
            newNum = Integer.parseInt(split2[1]);
        }catch (Exception e){
            return;
        }

        for(int i = 0; i < usedNumbers.size(); i++){
            if(usedNumbers.get(i) == oldNum)usedNumbers.set(i, newNum);
        }
    }

    public void clear(){
        if(root == null)return;

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

    public void setContactsButtonPressedListener(OnContactsButtonPressedListener buttonPressedListener){
        this.onContactsButtonPressedListener = buttonPressedListener;
    }

    public interface OnContactsButtonPressedListener {
        public void onPressed();
    }
}
