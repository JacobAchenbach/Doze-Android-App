package cache.doze.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Chris on 3/25/2019.
 */

public class TestViewGroup  extends RelativeLayout{

    public TestViewGroup(Context context) {
        super(context);
        sharedConst();
    }

    public TestViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConst();
    }

    public TestViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConst();
    }

    public TestViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sharedConst();
    }

    private void sharedConst(){

    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d("FINDmyEVENT", "onInterceptTouchEvent: " + ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
