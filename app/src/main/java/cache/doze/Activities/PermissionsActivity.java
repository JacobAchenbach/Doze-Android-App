package cache.doze.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import cache.doze.R;
import cache.doze.Tools.PermissionsHelper;

/**
 * Created by Chris on 8/22/2018.
 */

public class PermissionsActivity extends AppCompatActivity{

    Activity delegate;

    ImageView statusIcon;
    Button okayButton;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        delegate = this;
        statusIcon = findViewById(R.id.icon_status);
        okayButton = findViewById(R.id.button_permission_okay);
        nextButton = findViewById(R.id.button_permission_next);

        setUpStatusIcon();
        setUpOkayButton();
    }

    private void setUpStatusIcon(){
        statusIcon.setAlpha(0f);
        statusIcon.post(new Runnable() {
            @Override
            public void run() {
                statusIcon.animate().alpha(1f).setStartDelay(550).setDuration(1000)
                        .setInterpolator(new AccelerateInterpolator()).start();
            }
        });
    }

    private void setUpOkayButton(){
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusIcon.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.cute_android));
                if(!PermissionsHelper.checkReceiveSMSPermission(getBaseContext()))
                    PermissionsHelper.getPermissionToReceiveSMS(delegate);
                else if (!PermissionsHelper.checkReadSMSPermission(getBaseContext()))
                    PermissionsHelper.getPermissionToReadSMS(delegate);
                else if(!PermissionsHelper.checkReadContactsPermission(getBaseContext()))
                    PermissionsHelper.getPermissionToReadContacts(delegate);
                else
                    finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsHelper.RECEIVE_SMS_PERMISSIONS_REQUEST) { //Receive SMS
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                if(!PermissionsHelper.checkReadSMSPermission(delegate)){
                    PermissionsHelper.getPermissionToReadSMS(delegate);
                }else if(!PermissionsHelper.checkReadContactsPermission(delegate)) {
                    PermissionsHelper.getPermissionToReadContacts(delegate);
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateAndEnd();
                        }
                    });
                }
            } else { //Permission denied
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //:(
                    }
                });
            }

        }//Read SMS
        else if (requestCode == PermissionsHelper.READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                if(!PermissionsHelper.checkReceiveSMSPermission(delegate)){
                    PermissionsHelper.getPermissionToReceiveSMS(delegate);
                }else if(!PermissionsHelper.checkReadContactsPermission(delegate)) {
                    PermissionsHelper.getPermissionToReadContacts(delegate);
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateAndEnd();
                        }
                    });
                }
            } else { //Permission denied
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //:(
                    }
                });
            }

        }//Read Contacts
        else if(requestCode == PermissionsHelper.READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                if(!PermissionsHelper.checkReadSMSPermission(delegate)){
                    PermissionsHelper.getPermissionToReadSMS(delegate);
                }else if(!PermissionsHelper.checkReceiveSMSPermission(delegate)) {
                    PermissionsHelper.getPermissionToReceiveSMS(delegate);
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            animateAndEnd();
                        }
                    });
                }
            } else { //Permission denied
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }
    }

    private void animateAndEnd(){
        final ObjectAnimator moveAnim
                = ObjectAnimator.ofFloat(statusIcon, "y", statusIcon.getY(), -statusIcon.getHeight());
        moveAnim.setInterpolator(new AccelerateInterpolator());
        moveAnim.setDuration(150);
        moveAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Intent result = new Intent();
                result.putExtra("result", "granted");
                setResult(RESULT_OK, result);
                finish();
                overridePendingTransition(0, R.anim.slide_out_top);
            }
        });
        moveAnim.start();
    }

    @Override
    public void onBackPressed(){
        finishAffinity();
    }
}
