package cache.doze.Fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.ReplyItem;
import cache.doze.Model.ReplyListAdapter;
import cache.doze.R;
import cache.doze.Views.FunFab.FunFab;
import me.everything.android.ui.overscroll.IOverScrollDecor;

/**
 * Created by Chris on 2/22/2018.
 */

public class HomeFragment extends DozeFragment {

    private Context context;
    private MainActivity mainActivity;
    public AddNewReplyFragment addNewFrag;

    View root;
    private RecyclerView replyRecyclerView;
    private ReplyListAdapter recyclerViewAdapter;
    public FunFab fab;

    public ItemTouchHelper touchHelper;
    public IOverScrollDecor overScrollDecor;

    public static HomeFragment newInstance(int page, String title) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        this.context = getContext();

        root = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = (MainActivity) getActivity();
        replyRecyclerView = root.findViewById(R.id.recycler_view);

        setUpRecyclerView();
        if (addNewFrag == null) setUpFab();
        super.onCreateView(root);
        return root;
    }

    private void setUpRecyclerView() {
        getToolbar().setScroller(replyRecyclerView);

        replyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        replyRecyclerView.setAdapter(recyclerViewAdapter = new ReplyListAdapter(MainActivity.replyItems, mainActivity, replyRecyclerView, this));

        touchHelper = new ItemTouchHelper(recyclerViewAdapter.getTouchCallback());


        replyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);;

                if (Math.abs(dy) > 10)
                    recyclerViewAdapter.fingerDown = false;
            }
        });


        touchHelper.attachToRecyclerView(replyRecyclerView);
        replyRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public void setFab(FunFab fab) {
        this.fab = fab;
    }

    boolean fabExpanded = false;
    boolean showContactsPage = false;

    public void setUpFab() {
        if(fab == null)fab = mainActivity.getFab();
        if (getActivity() != null && getContext() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point screen = new Point();
            display.getSize(screen);
            float w = screen.x;
            float h = screen.y;

            addNewFrag = (AddNewReplyFragment) fab.init(getActivity().getSupportFragmentManager(), 0.80f, 1);
            changeFragProperties(true);
            fab.setFabExpandListener(new FunFab.FabExpandListener() {
                @Override
                public void onFabExpanded(boolean shown) {
                    fabExpanded = shown;
                    if (!shown) fabClosed();
                    fab.setSuspendable(addNewFrag.getState() != AddNewReplyFragment.STATE_ADD_NEW);
                }
            });

            fab.setFabSubmitListener(new FunFab.FabSubmitListener() {
                @Override
                public void onSubmit() {
                    if (addNewFrag.getState() == AddNewReplyFragment.STATE_ADD_NEW) {
                        MainActivity.replyItems.add(addNewFrag.getReplyItem());
                        recyclerViewAdapter.notifyDataSetChanged();
                    } else {
                        editReplyItem(addNewFrag.getReplyItem());
                        changeFragProperties(true);
                    }
                    mainActivity.saveReplyItems();
                    //mainActivity.setToolbarColor(ContextCompat.getColor(context, R.color.white));
                    //getToolbar().setTitle("Doze");
                }
            });

            fab.setFabCancelListener(new FunFab.FabCancelListener() {
                @Override
                public void onCancel() {
                    changeFragProperties(true);
                    //mainActivity.setToolbarColor(ContextCompat.getColor(context, R.color.white));
                    //getToolbar().setTitle("Doze");
                }
            });

            addNewFrag.setContactsButtonPressedListener(new AddNewReplyFragment.OnContactsButtonPressedListener() {
                @Override
                public void onPressed() {
                    if(!fab.isOpen()) return;
                    fab.setIcon(ContextCompat.getDrawable(context, R.drawable.baseline_more_vert_black_36));
                    fab.expandFab(false, true);
                    showContactsPage = true;
                    fab.hide();
                }
            });

            recyclerViewAdapter.setOnItemClickedListener(new ReplyListAdapter.onItemClickedListener() {
                @Override
                public void onItemClick(ReplyItem item, int position) {
                    if(fab.isAnimating()) return;
/*                    if (addNewFrag.getState() == AddNewReplyFragment.STATE_ADD_NEW)
                        mainActivity.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary), item.getColors()[0]);*/
                    //getToolbar().setTitle(item.getTitle());
                    addNewFrag.setReplyItem(item);
                    changeFragProperties(false);
                    //fab.setFabExpandedBackground(item.isChecked() ? item.getGradientTurned(GradientDrawable.Orientation.BOTTOM_TOP) : item.getGradientLighter());
                    fab.expandFab(true, false);
                    //fab.setFabClosedBackground(item.getColors()[item.getColors().length - 1]);
                }
            });

            recyclerViewAdapter.init();


        }
    }

    private void changeFragProperties(boolean addNew) {
        if (addNew) {
            addNewFrag.clear();
            fab.setSubmitText("Add");
            fab.setSubmitImage(R.drawable.baseline_add_black_18);
            fab.setCancelText("Cancel");
//            fab.setFabExpandedBackground(new ColorDrawable(ContextCompat.getColor(fab.getContext(), R.color.colorPrimary)));
//            fab.setFabClosedBackground(ContextCompat.getColor(fab.getContext(), R.color.colorAccent));
            fab.setFabClosedBackground(ContextCompat.getColor(fab.getContext(), R.color.colorAccent));
        } else {
            addNewFrag.setState(AddNewReplyFragment.STATE_EDITING);
            fab.setSubmitText("Save");
            fab.setSubmitImage(R.drawable.baseline_check_black_18);
            fab.setCancelText("Discard");
        }
    }

    private void fabClosed() {
        InputMethodManager imm = ((InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null) imm.hideSoftInputFromWindow(addNewFrag.inputMessage.getWindowToken(), 0);
        if (showContactsPage) {
            mainActivity.showContactsFrag(addNewFrag.getReplyItem());
            showContactsPage = false;
        }
    }

    public void setFabExpanded(boolean expand) {
        if (fab != null)
            fab.expandFab(expand, false);
    }

    private void editReplyItem(ReplyItem replyItem) {
        //recyclerViewAdapter.updateReplyItem(replyItem);
        int position = 0;
        for (int i = 0; i < MainActivity.replyItems.size(); i++) {
            if (MainActivity.replyItems.get(i).getId().equalsIgnoreCase(replyItem.getId())) {
                position = i;
                break;
            }
        }
        recyclerViewAdapter.notifyItemChanged(position);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fab != null) {
            fab.show();
            if (isShown && getContext() != null)
                fab.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.baseline_create_black_36));
        }
    }


    @Override
    public boolean onBackPressed() {
        if (fabExpanded) {
            fab.expandFab(false, true);
            return true;
        }
        return false;
    }


}
