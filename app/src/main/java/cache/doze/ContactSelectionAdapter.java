package cache.doze;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cache.doze.Model.Contact;
import cache.doze.Tools.QuickTools;

/**
 * Created by Chris on 2/15/2018.
 */

public class ContactSelectionAdapter extends RecyclerView.Adapter<ContactSelectionAdapter.ViewHolder> implements SectionIndexer {

    private List<Contact> allContacts;
    private List<Contact> contacts;
    private Activity activity;
    private ArrayList<Integer> mSectionPositions;
    private ArrayList<ViewHolder> viewHolders = new ArrayList<>();

    private onContactCheckedListener contactCheckedListener;
    private onItemClickedListener itemClickedListener;

    private int imageSize = -1;
    private int red, green, blue;
    private int maxColor = 220;
    private int minColor = 170;
    private int randomBound = maxColor - (maxColor - minColor) / 2;

    public interface onContactCheckedListener {
        void onContactChecked(View view, int position);
    }

    public interface onItemClickedListener {
        void onItemClick(View view, int position);
    }

    public ContactSelectionAdapter(List<Contact> items, Activity activity) {
        this.contacts = items;
        allContacts = new ArrayList<>();
        generateAllContactsList();
        this.activity = activity;
        for(Contact c: items){
            ViewHolder v = null;
            viewHolders.add(v);
        }
    }

    private void generateAllContactsList(){
        for(Contact victim: contacts){
            victim.setDisplayedPosition(allContacts.size());
            allContacts.add(new Contact(victim));
        }
    }

