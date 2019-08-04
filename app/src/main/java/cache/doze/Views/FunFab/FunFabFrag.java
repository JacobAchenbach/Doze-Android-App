package cache.doze.Views.FunFab;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FunFabFrag extends Fragment {

    protected FunFab.KeyboardVisibilityListener keyboardVisibilityListener;

    public void setKeyboardVisibilityListener(FunFab.KeyboardVisibilityListener visibilityListener) {
        this.keyboardVisibilityListener = visibilityListener;
    }

//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
//        mainActivity = (MainActivity) getActivity();
//        currentState = STATE_ADD_NEW;
//        root = inflater.inflate(R.layout.fragment_add_new, container, false);
//
//
//        return root;
//    }
}
