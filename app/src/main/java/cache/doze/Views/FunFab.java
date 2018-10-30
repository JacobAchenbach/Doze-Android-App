package cache.doze.Views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import cache.doze.Fragments.AddNewReplyFragment;
import cache.doze.R;
import cache.doze.Tools.QuickTools;

/**
 * Created by Chris on 10/18/2018.
 */

public class FunFab extends CardView{
    View rootView;
    CardView fabView;
    RelativeLayout.LayoutParams fabLP;
    ImageView icon;
    LinearLayout submitButton;
    LinearLayout cancelButton;
    View divider;

    private Context context;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private FabExpandListener fabExpandListener;
    private FabSubmitListener fabSubmitListener;
    private FabCancelListener fabCancelListener;

    private int funFabRadius;
    private int funFabClosedWH;

    private int screenWidth;
    private int screenHeight;
    private int openH;
    private int openW;
    private float extraHeight;
    private float originalX;
    private float originalY;
    private float openY;
    private int marginEnd;
    private int marginBottom;

    private boolean open = false;
    private boolean animating = false;
    private boolean doSubmit = false;

    private ViewGroup expandedView;

    private final int START_ANIM_TIME = 100;
    private final int END_ANIM_TIME = 200;

    public FunFab(Context context){
        super(context);
        this.context = context;

        sharedConstructor();
    }

    public FunFab(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;

        sharedConstructor();
    }

    public FunFab(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        sharedConstructor();
    }

    private void sharedConstructor(){
        rootView = inflate(context, R.layout.view_fun_fab,null);
        addView(rootView);

        getBackground().setAlpha(0);
        rootView.getBackground().setAlpha(0);

        funFabRadius = QuickTools.convertDpToPx(context, 28);
        funFabClosedWH = QuickTools.convertDpToPx(context, 56);

        fabView = rootView.findViewById(R.id.fab);
        icon = rootView.findViewById(R.id.icon);
        submitButton = rootView.findViewById(R.id.submit_button_wrapper);
        cancelButton = rootView.findViewById(R.id.cancel_button_wrapper);
        divider = rootView.findViewById(R.id.divider);
        expandedView = rootView.findViewById(R.id.container);

        fabLP = (RelativeLayout.LayoutParams) fabView.getLayoutParams();
    }

    public Fragment init(FragmentManager supportFragmentManager, int viewHeight, float heightRatio, int viewWidth, float widthRatio) {
        fragmentManager = supportFragmentManager;

        screenHeight = viewHeight;
        screenWidth = viewWidth;

        this.openH = (int) (viewHeight * heightRatio);
        extraHeight = openH * 0.02f + funFabRadius * 2;

        this.openW = (int) (viewWidth * widthRatio);

        originalX = -1;
        originalY = -1;
        marginBottom = -1;
        marginEnd = -1;
        openY = -1;

        setupCloseBorder();
        setupOnClick();
        setupSubmitListener();
        setupCloseListener();
        setDraggable();

        if (fragment == null) {
            fragment = new AddNewReplyFragment();
            fragmentManager.beginTransaction().add(R.id.container, fragment, "Add New").commitAllowingStateLoss();
        }


        return fragment;
    }