    public Contact getDisplayed(int position){
        return contacts.get(position);
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_listed_contact, parent, false);
        return new ViewHolder(v);
    }

    private int lastColor = genRandomColor();
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Contact item = contacts.get(position);
        holder.address.setText(item.getAddress());
        holder.checkBox.setChecked(item.getSelected());
        if(imageSize == -1)imageSize = holder.proPic.getLayoutParams().width;
        if(item.getPhoto() != null){
            holder.proPic.setBackground(null);
            holder.proPic.setImageBitmap(item.getPhoto());
            holder.proPic.getLayoutParams().width = imageSize;
            holder.proPic.getLayoutParams().height = imageSize;
        }else{
            holder.proPic.setImageBitmap(null);
            setEmojiFromSelected(holder, item);
            holder.proPic.getLayoutParams().width = imageSize - QuickTools.convertDpToPx(activity, 10);
            holder.proPic.getLayoutParams().height = imageSize - QuickTools.convertDpToPx(activity, 10);
            CardView parent = (CardView)holder.proPic.getParent();
            if(item.getBackgroundColor() == null)
                parent.setCardBackgroundColor(item.setBackgroundColor(increaseRandomPartOfColor()));
            else
                parent.setCardBackgroundColor(item.getBackgroundColor());
        }
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemClickedListener != null) itemClickedListener.onItemClick(view, position);
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(contactCheckedListener != null) contactCheckedListener.onContactChecked(view, position);
                updateEmojiForPosition(position);
            }
        });

        if(viewHolders.get(position) == null)viewHolders.set(position, holder);
    }

    private int genRandomColor(){
        Random random = new Random();
        red = random.nextInt(maxColor - minColor) + minColor;
        green = random.nextInt(maxColor - minColor) + minColor;
        blue = random.nextInt(maxColor - minColor) + minColor;
        return new Color().argb(255, red, green, blue);
    }

    int count = -1;
    int randNum = new Random().nextInt(3);
    int incAmt = 0;
    private int increaseRandomPartOfColor(){
        int color;
        if(incAmt == 0){ //If there's only 10 elements, change the color more drastically
            int change = maxColor - minColor;
            incAmt = contacts.size() > change? change / 10: change * 10 / contacts.size() / 2;
            incAmt = contacts.size() > change? 10: 30;
        }

        switch (randNum){
            case 0:
                if(incAmt > 0){
                    if(red < randomBound - incAmt && red < maxColor)
                        red += incAmt;
                    else
                        switchColor(1);

                } else {
                    if (red > randomBound + incAmt && red > minColor)
                        red += incAmt;
                    else
                        switchColor(1);
                }
                break;
            case 1:
                if(incAmt > 0){
                    if(green < randomBound - incAmt && green < maxColor)
                        green += incAmt;
                    else
                        switchColor(2);

                } else {
                    if (green > randomBound + incAmt && green > minColor)
                        green += incAmt;
                    else
                        switchColor(2);
                }
                break;
            case 2:
                if(incAmt > 0){
                    if(blue < randomBound - incAmt && blue < maxColor)
                        blue += incAmt;
                    else
                        switchColor(0);

                } else {
                    if (blue > randomBound + incAmt && blue > minColor)
                        blue += incAmt;
                    else
                        switchColor(0);

                }
                break;
        }
        count++;
        return lastColor = new Color().argb(255, red, green, blue);
    }
    private void switchColor(int to){
        randNum = to;
        incAmt = -incAmt;
        randomBound = new Random().nextInt(maxColor - minColor) + minColor;
    }

    public void setOnItemClickedListener(final onItemClickedListener itemClickedListener){
        this.itemClickedListener = itemClickedListener;
    }

    public void setOnContactCheckedListener(final onContactCheckedListener contactCheckedListener){
        this.contactCheckedListener = contactCheckedListener;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView proPic;
        public CheckBox checkBox;
        public TextView address;
        public RelativeLayout bg;

        public ViewHolder(View itemView) {
            super(itemView);
            proPic = (ImageView) itemView.findViewById(R.id.profile_picture);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            address = (TextView) itemView.findViewById(R.id.address);
            bg = (RelativeLayout) itemView.findViewById(R.id.background);

            fixCheckBoxBounds();
        }

        private void fixCheckBoxBounds(){
            checkBox.post(new Runnable() {
                @Override
                public void run() {
                    final View viewParent = (View) checkBox.getParent();
                    final Rect bound = new Rect();
                    checkBox.getHitRect(bound);
                    int amt = QuickTools.convertDpToPx(checkBox.getContext(), 8);

                    bound.top -= amt;
                    //bound.left -= amt;
                    bound.bottom += amt;
                    viewParent.setTouchDelegate(new TouchDelegate(bound, checkBox));
                }

            });
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        for (int i = 0, size = contacts.size(); i < size; i++) {
            String section = String.valueOf(contacts.get(i).getAddress().charAt(0)).toUpperCase();
            if (!sections.contains(section)) {
                sections.add(section);
                mSectionPositions.add(i);
            }
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mSectionPositions.get(sectionIndex);
    }

    public void updateChecked(int startPos, int endPos){
        for(int i = startPos; i < endPos; i++){
            ViewHolder v = viewHolders.get(i);
            if(v == null)continue;
            v.checkBox.setChecked(contacts.get(i).getSelected());
        }
    }

    public void updateEmojiForPosition(int position){
        Contact contact = contacts.get(position);
        ViewHolder holder = viewHolders.get(contact.getDisplayedPosition());
        if(holder == null)return;
        String name = holder.address.getText().toString();
        if(!name.equalsIgnoreCase(contact.getAddress()))contact = findContactByAddress(holder.address.getText().toString());
        if(contact != null && contact.getPhoto() == null){
            setEmojiFromSelected(holder, contact);
        }
    }

    private void setEmojiFromSelected(ViewHolder holder, Contact contact){
        Drawable drawable;
        if(contact.getSelected()){
            drawable = ContextCompat.getDrawable(activity, R.drawable.baseline_insert_emoticon_black_48);
        }else{
            drawable = ContextCompat.getDrawable(activity, R.drawable.sleep_emoji);
        }
        if(drawable != null) drawable.setTint(ContextCompat.getColor(activity, R.color.white));
        holder.proPic.setBackground(drawable);
    }

    private Contact findContactByAddress(String address){
        for(Contact c: contacts){
            if(c.getAddress().equalsIgnoreCase(address))return c;
        }
        return null;
    }

    // Filter Class
    public void filter(String charText) {
        contacts.clear();
        viewHolders.clear();
        if (charText.length() == 0) {
            for(Contact contact: allContacts){
                contact.setDisplayedPosition(contacts.size());
                contacts.add(contact);
                viewHolders.add(null);
            }
        } else {
            for (int i = 0; i < allContacts.size(); i++) {
                Contact current = allContacts.get(i);
                if (current.getAddress().toLowerCase().contains(charText.toLowerCase())) {
                    current.setDisplayedPosition(contacts.size());
                    contacts.add(current);
                    viewHolders.add(null);
                }
            }
        }
        notifyDataSetChanged();
    }

}