package cache.doze.Views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cache.doze.R;

public class DozeSnackbar extends RelativeLayout {

    private Context context;
    private CardView cardView;
    private PowerTextView descriptionText;
    private ImageView closeButton;
    private RelativeLayout closeButtonWrapper;

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    private boolean open;
    private int timeout = 3000;
    private int animationTime = 200;
    private int cardWidth, cardHeight;

    public DozeSnackbar(Context context) {
        super(context);
        sharedConstructor(context);
    }

    public DozeSnackbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context);
    }

    private void sharedConstructor(Context context){
        this.context = context;

        setAlpha(0f);

        LayoutInflater.from(context).inflate(R.layout.view_doze_snackbar, this);
        cardView = findViewById(R.id.doze_snackbar_card);
        descriptionText = findViewById(R.id.text_description);
        closeButtonWrapper = findViewById(R.id.button_close_wrapper);

        descriptionText.setAlpha(0f);

        cardView.getViewTreeObserver().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                cardHeight = cardView.getHeight();
                cardWidth = cardView.getWidth();
                LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
                lp.width = lp.height = 0;
                cardView.setLayoutParams(lp);
            }
        });

        setUpCloseButton();
    }

    private void setUpCloseButton(){
        closeButtonWrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionText.slideOut(()->{
                    hide();
                });
            }
        });
    }

    public void show(){
        show("Alert");
    }

    public void show(String description){
        if(open) {
            this.descriptionText.setText(description, true);
            startTimeout();
            return;
        }
        open = true;

        animate(true, ()->{
            if(!attached()) return;

            this.descriptionText.setText(description);
            this.descriptionText.slideIn();
        } );

        startTimeout();
    }

    public void hide(){
        if(!open) return;
        open = false;

        animate(false);

        endTimeout();
    }

    private void animate(boolean show){
        animate(show, null);
    }

    private void animate(boolean show, Runnable finished){
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

        animate().alpha(show? 1f: 0f).setDuration(animationTime).setInterpolator(interpolator).start();
        ValueAnimator widthAnimation, heightAnimation;
        widthAnimation = ValueAnimator.ofInt(cardView.getWidth(), show? cardWidth: 0).setDuration(animationTime);
        heightAnimation = ValueAnimator.ofInt(cardView.getHeight(), show? cardHeight: 0).setDuration(animationTime);
        widthAnimation.setInterpolator(interpolator);
        heightAnimation.setInterpolator(interpolator);

        AnimatorSet scaleAnimations = new AnimatorSet();
        scaleAnimations.playTogether(widthAnimation, heightAnimation);
        scaleAnimations.setDuration(animationTime);

        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
                lp.width = (Integer) valueAnimator.getAnimatedValue();
                cardView.setLayoutParams(lp);
            }
        });


        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LayoutParams lp = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                cardView.setLayoutParams(lp);
            }
        });


        scaleAnimations.addListener(new AnimatorListenerAdapter() {
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
                if(finished != null && attached())
                    finished.run();
            }
        });

        scaleAnimations.start();
    }

    private void startTimeout(){
        if(timeoutHandler == null) timeoutHandler = new Handler();
        if(timeoutRunnable != null){
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(!attached()) return;

                descriptionText.slideOut(()->{
                    hide();
                });
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable,  timeout);
    }

    private void endTimeout(){
        if(timeoutRunnable != null){
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private boolean attached(){
        return getContext() != null;
    }

}
