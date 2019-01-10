package cache.doze.Views;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cache.doze.R;
import cache.doze.SaveEditText;
import cache.doze.Tools.QuickTools;

public class FluidSearchView extends FrameLayout{
    private Toolbar toolbar;
    private Activity activity;
    private Context context;

    View parent;
    View root;
    ImageView searchMag;
    ImageView searchClose;
    EditText searchField;
    View background;

    private OnClickListener searchClickListener;
    private OnClickListener searchCloseListener;
    private SearchView.OnQueryTextListener onQueryTextListener;

    public float searchMagStartLeft = -1;
    float searchFieldStartLeft = -1;
    private boolean searchShowing;
    public static boolean isDetached = true;
    private int animationTime;

    public FluidSearchView(Context context){
        super(context);
        this.context = context;
    }

    public FluidSearchView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
    }

    public FluidSearchView(Toolbar toolbar, Activity activity){
        super(toolbar.getContext());
        this.toolbar = toolbar;
        this.activity = activity;
        this.context = toolbar.getContext();
    }


    public void build(){
        parent = activity.getLayoutInflater().inflate(R.layout.view_fluid_searchview, (ViewGroup) toolbar.getParent().getParent(), true);
        root = parent.findViewById(R.id.root_layout);

        searchMag = root.findViewById(R.id.search_mag);
        searchClose = root.findViewById(R.id.search_close);
        searchField = root.findViewById(R.id.search_field);
        background = root.findViewById(R.id.background);

        searchShowing = false;

        setUpOnSearchClick();
        setUpOnCloseClick();
        setUpDefaultQueryTextListener();
        setUpInputListener();
        checkMagOffset();


        show();
        FluidSearchView.isDetached = false;
    }

    private void adjustHitBounds(final View view, final int amt){
        view.post(new Runnable() {
            @Override
            public void run() {
                final View viewParent = (View) view.getParent();
                final Rect bound = new Rect();
                view.getHitRect(bound);
                bound.top -= amt;
                bound.left -= amt;
                bound.bottom += amt;
                viewParent.setTouchDelegate(new TouchDelegate(bound, view));
            }
        });
    }

    private void checkMagOffset(){
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                searchField.getLayoutParams().width = toolbar.getWidth() - QuickTools.convertDpToPx(context, 128);

                final ViewGroup actionMenu = (ViewGroup) toolbar.getChildAt(1);
                actionMenu.post(new Runnable() {
                    @Override
                    public void run() {
                        if(actionMenu.getChildCount() != 0) {
                            final View firstItem = actionMenu.getChildAt(0);
                            if (firstItem != null) {
                                int[] loc = new int[2];
                                firstItem.getLocationOnScreen(loc);
                                int dp = QuickTools.convertDpToPx(context, 48);
                                searchMagStartLeft = loc[0] + ((ViewGroup) searchMag.getParent()).getPaddingEnd() / 2 - dp;
                                searchMag.setX((int) searchMagStartLeft);
                            }
                        }
                        searchMag.setVisibility(View.VISIBLE);
                        adjustHitBounds(searchMag, QuickTools.convertDpToPx(context, 24));
                        adjustHitBounds(searchClose, QuickTools.convertDpToPx(context, 24));
                    }
                });
            }
        });
    }

    private void setUpOnSearchClick(){
        searchClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateViews(!searchShowing);
                //searchField.setText("");
            }
        };

        searchMag.setOnClickListener(searchClickListener);
    }

    private void setUpOnCloseClick(){
        searchCloseListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(searchField.getText())) {
                    animateViews(false);
                }
                else{
                    searchField.setText("");
                }
            }
        };

        searchClose.setOnClickListener(searchCloseListener);
    }

    private void setUpDefaultQueryTextListener(){
        onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    private void setUpInputListener(){
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onQueryTextListener.onQueryTextChange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setOnSearchClickListener(final OnClickListener clickListener){
        if(FluidSearchView.isDetached)return;

        searchMag.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchClickListener.onClick(v);

                clickListener.onClick(v);
            }
        });
    }

    public void setOnCloseClickListener(final OnClickListener clickListener){
        if(FluidSearchView.isDetached)return;

        searchClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCloseListener.onClick(v);

                clickListener.onClick(v);
            }
        });
    }

    public void setOnQueryTextListener(SearchView.OnQueryTextListener queryTextListener){
        this.onQueryTextListener = queryTextListener;
    }

    public void setQueryHint(String hint){
        searchField.setHint(hint);
    }
    public String getQueryHint(){
        return searchField.getHint().toString();
    }

    //This is obnoxiously long
    protected void animateViews(boolean show){
        if(searchMagStartLeft == -1) searchMagStartLeft = searchMag.getLeft();
        if(searchFieldStartLeft == -1) searchFieldStartLeft = searchField.getX();
        final int magOffset = QuickTools.convertDpToPx(context, 16);
        final int searchOffset = QuickTools.convertDpToPx(context, 64);
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        setMenuItemsEnabled(!show);


        if(show) { //Animate SearchView open
            searchShowing = true;
            final View navBurger = toolbar.getChildAt(0);
            final AnimatorSet shrinkSet = new AnimatorSet();
            ObjectAnimator shrinkX = ObjectAnimator.ofFloat(navBurger,"ScaleX",1,0);
            ObjectAnimator shrinkY = ObjectAnimator.ofFloat(navBurger,"ScaleY",1,0);
            shrinkSet.playTogether(shrinkX, shrinkY);
            searchClose.setVisibility(View.VISIBLE);
            shrinkSet.setDuration(100).start();
            shrinkSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    navBurger.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    navBurger.setVisibility(View.GONE);
                }
            });

            background.setBackground(toolbar.getBackground());
            background.setAlpha(0);
            background.setVisibility(VISIBLE);
            ObjectAnimator backgroundFadeIn = ObjectAnimator.ofFloat(background, "alpha", 0f, 1f);
            backgroundFadeIn.setDuration(100);
            backgroundFadeIn.start();

            final AnimatorSet moveViews = new AnimatorSet();

            //Move Mag Icon
            ObjectAnimator magMove = ObjectAnimator.ofFloat(searchMag, "x", searchMag.getX(), magOffset - 20);
            magMove.setInterpolator(new AccelerateInterpolator());

            //Move EditText
            ObjectAnimator textMove = ObjectAnimator.ofFloat(searchField, "x", searchMag.getX() + magOffset, searchOffset - 20);
            textMove.setInterpolator(new AccelerateInterpolator());

            //Fade it in too
            searchField.setAlpha(0);
            searchField.setVisibility(VISIBLE);
            ObjectAnimator textFadeIn = ObjectAnimator.ofFloat(searchField, "alpha", 0f, 1f);
            textFadeIn.setDuration(150);
            textFadeIn.setStartDelay(50);
            textFadeIn.start();

            moveViews.playTogether(magMove, textMove);
            moveViews.setDuration(100);
            moveViews.start();

            moveViews.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishMoving();
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    finishMoving();
                }
                private void finishMoving(){
                    //Ease Mag Icon Back
                    ObjectAnimator magMove = ObjectAnimator.ofFloat(searchMag, "x", searchMag.getX(), searchMag.getX() + 20);
                    magMove.setInterpolator(new DecelerateInterpolator());

                    //Ease EditText Back
                    ObjectAnimator textMove = ObjectAnimator.ofFloat(searchField, "x", searchField.getX(), searchField.getX() + 20);
                    textMove.setInterpolator(new DecelerateInterpolator());

                    moveViews.removeAllListeners();
                    moveViews.setDuration(100).playTogether(magMove, textMove);
                    moveViews.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            finish();
                        }
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finish();
                        }
                        private void finish(){
                            searchMag.setX(magOffset);
                            searchField.setX(searchOffset);
                            adjustHitBounds(searchMag, 0);

                            searchField.requestFocus();
                            if(imm != null) imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                    moveViews.start();

                    searchField.setAlpha(1f);
                    searchField.setVisibility(View.VISIBLE);

                }
            });


            AnimatorSet growSet = new AnimatorSet();
            ObjectAnimator growX = ObjectAnimator.ofFloat(searchClose,"ScaleX",0,1);
            ObjectAnimator growY = ObjectAnimator.ofFloat(searchClose,"ScaleY",0,1);
            growSet.playTogether(growX, growY);
            searchClose.setScaleX(0);
            searchClose.setScaleY(0);
            searchClose.setVisibility(View.VISIBLE);
            searchClose.setClickable(true);
            growSet.setStartDelay(25);
            growSet.setDuration(100).start();
            growSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    searchClose.setVisibility(VISIBLE);
                    searchClose.setScaleX(1);
                    searchClose.setScaleY(1);
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    searchClose.setVisibility(VISIBLE);
                    searchClose.setScaleX(1);
                    searchClose.setScaleY(1);
                }
            });

        }else{ //Animate SearchView close
            searchShowing = false;
            final View navBurger = toolbar.getChildAt(0);
            AnimatorSet growSet = new AnimatorSet();
            ObjectAnimator growX = ObjectAnimator.ofFloat(navBurger,"ScaleX",0,1);
            ObjectAnimator growY = ObjectAnimator.ofFloat(navBurger,"ScaleY",0,1);
            growSet.playTogether(growX, growY);
            navBurger.setVisibility(View.VISIBLE);
            growSet.setStartDelay(25);
            growSet.setDuration(100).start();
            growSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    navBurger.setScaleX(1);
                    navBurger.setScaleY(1);
                }
                @Override
                public void onAnimationEnd(Animator animation){
                    navBurger.setScaleX(1);
                    navBurger.setScaleY(1);

                }
            });

            background.setBackground(toolbar.getBackground());
            background.setAlpha(1f);
            background.setVisibility(VISIBLE);
            ObjectAnimator backgroundFadeOut = ObjectAnimator.ofFloat(background, "alpha", 1f, 0f);
            backgroundFadeOut.setStartDelay(50);
            backgroundFadeOut.setDuration(100);
            backgroundFadeOut.start();

            AnimatorSet moveViews = new AnimatorSet();

            ObjectAnimator textFadeOut = ObjectAnimator.ofFloat(searchField, "alpha", 1f, 0f);
            textFadeOut.setDuration(150);
            textFadeOut.start();

            //Move Mag Icon
            ObjectAnimator magMove = ObjectAnimator.ofFloat(searchMag, "x", searchMag.getX(), searchMagStartLeft);
            magMove.setInterpolator(new AccelerateInterpolator());

            //Move EditText
            ObjectAnimator textMove = ObjectAnimator.ofFloat(searchField, "x", searchField.getX(), searchMagStartLeft + magOffset);
            textMove.setInterpolator(new AccelerateInterpolator());

            moveViews.playTogether(magMove, textMove);
            moveViews.setDuration(150);
            moveViews.start();

            moveViews.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishMoving();
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    finishMoving();
                }

                private void finishMoving(){
                    adjustHitBounds(searchMag, 0);

                    searchField.setVisibility(GONE);
                    if(imm != null)imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
                }
            });

            AnimatorSet shrinkSet = new AnimatorSet();
            ObjectAnimator shrinkX = ObjectAnimator.ofFloat(searchClose,"ScaleX",1,0);
            ObjectAnimator shrinkY = ObjectAnimator.ofFloat(searchClose,"ScaleY",1,0);
            shrinkSet.playTogether(shrinkX, shrinkY);
            searchClose.setVisibility(View.VISIBLE);
            searchClose.setClickable(false);
            shrinkSet.setDuration(100).start();
            shrinkSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    searchClose.setVisibility(View.GONE);
                    searchClose.setClickable(false);
                    searchClose.setScaleX(0);
                    searchClose.setScaleY(0);
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    searchClose.setVisibility(View.GONE);
                    searchClose.setClickable(false);
                    searchClose.setScaleX(0);
                    searchClose.setScaleY(0);
                }
            });
        }
    }

    private void staticAdjustSearch(boolean show){
        if(show){

        }else{
            searchMag.setX((int) searchMagStartLeft);
            searchField.setVisibility(View.GONE);
            background.setAlpha(0f);
            background.setVisibility(View.GONE);
            searchClose.setVisibility(View.GONE);

            View navBurger = toolbar.getChildAt(0);
            if(navBurger != null){
                navBurger.setVisibility(View.VISIBLE);
                navBurger.setScaleX(1);
                navBurger.setScaleY(1);
            }

            final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null)imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
            setMenuItemsEnabled(true);
            searchShowing = false;
        }
    }

    public ImageView getMagIcon(){
        return searchMag;
    }

    public void setMagIcon(Drawable drawable) {
        searchMag.setBackground(drawable);
    }

    private void setMenuItemsEnabled(boolean enabled){
        ActionMenuView menuView = (ActionMenuView) toolbar.getChildAt(1);
        Menu menu = menuView.getMenu();
        if(menu == null)return;

        for(int i = 0; i < menu.size(); i++)
            menu.getItem(i).setEnabled(enabled);
    }

    public boolean isOpened(){
        return searchShowing;
    }

    public void onSearch(){
        searchClickListener.onClick(searchMag);
    }

    public void onClose(){
        searchCloseListener.onClick(searchClose);
    }

    public void staticOnClose(){
        staticAdjustSearch(false);
    }

    public void show(){
        root.setVisibility(View.VISIBLE);
    }

    public void hide(){
        if(root != null)
            root.setVisibility(View.GONE);
    }

    public void detach(){
        if(parent == null)return;
        staticAdjustSearch(false);
        ((ViewGroup)parent).removeView(root);
        parent.requestLayout();
        FluidSearchView.isDetached = true;
    }
}