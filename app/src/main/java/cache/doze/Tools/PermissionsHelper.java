package cache.doze.Tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Chris on 8/22/2018.
 */

public class PermissionsHelper {

    public static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    public static final int RECEIVE_SMS_PERMISSIONS_REQUEST = 2;
    public static final int READ_SMS_PERMISSIONS_REQUEST = 3;

    public static boolean checkReadSMSPermission(Context context) {
        String permission = Manifest.permission.SEND_SMS;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkReceiveSMSPermission(Context context) {
        String permission = Manifest.permission.RECEIVE_SMS;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkReadContactsPermission(Context context) {
        String permission = Manifest.permission.READ_CONTACTS;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static void getPermissionToReadSMS(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
/*            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
//                Toast.makeText(this, "Required for functionallity", Toast.LENGTH_SHORT).show();
            }*/
            activity.requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    public static void getPermissionToReceiveSMS(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
/*            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
//                Toast.makeText(this, "Required for functionallity", Toast.LENGTH_SHORT).show();
            }*/
            activity.requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                    RECEIVE_SMS_PERMISSIONS_REQUEST);
        }
    }

    public static void getPermissionToReadContacts(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
           /* if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_CONTACTS)) {
//                Toast.makeText(this, "Required To Select contacts", Toast.LENGTH_SHORT).show();
            }*/
            activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }
}
