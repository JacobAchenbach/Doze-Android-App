package cache.doze.Views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cache.doze.R;
import cache.doze.Tools.QuickTools;

public class ExpandingOptionsButton extends RelativeLayout {

    private Context context;

    private ImageView mainIcon;
    private RelativeLayout optionIconsContainer;
    private ArrayList<ExpandedIcon> expandedIcons = new ArrayList<>();

    private OnClickListener onExpandListener;

    private int iconSize;
    private int mediumPadding;

    private boolean expanded;
    private boolean animating;

    public ExpandingOptionsButton(Context context) {
        super(context);
        sharedConstructor(context);
    }

    public ExpandingOptionsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context);
    }

    public ExpandingOptionsButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor(context);
    }

    public void sharedConstructor(Context context){
        this.context = context;
        setElevation(QuickTools.convertDpToPx(context, 8));
        LayoutInflater.from(context).inflate(R.layout.view_expanding_options_button, this);
        mainIcon = findViewById(R.id.icon_options);
        optionIconsContainer = findViewById(R.id.container_option_icons);
        optionIconsContainer.setVisibility(INVISIBLE);
        iconSize = QuickTools.convertDpToPx(context, 24);
        mediumPadding = (int) getResources().getDimension(R.dimen.padding_medium);

        setUpMainIcon();
    }

    private void setUpMainIcon(){
        ((ViewGroup) mainIcon.getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(!expanded);
            }
        });
    }

    public void expand(boolean show){
        if(animating) return;

        if(show)
            onExpandListener.onClick(this);


        expanded = show;
        animating = true;

        int destWidth;

        if(show) {
            int numberOfIcons = expandedIcons.size();
            destWidth = iconSize * numberOfIcons + iconSize * numberOfIcons;

            ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(mainIcon, "rotation", 0f, 90f);
            rotationAnim.setInterpolator(new AccelerateInterpolator());
            rotationAnim.setDuration(150).start();

            //Change Width to expanded width
            ValueAnimator widthAnimation = ValueAnimator.ofInt(optionIconsContainer.getWidth(), destWidth).setDuration(200);
            widthAnimation.setInterpolator(new DecelerateInterpolator());
            widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = optionIconsContainer.getLayoutParams();
                    layoutParams.width = val;
                    optionIconsContainer.setLayoutParams(layoutParams);
                }
            });
            widthAnimation.setStartDelay(150);
            widthAnimation.start();

            mainIcon.animate().translationX(50).setInterpolator(new AccelerateInterpolator()).setStartDelay(0).setDuration(150).start();
            mainIcon.animate().alpha(0f).setStartDelay(0).setDuration(150).start();
            optionIconsContainer.setAlpha(0f);
            optionIconsContainer.setVisibility(VISIBLE);
            optionIconsContainer.animate().alpha(1f).setStartDelay(150).setDuration(150).start();
        }else{
            destWidth = iconSize;

            ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(mainIcon, "rotation", 90f, 180f);
            rotationAnim.setInterpolator(new DecelerateInterpolator());
            rotationAnim.setStartDelay(0);
            rotationAnim.setDuration(350).start();

            //Change Width to expanded width
            ValueAnimator widthAnimation = ValueAnimator.ofInt(optionIconsContainer.getWidth(), destWidth).setDuration(100);
            widthAnimation.setInterpolator(new AccelerateInterpolator());
            widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = optionIconsContainer.getLayoutParams();
                    layoutParams.width = val;
                    optionIconsContainer.setLayoutParams(layoutParams);
                }
            });
            widthAnimation.start();

            mainIcon.animate().translationX(0).setInterpolator(new DecelerateInterpolator()).setStartDelay(100).setDuration(150).start();
            mainIcon.animate().alpha(1f).setStartDelay(100).setDuration(250).start();
            optionIconsContainer.animate().alpha(0f).setStartDelay(0).setDuration(100).start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!expanded)
                        optionIconsContainer.setVisibility(GONE);
                }
            }, 250);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(animating) animating = false;
            }
        }, 350);
    }

    public void setIcon(@DrawableRes int res){
        mainIcon.setBackground(ContextCompat.getDrawable(context, res));
    }

    public void addIcon(@DrawableRes int res, OnClickListener onClickListener){
        ExpandedIcon option = new ExpandedIcon(context, ContextCompat.getDrawable(context, res), onClickListener);
        optionIconsContainer.addView(option);
        expandedIcons.add(option);
    }

    public void addIcon(@DrawableRes int res, OnTouchListener onTouchListener){
        ExpandedIcon option = new ExpandedIcon(context, ContextCompat.getDrawable(context, res), onTouchListener);
        optionIconsContainer.addView(option);
        expandedIcons.add(option);
    }


    public void setTint(int color){
        mainIcon.getBackground().setTint(color);

        for(ExpandedIcon option: expandedIcons){
            option.setTint(color);
        }
    }

    public void setOnExpandListener(OnClickListener onExpandListener){
        this.onExpandListener = onExpandListener;
    }

    public boolean isExpanded(){
        return expanded;
    }

    public int getIconCount(){
        return expandedIcons.size();
    }

    public int getContainerWidth(){
        return optionIconsContainer.getWidth();
    }

    private class ExpandedIcon extends RelativeLayout {//AppCompatImageView {
        ImageView icon;
        OnClickListener onClickListener;

        public ExpandedIcon(Context context, Drawable drawable, OnClickListener onClickListener) {
            super(context);
            this.onClickListener = onClickListener;
            setOnClickListener(onClickListener);

            init(drawable);
        }

        public ExpandedIcon(Context context, Drawable drawable, OnTouchListener onTouchListener) {
            super(context);
            setOnTouchListener(onTouchListener);

            init(drawable);
        }

        private void init(Drawable drawable){
            icon = new ImageView(context);
            addView(icon);

            icon.setBackground(drawable);
            icon.getBackground().setTint(ContextCompat.getColor(context, R.color.black));
            RelativeLayout.LayoutParams iconLP = new RelativeLayout.LayoutParams(iconSize, iconSize);
            iconLP.addRule(CENTER_IN_PARENT);
            icon.setLayoutParams(iconLP);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(iconSize * 2, iconSize * 2);
            lp.addRule(CENTER_VERTICAL);
            lp.addRule(ALIGN_PARENT_END);
            int numberOfIcons = expandedIcons.size();
            int margin = iconSize * (numberOfIcons + 1) + iconSize * (numberOfIcons - 1);
            //if(numberOfIcons > 0) margin += mediumPadding;
            lp.setMargins(0,0, margin,0);
            setLayoutParams(lp);
        }

        public void setTint(int color){
            icon.getBackground().setTint(color);
            //optionIconsContainer.setBackgroundTintList(new ColorStateList(new int[][]{}, new int[]{ContextCompat.getColor(context, R.color.black)}));
        }

    }
}
