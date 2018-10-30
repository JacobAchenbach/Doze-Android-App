package cache.doze.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.WindowManager;

import cache.doze.R;

/**
 * Created by Chris on 3/20/2018.
 */

public class TextEditorActivity extends AppCompatActivity{
    EditedText editQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_edittext);
        final TextEditorActivity activity = this;
        Intent extras = getIntent();
        String startText = extras.getStringExtra("start_text");
        final int startPosition = extras.getIntExtra("start_position", 0);



        editQuery = findViewById(R.id.edit_query);
        editQuery.setText(startText);
        editQuery.setTextEditorActivity(this);

        editQuery.post(new Runnable() {
            @Override
            public void run() {
                editQuery.requestFocus();
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                editQuery.setSelection(startPosition);
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("return_text", editQuery.getText().toString());
        returnIntent.putExtra("start_position", editQuery.getSelectionStart());
        setResult(MainActivity.RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


}

class EditedText extends AppCompatEditText {
    TextEditorActivity editorActivity;

    public void setTextEditorActivity(TextEditorActivity editorActivity){this.editorActivity = editorActivity;}

    public EditedText(Context context)
    {
        super(context);
    }

    public EditedText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme (int keyCode, KeyEvent event){

        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

            editorActivity.onBackPressed();

            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}

