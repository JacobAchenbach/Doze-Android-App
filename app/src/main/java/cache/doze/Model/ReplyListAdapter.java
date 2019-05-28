package cache.doze.Model;

/**
 * Created by Chris on 1/12/2019.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.skydoves.powermenu.AbstractPowerMenu;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.MenuBaseAdapter;
import com.skydoves.powermenu.OnMenuItemClickListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cache.doze.Activities.MainActivity;
import cache.doze.Fragments.AddNewReplyFragment;
import cache.doze.Fragments.RepliesFragment;
import cache.doze.R;
import cache.doze.Tools.ItemMoveCallback;
import cache.doze.Tools.QuickTools;
import cache.doze.Views.FunFab.FunFab;
import me.everything.android.ui.overscroll.IOverScrollDecor;

/**
 * Adapter and ViewHolder Classes
 */

public class ReplyListAdapter extends RecyclerView.Adapter<ReplyListAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    public Context context;
    RecyclerView recyclerView;
    private List<ReplyItem> replyItems;
    private MainActivity mainActivity;
    private RepliesFragment repliesFragment;
    private AddNewReplyFragment addNewFrag;
    private ItemTouchHelper touchHelper;
    private IOverScrollDecor overScrollDecor;
    private FunFab fab;


    private onItemClickedListener itemClickedListener;
    private View.OnLongClickListener longClickListener;

    private int imageSize = -1;
    private int red, green, blue;
    private int maxColor = 220;
    private int minColor = 170;
    private int randomBound = maxColor - (maxColor - minColor) / 2;
    public boolean fingerDown;
    private boolean dragging;

    public ReplyListAdapter(List<ReplyItem> items, MainActivity mainActivity, RecyclerView recyclerView, RepliesFragment repliesFragment) {
        this.replyItems = items;
        this.mainActivity = mainActivity;
        this.recyclerView = recyclerView;
        this.repliesFragment = repliesFragment;

        this.context = recyclerView.getContext();
        canAnimate = true;
    }

    public void setUp(){
        addNewFrag = repliesFragment.addNewFrag;
        fab = repliesFragment.fab;
        touchHelper = repliesFragment.touchHelper;
        overScrollDecor = repliesFragment.overScrollDecor;
    }

    public void fixRecyclerView(){
        repliesFragment.refreshRecyclerView();
        repliesFragment.setUpRecyclerViewOverScroll();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_reply_item, parent, false);
        return new ViewHolder(v);
    }

    int pointerX;
    int pointerY;
    boolean longClicked;
    int lastPosOnScreen = -1;
    public boolean canAnimate;
    @Override public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ReplyItem item = replyItems.get(holder.getAdapterPosition());
        holder.title.setText(item.getTitle());
        holder.replyText.setText(item.getReplyText());
        holder.contactsText.setText(getSomeContacts(item));

        if(item.getGradient() == null) setUpColorScheme(holder, item);
        holder.enabledInd.setBackground(item.getGradient());
        holder.disabledInd.setBackground(item.getBorder());
        setHandle(holder, item.isChecked());

        setEnabled(holder, item.isChecked(), false);

        //if(imageSize == -1)imageSize = holder.proPic.getLayoutParams().width;
        holder.itemView.setTag(item);

        setUpTouchListener(position, holder);
        setUpOptionsHandle(position, holder);
        setUpLoadAnim(position, holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int index = recyclerView.getChildAdapterPosition(holder.itemView);
        setHandle(holder, replyItems.get(index).isChecked());
    }

    private void setHandle(ViewHolder holder, boolean checked){
        holder.optionsHandle.setBackground(ContextCompat.getDrawable(context, dragging? R.drawable.baseline_check_black_24: R.drawable.baseline_more_horiz_black_24));
        holder.optionsHandle.getBackground().setTint(dragging? ContextCompat.getColor(context, R.color.thatsGoodGreen):
                checked? ContextCompat.getColor(context, R.color.white): ContextCompat.getColor(context, R.color.black));
        holder.dragHandle.setVisibility(dragging? View.VISIBLE: View.INVISIBLE);
    }

    private void setUpLoadAnim(int position, ViewHolder holder){
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        lastPosOnScreen = lm.findLastVisibleItemPosition() + 1;
        int firstItemPos = lm.findFirstCompletelyVisibleItemPosition();
        int time = 150 + 50 * (position - firstItemPos + 1);
        //if(time > 800) time = 800;
        //animateIn(holder, position, time);
        if(canAnimate) animateIn(holder, position, time);
        else holder.itemView.setAlpha(1f);
    }
    private int lastPosition = -1;
    private void animateIn(ViewHolder holder, int position, int time)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            View itemview = holder.itemView;
            itemview.post(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
                    animation.setDuration(time);
                    animation.setInterpolator(new DecelerateInterpolator());
                    holder.itemView.startAnimation(animation);
                    itemview.animate().alpha(1f).setDuration(250).setStartDelay(250 + time / 4).start();
                    lastPosition = position;
                }
            });
        }else
            holder.itemView.setAlpha(1f);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    private String getSomeContacts(ReplyItem item){
        ArrayList<Contact> contacts = item.getContacts();
        if(contacts == null || contacts.isEmpty()) return "No Contacts Selected ";

        StringBuilder stringBuilder = new StringBuilder();
        char lastChar = contacts.get(0).getShortenedAddress().charAt(0);

        if(contacts.size() == 1)
            stringBuilder.append(contacts.get(0).getAddress());
        else
            stringBuilder.append(contacts.get(0).getShortenedAddress());

        if(contacts.size() == 2) {
            stringBuilder.append(", and ");
            stringBuilder.append(contacts.get(1).getShortenedAddress());
        }else if(contacts.size() == 3){
            String fin = ", " + contacts.get(1).getShortenedAddress() + ", " +contacts.get(2).getShortenedAddress();
            stringBuilder.append(fin);
            return stringBuilder.toString();
        }

        if(contacts.size() > 3)
            stringBuilder.append(", ");

        boolean goBack = false;
        int refIndex = -1;
        int size = 1;
        for(int i = 1; i < contacts.size(); i++){
            if(i == -1)break;
            String currentName = contacts.get(i).getShortenedAddress();
            if(i == contacts.size() - 1){
                if(contacts.size() == 4){
                    stringBuilder.append(currentName);
                    String fin = " and 1 other...";
                    stringBuilder.append(fin);
                    break;
                }
                if(goBack) {
                    stringBuilder.append(currentName);
                    String fin = contacts.size() > 3? " and " +(contacts.size() - 3) +" others...": "";
                    stringBuilder.append(fin);
                    break;
                }else {
                    i = refIndex - 1;
                    goBack = true;
                    continue;
                }
            }
            if(currentName.charAt(0) == lastChar && !goBack){
                refIndex = i;
            }else{
                if(size == 1) {
                    stringBuilder.append(currentName);
                    stringBuilder.append(", ");
                    lastChar = currentName.charAt(0);
                    size++;
                }else if(size == 2){
                    stringBuilder.append(currentName);
                    String fin = ", and " +(contacts.size() - 3) +" others...";
                    stringBuilder.append(fin);
                    break;
                }
            }

        }

        return stringBuilder.toString();
    }

    private void setUpColorScheme(ViewHolder holder, ReplyItem item){
        String[] gradient = QuickTools.getRandomGradient().split(" ");
        gradient = new String[]{Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimary)),
                Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimaryDark)), ""};
        item.setColorScheme(context, gradient);
        holder.enabledInd.setBackground(item.getGradient());
    }

    private void setUpOptionsHandle(int position, ViewHolder holder){
        Context context = holder.title.getContext();

        CustomPowerMenu powerMenu = new CustomPowerMenu.Builder<>(context, new CustomSpinnerAdapter((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
                .addItem(new SpinnerItem("Reorder", ContextCompat.getDrawable(context, R.drawable.baseline_reorder_black_24)))
                .addItem(new SpinnerItem("Delete", ContextCompat.getDrawable(context, R.drawable.baseline_close_black_24)))
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setShowBackground(false)
                .setWidth((int) context.getResources().getDimension(R.dimen.item_size_xxlarge))
                .build();

        try{
            Field menuWindowF = AbstractPowerMenu.class.getDeclaredField("menuWindow");
            menuWindowF.setAccessible(true);
            PopupWindow menuWindow = (PopupWindow) menuWindowF.get(powerMenu);
            menuWindow.setAnimationStyle(R.style.PopupAnimation);
        }catch (Exception e){
            e.printStackTrace();
        }

        OnMenuItemClickListener<SpinnerItem> onMenuItemClickListener = new OnMenuItemClickListener<SpinnerItem>() {
            @Override
            public void onItemClick(int index, SpinnerItem item) {
                switch (index){
                    case 0:
                        setDragging(true);
                        break;
                    case 1:
                        removeItem(holder, index);
                        break;
                }
                powerMenu.dismiss();
            }
        };

        powerMenu.setOnMenuItemClickListener(onMenuItemClickListener);


        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dragging) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        overScrollDecor.detach();
                        touchHelper.startDrag(holder);
                        animateElevation(holder, QuickTools.convertDpToPx(context, 8)).start();
                    }
                }
                return false;
            }

        });
        holder.optionsWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!dragging) {
                    int handleWidth = powerMenu.getContentViewWidth();
                    powerMenu.showAsDropDown(v, -handleWidth + holder.optionsWrapper.getWidth(), -holder.optionsHandle.getHeight() - holder.optionsHandle.getHeight());
                }else{
                    setDragging(false);
                }
            }
        });
    }

    private void setHandlesDragging(){
        for(int i = 0; i < getItemCount(); i++) {
            ViewHolder holder = (ViewHolder)recyclerView.findViewHolderForAdapterPosition(i);
            if(holder == null) continue;
            setHandle(holder, replyItems.get(i).isChecked());
        }
    }

    private void setDragging(boolean dragging){
        this.dragging = dragging;
        if(dragging)
            fab.hide();
        else
            fab.show();

        setHandlesDragging();
    }

    public void changeViewHolderElevation(ViewHolder holder, int elevation){
        animateElevation(holder, elevation).start();
    }

    public void removeItem(ViewHolder holder, int index){
        int position = holder.getAdapterPosition() != -1? holder.getAdapterPosition(): index;

        holder.itemView.setAlpha(1f);
        holder.itemView.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
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
                MainActivity.replyItems.remove(position);
                addNewFrag.notifyItemRemoved(holder.title.getText().toString());
                notifyItemRemoved(position);
            }
        }).start();

    }

    class SingleTapDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int pos = focusedHolder.getAdapterPosition();
            itemClickedListener.onItemClick(MainActivity.replyItems.get(pos), pos);
            return true;
        }
    }

    class DoubleTapDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onLongClick(focusedHolder.getAdapterPosition(), focusedHolder);
            return true;
        }
    }

    ViewHolder focusedHolder;
    private void setUpTouchListener(int position, ViewHolder holder){
        final GestureDetector tapDetector = new GestureDetector(context, new SingleTapDetector());
        final GestureDetector doubleTapDetector = new GestureDetector(context, new DoubleTapDetector());
        ReplyItem item = MainActivity.replyItems.get(position);
        final Runnable longPressRunnable = new Runnable() {
            @Override
            public void run() {
                if(dragging)return;
                if(fingerDown) {
                    onLongClick(position, holder);
                    longClicked = true;
                }
            }
        };
        final Handler handler = new Handler();
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private int longClickDuration = 200;
            private float lastY;
            private float curY;

            @Override
            public boolean onTouch(View view, MotionEvent ev) {
/*                 focusedHolder = holder;
                tapDetector.onTouchEvent(ev);
                doubleTapDetector.onTouchEvent(ev);
               if(tapDetector.onTouchEvent(ev))
                    itemClickedListener.onItemClick(item, holder.getAdapterPosition());
                if(doubleTapDetector.onTouchEvent(ev)){
                    onLongClick(position, holder);
                    longClicked = true;
                }*/


                switch (ev.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        pointerX = (int) ev.getX();
                        pointerY = (int) ev.getY();

                        lastY = pointerY;
                        fingerDown = true;
                        handler.postDelayed(longPressRunnable, longClickDuration);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        pointerX = (int) ev.getX();
                        pointerY = (int) ev.getY();

                        curY = pointerY;

                        if(Math.abs(curY - lastY) > 10)fingerDown = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPressRunnable);
                        fingerDown = false;

                        float t1 = ev.getEventTime();
                        float t2 = ev.getDownTime();
                        if(!longClicked &&  t1 - t2 < 100) {
                            if (itemClickedListener != null && holder.itemView.getParent() != null) {
                                if(!dragging)
                                    itemClickedListener.onItemClick(item, holder.getAdapterPosition());
                                else
                                    setDragging(false);
                            }
                            return false;
                        }
                        longClicked = false;

                        break;
                }

                return false;
            }
        });
    }

    private void onLongClick(int position, ViewHolder holder){
        if(holder.itemView.getParent() == null)return;
        ReplyItem item = MainActivity.replyItems.get(position);

        boolean checked = item.isChecked();

        item.setChecked(!checked);
        setEnabled(holder, !checked, true);

        Snackbar snackbar;
        if(!checked)snackbar = mainActivity.startSMSService();
        else snackbar = mainActivity.endSMSService();

        if(snackbar != null)
            fab.moveForSnackBar(snackbar);


        /*if(addNewFrag.getState() == AddNewReplyFragment.STATE_EDITING)
            fab.setFabExpandedBackground(!checked? item.getGradientTurned(GradientDrawable.Orientation.BOTTOM_TOP): item.getGradientLighter());*/


        longClicked = true;
    }

    private void setEnabled(final ViewHolder holder, boolean enabled, boolean animate){
        int tint, tintLight;
        int goalElevation;
        if(enabled){
            goalElevation = QuickTools.convertDpToPx(context, 4);
            if(animate){
                holder.enabledInd.setAlpha(1f);
                ValueAnimator elevationAnim = animateElevation(holder, goalElevation);


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
                holder.enabledInd.clearAnimation();
                holder.enabledInd.setAlpha(1f);
                holder.enabledInd.setVisibility(View.VISIBLE);
                holder.disabledInd.setVisibility(View.INVISIBLE);
            }
            tint = ContextCompat.getColor(holder.title.getContext(), R.color.white);
            tintLight = ContextCompat.getColor(holder.title.getContext(), R.color.light_cream);
        }else{
            goalElevation = QuickTools.convertDpToPx(context, 1);
            if(animate){
                holder.enabledInd.clearAnimation();
                holder.enabledInd.setAnimation(null);

                animateElevation(holder, goalElevation).start();

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
            tintLight = ContextCompat.getColor(holder.title.getContext(), R.color.cream);
        }
        holder.bg.setCardElevation(goalElevation);
        holder.title.setTextColor(tint);
        holder.replyText.setTextColor(tint);
        holder.contactsText.setTextColor(tintLight);
        if(!dragging)
            holder.optionsHandle.getBackground().setTint(tint);
        holder.dragHandle.getBackground().setTint(tint);
    }

    private ValueAnimator animateElevation(ViewHolder holder, int to){
        ValueAnimator elevationAnim = ValueAnimator.ofInt((int) holder.bg.getElevation(), to);
        elevationAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                holder.bg.setCardElevation((int) valueAnimator.getAnimatedValue());
            }
        });
        elevationAnim.setDuration(150);
        return elevationAnim;
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

    public void updateReplyItem(ReplyItem replyItem){
        for(int i = 0; i < replyItems.size(); i++){
            if(replyItems.get(i).getId().equalsIgnoreCase(replyItem.getId())) {
                replyItems.set(i, replyItem);
                notifyItemChanged(i);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(replyItems, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(replyItems, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(RecyclerView.ViewHolder viewHolder) {
        //((ViewHolder)viewHolder).bg.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(RecyclerView.ViewHolder viewHolder) {
        //((ViewHolder)viewHolder).rowView.setBackgroundColor(Color.WHITE);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView replyText;
        public TextView contactsText;
        public ImageView optionsHandle;
        public ImageView dragHandle;
        public View optionsWrapper;
        public FrameLayout enabledInd;
        public FrameLayout disabledInd;
        public CardView bg;

        float startY;
        Animator animator;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            replyText = (TextView) itemView.findViewById(R.id.reply_text);
            contactsText = (TextView) itemView.findViewById(R.id.reply_contacts_text);
            optionsHandle = (ImageView) itemView.findViewById(R.id.item_options);
            dragHandle = (ImageView) itemView.findViewById(R.id.item_drag);
            optionsWrapper = itemView.findViewById(R.id.wrapper_item_options);
            enabledInd = (FrameLayout) itemView.findViewById(R.id.enabled_indicator);
            disabledInd = (FrameLayout) itemView.findViewById(R.id.disabled_indicator);
            bg = (CardView) itemView.findViewById(R.id.background);

        }

        public void resetY(){
            if(startY != -1) itemView.setY(startY);
        }
    }

    public interface onItemClickedListener {
        void onItemClick(ReplyItem item, int position);
    }

    public interface onLongClickListener {
        void onLongClick(ReplyItem replyItem, int position);
    }

    public class CustomSpinnerAdapter extends MenuBaseAdapter<SpinnerItem> {
        LayoutInflater inflater;

        public CustomSpinnerAdapter(LayoutInflater inflater){
            this.inflater = inflater;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = inflater.inflate(R.layout.view_spinner_item, viewGroup, false);
            }

            SpinnerItem item = (SpinnerItem) getItem(index);
            final ImageView icon = view.findViewById(R.id.icon);
            icon.setBackground(item.image);
            final TextView title = view.findViewById(R.id.text);
            title.setText(item.text);
            return super.getView(index, view, viewGroup);
        }
    }

    private class SpinnerItem{
        String text;
        Drawable image;
        public SpinnerItem(String text, Drawable image){
            this.text = text;
            this.image = image;
        }
    }
}
