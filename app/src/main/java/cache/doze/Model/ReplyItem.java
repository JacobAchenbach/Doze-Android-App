package cache.doze.Model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cache.doze.R;
import cache.doze.Tools.QuickTools;

/**
 * Created by Chris on 10/4/2018.
 */

public class ReplyItem implements Serializable{
    private String title;
    private String replyText;
    private boolean checked;
    private ArrayList<Contact> contacts;
    private String id;
    private int[] drawableColors;
    private transient GradientDrawable bgDrawable;
    private transient GradientDrawable borderDrawable;

    public ReplyItem(String title, String reply) {
        this.title = title;
        this.replyText = reply;
        checked = false;

        id = (System.currentTimeMillis() + new Random().nextInt(Math.abs((int) System.currentTimeMillis()))) + reply.length() + "";
    }

    public void setColorScheme(Context context, String[] colors){
        bgDrawable = new GradientDrawable();

        drawableColors = new int[colors.length - 1];
        for(int i = 0; i < colors.length - 1; i++)
            drawableColors[i] = Color.parseColor("#" + colors[i]);
        Log.d("Title: ", colors[colors.length - 1]);

        bgDrawable.setColors(drawableColors);
        int rand = new Random().nextInt(2);
        bgDrawable.setOrientation(rand == 0? GradientDrawable.Orientation.TL_BR: GradientDrawable.Orientation.BL_TR);
        bgDrawable.setShape(GradientDrawable.RECTANGLE);
        bgDrawable.setCornerRadius(context.getResources().getDimension(R.dimen.default_rounded_corner));

        borderDrawable = new GradientDrawable();


        borderDrawable.setColor(ContextCompat.getColor(context, R.color.white));
        borderDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        borderDrawable.setStroke(QuickTools.convertDpToPx(context, 2), drawableColors[drawableColors.length - 1]);
        borderDrawable.setShape(GradientDrawable.RECTANGLE);
        borderDrawable.setCornerRadius(context.getResources().getDimension(R.dimen.default_rounded_corner));
    }
    public GradientDrawable getGradient(){
        return bgDrawable;
    }

    public int[] getColors(){
        return drawableColors;
    }

    public GradientDrawable getGradientTurned(GradientDrawable.Orientation orientation){
        if(orientation == null || bgDrawable == null || bgDrawable.getConstantState() == null)return null;

        GradientDrawable rotated = (GradientDrawable) bgDrawable.getConstantState().newDrawable().mutate();
        rotated.setOrientation(orientation);
        return rotated;
    }

    public GradientDrawable getGradientLighter(){
        if(bgDrawable == null || bgDrawable.getConstantState() == null) return null;

        float factor = 0.3f;
        int[] lighterColors = new int[drawableColors.length];
        for(int i = 0; i < drawableColors.length; i++) {
            int color = drawableColors[i];
            int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
            int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
            int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
            lighterColors[i] = Color.argb(Color.alpha(color), red, green, blue);
        }

        GradientDrawable clone = (GradientDrawable) bgDrawable.getConstantState().newDrawable().mutate();
        clone.setColors(lighterColors);
        clone.setOrientation(GradientDrawable.Orientation.BR_TL);
        return clone;
    }
    public GradientDrawable getBorder(){
        return borderDrawable;
    }

    public ReplyItem(ReplyItem replyItem){
        this.title = replyItem.title;
        this.replyText = replyItem.replyText;
        this.checked = replyItem.checked;

        this.id = replyItem.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setContacts(ArrayList<Contact> contacts){
        this.contacts = contacts;
    }
    public ArrayList<Contact> getContacts() {
        return contacts != null? contacts: new ArrayList<Contact>();
    }
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
    public String getReplyText() {
        return replyText;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public boolean isChecked() {
        return checked;
    }

    public String getId() {
        return id;
    }

    public boolean hasContact(String number){
        for(int i = 0; i < contacts.size(); i++){
            if(contacts.get(i).hasNumber(number)){
                return true;
            }
        }

        return false;
    }
}
