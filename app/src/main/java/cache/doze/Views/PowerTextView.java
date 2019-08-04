package cache.doze.Views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import cache.doze.R;

/**
 * Created by Chris on 1/23/2019.
 */

public class PowerTextView extends AppCompatTextView {

    private Context context;

    private int animationTime = 150;

    public PowerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs);
    }

    public PowerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public PowerTextView(Context context) {
        super(context);
        this.context = context;
    }

    private void init(AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PowerTextView);

        String font = a.getString(R.styleable.PowerTextView_textFont);
        setCustomTypeFace(font);

        a.recycle();
    }

    public void setCustomTypeFace(String path) {
        if(path == null || path.equals("") ) return;

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + path);
        setTypeface(tf ,Typeface.NORMAL);
    }

    public void setText(String text){
        super.setText(text);
    }

    public void setText(String text, boolean animated){
        if(!animated){
            this.setText(text);
            return;
        }

        slideOut(()->{
            setText(text);
            slideIn();
        });
    }

    public void slideOut(){
        slideOut(null);
    }

    public void slideOut(Runnable callback){
        ObjectAnimator slideDown = ObjectAnimator.ofFloat(this, "translationY", 0, 100);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);

        final AnimatorSet out = new AnimatorSet();
        out.playTogether(slideDown, fadeOut);
        out.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                end();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                end();
            }
            private void end(){
                out.removeAllListeners();

                if(callback != null)
                    callback.run();
            }
        });
        out.setDuration(animationTime).start();
    }

    public void slideIn(){
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(this, "translationY", -100, 0);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);

        final AnimatorSet in = new AnimatorSet();
        in.playTogether(slideIn, fadeIn);
        in.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                end();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                end();
            }
            private void end(){
                in.removeAllListeners();
            }
        });
        in.setDuration(animationTime).start();
    }

    public int getAnimationTime(){
        return animationTime;
    }
}
