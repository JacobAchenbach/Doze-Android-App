package cache.doze;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import cache.doze.Activities.MainActivity;

/**
 * Created by Chris on 8/27/2018.
 */

public class SaveEditText extends EditText {
    public SaveEditText(Context context) {
        super(context);
    }

    public SaveEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SaveEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            String input = getText().toString();
            MainActivity.preset = input;
            getContext().getSharedPreferences(MainActivity.DEFAULT_PREFS, Context.MODE_PRIVATE).edit()
                    .putString(MainActivity.PRESET_PREF, input).apply();
        }
        this.clearFocus();
        return super.onKeyDown(keyCode, event);
    }
}