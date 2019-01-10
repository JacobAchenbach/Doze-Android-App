package cache.doze.Model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Chris on 2/15/2018.
 */

public class Contact implements Serializable {
    private String address;
    private boolean selected;
    private String id;
    private Bitmap photo;
    private Integer backgroundColor;
    private Integer displayedPosition;
    private ArrayList<String> knownNumbers = new ArrayList<>();

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
        selected = victim.selected;
        id = victim.id;
        photo = victim.photo;
        backgroundColor = victim.backgroundColor;
        displayedPosition = victim.displayedPosition;
    }

    public void addNumber(String number){
        knownNumbers.add(number);
    }

    public boolean hasNumber(String inNumber){
        inNumber = formatNum(inNumber);

        for(String myNumber: knownNumbers){
            if(formatNum(myNumber).equalsIgnoreCase(inNumber))return true;
        }
        return false;
    }

    private String formatNum(String inNumber){
        String endNum = "";
        for(int i = 0; i < inNumber.length(); i++){
            try {
                String digit = inNumber.substring(i, i + 1);
                Integer.valueOf(digit);
                endNum += digit;
            }catch (Exception e){

            }
        }
        if(endNum.substring(0, 1).equalsIgnoreCase("1"))
            endNum = endNum.substring(1, endNum.length());

        return endNum;
    }


    public String getAddress() {
        return address;
    }

    public String getShortenedAddress(){
        String ret = address.split(" ")[0];
        if(ret.length() > 10)ret = ret.substring(0, 10);
        return ret;
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

    public ArrayList<String> getNumbers() {
        return knownNumbers;
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
