package cache.doze.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.Tools.QuickTools;
import cache.doze.Views.FunFab;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by Chris on 2/22/2018.
 */

public class MainRepliesFragment extends Fragment {

    MainActivity mainActivity;
    AddNewReplyFragment addNewFrag;

    View root;
    TextView title;
    RecyclerView replyRecycler;
    ReplyListAdapter recyclerViewAdapter;
    FunFab fab;

    public static MainRepliesFragment newInstance(int page, String title) {
        MainRepliesFragment mainRepliesFragment = new MainRepliesFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        mainRepliesFragment.setArguments(args);
        return mainRepliesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        root = inflater.inflate(R.layout.fragment_main_replies, container, false);

        mainActivity = (MainActivity) getActivity();
        title = root.findViewById(R.id.title);
        replyRecycler = root.findViewById(R.id.recycler_view);
        fab = root.findViewById(R.id.fab);


        setUpRecyclerView();
        mainActivity.getToolbar().post(new Runnable() {
            @Override
            public void run() {
                setUpFab();
            }
        });
        return root;
    }

    private void setUpRecyclerView(){
        replyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        replyRecycler.setAdapter(recyclerViewAdapter = new ReplyListAdapter(MainActivity.replyItems, getActivity()));

        OverScrollDecoratorHelper.setUpOverScroll(replyRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        replyRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalY;
            float paddingTop = getResources().getDimension(R.dimen.padding_xlarge);
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalY+= dy;

                if(totalY <= paddingTop) {
                    float perc = totalY / paddingTop;
                    title.setAlpha(1 - perc);
                }else title.setAlpha(0);

            }
        });
    }

    boolean fabExpanded = false;
    private void setUpFab(){
        if(getActivity() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point screen = new Point();
            display. getSize(screen);
            float w = screen.x;

            int toolbar = mainActivity.getToolbar().getHeight();
            float h = screen.y - toolbar;

            addNewFrag = (AddNewReplyFragment) fab.init(getActivity().getSupportFragmentManager(), (int) h, 0.85f, (int) w, 1);
            fab.setFabExpandListener(new FunFab.FabExpandListener() {
                @Override
                public void onFabExpanded(boolean shown) {
                    fabExpanded = shown;
                    if(!shown)fabClosed();
                }
            });

            fab.setFabSubmitListener(new FunFab.FabSubmitListener() {
                @Override
                public void onSubmit() {
                    if(addNewFrag.getState() == AddNewReplyFragment.STATE_ADD_NEW) {
                        MainActivity.replyItems.add(addNewFrag.getReplyItem());
                    }
                    else {
                        editReplyItem(addNewFrag.getReplyItem());
                        addNewFrag.clear();
                    }
                }
            });

            fab.setFabCancelListener(new FunFab.FabCancelListener() {
                @Override
                public void onCancel() {
                    addNewFrag.clear();
                    addNewFrag.setState(AddNewReplyFragment.STATE_ADD_NEW);
                }
            });

            recyclerViewAdapter.setOnItemClickedListener(new onItemClickedListener() {
                @Override
                public void onItemClick(View view, int position) {
                    addNewFrag.setReplyItem(MainActivity.replyItems.get(position));
                    addNewFrag.setState(AddNewReplyFragment.STATE_EDITING);
                    fab.expandFab(true, true);
                }
            });
        }
    }

    private void fabClosed(){
        InputMethodManager imm = ((InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
        if(imm != null) imm.hideSoftInputFromWindow(addNewFrag.inputPreset.getWindowToken(), 0);
    }

    private void editReplyItem(ReplyItem replyItem){
        recyclerViewAdapter.updateReplyItem(replyItem);

//        itemView.title.setText(replyItem.getTitle());
//        itemView.replyText.setText(replyItem.getReplyText());
//        itemView.itemView.invalidate();
        recyclerViewAdapter.notifyDataSetChanged();


    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);

    }

    @Override
    public void onResume(){
        super.onResume();
    }


    public boolean onBackPressed(){
        if(fabExpanded){
            fab.expandFab(false, true);
            return true;
        }
        return false;
    }


    /**
     * Adapter and ViewHolder Classes
     */

    class ReplyListAdapter extends RecyclerView.Adapter<ReplyListAdapter.ViewHolder>{

        private List<ReplyItem> replyItems;
        private List<ViewHolder> viewHolders;
        private Activity activity;

        private onItemClickedListener itemClickedListener;
        private View.OnLongClickListener longClickListener;

        private int imageSize = -1;
        private int red, green, blue;
        private int maxColor = 220;
        private int minColor = 170;
        private int randomBound = maxColor - (maxColor - minColor) / 2;

        public ReplyListAdapter(List<ReplyItem> items, Activity activity) {
            this.replyItems = items;
            this.activity = activity;

            viewHolders = new ArrayList<>();
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_listed_replyitem, parent, false);
            return new ViewHolder(v);
        }

        int pointerX;
        int pointerY;
        @Override public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            viewHolders.add(holder);

            final ReplyItem item = replyItems.get(position);
            holder.title.setText(item.getTitle());
            holder.replyText.setText(item.getReplyText());
            setEnabled(holder, item.isChecked(), false);

            //if(imageSize == -1)imageSize = holder.proPic.getLayoutParams().width;
            holder.itemView.setTag(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemClickedListener != null) itemClickedListener.onItemClick(view, position);
                }
            });
            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent ev) {
                    switch (ev.getActionMasked()){
                        case MotionEvent.ACTION_DOWN:
                            pointerX = (int) ev.getX();
                            pointerY = (int) ev.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            pointerX = (int) ev.getX();
                            pointerY = (int) ev.getY();
                            break;
                    }
                    return false;
                }
            });
            holder.itemView.setOnLongClickListener(longClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    boolean checked = item.isChecked();

                    if(!checked)mainActivity.startSMSService();
                    else mainActivity.endSMSService();

                    item.setChecked(!checked);
                    setEnabled(holder, !checked, true);
                    return false;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemClickedListener != null)itemClickedListener.onItemClick(view, position);
                }
            });

        }

        private void setEnabled(final ViewHolder holder, boolean enabled, boolean animate){
            int tint;
            int goalElevation;
            if(enabled){
                goalElevation = QuickTools.convertDpToPx(getContext(), 6);
                if(animate){
                    holder.enabledInd.setAlpha(1f);

                    //AnimatorSet animatorSet = new AnimatorSet();
                    ValueAnimator elevationAnim = ValueAnimator.ofInt((int) holder.bg.getElevation(), goalElevation);
                    elevationAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            holder.bg.setCardElevation((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    elevationAnim.setDuration(150);

                    final Animator circularReveal = ViewAnimationUtils.createCircularReveal(holder.enabledInd, pointerX, pointerY,0, holder.itemView.getWidth() * 1.1f);
                    circularReveal.setDuration(150);
                    circularReveal.setInterpolator(new AccelerateInterpolator());
                    circularReveal.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.enabledInd.setVisibility(View.VISIBLE);
                            holder.disabledInd.setVisibility(View.INVISIBLE);
                            super.onAnimationEnd(animation);
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            holder.enabledInd.setVisibility(View.VISIBLE);
                            holder.disabledInd.setVisibility(View.INVISIBLE);
                            super.onAnimationEnd(animation);
                        }
                    });
                    holder.enabledInd.setVisibility(View.VISIBLE);
                    circularReveal.start();
                    elevationAnim.start();
                }else{
                    holder.enabledInd.setVisibility(View.VISIBLE);
                    holder.disabledInd.setVisibility(View.INVISIBLE);
                }
                tint = ContextCompat.getColor(holder.title.getContext(), R.color.white);
            }else{
                goalElevation = QuickTools.convertDpToPx(getContext(), 2);
                if(animate){
                    holder.enabledInd.clearAnimation();
                    holder.enabledInd.setAnimation(null);

                    ValueAnimator elevationAnim = ValueAnimator.ofInt((int) holder.bg.getElevation(), goalElevation);
                    elevationAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            holder.bg.setCardElevation((int) valueAnimator.getAnimatedValue());
                        }
                    });
                    elevationAnim.setDuration(150);
                    elevationAnim.start();

                    holder.enabledInd.setAlpha(1f);
                    holder.disabledInd.setAlpha(0f);
                    holder.enabledInd.setVisibility(View.VISIBLE);
                    holder.disabledInd.setVisibility(View.VISIBLE);

                    holder.enabledInd.animate().alpha(0f).setDuration(100).start();
                    holder.disabledInd.animate().alpha(1f).setDuration(100).start();
                }else{
                    holder.enabledInd.setVisibility(View.INVISIBLE);
                    holder.disabledInd.setVisibility(View.VISIBLE);
                }
                tint = ContextCompat.getColor(holder.title.getContext(), R.color.black);
            }
            holder.bg.setCardElevation(goalElevation);
            holder.title.setTextColor(tint);
            holder.replyText.setTextColor(tint);
            holder.options.getBackground().setTint(tint);
        }

        private int genRandomColor(){
            Random random = new Random();
            red = random.nextInt(maxColor - minColor) + minColor;
            green = random.nextInt(maxColor - minColor) + minColor;
            blue = random.nextInt(maxColor - minColor) + minColor;
            return new Color().argb(255, red, green, blue);
        }

        public void setOnItemClickedListener(final onItemClickedListener itemClickedListener){
            this.itemClickedListener = itemClickedListener;
        }

        @Override
        public int getItemCount() {
            return replyItems.size();
        }

        public ViewHolder getItemViewFor(ReplyItem replyItem){
            int pos = 0;
            for(int i = 0; i < replyItems.size(); i++){
                if(replyItems.get(i).getId().equalsIgnoreCase(replyItem.getId()))
                    pos = i;
            }

            return viewHolders.get(pos);
        }

        public void updateReplyItem(ReplyItem replyItem){
            for(int i = 0; i < replyItems.size(); i++){
                if(replyItems.get(i).getId().equalsIgnoreCase(replyItem.getId())) {
                    replyItems.set(i, replyItem);
                    notifyItemChanged(i);
                }
            }
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView replyText;
            public Button options;
            public FrameLayout enabledInd;
            public FrameLayout disabledInd;
            public CardView bg;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                replyText = (TextView) itemView.findViewById(R.id.reply_text);
                options = (Button) itemView.findViewById(R.id.item_options);
                enabledInd = (FrameLayout) itemView.findViewById(R.id.enabled_indicator);
                disabledInd = (FrameLayout) itemView.findViewById(R.id.disabled_indicator);
                bg = (CardView) itemView.findViewById(R.id.background);

            }
        }

    }

    public interface onItemClickedListener {
        void onItemClick(View view, int position);
    }

    public interface onLongClickListener {
        void onLongClick(ReplyItem replyItem, int position);
    }
}
