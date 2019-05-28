package cache.doze.Views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cache.doze.R;
import cache.doze.Tools.QuickTools;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class DozeToolbar extends Toolbar {

    private Context context;
    private CardView root;
    private TextView title;
    private ImageView settingsIcon;

    private RecyclerView recyclerView;

    private float maxEndMargins;
    private float scrollerY;
    private float minHeight;
    private float maxHeight;
    private float maxElevation;
    private float minTextSize;
    private float maxTextSize;
    private float minIconSize;
    private float maxIconSize;


    public DozeToolbar(Context context) {
        super(context);
        sharedConstructor(context);
    }

    public DozeToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context);
    }

    public DozeToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor(context);
    }

    private void sharedConstructor(Context context){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_doze_toolbar, this, true);
        root = findViewById(R.id.root_layout);
        title = root.findViewById(R.id.text_title);
        settingsIcon = root.findViewById(R.id.icon_settings);

    }


    public void setScroller(RecyclerView recyclerView){
        this.recyclerView = recyclerView;

        post(new Runnable() {
            @Override
            public void run() {
                initValues();
                setUpRecyclerView();
            }
        });
    }

    private void initValues(){
        maxHeight = getMeasuredHeight();
        minHeight = getResources().getDimension(R.dimen.item_size_medium);
        maxElevation = QuickTools.convertDpToPx(context, 8);
        maxTextSize = title.getTextSize();
        minTextSize = QuickTools.convertSpToPx(context, 24);
        maxIconSize = settingsIcon.getHeight();
        minIconSize = QuickTools.convertDpToPx(context, 28);
        maxEndMargins = getResources().getDimension(R.dimen.padding_large);
    }

    private void setUpRecyclerView(){
        final float limit = minHeight / 2;

        recyclerView.setPadding(recyclerView.getPaddingLeft(), (int) (recyclerView.getPaddingTop() + limit), recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
        recyclerView.setClipToPadding(false);
        recyclerView.scrollTo(0, 0);
        recyclerView.scrollBy(0, -1000);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollerY = recyclerView.computeVerticalScrollOffset();
                if(scrollerY < 0)
                    scrollerY = 0;
                else if(scrollerY > limit)
                    scrollerY = limit;

                float percentage = maxHeight == 0? 0: scrollerY / limit;

                int height = (int) (maxHeight - ((maxHeight - minHeight) * percentage));
                int toolbarMargin = (int) (maxEndMargins * (1 - percentage));
                ViewGroup.LayoutParams subLP = root.getLayoutParams();
                subLP.height = height;
                root.setLayoutParams(subLP);

                ViewGroup.MarginLayoutParams myLP = (MarginLayoutParams) getLayoutParams();
                myLP.height = height;
                //myLP.leftMargin = myLP.rightMargin = toolbarMargin;
                setLayoutParams(myLP);

                //root.setCardElevation(maxElevation * percentage);
                setElevation(maxElevation * percentage);

                title.setTextSize(COMPLEX_UNIT_PX, maxTextSize - ((maxTextSize - minTextSize) * percentage));
                int titleMargin = (int) (maxEndMargins * percentage);
                ViewGroup.MarginLayoutParams titleLP = (MarginLayoutParams) title.getLayoutParams();
                //titleLP.leftMargin = titleMargin;
                title.setLayoutParams(titleLP);

                int iconHeight = (int) (maxIconSize - ((maxIconSize - minIconSize) * percentage));
                int iconMargin = (int) (maxEndMargins * percentage);
                ViewGroup.MarginLayoutParams settingsLP = (MarginLayoutParams) settingsIcon.getLayoutParams();
                settingsLP.height = settingsLP.width = iconHeight;
                //settingsLP.rightMargin = iconMargin;
                settingsIcon.setLayoutParams(settingsLP);
            }
        });
    }


}
