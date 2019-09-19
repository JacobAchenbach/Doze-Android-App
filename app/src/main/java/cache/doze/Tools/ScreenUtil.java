package cache.doze.Tools;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

import cache.doze.R;

public class ScreenUtil {

    private static ArrayList<OnKeyboardVisibilityChangedListener> keyboardVisibilityChangedListeners;
    public static boolean isKeyboardShowing;

    public static void setUpKeyboardListener(View contentView, Activity activity){
        keyboardVisibilityChangedListeners = new ArrayList<>();

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                //Log.d(TAG, "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true;
                        onKeyboardVisibilityChanged(true);
                    }
                }
                else {
                    // keyboard is closed
                    if (isKeyboardShowing) {
                        isKeyboardShowing = false;
                        onKeyboardVisibilityChanged(false);
                    }
                }
            }
        });
    }

    private static void onKeyboardVisibilityChanged(boolean isOpen){
        for(OnKeyboardVisibilityChangedListener keyboardVisibilityChangedListener: keyboardVisibilityChangedListeners)
            keyboardVisibilityChangedListener.onKeyboardVisibilityChanged(isOpen);
    }

    public static void addKeyboardVisibilityChangedListener(OnKeyboardVisibilityChangedListener keyboardVisibilityChangedListener){
        keyboardVisibilityChangedListeners.add(keyboardVisibilityChangedListener);
    }

    public interface OnKeyboardVisibilityChangedListener{
        void onKeyboardVisibilityChanged(boolean isOpen);
    }
}
