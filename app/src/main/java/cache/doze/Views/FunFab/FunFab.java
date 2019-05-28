package cache.doze.Views.FunFab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cache.doze.Fragments.AddNewReplyFragment;
import cache.doze.R;
import cache.doze.Tools.QuickTools;

/**
 * Created by Chris on 10/18/2018.
 */

public class FunFab extends CardView {
    private View rootView;
    private View background;
    private CardView fabView;
    private RelativeLayout.LayoutParams fabLP;
    private ImageView icon;
    private RelativeLayout containerHeader;
    private LinearLayout submitButton;
    private LinearLayout cancelButton;
    private View divider;

    private Context context;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private FabExpandListener fabExpandListener;
    private FabSubmitListener fabSubmitListener;
    private FabCancelListener fabCancelListener;

    private int funFabRadius;
    private int funFabClosedWH;

    private int knownBottom;
    private int screenWidth;
    private int screenHeight;
    private float heightRatio;
    private float widthRatio;
    private int openH;
    private int openW;
    private float extraHeight;
    private float originalX;
    private float originalY;
    private float openY;
    private int marginEnd;
    private int marginBottom;

    private boolean open = false;
    private boolean suspended = false;
    private boolean animating = false;
    private boolean doSubmit = false;
    private boolean doCancel = false;

    private ViewGroup expandedView;

    private final int START_ANIM_TIME = 100;
    private final int END_ANIM_TIME = 250;

    private FabState currentState;

    public FunFab(Context context) {
        super(context);
        this.context = context;

        sharedConstructor();
    }

