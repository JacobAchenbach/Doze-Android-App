package cache.doze.Fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cache.doze.MainActivity;
import cache.doze.Contact;
import cache.doze.ContactSelectionAdapter;
import cache.doze.R;
import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;

/**
 * Created by Chris on 2/22/2018.
 */

public class Page2 extends Fragment implements SearchView.OnQueryTextListener{

    private static int recyclerViewScrollPos;
    MainActivity mainActivity;
    IndexFastScrollRecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;
    private TextView noContactsText;
    ContactSelectionAdapter adapter;

    View rootView;

    public static Page2 newInstance(int page, String title) {
        Page2 page2 = new Page2();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        page2.setArguments(args);
        return page2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.page_two, container, false);

        mainActivity = (MainActivity) getActivity();

        progressBar = rootView.findViewById(R.id.progress_bar);
        noContactsText = rootView.findViewById(R.id.no_contacts_text);

        initSwipeRefresh();
        initRecyclerView();
        populateContactAddresses();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                populateContactAddresses();
                refreshCheckedStates();
                if(recyclerView != null && adapter != null && adapter.getItemCount() > Page2.recyclerViewScrollPos) recyclerView.scrollToPosition(Page2.recyclerViewScrollPos);
            }
        }, 500);
        }

    @Override
    public void onPause(){
        super.onPause();
        if(!mainActivity.getContactList().isEmpty())saveCheckedContacts();
        if(recyclerView != null)
            Page2.recyclerViewScrollPos = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    private void initSwipeRefresh(){
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(swipeRefresh.getContext(), R.color.colorAccent));
        swipeRefresh.setDistanceToTriggerSync((int) swipeRefresh.getResources().getDimension(R.dimen.item_size_small));

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mainActivity.fluidSearchView.isOpened())mainActivity.fluidSearchView.onClose();
                populateContactAddresses();
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setIndexbarWidth(70f);
        recyclerView.setIndexBarCornerRadius(0);
        recyclerView.setIndexbarMargin(0);
        recyclerView.setIndexBarHighLateTextVisibility(true);
        recyclerView.setIndexbarHighLateTextColor("#" + Integer.toHexString(ContextCompat.getColor(recyclerView.getContext(), R.color.main_blue)));
        recyclerView.setIndexBarTransparentValue(0.5f);
    }

    public void populateContactAddresses(){
        recyclerView.setVisibility(View.INVISIBLE);
        noContactsText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        final ArrayList<Contact> contactList = new ArrayList<>();
        getContactDetails(contactList);

        if(contactList.isEmpty()){
            progressBar.setVisibility(View.GONE);
            noContactsText.setVisibility(View.VISIBLE);
            if(swipeRefresh != null)swipeRefresh.setRefreshing(false);
            return;
        }
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return contact.getAddress().compareToIgnoreCase(contact2.getAddress());
            }
        });

        adapter = new ContactSelectionAdapter(contactList, getActivity());
        recyclerView.setAdapter(adapter);
        mainActivity.setContactList(contactList);

        if(mainActivity.getContactList().size() <= 20)
            recyclerView.setIndexBarVisibility(false);

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


        if(swipeRefresh != null)swipeRefresh.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private ArrayList<Contact> getContactDetails(ArrayList<Contact> contactList){
        Cursor phones = mainActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(phones == null){
            Log.e("Phones error", "Could not start phones cursor");
            return contactList;
        }

        boolean selected = mainActivity.getReplyAll();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String id = phones.getString(phones.getColumnIndex(ContactsContract.Contacts._ID));
            String num = "";
            Contact c = new Contact(name, selected, id);
            c.setNumber(num);

            if (contactListHas(contactList, c)) continue;
            if (name.equals("")) //Contact has no assigned name, use number as display name instead
                contactList.add(new Contact(num, selected, id));
            else //Add to list
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

        return contactList;
    }

    //Check if the address is already in the list
    private boolean contactListHas(ArrayList<Contact> contactList, Contact contact){
        for (Contact c: contactList){
            if(c.getId().equals(contact.getId()))
                return true;

        }
        return false;
    }

    private void saveCheckedContacts(){
        ArrayList<Contact> contacts = mainActivity.getContactList();
        JSONArray contactsJson = new JSONArray();
        try{
            for (Contact contact : contacts) {
                JSONObject checkedJson = new JSONObject();
                checkedJson.put(contact.getId(), contact.getSelected());
                contactsJson.put(checkedJson);
            }
        }catch(JSONException e) {
            e.printStackTrace();
        }

        mainActivity.getPrefs().edit().putString(MainActivity.PREFS_CHECKED_CONTACTS, contactsJson.toString()).apply();
    }

    private void refreshCheckedStates(){
        ArrayList<Contact> contacts = mainActivity.getContactList();
        if(contacts.isEmpty())return;

        String jsonString = mainActivity.getPrefs().getString(MainActivity.PREFS_CHECKED_CONTACTS, "");
        try{
            JSONArray checkedArray = new JSONArray(jsonString);
            boolean attemptedToReverse = false;
            int offset = 0;
            for(int i = 0; i < checkedArray.length(); i++){
                if(contacts.size() + offset - 1 < i)return;
                Contact itemInList = contacts.get(i + offset);
                JSONObject itemJSON = checkedArray.getJSONObject(i);
                if(itemJSON.has(itemInList.getId())) {
                    boolean checked = (boolean) itemJSON.get(itemInList.getId());
                    itemInList.setSelected(checked);
                }else{
                    int diff = findNameInList(contacts, itemInList.getId());
                    if(diff == -1)
                        offset = -1;
                    else
                        offset = i - diff;
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private int findNameInList(ArrayList<Contact> contacts, String id){
        for(int i = 0; i < contacts.size(); i++){
            if(contacts.get(i).getId().equals(id))return ++i;
        }

        return -1;
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
