package cache.doze.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.l4digital.fastscroll.FastScrollRecyclerView;
import com.l4digital.fastscroll.FastScroller;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cache.doze.Activities.MainActivity;
import cache.doze.Model.Contact;
import cache.doze.ContactSelectionAdapter;
import cache.doze.Model.ReplyItem;
import cache.doze.R;
import cache.doze.Views.FunFab.FunFab;

/**
 * Created by Chris on 2/22/2018.
 */

public class ContactsFragment extends DozeFragment implements SearchView.OnQueryTextListener{

    private MainActivity mainActivity;
    private ReplyItem replyItem;

    private FunFab fab;
    private FastScrollRecyclerView recyclerView;
    private StickyRecyclerHeadersDecoration recyclerDecorator;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView notifText;
    private ContactSelectionAdapter adapter;

    boolean loadingContacts;
    private boolean canSwipeRefresh = true;

    View rootView;

    public static ContactsFragment newInstance(int page, String title) {
        ContactsFragment contactsFragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        contactsFragment.setArguments(args);
        return contactsFragment;
    }

    public void setReplyItem(ReplyItem replyItem) {
        this.replyItem = replyItem;
        updateCheckedStates();
    }

    private void updateCheckedStates(){
        if(replyItem == null)return;

        ArrayList<Contact> newContacts = new ArrayList<>(replyItem.getContacts());
        if(!newContacts.isEmpty()){
            ArrayList<Contact> contactList = mainActivity.getContactList();
            for(int i = 0; i < contactList.size(); i++){
                Contact listedContact = contactList.get(i);
                listedContact.setSelected(false);
                for(int j = 0; j < newContacts.size(); j++){
                    Contact replyContact = newContacts.get(j);
                    if(listedContact.getId().equalsIgnoreCase(replyContact.getId()))
                        listedContact.setSelected(true);
                }
            }
        }
        else{
            for(Contact contact: mainActivity.getContactList())
                contact.setSelected(false);
        }
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        rootView.setVisibility(View.GONE);

        mainActivity = (MainActivity) getActivity();

        progressBar = rootView.findViewById(R.id.progress_bar);
        notifText = rootView.findViewById(R.id.notif_text);

        initSwipeRefresh();
        initRecyclerView();
        //populateContactAddresses();

        super.onCreateView(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!loadingContacts && mainActivity.getContactList().isEmpty()) {
            populateContactAddresses(true);
            //refreshCheckedStates();
        }else if(loadingContacts && isShown) {
            animateViewsUp();
        }else if(!mainActivity.getContactList().isEmpty()){
            progressBar.setVisibility(View.INVISIBLE);
            notifText.setVisibility(View.INVISIBLE);
        }
        if(isShown)
            fab.hide();
        //if(recyclerView != null && adapter != null && adapter.getItemCount() > ContactsFragment.recyclerViewScrollPos) recyclerView.scrollToPosition(ContactsFragment.recyclerViewScrollPos);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(!mainActivity.getContactList().isEmpty())saveCheckedContacts();
        fab.show();

    }

    public void setFab(FunFab fab) {
        this.fab = fab;
    }

