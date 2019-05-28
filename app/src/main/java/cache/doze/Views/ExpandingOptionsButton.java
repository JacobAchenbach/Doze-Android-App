package cache.doze.Views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cache.doze.R;
import cache.doze.Tools.QuickTools;

public class ExpandingOptionsButton extends RelativeLayout {

    private Context context;

    private ImageView mainIcon;
    private RelativeLayout optionIconsContainer;
    private ArrayList<ExpandedIcon> expandedIcons;

    private int iconSize;

    private boolean expanded;

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
        mainIcon = findViewById(R.id.icon_options);
        optionIconsContainer = findViewById(R.id.container_option_icons);
        iconSize = QuickTools.convertDpToPx(context, 24);

        setUpMainIcon();
    }

    private void setUpMainIcon(){
        mainIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(!expanded);
                expanded = !expanded;
            }
        });
    }

    private void expand(boolean show){
        if(show) {
            int destWidth = expandedIcons.size() * iconSize;

            //Change Width to expanded width
            ValueAnimator widthAnimation = ValueAnimator.ofInt(optionIconsContainer.getWidth(), destWidth).setDuration(250);
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

            optionIconsContainer.animate().alpha(1f).setDuration(150).start();
        }
    }

    public void setIcon(@DrawableRes int res){
        mainIcon.setBackground(ContextCompat.getDrawable(context, res));
    }

    public void addIcon(@DrawableRes int res, OnClickListener onClickListener){
        ExpandedIcon option = new ExpandedIcon(context, ContextCompat.getDrawable(context, res), onClickListener);
        optionIconsContainer.addView(option);
        expandedIcons.add(option);
    }



    private class ExpandedIcon extends AppCompatImageView {
        OnClickListener onClickListener;

        public ExpandedIcon(Context context, Drawable drawable, OnClickListener onClickListener) {
            super(context);
            this.onClickListener = onClickListener;
            setOnClickListener(onClickListener);

            setBackground(drawable);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(iconSize, iconSize);
            lp.addRule(CENTER_VERTICAL);
            setLayoutParams(lp);
        }

    }
}
