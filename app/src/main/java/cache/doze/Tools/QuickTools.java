package cache.doze.Tools;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Chris on 8/28/2018.
 */

public class QuickTools {

    public static int convertDpToPx(Context context, int dp){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float convertDpToPx(Context context, float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