    private void initSwipeRefresh(){
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(swipeRefresh.getContext(), R.color.colorAccent));
        swipeRefresh.setDistanceToTriggerSync((int) swipeRefresh.getResources().getDimension(R.dimen.item_size_small));

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mainActivity.fluidSearchView.isOpened())mainActivity.fluidSearchView.onClose();
                loading(true);
                populateContactAddresses(false);
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return !loadingContacts;
            }
        });

        recyclerView.setFastScrollListener(new FastScroller.FastScrollListener() {
            @Override
            public void onFastScrollStart(FastScroller fastScroller) {
                swipeRefresh.setEnabled(false);
            }

            @Override
            public void onFastScrollStop(FastScroller fastScroller) {
                swipeRefresh.setEnabled(true);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int offset = recyclerView.computeVerticalScrollOffset();
                //swipeRefresh.setEnabled(offset <= 5);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

/*        recyclerView.setIndexbarWidth(70f);
        recyclerView.setIndexBarCornerRadius(0);
        recyclerView.setIndexbarMargin(0);
        recyclerView.setIndexBarHighLateTextVisibility(true);
        recyclerView.setIndexbarHighLateTextColor("#" + Integer.toHexString(ContextCompat.getColor(recyclerView.getContext(), R.color.main_blue)));
        recyclerView.setIndexBarTransparentValue(0.5f);*/
    }

    private void loading(boolean isLoading){
        loadingContacts = isLoading;
        recyclerView.animate().alpha(isLoading? 0.3f: 1.0f);
        recyclerView.setFastScrollEnabled(!isLoading);
        //animateVisibility(swipeRefresh, !isLoading);
        if(!isLoading) {
            animateVisibility(progressBar, false);
            //animateVisibility(notifText, isLoading);
        }

//        swipeRefresh.setVisibility(isLoading? View.INVISIBLE: View.VISIBLE);
//        progressBar.setVisibility(isLoading? View.VISIBLE: View.INVISIBLE);
//        notifText.setVisibility(isLoading? View.VISIBLE: View.INVISIBLE);
        //notifText.setText("Getting your contacts! \uD83D\uDE34");
        if(loadingContacts){
            animateViewsUp();
        }
    }

    private void animateViewsUp(){
        notifText.post(new Runnable() {
            @Override
            public void run() {
                float notifY = notifText.getY();
                float progY = progressBar.getY();
                notifText.setY(notifY + 100);
                progressBar.setY(progY + 100);
                notifText.setAlpha(0f);
                progressBar.setAlpha(0f);
                DecelerateInterpolator interpolator = new DecelerateInterpolator();
                notifText.animate().y(notifY).setInterpolator(interpolator).setDuration(1500).start();
                progressBar.animate().y(progY).setInterpolator(interpolator).setDuration(1500).start();
                notifText.animate().alpha(1f).setDuration(500).start();
                progressBar.animate().alpha(1f).setDuration(500).start();
            }
        });
    }

    private void animateVisibility(View view, boolean visible){
        if(view == null)return;
        view.animate().setDuration(150).alpha(visible? 1f: 0f).setInterpolator(new AccelerateInterpolator());
    }

    public void populateContactAddresses(boolean loading){
        if(loading)loading(true);
        new PullContacts().execute();
    }

    private void setUpAdapter(){
        adapter = new ContactSelectionAdapter(mainActivity.getContactList(), getActivity());
        recyclerView.setAdapter(adapter);
        if(recyclerDecorator != null) recyclerDecorator.invalidateHeaders();
        if(recyclerDecorator == null) recyclerView.addItemDecoration(recyclerDecorator = new StickyRecyclerHeadersDecoration(adapter));
        //mainActivity.setContactList(contactList);

/*        if(mainActivity.getContactList().size() <= 20)
            recyclerView.setIndexBarVisibility(false);*/

        adapter.setOnItemClickedListener(new ContactSelectionAdapter.onItemClickedListener(){
            @Override
            public void onItemClick(View view, int position) {
                Contact obj = adapter.getDisplayed(position);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(obj.getId()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        adapter.setOnContactCheckedListener(new ContactSelectionAdapter.onContactCheckedListener() {
            @Override
            public void onContactChecked(View view, int position) {
                Contact contact = adapter.getDisplayed(position);
                contact.setSelected(!contact.getSelected());
            }
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                loading(false);
            }
        });
    }



    private void saveCheckedContacts(){
        if(replyItem == null)return;
        ArrayList<Contact> selectedContacts = new ArrayList<>();
        for(Contact contact: mainActivity.getContactList()){
            if(contact.getSelected())selectedContacts.add(contact);
        }
        replyItem.setContacts(selectedContacts);
    }

    public void updateCheckStates() {
        int startPos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int endPos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        if(startPos > 0)startPos--;
        if(endPos < adapter.getItemCount() - 2)endPos+=3;
        else if(endPos < adapter.getItemCount())endPos++;

        ArrayList<Contact> contacts = mainActivity.getContactList();
        for(int i = startPos; i < endPos; i++){
            adapter.notifyItemChanged(i);
            adapter.updateEmojiForPosition(i);
            View v = recyclerView.getLayoutManager().getChildAt(i);
            if(v != null){
                CheckBox checkbox = v.findViewById(R.id.checkbox);
                checkbox.setChecked(contacts.get(i).getSelected());
            }
        }
    }

    public void scrollRecyclerView(int position){
        recyclerView.scrollToPosition(position);
    }

    private class PullContacts extends AsyncTask<Object, Object, ArrayList<Contact>>{

        @Override
        protected ArrayList<Contact> doInBackground(Object... params) {
            ArrayList<Contact> contactList = new ArrayList<>();
            getContactDetails(contactList);
            return contactList;
        }

        private void getContactDetails(ArrayList<Contact> contactList){
            Cursor phones = mainActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if(phones == null){
                Log.e("Phones error", "Could not start phones cursor");
                return;
            }

            boolean selected = false;
            while (phones.moveToNext()) { //Begin query of all numbers
                String name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String id = phones.getString(phones.getColumnIndex(ContactsContract.Contacts._ID));


                Contact c = new Contact(name, selected, id);

                if(contactListHas(contactList, c)) continue;

                Cursor nums = mainActivity.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{id}, null);
                if(nums != null){
                    while (nums.moveToNext()) {
                        if (nums.getInt(nums.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                            c.addNumber(nums.getString(nums.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                    nums.close();
                }

                if (name.equals("")) //Contact has no assigned name, use number as display name instead
                    c.setAddress(c.getNumbers().isEmpty()? "*No Name Assigned*": c.getNumbers().get(0));

                contactList.add(c);

                Bitmap image = null;
                String image_uri = phones.getString(phones.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (image_uri != null) {

                    try {
                        image = MediaStore.Images.Media
                                .getBitmap(mainActivity.getContentResolver(),
                                        Uri.parse(image_uri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(image != null)c.setPhoto(image);
            }
            phones.close();

        }
        //Check if the address is already in the list
        private boolean contactListHas(ArrayList<Contact> contactList, Contact contact){
            for (Contact c: contactList){
                if(c.getId().equals(contact.getId()))
                    return true;

            }
            return false;
        }


        @Override
        protected void onPostExecute(ArrayList<Contact> contactList){
            super.onPostExecute(contactList);

            if(contactList.isEmpty()){
                progressBar.setVisibility(View.GONE);
                notifText.setText("No Contacts to show :/");
                notifText.setVisibility(View.VISIBLE);
                if(swipeRefresh != null)swipeRefresh.setRefreshing(false);
                return;
            }
            Collections.sort(contactList, new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact contact2) {
                    return contact.getAddress().compareToIgnoreCase(contact2.getAddress());
                }
            });
            mainActivity.setContactList(contactList);

            setUpAdapter();
            updateCheckedStates();

            if(swipeRefresh != null)swipeRefresh.setRefreshing(false);
            loading(false);
            recyclerView.animate().alpha(1);
        }
    }


    @Override
    public boolean onBackPressed(){
        mainActivity.showMainRepliesFrag();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.expandFab(true, true);
            }
        }, ANIMATION_TIME);
        return true;
    }

    /**
     * Handle Searching
     */
    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        adapter.filter(text);
        return false;
    }
}
