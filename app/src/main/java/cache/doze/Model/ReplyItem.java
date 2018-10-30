package cache.doze.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Created by Chris on 10/4/2018.
 */

public class ReplyItem implements Serializable{
    private String title;
    private String replyText;
    private boolean checked;
    private List<Contact> contacts;
    private String id;

    public ReplyItem(String title, String reply){
        this.title = title;
        this.replyText = reply;
        checked = false;

        id = (System.currentTimeMillis() + new Random().nextInt(Math.abs((int)System.currentTimeMillis()))) + reply.length() +"";
    }

    public ReplyItem(ReplyItem replyItem){
        this.title = replyItem.title;
        this.replyText = replyItem.replyText;
        this.checked = replyItem.checked;

        this.id = replyItem.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setContacts(List<Contact> contacts){
        this.contacts = contacts;
    }
    public List<Contact> getContacts() {
        return contacts;
    }
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
    public String getReplyText() {
        return replyText;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public boolean isChecked() {
        return checked;
    }

    public String getId() {
        return id;
    }
}
