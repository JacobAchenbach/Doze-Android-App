package cache.doze;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Chris on 2/15/2018.
 */

public class Contact implements Serializable {
    private String address;
    private String number;
    private boolean selected;
    private String id;
    private Bitmap photo;
    private Integer backgroundColor;
    private Integer displayedPosition;

    public Contact(String address, boolean selected, String id){
        this.address = address;
        this.selected = selected;
        this.id = id;
    }

    public Contact(String address, String id){
        this.address = address;
        this.selected = true;
        this.id = id;
    }

    public Contact(Contact victim){
        address = victim.address;
        number = victim.number;
        selected = victim.selected;
        id = victim.id;
        photo = victim.photo;
        backgroundColor = victim.backgroundColor;
        displayedPosition = victim.displayedPosition;
    }

    public String getAddress() {
        return address;
    }

    public boolean getSelected(){
        return selected;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDisplayedPosition() {
        return displayedPosition;
    }

    public void setDisplayedPosition(int displayedPosition) {
        this.displayedPosition = displayedPosition;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public int setBackgroundColor(int backgroundColor) {
        return this.backgroundColor = backgroundColor;
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }
}
