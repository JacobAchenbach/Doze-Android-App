package cache.doze.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Chris on 11/27/2018.
 */

public class MoreAnimationButton extends RelativeLayout {
    Context context;

    public MoreAnimationButton(Context context){
        super(context);
        this.context = context;

        sharedConstructor();
    }

    public MoreAnimationButton(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;

        sharedConstructor();
    }

    private void sharedConstructor(){

    }
}
