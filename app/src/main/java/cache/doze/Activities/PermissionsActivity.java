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

public class PermissionsActivity extends AppCompatActivity {

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

    private void setUpStatusIcon() {
        statusIcon.setAlpha(0f);
        statusIcon.post(new Runnable() {
            @Override
            public void run() {
                statusIcon.animate().alpha(1f).setStartDelay(550).setDuration(1000)
                        .setInterpolator(new AccelerateInterpolator()).start();
            }
        });
    }

    private void setUpOkayButton() {
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runPermissionsTest();
            }
        });
    }

    private void runPermissionsTest() {
        if (!PermissionsHelper.checkReceiveSMSPermission(getBaseContext())) {
            PermissionsHelper.getPermissionToReceiveSMS(delegate);
            setDroidState(2);
        } else if (!PermissionsHelper.checkReadSMSPermission(getBaseContext())) {
            PermissionsHelper.getPermissionToReadSMS(delegate);
            setDroidState(3);
        } else if (!PermissionsHelper.checkReadContactsPermission(getBaseContext())) {
            PermissionsHelper.getPermissionToReadContacts(delegate);
            setDroidState(3);
        } else
            animateAndEnd();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsHelper.RECEIVE_SMS_PERMISSIONS_REQUEST) { //Receive SMS
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runPermissionsTest();
                    }
                });
            } else { //Permission Denied

            }

        }//Read SMS
        else if (requestCode == PermissionsHelper.READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runPermissionsTest();
                    }
                });
            } else { //Permission denied

            }

        }//Read Contacts
        else if (requestCode == PermissionsHelper.READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Not null and permission granted
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runPermissionsTest();
                    }
                });
            } else { //Permission denied

            }
        }
    }

    private void setDroidState(int state) {
        switch (state) {
            case 1:
                statusIcon.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.cute_android_sleeping));
                break;
            case 2:
                statusIcon.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.cute_android_half_awake));
                final ObjectAnimator moveAnim
                        = ObjectAnimator.ofFloat(statusIcon, "y", statusIcon.getY(), -statusIcon.getHeight());
                moveAnim.setInterpolator(new AccelerateInterpolator());
                moveAnim.setDuration(150);
                break;
            case 3:
                statusIcon.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.cute_android));
                break;
        }
    }

    private void animateAndEnd() {
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
    public void onBackPressed() {
        finishAffinity();
    }
}
