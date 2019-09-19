package cache.doze.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.SaveEditText;
import cache.doze.Tools.ScreenUtil;
import cache.doze.Views.FunFab.FunFabFrag;

/**
 * Created by Chris on 10/4/2018.
 */

public class AddNewReplyFragment extends FunFabFrag {

    MainActivity mainActivity;
    ReplyItem currentReplyItem;

    private View root;
    private LinearLayout contactsButton;
    private SaveEditText inputTitle;
    public EditText inputMessage;

    private static ArrayList<Integer> usedNumbers = new ArrayList<>();

    //boolean tryCheckSwitch = false;

    private int currentState;
    private boolean inputFocused;

    public final static int STATE_ADD_NEW = 0;
    public final static int STATE_EDITING = 1;

    private OnContactsButtonPressedListener onContactsButtonPressedListener;


    public void show() {
        if (root != null) root.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        currentState = STATE_ADD_NEW;
        root = inflater.inflate(R.layout.fragment_add_new, container, false);

        inputTitle = root.findViewById(R.id.input_title);
        inputMessage = root.findViewById(R.id.input_reply_message);
        contactsButton = root.findViewById(R.id.add_contacts_wrapper);

        setUpContactsButton();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
/*        inputTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            boolean hadFocus;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    showKeyboard();
                else if(this.hadFocus) {
                    hideKeyboard();
                }

                this.hadFocus = hasFocus;
            }
        });
        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            boolean hadFocus;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    keyboardVisibilityListener.onKeyboardVisibility(true);
                else if(this.hadFocus) {
                    keyboardVisibilityListener.onKeyboardVisibility(false);
                }

                this.hadFocus = hasFocus;
            }
        });*/
        //setupSwitchesAndOnClicks();

    }

    @Override
    public void onResume() {
        //presetInput.setText(MainActivity.preset);
        super.onResume();
    }

    private void setupSwitchesAndOnClicks() {
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


    private void setUpContactsButton() {
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onContactsButtonPressedListener != null)
                    onContactsButtonPressedListener.onPressed();
            }
        });
    }


    private int findUnusedNumber() {
        for (int i = 1; i < usedNumbers.size() + 2; i++) {
            if (usedNumbers.contains(i)) continue;
            usedNumbers.add(i);
            return i;
        }

        return 1;
    }

    public static void updateUsedNumbers(ArrayList<ReplyItem> replyItems) {
        usedNumbers.clear();
        for (int i = 0; i < replyItems.size(); i++) {
            String title = replyItems.get(i).getTitle();
            String[] split;
            if (!(split = title.split(" "))[0].equalsIgnoreCase("reply")) continue;

            int num;
            try {
                num = Integer.parseInt(split[1]);
            } catch (Exception e) {
                return;
            }

            usedNumbers.add(num);
        }
    }

    public void notifyItemRemoved(String title) {
        String[] split;
        if (!(split = title.split(" "))[0].equalsIgnoreCase("reply")) return;

        int num;
        try {
            num = Integer.parseInt(split[1]);
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < usedNumbers.size(); i++) {
            if (usedNumbers.get(i) == num) usedNumbers.remove(i);
        }
    }

    private void notifyTitleChanged(String oldTitle, String newTitle) {
        String[] split1;
        String[] split2;
        if (!(split1 = oldTitle.split(" "))[0].equalsIgnoreCase("reply")
                || !(split2 = newTitle.split(" "))[0].equalsIgnoreCase("reply")) return;

        int oldNum;
        int newNum;
        try {
            oldNum = Integer.parseInt(split1[1]);
            newNum = Integer.parseInt(split2[1]);
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < usedNumbers.size(); i++) {
            if (usedNumbers.get(i) == oldNum) usedNumbers.set(i, newNum);
        }
    }

    public void clear() {
        if (root == null) return;

        inputTitle.setText("");
        inputMessage.setText("");
        currentState = STATE_ADD_NEW;
    }

    private void refresh() {
        root.post(new Runnable() {
            @Override
            public void run() {
                inputTitle.setText(currentReplyItem.getTitle());
                inputMessage.setText(currentReplyItem.getReplyText());
            }
        });
    }

    public String getPresetText() {
        return inputMessage.getText().toString();
    }

    public void setPresetText(String input) {
        inputMessage.setText(input);
    }

    public void setState(int state) {
        currentState = state;
    }

    public int getState() {
        return currentState;
    }

    public void setReplyItem(ReplyItem replyItem) {
        currentReplyItem = replyItem;
        refresh();
    }

    public ReplyItem getReplyItem() {
        String title = inputTitle.getText().toString().isEmpty() ? "Reply " + (findUnusedNumber()) : inputTitle.getText().toString();
        String reply = inputMessage.getText().toString();
        if (currentState == STATE_ADD_NEW) {
            currentReplyItem = new ReplyItem(title, reply);
        } else if (currentState == STATE_EDITING) {
            notifyTitleChanged(currentReplyItem.getTitle(), title);
            currentReplyItem.setTitle(title);
            currentReplyItem.setReplyText(reply);
        }
        return currentReplyItem;
    }

    public boolean canSubmit() {
        return !inputMessage.getText().toString().isEmpty();
    }

    public void setContactsButtonPressedListener(OnContactsButtonPressedListener buttonPressedListener) {
        this.onContactsButtonPressedListener = buttonPressedListener;
    }

    public interface OnContactsButtonPressedListener {
        public void onPressed();
    }

    private void showKeyboard(){
        if (getActivity() == null) return;

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm == null) return;

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

/*        if (inputTitle.hasFocus())
            inputTitle.clearFocus();
        if (inputMessage.hasFocus())
            inputMessage.clearFocus();*/

        keyboardVisibilityListener.onKeyboardVisibility(true);
    }

    private void hideKeyboard() {
        if (getActivity() == null) return;

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null || getActivity().getCurrentFocus() == null) return;

        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        if (inputTitle.hasFocus())
            inputTitle.clearFocus();
        if (inputMessage.hasFocus())
            inputMessage.clearFocus();

        //keyboardVisibilityListener.onKeyboardVisibility(false);
    }


}
