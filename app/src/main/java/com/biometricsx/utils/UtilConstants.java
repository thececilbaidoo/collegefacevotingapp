package com.biometricsx.utils;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.biometricsx.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class UtilConstants {
    public static final String LOGIN_AS_BTN_VISIBLE_KEY = "LOGIN_AS_BTN_VISIBLE_KEY";
    public static final String PROFILE_TITLE = "My Profile";
    public static final String TOTAL_LOGIN_KEY = "TOTAL_LOGIN_KEY";
    public static final String MSG_KEY = "MSG_KEY";
    public static final String FACE_KEY = "FACE_KEY";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED= "Approved";
    public static final String NA = "NA";
    public static final String mPersonGroupId = "547272bd-a187-4d2a-85b4-4c50c571dbe2";
    public static final String TAG = "TAG";
    public static final String WHICH_POLL = "WHICH_POLL";
    public static final String POLLDETAIL_ID = "POLLDETAIL_ID";
    public static final String NO = "no";
    public static final String YES = "yes";
    public static final String CID_toFACE = "cid";
    public static final String PID_toFACE = "pid";

    public static Bitmap decodeBitmap(String imageString) {
        ByteArrayInputStream bArrIS = new ByteArrayInputStream(Base64.decode(imageString, Base64.DEFAULT));
        return BitmapFactory.decodeStream(bArrIS);
    }

    public static String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream barrOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, barrOS);
        return Base64.encodeToString(barrOS.toByteArray(), Base64.DEFAULT);
    }
    public static void showCustomToast(String toastText, AppCompatActivity context) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    public static void showCustomToastNormal(String toastText, AppCompatActivity context) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_normal, null);
        TextView text = layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
