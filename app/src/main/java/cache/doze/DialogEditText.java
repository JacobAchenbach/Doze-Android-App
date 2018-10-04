package cache.doze;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Chris on 1/11/2017.
 */
public class DialogEditText extends Dialog {

    public Activity c;
    public Dialog d;
    public ImageView ok, cancel;
    public EditText input;
    String msg;
    String pastText;
    int blankSize;

    public DialogEditText(Activity a, String msg, String pastText) {
        super(a);
        this.c = a;
        this.msg = msg;
        this.pastText = pastText;
    }

    public DialogEditText(Activity a, String msg, String pastText, int blankSize) {
        super(a);
        this.c = a;
        this.msg = msg;
        this.pastText = pastText;
        this.blankSize = blankSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_text);
        ((TextView)findViewById(R.id.alertTitle)).setText(msg);
        input = (EditText)findViewById(R.id.input);
        input.setText(pastText);
        input.setTextSize(20);
        input.selectAll();
        //if(blankSize > 0){
        //    input.setWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, blankSize, c.getResources().getDisplayMetrics()));
        //}else input.setWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, c.getResources().getDisplayMetrics()));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.getBackground().setColorFilter(c.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        ok = (ImageView) findViewById(R.id.alertOkButton);
        cancel = (ImageView) findViewById(R.id.alertCancelButton);
    }

}
