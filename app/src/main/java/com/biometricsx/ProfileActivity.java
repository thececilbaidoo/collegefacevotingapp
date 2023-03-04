package com.biometricsx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import com.biometricsx.pojo.UsrsPojo;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.webservices.JSONParser;
import com.biometricsx.webservices.RestAPI;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.ContentValues.TAG;


public class ProfileActivity extends AppCompatActivity {
    private EditText userNameET, userEmailET, userContET, userPassportET;
    private UsrsPojo pojo;
    private ArrayList<String> list;
    private Dialog dialogLoader;
    private ScrollView sView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        initUI();
        getProfile();
    }

    private void getProfile() {
        MyProfileDetailTask task = new MyProfileDetailTask();
        task.execute(LoginSharedPref.getUIdKey(ProfileActivity.this));
    }

    private void initUI() {
        userNameET = findViewById(R.id.et_user_name);
        userEmailET = findViewById(R.id.et_user_email);
        userPassportET = findViewById(R.id.et_passport);
        userContET = findViewById(R.id.et_user_contact);
        sView = findViewById(R.id.sv_profile);

    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }

    public void onProfileUpdateClicked(View view) {
        if (userNameET.getText().toString().isEmpty()) {
            userNameET.setError("Please enter your full name");
        } else if (userEmailET.getText().toString().isEmpty()) {
            userEmailET.setError("Please enter school email ID");
        } else if (userContET.getText().toString().isEmpty()) {
            userContET.setError("Please enter contact number");
        } else if (userPassportET.getText().toString().isEmpty()) {
            userPassportET.setError("Please enter student ID number");
        } else {
            UpdateTask task = new UpdateTask();
            task.execute(LoginSharedPref.getUIdKey(ProfileActivity.this),
                    userNameET.getText().toString(), userEmailET.getText().toString(), userContET.getText().toString(),
                    userPassportET.getText().toString());
        }
    }

    private class MyProfileDetailTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.UgetProfile(strings[0]);
                JSONParser jsonParser = new JSONParser();
                result = jsonParser.parseJSON(jsonObject);
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnimation();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stopAnimation();
            Log.d("reply ChangeOrder", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ProfileActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    Log.d("reply", ans);
                    if (ans.compareTo("no") == 0) {
                        Snackbar.make(sView, "Please update your profile", Snackbar.LENGTH_SHORT).show();
                    } else if (ans.compareTo("ok") == 0) {
                        //uid,name,email,contact,idno
                        JSONArray jsonDataArray = json.getJSONArray("Data");
                        for (int j = 0; j < jsonDataArray.length(); j++) {
                            JSONObject jsonO = jsonDataArray.getJSONObject(j);
                            pojo = new UsrsPojo(jsonO.getString("data0"), jsonO.getString("data1")
                                    , jsonO.getString("data2"), jsonO.getString("data3"),
                                    jsonO.getString("data4"));
                            Log.i(TAG, "onPostExecute: " + jsonO.getString("data0") + jsonO.getString("data1")
                                    + jsonO.getString("data2") + jsonO.getString("data3") +
                                    jsonO.getString("data4"));
                        }
                        setProfile();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Snackbar.make(sView, error, Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }

    private class UpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.UupdateProfile(strings[0], strings[1], strings[2], strings[3], strings[4]
                );
                JSONParser jsonParser = new JSONParser();
                result = jsonParser.parseJSON(jsonObject);
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnimation();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stopAnimation();
            Log.d("reply uipdate", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ProfileActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    Log.d("reply", ans);
                    if (ans.compareTo("true") == 0) {
                        Snackbar.make(sView, "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                        finish();
                    } else if (ans.compareTo("email") == 0) {
                        Snackbar.make(sView, "This email id is already registered", Snackbar.LENGTH_SHORT).show();

                    } else if (ans.compareTo("idno") == 0) {
                        Snackbar.make(sView, "This id number is already registered", Snackbar.LENGTH_SHORT).show();

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Snackbar.make(sView, error, Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }

    private void setProfile() {
        userNameET.setText(pojo.getName());
        userEmailET.setText(pojo.getEmail());
        userContET.setText(pojo.getContact());
        userPassportET.setText(pojo.getIdentificationno());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }
}