    public FunFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        sharedConstructor();
    }

    public FunFab(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        sharedConstructor();
    }

    private void sharedConstructor() {
        rootView = LayoutInflater.from(context).inflate(R.layout.view_fun_fab, this, false);
        addView(rootView);

        funFabRadius = QuickTools.convertDpToPx(context, 28);
        funFabClosedWH = QuickTools.convertDpToPx(context, 56);

        background = rootView.findViewById(R.id.background);
        fabView = rootView.findViewById(R.id.fab);
        icon = rootView.findViewById(R.id.icon);
        containerHeader = rootView.findViewById(R.id.fab_header);
        submitButton = rootView.findViewById(R.id.submit_button_wrapper);
        cancelButton = rootView.findViewById(R.id.cancel_button_wrapper);
        divider = rootView.findViewById(R.id.divider);
        expandedView = rootView.findViewById(R.id.container);

        fabLP = (RelativeLayout.LayoutParams) fabView.getLayoutParams();
        setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.transparent)));

    }

    public Fragment init(Fragment fragment, FragmentManager supportFragmentManager) {
        return init(supportFragmentManager, 0.85f, 1);
    }

    public Fragment init(FragmentManager supportFragmentManager, float heightRatio, float widthRatio) {
        this.heightRatio = heightRatio;
        this.widthRatio = widthRatio;

        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                screenHeight = getHeight();
                screenWidth = getWidth();

                openH = (int) (screenHeight * heightRatio);
                extraHeight = openH * 0.02f + funFabRadius * 2;

                openW = (int) (screenWidth * widthRatio);

                knownBottom = -1;
                originalX = -1;
                originalY = -1;
                marginBottom = -1;
                marginEnd = -1;
                openY = -1;
                currentState = new FabStateContainer();

                setupCloseBorder();
                setupOnClick();
                setupSubmitListener();
                setupCloseListener();
                setDraggable();

            }
        });

        fragmentManager = supportFragmentManager;

        if (fragment == null) {
            fragment = new AddNewReplyFragment();
            fragmentManager.beginTransaction().add(R.id.container, fragment, "Add New").commitAllowingStateLoss();
        }
        return fragment;
    }

    private void setupCloseBorder() {
        background.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (animating || !open) return false;

                float fabTop = fabView.getY();
                float evY = event.getY();
                //if(evY < fabTop && !suspended) {
                if (!suspended) {
                    if (suspendable && !animating)
                        swipeExpand(false, 1);
                    else
                        expandFab(false, true);
                }
                return false;
            }
        });
        background.setClickable(false);
        background.setFocusable(false);
    }

    private void setupOnClick() {
        fabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!open) expand(true);
            }
        });
    }

    private void setupSubmitListener() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewFlung) expandFab(false, true);
                //if(fabSubmitListener != null && wasClick)doSubmit = true;
            }
        });
    }

    private void setupCloseListener() {
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewFlung) expandFab(false, true);
                //if(fabCancelListener != null && wasClick) fabCancelListener.onCancel();
            }
        });
    }

    public boolean isAnimating() {
        return animating;
    }

    /**
     * Animations for expanding the fab into a fragment container
     */
    private void expand(boolean show) {
        expand(show, false);
    }

    private void expand(boolean show, boolean fast) {
        if (animating) return;
        animating = true;
        //currentState.expand(show, fast);
        dist = 0;
        final int startTime = !fast ? START_ANIM_TIME : START_ANIM_TIME * 2 / 3;
        final int endTime = !fast ? END_ANIM_TIME : END_ANIM_TIME * 2 / 3;

        if(fabLP == null) fabLP = (RelativeLayout.LayoutParams) fabView.getLayoutParams();
        if (knownBottom == -1) knownBottom = rootView.getBottom();
        if (marginEnd == -1) marginEnd = fabLP.getMarginEnd();
        if (marginBottom == -1) marginBottom = fabLP.bottomMargin;
        if (originalX == -1 && marginEnd != -1)
            originalX = screenWidth - marginEnd * 2 - funFabClosedWH * 2;
        if (originalY == -1) originalY = getHeight() - funFabClosedWH;
        if (originalY == -1) originalY = fabView.getY();
        if (openY == -1) {
            openY = getHeight() - openH + marginBottom;
            //openY = getHeight() - openH + marginBottom;
            //openY = getHeight() * heightRatio;
            maxY = openY - extraHeight;
        }


        if (show) { //Open FunFab
            openFab(startTime, endTime);

        } else { //Close FunFab
            if (rootView.getBottom() < knownBottom) {
                hideKeyboardAndWaitToClose(startTime, endTime); //Keyboard is open, we have to wait until it's closed
            } else
                closeFab(startTime, endTime);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animating = false;
            }
        }, open ? endTime : startTime + endTime);
    }

    private void invokeButtonListeners() {
        if (fabSubmitListener != null && doSubmit) fabSubmitListener.onSubmit();
        if (fabCancelListener != null && doCancel) fabCancelListener.onCancel();
        doSubmit = doCancel = false;
    }

    /**
     * The native nav bar is shown when the SoftKeyboard is, this often messes
     * up the calculations for where the bottom of the user's screen is.
     * <p>
     * The bottom of your screen changes and it's reasonably the only way to know that the keyboard
     * We wait until it looks like its closed to continue, but we only try it 5 times
     *
     * @param startTime - Inherited; first animation time
     * @param endTime   - Inherited; second animation time
     */
    private void hideKeyboardAndWaitToClose(final int startTime, final int endTime) {
        hideKeyboard(context, getWindowToken());

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int attempts = 0;

            @Override
            public void run() {
                if (rootView.getBottom() >= knownBottom) {
                    invokeButtonListeners();
                    closeFab(startTime, endTime);
                    attempts = 0;
                } else if (isAttachedToWindow() && attempts < 5) {
                    handler.postDelayed(this, 250);
                    attempts++;
                } else if (isAttachedToWindow() && attempts >= 5) {
                    invokeButtonListeners();
                    closeFab(startTime, endTime);
                }
            }
        };

        handler.postDelayed(runnable, 250);
    }


    private void openFab(int startTime, int endTime) {
        if (suspended) {
            swipeExpand(true, 0);
            suspend(false);
            return;
        }

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
        ValueAnimator marginBottomAnimation = ValueAnimator.ofInt(fabLP.bottomMargin, (int) -extraHeight - marginBottom).setDuration(endTime);
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

        ObjectAnimator.ofFloat(fabView, "cardElevation", fabView.getCardElevation(), QuickTools.convertDpToPx(context, 16)).setDuration(startTime).start();

        ObjectAnimator.ofFloat(fabView, "radius", funFabRadius, getResources().getDimension(R.dimen.default_rounded_corner)).setDuration(startTime).start();

        //fabView.setY(openY - marginBottom);

        setViewVisibility(true, startTime, endTime);


        finishOp(startTime, endTime);
    }

    /**
     *
     */

    private void closeFab(int startTime, int endTime) {
        if (fabSubmitListener != null && doSubmit) fabSubmitListener.onSubmit();
        if (fabCancelListener != null && doCancel) fabCancelListener.onCancel();
        doSubmit = doCancel = suspended = false;

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

        ObjectAnimator.ofFloat(fabView, "cardElevation", fabView.getCardElevation(), QuickTools.convertDpToPx(context, 2)).setDuration(startTime).start();

        ObjectAnimator.ofFloat(fabView, "radius", fabView.getRadius(), funFabRadius).setDuration(endTime).start();


        if (toTopAnimation != null) toTopAnimation.cancel();


        setViewVisibility(false, startTime, endTime);


        finishOp(startTime, endTime);
    }

    private void finishOp(int startTime, int endTime) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                open = !open;
                if (fabExpandListener != null) fabExpandListener.onFabExpanded(open);
                animating = false;
                if (!open) {
                    fabView.animate().y(getStartY()).setDuration(endTime).start();
                    setFadedBackground(startTime, false);
                }
            }
        }, open ? endTime : startTime + endTime);
    }

    private int getStartY() {
        int er = getHeight() - marginBottom - funFabClosedWH;
        return er;
    }

    private void setViewVisibility(boolean show, int startTime, int endTime) {
        if (expandedView == null || fragment == null) return;

        setFadedBackground(startTime, show);

        ValueAnimator fabColorAnim;
        if (show) {
            fabColorAnim = ValueAnimator.ofArgb(fabView.getCardBackgroundColor().getDefaultColor(), ContextCompat.getColor(context, R.color.white));


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

                private void end() {
                    submitButton.setVisibility(VISIBLE);
                    submitButton.setAlpha(1f);

                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setAlpha(1f);

//                    divider.setVisibility(View.VISIBLE);
//                    divider.setAlpha(1f);
                }

            }).start();

            cancelButton.setAlpha(0f);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.animate().alpha(1f).setDuration(endTime).start();