    private void setupCloseBorder(){
        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(open)expand(false, true);
            }
        });
        rootView.setClickable(false);
        rootView.setFocusable(false);
    }

    private void setupOnClick(){
        fabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!open) expand(true);
            }
        });
    }

    private void setupSubmitListener(){
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandFab(false, true);
                if(fabSubmitListener != null && canSubmit)doSubmit = true;
            }
        });
    }

    private void setupCloseListener(){
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                expandFab(false, true);
                if(fabCancelListener != null && canSubmit) fabCancelListener.onCancel();
            }
        });
    }

    private void expand(boolean show){
        expand(show, false);
    }

    private void expand(boolean show, boolean fast){
        if(animating) return;
        animating = true;
        dist = 0;
        final int startTime = !fast? START_ANIM_TIME: START_ANIM_TIME*2/3;
        final int endTime = !fast? END_ANIM_TIME: END_ANIM_TIME*2/3;

        if(marginEnd == -1)marginEnd = fabLP.getMarginEnd();
        if(marginBottom == -1)marginBottom = fabLP.bottomMargin;
        if(originalX == -1 && marginEnd != -1)originalX = screenWidth - marginEnd * 2- funFabClosedWH * 2;
        if(originalY == -1 && marginBottom != -1)originalY = screenHeight - marginBottom - funFabClosedWH * 2;
        if(openY == -1){
            openY = screenHeight - openH + marginBottom;
            maxY = openY - extraHeight;
        }

        if (show) { //Open FunFab
            openFab(startTime, endTime);

        } else{ //Close FunFab
            if(fabView.getY() < maxY) {
                hideKeyboardAndWaitToClose(startTime, endTime); //Keyboard is open, we have to wait until it's closed
            }else
                closeFab(startTime, endTime);
        }
    }

    private void hideKeyboardAndWaitToClose(final int startTime, final int endTime){
        hideKeyboard(context, getWindowToken());

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int attempts = 0;
            @Override
            public void run() {
                if(fabView.getY() >= maxY) {
                    closeFab(startTime, endTime);
                    attempts = 0;
                }
                else if(isAttachedToWindow() && attempts < 5) {
                    handler.postDelayed(this, 250);
                    attempts++;
                }else if(isAttachedToWindow() && attempts >= 5){
                    closeFab(startTime, endTime);
                }
            }
        };

        handler.postDelayed(runnable, 250);
    }


    private void openFab(int startTime, int endTime){
        //Animate icon out
        icon.animate().alpha(0f).setDuration(startTime).start();

        //Change end margins to fit screen
        ValueAnimator marginEndAnimation = ValueAnimator.ofInt(fabLP.getMarginEnd(), 0).setDuration(startTime);
        marginEndAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                fabLP.setMarginEnd(val);
                fabView.setLayoutParams(fabLP);
            }
        });
        marginEndAnimation.start();

        //Change Width to desired width
        ValueAnimator widthAnimation = ValueAnimator.ofInt(funFabClosedWH, openW).setDuration(startTime);
        widthAnimation.setInterpolator(new AccelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                layoutParams.width = val;
                fabView.setLayoutParams(layoutParams);
            }
        });
        widthAnimation.start();


        //Change bottom margins to fit screen
        ValueAnimator marginBottomAnimation = ValueAnimator.ofInt(fabLP.bottomMargin, (int) -extraHeight).setDuration(endTime);
        marginBottomAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fabLP.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                fabView.setLayoutParams(fabLP);
            }
        });
        marginBottomAnimation.setStartDelay(startTime);
        marginBottomAnimation.start();

        //Change Height to desired height
        ValueAnimator heightAnimation = ValueAnimator.ofInt(funFabClosedWH, (int) (openH + extraHeight)).setDuration(endTime);
        heightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                layoutParams.height = val;
                fabView.setLayoutParams(layoutParams);
            }
        });
        heightAnimation.setStartDelay(startTime);
        heightAnimation.start();

        setViewVisibility(true, startTime, endTime);


        finishOp(startTime, endTime);
    }

    /**
     *
     */

    private void closeFab(int startTime, int endTime){
        //Animate icon in
        icon.animate().alpha(1f).setStartDelay(startTime).setDuration(100).start();


        //Change bottom margins to fit screen
        ValueAnimator marginBottomAnimation = ValueAnimator.ofInt(fabLP.bottomMargin, marginBottom).setDuration(endTime);
        marginBottomAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                fabLP.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                fabView.setLayoutParams(fabLP);
            }
        });
        marginBottomAnimation.start();

        //Change Height to original height
        ValueAnimator heightAnimation = ValueAnimator.ofInt(fabView.getHeight(), funFabClosedWH).setDuration(endTime);
        heightAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                layoutParams.height = val;
                fabView.setLayoutParams(layoutParams);
            }
        });
        heightAnimation.start();


        //Change end margins to old margins
        ValueAnimator marginEndAnimation = ValueAnimator.ofInt(fabLP.getMarginEnd(), marginEnd).setDuration(endTime);
        marginEndAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                fabLP.setMarginEnd(val);
                fabView.setLayoutParams(fabLP);
            }
        });
        //marginEndAnimation.setStartDelay(100);
        marginEndAnimation.start();

        //Change Width to original width
        ValueAnimator widthAnimation = ValueAnimator.ofInt(fabView.getWidth(), funFabClosedWH).setDuration(endTime);
        widthAnimation.setInterpolator(new AccelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                layoutParams.width = val;
                fabView.setLayoutParams(layoutParams);
            }
        });
        widthAnimation.start();

        if(toTopAnimation != null) toTopAnimation.cancel();

        fabView.animate().y(screenHeight - marginBottom - funFabClosedWH).setDuration(endTime).start();

        setViewVisibility(false, startTime, endTime);


        finishOp(startTime, endTime);
    }

    private void finishOp(int startTime, int endTime){
        if(doSubmit)fabSubmitListener.onSubmit();
        doSubmit = false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open = !open;
                if(fabExpandListener != null)fabExpandListener.onFabExpanded(open);
                animating = false;
            }
        }, open? endTime: startTime + endTime);
    }

    private void setViewVisibility(boolean show, int startTime, int endTime){
        if(expandedView == null || fragment == null)return;

        if(show){
            rootView.setClickable(true);
            rootView.setFocusable(true);

            expandedView.setAlpha(0f);
            expandedView.setVisibility(View.VISIBLE);
            expandedView.animate().alpha(1f).setStartDelay(endTime).setDuration(startTime).start();

            submitButton.setAlpha(0f);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.animate().alpha(1f).setDuration(endTime).setListener(new AnimatorListenerAdapter() {
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
                    submitButton.setVisibility(VISIBLE);
                    submitButton.setAlpha(1f);

                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setAlpha(1f);

                    divider.setVisibility(View.VISIBLE);
                    divider.setAlpha(1f);
                }

            }).start();

            cancelButton.setAlpha(0f);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.animate().alpha(1f).setDuration(endTime).start();

            divider.setAlpha(0f);
            divider.setVisibility(View.VISIBLE);
            divider.animate().alpha(1f).setDuration(endTime).start();

            ObjectAnimator backgroundAnim = ObjectAnimator.ofPropertyValuesHolder(rootView.getBackground(),
                            PropertyValuesHolder.ofInt("alpha", 0, (int)(255 * 0.2f)));
            backgroundAnim.setDuration(endTime);
            backgroundAnim.start();

            fragment.onResume();
        }else {
            rootView.setClickable(false);
            rootView.setFocusable(false);
            expandedView.setAlpha(1f);
            expandedView.animate().alpha(0f).setDuration(startTime).start();

            submitButton.setAlpha(1f);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.animate().alpha(0f).setDuration(startTime).setListener(new AnimatorListenerAdapter() {
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
                    submitButton.setVisibility(GONE);
                    cancelButton.setVisibility(GONE);
                    divider.setVisibility(GONE);
                }

            }).start();

            cancelButton.setAlpha(1f);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.animate().alpha(0f).setDuration(startTime).start();

            divider.setAlpha(1f);
            divider.setVisibility(View.VISIBLE);
            divider.animate().alpha(0f).setDuration(startTime).start();

            fabView.requestFocus();
            fragment.onPause();

            ObjectAnimator backgroundAnim = ObjectAnimator.ofPropertyValuesHolder(rootView.getBackground(),
                    PropertyValuesHolder.ofInt("alpha", rootView.getBackground().getAlpha(), 0));
            backgroundAnim.setDuration(startTime);
            backgroundAnim.start();
        }
    }

    public void expandFab(boolean show, boolean fast){
        expand(show, fast);
    }

    boolean viewFlung;
    boolean draggingView;
    boolean canSubmit;
    float lastY;
    float maxY;
    float minY;
    float dist; //Distance to drag view, when dist = 0: the view is in the starting position and hasn't been dragged yet
    float moveAmt;
    ObjectAnimator toTopAnimation;

    @SuppressLint("ClickableViewAccessibility")
    public void setDraggable() {
        final GestureDetector flingDetector = new GestureDetector(context, new FlingGestureDetector());

        minY = screenHeight - extraHeight - funFabClosedWH;
        OnTouchListener touchListener = new OnTouchListener() {
            boolean scrolledUp;


            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (ev.getPointerCount() > 1 || !open || animating)
                    return false;

                flingDetector.onTouchEvent(ev);

                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        canSubmit = true;

                        lastY = ev.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float curY = ev.getY();
                        float change = lastY - curY;
                        scrolledUp = lastY - curY > 0;


                        if(Math.abs(change) > 10)
                            canSubmit = false;

                        //Basically prevents scroll from "twitching" when user uses 2 fingers
                        if (Math.abs(change) > 200)
                            lastY = curY;

                        dist += change; // dist is negative as you move down the screen

                        //For temporarily disabling scroll
                        if (!open || viewFlung || canSubmit) {
                            if(canSubmit)dist -= change;
                            break;
                        }

                        //Close any keyboards if scrolling down
                        if(!scrolledUp){
                            hideKeyboard(context, getWindowToken());
                        }

                        float y = fabView.getY();
/*                        if(y < openY){ //Below thresh
                            float threshDistance = openY - maxY;
                            float perc = dist / threshDistance;
                            if(perc > 1)perc = 1f;
                            dist = dist - (threshDistance)* (1 - perc);
                        }*/

                        moveAmt = -dist * 0.55f;
                        float newY = openY + moveAmt;

                        //Clamping move amount to either bound
                        if (newY < maxY)
                            newY = maxY;
                        else if (newY > minY) {
                            if(open && !animating) {
                                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                                layoutParams.height = screenHeight - fabView.getTop() + (int) extraHeight;
                                fabView.setLayoutParams(layoutParams);
                                expand(false);
                            }
                            return false;
                        }


                        if(open && !animating)
                            fabView.setY(newY);

                        break;
                    case MotionEvent.ACTION_UP:
                        if(openY + moveAmt < openY) {
                            swipeExpand(true);
                            dist = 0;
                        }else if(!viewFlung && !scrolledUp && (openY + moveAmt > screenHeight - openH * 3/5f)){
                            expand(false);
                        }
                        lastY = 0;
                        viewFlung = false;
                        //if(dist < openY - moveAmt) bounce();
                        break;
                }
                return false;
            }
        };


        fabView.setOnTouchListener(touchListener);
        submitButton.setOnTouchListener(touchListener);
        cancelButton.setOnTouchListener(touchListener);
    }

    protected void bounce(){
        dist = openY;

        //ViewPropertyAnimator ymove = fabView.animate().y(openY).setDuration(END_ANIM_TIME).setInterpolator(new DecelerateInterpolator());

    }

    class FlingGestureDetector extends android.view.GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if(Math.abs(velocityY) > 800) {
                swipeExpand(velocityY < 0);
                viewFlung = true;
            }
            return false;
        }
    }

    private void swipeExpand(boolean expand){
        if(expand){
            toTopAnimation = ObjectAnimator.ofFloat(fabView, "y", fabView.getY(), openY).setDuration(100);
            toTopAnimation.start();
            dist = 0;
        }else {
            ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
            layoutParams.height = screenHeight - getTop() + (int) extraHeight;
            fabView.setLayoutParams(layoutParams);
            expand(false, true);
        }
    }

    public void setIcon(Drawable icon){
        this.icon.setBackground(icon);
    }

    public static void hideKeyboard(Context context, IBinder token){
        InputMethodManager imm = ((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if(imm != null) imm.hideSoftInputFromWindow(token, 0);
    }

    public interface FabExpandListener{
        public void onFabExpanded(boolean shown);
    }
    public interface FabSubmitListener{
        public void onSubmit();
    }
    public interface FabCancelListener {
        public void onCancel();
    }

    public void setFabExpandListener(FabExpandListener fabExpandListener){
        this.fabExpandListener = fabExpandListener;
    }
    public void setFabSubmitListener(FabSubmitListener fabSubmitListener){
        this.fabSubmitListener = fabSubmitListener;
    }
    public void setFabCancelListener(FabCancelListener fabCancelListener){
        this.fabCancelListener = fabCancelListener;
    }

}
