package cache.doze.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.MenuBaseAdapter;
import com.skydoves.powermenu.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.Contact;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.Tools.QuickTools;
import cache.doze.Views.FunFab.FunFab;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by Chris on 2/22/2018.
 */

public class MainRepliesFragment extends DozeFragment {

    MainActivity mainActivity;
    AddNewReplyFragment addNewFrag;

    View root;
    TextView title;
    RecyclerView replyRecycler;
    ReplyListAdapter recyclerViewAdapter;
    FunFab fab;

    private boolean fingerDown;

    public static MainRepliesFragment newInstance(int page, String title) {
        MainRepliesFragment mainRepliesFragment = new MainRepliesFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        mainRepliesFragment.setArguments(args);
        return mainRepliesFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        root = inflater.inflate(R.layout.fragment_main_replies, container, false);

        mainActivity = (MainActivity) getActivity();
        title = root.findViewById(R.id.title);
        replyRecycler = root.findViewById(R.id.recycler_view);

        setUpRecyclerView();
        if(addNewFrag == null) setUpFab();
        super.onCreateView(root);
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
                if(!recyclerView.canScrollVertically(-1))totalY = 0;
                totalY+= dy;

                if(totalY <= paddingTop) {
                    float perc = totalY / paddingTop;
                    title.setAlpha(1 - perc);
                }else title.setAlpha(0);

                if(Math.abs(dy) > 10)
                    fingerDown = false;
            }
        });

    }

    public void setFab(FunFab fab){
        this.fab = fab;
    }
    boolean fabExpanded = false;
    boolean showContactsPage = false;
    public void setUpFab(){
        if(getActivity() != null && getContext() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point screen = new Point();
            display. getSize(screen);
            float w = screen.x;
            float h = screen.y;

            addNewFrag = (AddNewReplyFragment) fab.init(getActivity().getSupportFragmentManager(), (int) h, 0.85f, (int) w, 1);
            changeFragProperties(true);
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
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                    else {
                        editReplyItem(addNewFrag.getReplyItem());
                        changeFragProperties(true);
                    }
                    mainActivity.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    getToolbar().setTitle("Doze");
                }
            });

            fab.setFabCancelListener(new FunFab.FabCancelListener() {
                @Override
                public void onCancel() {
                    changeFragProperties(true);
                    mainActivity.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    getToolbar().setTitle("Doze");
                }
            });

            addNewFrag.setContactsButtonPressedListener(new AddNewReplyFragment.OnContactsButtonPressedListener() {
                @Override
                public void onPressed() {
                    fab.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.baseline_more_vert_black_36));
                    fab.expandFab(false, true);
                    showContactsPage = true;
                }
            });

            recyclerViewAdapter.setOnItemClickedListener(new onItemClickedListener() {
                @Override
                public void onItemClick(ReplyItem item, int position) {
                    if(addNewFrag.getState() == AddNewReplyFragment.STATE_ADD_NEW) mainActivity.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary), item.getColors()[item.getColors().length - 1]);
                    getToolbar().setTitle(item.getTitle());
                    addNewFrag.setReplyItem(item);
                    changeFragProperties(false);
                    fab.setFabExpandedBackground(item.isChecked()? item.getGradientTurned(GradientDrawable.Orientation.BOTTOM_TOP): item.getGradientLighter());
                    fab.expandFab(true, true);
                    fab.setFabClosedBackground(item.getColors()[item.getColors().length - 1]);
                }
            });

        }
    }

    private void changeFragProperties(boolean addNew){
        if(addNew){
            addNewFrag.clear();
            fab.setSubmitText("Add");
            fab.setSubmitImage(R.drawable.baseline_add_black_18);
            fab.setCancelText("Cancel");
            fab.setFabExpandedBackground(new ColorDrawable(ContextCompat.getColor(fab.getContext(), R.color.colorPrimary)));
            fab.setFabClosedBackground(ContextCompat.getColor(fab.getContext(), R.color.colorAccent));
        }else{
            addNewFrag.setState(AddNewReplyFragment.STATE_EDITING);
            fab.setSubmitText("Save");
            fab.setSubmitImage(R.drawable.baseline_check_black_18);
            fab.setCancelText("Discard");
        }
    }

    private void fabClosed(){
        InputMethodManager imm = ((InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
        if(imm != null) imm.hideSoftInputFromWindow(addNewFrag.inputPreset.getWindowToken(), 0);
        if(showContactsPage){
            mainActivity.showContactsFrag(addNewFrag.getReplyItem());
            showContactsPage = false;
        }
    }

    public void setFabExpanded(boolean expand){
        if(fab != null)
            fab.expandFab(expand, false);
    }

    private void editReplyItem(ReplyItem replyItem){
        //recyclerViewAdapter.updateReplyItem(replyItem);
        int position = 0;
        for(int i = 0; i < MainActivity.replyItems.size(); i++) {
            if (MainActivity.replyItems.get(i).getId().equalsIgnoreCase(replyItem.getId())) {
                position = i;
                break;
            }
        }
        recyclerViewAdapter.notifyItemChanged(position);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view, bundle);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isShown && fab != null && getContext() != null)fab.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.baseline_create_black_36));
    }


    @Override
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

        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_replyitem, parent, false);
            return new ViewHolder(v);
        }

        int pointerX;
        int pointerY;
        boolean longClicked;
        @Override public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final ReplyItem item = replyItems.get(holder.getAdapterPosition());
            holder.title.setText(item.getTitle());
            holder.replyText.setText(item.getReplyText());
            holder.contactsText.setText(getSomeContacts(item));

            if(item.getGradient() == null) setUpColorScheme(holder, item);
            holder.enabledInd.setBackground(item.getGradient());
            holder.disabledInd.setBackground(item.getBorder());

            setEnabled(holder, item.isChecked(), false);

            //if(imageSize == -1)imageSize = holder.proPic.getLayoutParams().width;
            holder.itemView.setTag(item);

            setUpTouchListener(item, holder);
            setUpOptionsHandle(item, holder);
        }

        private String getSomeContacts(ReplyItem item){
            ArrayList<Contact> contacts = item.getContacts();
            if(contacts == null || contacts.isEmpty())return "No Contacts Selected ";

            StringBuilder stringBuilder = new StringBuilder();
            char lastChar = contacts.get(0).getShortenedAddress().charAt(0);

            if(contacts.size() == 1)
                stringBuilder.append(contacts.get(0).getAddress());
            else
                stringBuilder.append(contacts.get(0).getShortenedAddress());

            if(contacts.size() == 2) {
                stringBuilder.append(", and ");
                stringBuilder.append(contacts.get(1).getShortenedAddress());
            }
            else if(contacts.size() > 2)
                stringBuilder.append(", ");

            boolean goBack = false;
            int refIndex = -1;
            int size = 1;
            for(int i = 1; i < contacts.size(); i++){
                if(i == -1)break;
                String currentName = contacts.get(i).getShortenedAddress();
                if(i == contacts.size() - 1){
                    if(goBack) {
                        stringBuilder.append(currentName);
                        String fin = contacts.size() > 3? ", and " +(contacts.size() - 3) +" others...": ".";
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
            item.setColorScheme(getContext(), gradient);
            holder.enabledInd.setBackground(item.getGradient());
        }

        private void setUpOptionsHandle(ReplyItem item, ViewHolder holder){
            Context context = holder.title.getContext();

            CustomPowerMenu powerMenu = new CustomPowerMenu.Builder<>(context, new CustomSpinnerAdapter((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
                    .addItem(new SpinnerItem("Reorder", ContextCompat.getDrawable(context, R.drawable.baseline_reorder_black_24)))
                    .addItem(new SpinnerItem("Delete", ContextCompat.getDrawable(context, R.drawable.baseline_close_black_24)))
                    .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
                    .setMenuRadius(10f)
                    .setMenuShadow(10f)
                    .setBackgroundAlpha(0f)
                    .setWidth((int) getResources().getDimension(R.dimen.item_size_xxlarge))
                    .build();

            OnMenuItemClickListener<SpinnerItem> onMenuItemClickListener = new OnMenuItemClickListener<SpinnerItem>() {
                @Override
                public void onItemClick(int index, SpinnerItem item) {
                    switch (index){
                        case 0:

                            break;
                        case 1:
                            removeItem(holder, holder.getAdapterPosition());
                            break;
                    }
                    powerMenu.dismiss();
                }
            };

            powerMenu.setOnMenuItemClickListener(onMenuItemClickListener);


            holder.optionsWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int handleWidth = powerMenu.getContentViewWidth();
                    powerMenu.showAsDropDown(v, -handleWidth + holder.optionsWrapper.getWidth(), -holder.optionsHandle.getHeight() - holder.optionsHandle.getHeight());
                }
            });
        }

        public void removeItem(ViewHolder holder, int position){
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

        private void setUpTouchListener(ReplyItem item, ViewHolder holder){
            final Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    if(fingerDown) {
                        onLongClick(item, holder);
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
                                if (itemClickedListener != null && holder.itemView.getParent() != null)
                                    itemClickedListener.onItemClick(item, holder.getAdapterPosition());
                                return false;
                            }
                            longClicked = false;

                            break;
                    }

                    return false;
                }
            });
        }

        private void onLongClick(ReplyItem item, ViewHolder holder){
            if(holder.itemView.getParent() == null)return;

            boolean checked = item.isChecked();

            Snackbar snackbar;
            if(!checked)snackbar = mainActivity.startSMSService();
            else snackbar = mainActivity.endSMSService();
            fab.moveForSnackBar(snackbar);

            item.setChecked(!checked);
            setEnabled(holder, !checked, true);
            if(addNewFrag.getState() == AddNewReplyFragment.STATE_EDITING)
                fab.setFabExpandedBackground(item.isChecked()? item.getGradientTurned(GradientDrawable.Orientation.BOTTOM_TOP): item.getGradientLighter());


            longClicked = true;
        }

        private void setEnabled(final ViewHolder holder, boolean enabled, boolean animate){
            int tint, tintLight;
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
                    holder.enabledInd.clearAnimation();
                    holder.enabledInd.setAlpha(1f);
                    holder.enabledInd.setVisibility(View.VISIBLE);
                    holder.disabledInd.setVisibility(View.INVISIBLE);
                }
                tint = ContextCompat.getColor(holder.title.getContext(), R.color.white);
                tintLight = ContextCompat.getColor(holder.title.getContext(), R.color.light_cream);
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
                tintLight = ContextCompat.getColor(holder.title.getContext(), R.color.cream);
            }
            holder.bg.setCardElevation(goalElevation);
            holder.title.setTextColor(tint);
            holder.replyText.setTextColor(tint);
            holder.contactsText.setTextColor(tintLight);
            holder.optionsHandle.getBackground().setTint(tint);
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView replyText;
            public TextView contactsText;
            public ImageView optionsHandle;
            public View optionsWrapper;
            public FrameLayout enabledInd;
            public FrameLayout disabledInd;
            public CardView bg;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                replyText = (TextView) itemView.findViewById(R.id.reply_text);
                contactsText = (TextView) itemView.findViewById(R.id.reply_contacts_text);
                optionsHandle = (ImageView) itemView.findViewById(R.id.item_options);
                optionsWrapper = itemView.findViewById(R.id.wrapper_item_options);
                enabledInd = (FrameLayout) itemView.findViewById(R.id.enabled_indicator);
                disabledInd = (FrameLayout) itemView.findViewById(R.id.disabled_indicator);
                bg = (CardView) itemView.findViewById(R.id.background);

            }
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