//            divider.setAlpha(0f);
//            divider.setVisibility(View.VISIBLE);
//            divider.animate().alpha(1f).setDuration(endTime).start();

            fragment.onResume();
        } else {
            fabColorAnim = ValueAnimator.ofArgb(fabView.getCardBackgroundColor().getDefaultColor(), ContextCompat.getColor(context, R.color.colorAccent));


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

                private void end() {
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
        }

        fabColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                fabView.setCardBackgroundColor(color);
            }
        });
        fabColorAnim.setDuration(startTime).start();
    }

    boolean backgroundShowing = false;

    private void setFadedBackground(int animTime, boolean enabled) {
        backgroundShowing = enabled;
        background.setClickable(enabled);
        background.setFocusable(enabled);
        if (background.getAlpha() != 0f || enabled) {
            background.animate().alpha(enabled? 0.2f: 0f).setDuration(animTime).start();
        }
    }

    public void expandFab(boolean show, boolean fast) {
        expand(show, fast);
    }

    public void setFabState(FabState state) {
        if (state == null) return;

        currentState = state;
    }

    public void hide() {
        fabView.animate().alpha(0f).setDuration(END_ANIM_TIME).start();
        fabView.setClickable(false);
        fabView.setFocusable(false);
    }

    public void show() {
        fabView.animate().alpha(1f).setDuration(START_ANIM_TIME).start();
        fabView.setClickable(true);
        fabView.setFocusable(true);
    }

    /**
     * Moves the fab whenever a SnackBar is shown
     */
    Handler handler = null;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            snackBarRunnable.waitAndHide();
        }
    };
    SnackRunnable snackBarRunnable;

    class SnackRunnable implements Runnable {
        boolean animRan = false;
        float preY = -1;

        int snackEndTime = 1750;

        Snackbar snackbar;

        public SnackRunnable(Snackbar snackbar) {
            this.snackbar = snackbar;
        }

        @Override
        public void run() {
            if (preY == -1) preY = fabView.getY();
            if (handler != null) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, snackEndTime);
                return;
            }
            handler = new Handler();
            handler.postDelayed(runnable, snackEndTime);
            fabView.setAnimation(null);
            fabView.clearAnimation();
            fabView.animate().y(preY - (snackbar.getView().getHeight())).setDuration(200).setListener(new AnimatorListenerAdapter() {
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

                private void end() {
                }
            }).start();
        }

        public void waitAndHide() {
            handler = null;
            if (!open) fabView.animate().y(getStartY()).setDuration(250).start();
            else fabView.animate().y(preY).setDuration(250).start();
        }
    }

    public void moveForSnackBar(Snackbar snackbar) {
        if (snackbar == null) return;
        fabView.setAnimation(null);
        fabView.clearAnimation();
        snackbar.getView().post(snackBarRunnable = new SnackRunnable(snackbar));
    }


    /**
     * STATE_FRAGMENT_CONTAINER
     * <p>
     * All of the code for when the FunFab is a container for a Fragment
     */
    boolean viewFlung;
    boolean wasClick;
    boolean suspendable;
    float change;
    float lastY;
    float maxY;
    float minY;
    float dist; //Distance to drag view, when dist = 0: the view is in the starting position and hasn't been dragged yet
    float moveAmt;
    ObjectAnimator toTopAnimation;

    final float SMOOTH_FACTOR = 0.55f;

    @SuppressLint("ClickableViewAccessibility")
    public void setDraggable() {
        final GestureDetector flingDetector = new GestureDetector(context, new FlingGestureDetector());

        minY = getHeight() - getResources().getDimension(R.dimen.item_size_medium);
        OnTouchListener touchListener = new OnTouchListener() {
            boolean scrolledUp;


            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (ev.getPointerCount() > 1 || !open || animating)
                    return false;

                flingDetector.onTouchEvent(ev);

                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        wasClick = true;

                        lastY = ev.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float fabY = fabView.getY();
                        float curY = ev.getY();
                        change = lastY - curY;
                        scrolledUp = lastY - curY > 0;

                        if (scrolledUp && !backgroundShowing) {
                            suspend(false);
                        }

                        //This lets us know that the user is not trying to click anything
                        // due to the amount they moved after putting their finger down
                        if (Math.abs(change) > 10)
                            wasClick = doSubmit = doCancel = false;

                        //Basically prevents scroll from "twitching" when user uses 2 fingers
                        if (Math.abs(change) > 200)
                            lastY = curY;

                        dist += change; // dist is negative as you move down the screen

                        //For temporarily disabling scroll
                        if (!open || viewFlung || wasClick) {
                            if (wasClick) {
                                dist -= change;
                                if (view == submitButton) doSubmit = true;
                                else if (view == cancelButton) doCancel = true;
                            }
                            break;
                        }

                        //Close any keyboards if scrolling down
                        if (!scrolledUp) {
                            hideKeyboard(context, getWindowToken());
                        }

                        float y = fabView.getY();

                        moveAmt = -dist * SMOOTH_FACTOR;
                        float newY = openY + moveAmt;

                        //Clamping move amount to either bound
                        if (newY < maxY)
                            newY = newY;//newY = maxY;
                        else if (newY > minY && !suspended) {
                            if (suspendable)
                                suspend(true);
                            else if (open && !animating) {
                                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                                layoutParams.height = getHeight() - fabView.getTop() + (int) extraHeight;
                                fabView.setLayoutParams(layoutParams);
                                fabView.animate().y(originalY).setDuration(END_ANIM_TIME).start();
                                expand(false);
                            }
                            return false;
                        } else if (newY > minY) {
                            newY = minY;
                            dist -= change;
                        }

                        if(newY < openY && newY > maxY){
                            float dest = openY - maxY; // Cus maxY < openY
                            float cur = newY - maxY;
                            float percentage = 1 - (cur / dest);//1 - (maxY / newY);
                            float newPerc = SMOOTH_FACTOR - (SMOOTH_FACTOR * percentage);
                            moveAmt = -dist * newPerc;
                            newY = openY + moveAmt;
                        }


                        if (open && !animating)
                            fabView.setY(newY); //Higher Positive value means closer to bottom of screen

                        break;
                    case MotionEvent.ACTION_UP:
                        if (openY + moveAmt < openY && !animating) {
                            toTopAnimation = ObjectAnimator.ofFloat(fabView, "y", fabView.getY(), openY);
                            toTopAnimation.addListener(new AnimatorListenerAdapter() {
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

                                private void end() {
                                    animating = false;
                                }
                            });
                            toTopAnimation.setDuration(100).start();
                            dist = 0;
                            animating = true;
                        } else if (!viewFlung && !scrolledUp && (openY + moveAmt > screenHeight - openH * 3 / 5f)) {
                            //expand(false);
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

    class FlingGestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if (Math.abs(velocityY) > 800) {
                swipeExpand(velocityY < 0, velocityY);
                viewFlung = true;
            } else {
//                if(velocityY > 500)
//                    verticalFling(fabView, velocityY, 0, velocityY);
//                else
//                    verticalFling(fabView, velocityY, 0, screenHeight - openY - getResources().getDimension(R.dimen.item_size_medium));
            }
            return false;
        }
    }

    private void swipeExpand(boolean expand, float velocity) {
        float fabY = fabView.getY();
        if (expand) {
            animating = true;
            verticalFling(fabView, velocity, 0, screenHeight, true);
            dist = 0;
        } else {
            if (suspendable) {
                animating = true;
                lockSheetBottom(velocity);
            } else {
                ViewGroup.LayoutParams layoutParams = fabView.getLayoutParams();
                layoutParams.height = getHeight() - getTop() + (int) extraHeight;
                fabView.setLayoutParams(layoutParams);
                fabView.animate().y(originalY).setDuration(END_ANIM_TIME * 2 / 3).start();
                expand(false, true);
            }
        }
    }

    private void lockSheetBottom(float velocity) {
        if (open) {
            //destination must be a large positive value to be at the bottom of the screen
            //final float destination = getHeight() - containerHeader.getHeight() * 3 + marginBottom;
            final float destination = minY;
            verticalFling(fabView, velocity, 0, destination, true);
            suspended = true;
            setDist(minY);
            setFadedBackground(END_ANIM_TIME, false);
        }
    }

    public void setSuspendable(boolean suspendable) {
        this.suspendable = suspendable;
    }

    private void suspend(boolean suspended) {
        this.suspended = suspended;
        setFadedBackground(END_ANIM_TIME, !suspended);
    }

    private void verticalFling(final View view, float velocity, float from, float to) {
        verticalFling(view, velocity, from, to, false);
    }

    private void verticalFling(final View view, float velocity, float from, float to, boolean doubleSpeed) {
        FlingAnimation flingAnimation = new FlingAnimation(view, DynamicAnimation.TRANSLATION_Y);
        if (velocity > 0) {
            if (velocity < to * 3) velocity = to * 3;
        } else {
            float ty = view.getTranslationY();
            //if (view.getY() - getHeight() - openY - velocity < (getHeight() - openY) * 3) velocity = -(getHeight() - openY) * 3;
            velocity = -(getHeight() - openY) * 3;
        }
        if (doubleSpeed)
            velocity *= 1.5f;
        //Finally figured out that setMaxValue uses: large numbers for bottom of screen, small numbers for top of screen.
        flingAnimation.setStartVelocity(velocity).setMinValue(0).setMaxValue(suspended? to - openY - 1: to - openY).setFriction(0.5f);
        flingAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                if (!canceled && Math.abs(velocity) > 200) {
                    SpringAnimation sa = new SpringAnimation(view,
                            DynamicAnimation.TRANSLATION_Y, value);
                    sa.setStartVelocity(velocity);
                    sa.start();
                }
                animating = false;
            }
        });
        try {
            flingAnimation.start();
            animating = true;
        } catch (Exception e) {
            e.printStackTrace();
            animating = false;
            //view.animate().y(minY).setDuration(END_ANIM_TIME).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }

    private void setDist(float newY) {
        lastY = 0;
        this.dist = -(newY - openY) / SMOOTH_FACTOR;
    }


    public void setIcon(Drawable icon) {
        this.icon.setBackground(icon);
    }

    public void setSubmitText(CharSequence text) {
        if (submitButton == null) return;

        ((TextView) submitButton.findViewById(R.id.submit_button)).setText(text);
    }

    public void setSubmitImage(int src) {
        if (submitButton == null) return;

        ((ImageView) submitButton.findViewById(R.id.submit_image)).setBackgroundResource(src);
    }

    public void setFabClosedBackground(int color) {
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), fabView.getCardBackgroundColor().getDefaultColor(), color).setDuration(END_ANIM_TIME);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                fabView.setCardBackgroundColor(val);
            }
        });
        colorAnim.start();
    }

    public void setFabExpandedBackground(Drawable background) {
        if (open) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.container).setBackground(background);

                }
            }, END_ANIM_TIME);
        } else
            findViewById(R.id.container).setBackground(background);
    }

    public void setCancelText(CharSequence text) {
        if (cancelButton == null) return;

        ((TextView) cancelButton.findViewById(R.id.cancel_button)).setText(text);
    }

    public void setCancelImage(int src) {
        if (cancelButton == null) return;

        ((ImageView) cancelButton.findViewById(R.id.cancel_image)).setBackgroundResource(src);
    }

    public void setSecondaryColor(int color) {
        ((TextView) submitButton.findViewById(R.id.submit_button)).setTextColor(color);
        ((TextView) cancelButton.findViewById(R.id.cancel_button)).setTextColor(color);
    }

    /**
     * STATE_OPTION
     * <p>
     * All of the code for when the FunFab is a menu with multiple options
     */

    private ArrayList<FabOption> fabOptions;

    public void addOption(String title, Drawable icon) {
        if (fabOptions == null) fabOptions = new ArrayList<>();

        fabOptions.add(new FabOption(title, icon));
    }

    private class FabOption {
        String title;
        Drawable icon;

        public FabOption(String title, Drawable icon) {
            this.title = title;
            this.icon = icon;
        }
    }

    public static void hideKeyboard(Context context, IBinder token) {
        InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null) imm.hideSoftInputFromWindow(token, 0);
    }


    /**
     * Handles the interfaces for listeners
     */
    public interface FabExpandListener {
        void onFabExpanded(boolean shown);
    }

    public interface FabSubmitListener {
        void onSubmit();
    }

    public interface FabCancelListener {
        void onCancel();
    }

    public void setFabExpandListener(FabExpandListener fabExpandListener) {
        this.fabExpandListener = fabExpandListener;
    }

    public void setFabSubmitListener(FabSubmitListener fabSubmitListener) {
        this.fabSubmitListener = fabSubmitListener;
    }

    public void setFabCancelListener(FabCancelListener fabCancelListener) {
        this.fabCancelListener = fabCancelListener;
    }

}
