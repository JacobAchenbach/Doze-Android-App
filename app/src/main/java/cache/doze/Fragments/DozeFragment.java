package cache.doze.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

import cache.doze.Activities.MainActivity;
import cache.doze.R;
import cache.doze.Views.DozeSnackbar;
import cache.doze.Views.DozeToolbar;

/**
 * Created by Chris on 11/8/2018.
 */

public class DozeFragment extends Fragment {
    public static ArrayList<DozeFragment> dozeFragments = new ArrayList<>();

    View rootView;
    private int viewWidth = 0;

    private Queue<Runnable> methodQueue = new PriorityQueue<>();

    public boolean isShown;

    public final static int ANIMATION_TIME = 250;

    public DozeToolbar getToolbar(){
        return getActivity() != null? ((MainActivity)getActivity()).getToolbar(): null;
    }
    public DozeSnackbar getSnackbar() {
        return getActivity() != null? ((MainActivity)getActivity()).getSnackbar(): null;
    }

    @Override
    public void onStart() {
        super.onStart();
        dozeFragments.add(this);
    }

    public void onCreateView(View rootView){
        this.rootView = rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewWidth = rootView.getWidth();
        while (!methodQueue.isEmpty() && rootView != null)
            methodQueue.remove().run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dozeFragments.remove(this);
    }

    public boolean onBackPressed(){
        return false;
    }

    public void fadeIn(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> fadeIn();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setAlpha(0f);
        runAnimation(true, rootView.animate().alpha(1f));
    }

    public void fadeOut(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> fadeOut();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setAlpha(1f);
        runAnimation(false, rootView.animate().alpha(0f));
    }

    public void slideOutLeft(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> slideOutLeft();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setX(0);
        runAnimation(false, rootView.animate().x(-rootView.getWidth()));
    }

    public void slideInRight(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> slideInRight();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setX(((ViewGroup)rootView.getParent()).getWidth());
        runAnimation(true, rootView.animate().x(0).setInterpolator(new AccelerateInterpolator()));
    }

    public void slideOutRight(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> slideOutRight();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setX(0);
        runAnimation(false, rootView.animate().x(rootView.getWidth()));
    }

    public void slideInLeft(){
        if(queueIfNotAdded()){
            Runnable queuedMethod = () -> slideInLeft();
            methodQueue.add(queuedMethod);
            return;
        }

        rootView.setX(-rootView.getWidth());
        runAnimation(true, rootView.animate().x(0).setInterpolator(new AccelerateInterpolator()));
    }

    private void runAnimation(boolean madeVisible, ViewPropertyAnimator animation){

        animation.setDuration(ANIMATION_TIME).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation){
                super.onAnimationStart(animation);
                isShown = madeVisible;
                rootView.setVisibility(View.VISIBLE);
                if(madeVisible)onResume();
            }
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
                if(madeVisible)
                    rootView.setVisibility(View.VISIBLE);
                else{
                    rootView.setVisibility(View.GONE);
                    onPause();
                }

            }
        }).start();

    }

    private boolean queueIfNotAdded(){
        return rootView == null;
    }

}
