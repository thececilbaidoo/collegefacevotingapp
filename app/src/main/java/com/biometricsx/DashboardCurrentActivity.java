package com.biometricsx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.biometricsx.adap.PollAdap;
import com.biometricsx.pojo.PollPojo;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.utils.UtilConstants;
import com.biometricsx.webservices.JSONParser;
import com.biometricsx.webservices.RestAPI;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardCurrentActivity extends AppCompatActivity {

    private ListView listView;
    private Dialog dialogLoader;
    private LinearLayoutCompat llTab;
    private ArrayList<PollPojo> pollPojos;
    private PollAdap prodAdap;
    private ImageView noHistImageView;
    private String sDateTime;
    Calendar cal;
    Calendar sCal;
    private SimpleDateFormat sdFormat;
    private RelativeLayout rLay;
    private Dialog dialogShowPollDetails;
    private TextInputEditText sCodeET;
    private Button btnSendCode;
    private String sCode;
    private ImageView ivCrossCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_current);
        Toolbar toolbar = findViewById(R.id.toolbar_add_order);
        //toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        setSupportActionBar(toolbar);
        initUI();
        initDataObj();
        getData();
    }

    private void getData() {
        GetCurrentPollTask task = new GetCurrentPollTask();
        task.execute();
    }

    private void initUI() {
        listView = findViewById(R.id.lv_current);
        llTab = findViewById(R.id.ll_tab);
        noHistImageView = findViewById(R.id.iv_no_history);
        rLay = findViewById(R.id.rl_image);
        pollPojos = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            profileIntent();
        } else if (item.getItemId() == R.id.action_logout) {
            logoutIntent();
        } else if (item.getItemId() == R.id.action_expired) {
            goToExpiredDash();
        }
        return true;
    }

    private void goToExpiredDash() {
        Intent regIntent = new Intent(DashboardCurrentActivity.this, DashboardExpiredActivity.class);
        startActivity(regIntent);
    }

    private void profileIntent() {
        Intent regIntent = new Intent(DashboardCurrentActivity.this, ProfileActivity.class);
        startActivity(regIntent);
    }

    private void logoutIntent() {
        clearSharedPref();
        Intent regIntent = new Intent(DashboardCurrentActivity.this, SplashCumLoginActivity.class);
        regIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(regIntent);
    }

    private void clearSharedPref() {
        LoginSharedPref.setUnameKey(DashboardCurrentActivity.this, "");
        LoginSharedPref.setPidKey(DashboardCurrentActivity.this, "");
        LoginSharedPref.setUIdKey(DashboardCurrentActivity.this, "");
        LoginSharedPref.setPunchInKey(DashboardCurrentActivity.this, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_incurrent, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void allowAccessToPrivatePoll(PollPojo pojo) {
        Intent intentDetail = new Intent(DashboardCurrentActivity.this, PollVoteActivity.class);
        intentDetail.putExtra(UtilConstants.POLLDETAIL_ID, pojo);
        intentDetail.putExtra(UtilConstants.WHICH_POLL, DashboardCurrentActivity.class.getSimpleName());
        startActivity(intentDetail);
    }

    private class GetCurrentPollTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnimation();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.viewpoll("Current", sDateTime);
                Log.i("TAG", "doInBackground: " + "Current " + sDateTime);
                JSONParser jsonParser = new JSONParser();
                result = jsonParser.parseJSON(jsonObject);
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stopAnimation();
            Log.d("reply currentpoll", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(DashboardCurrentActivity.this);
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
                    if (ans.compareTo("ok") == 0) {
                        //left
                        if (pollPojos.size() > 0)
                            pollPojos.clear();
                        PollPojo pollPojo = null;
                        JSONArray jarry = json.getJSONArray("Data");
                        Log.d("TAG", jarry.length() + " : main arr");
                        for (int i = 0; i < jarry.length(); i++) {
                            JSONObject jsonO = jarry.getJSONObject(i);
                            //pid,pname,pdesc,pstart,pend
                            //scored,tot - only for current
                            pollPojo = new PollPojo(jsonO.getString("data0"), jsonO.getString("data1"), jsonO.getString("data2"), jsonO.getString("data3"), jsonO.getString("data4"), jsonO.getString("data5"), jsonO.getString("data6")
                                    , jsonO.getString("data7")
                                    , jsonO.getString("data8"));
                            pollPojos.add(pollPojo);
                        }
                        setLV();
                    } else if (ans.compareTo("no") == 0) {
                        listView.setAdapter(null);
                        listView.setVisibility(View.GONE);
                        noHistImageView.setVisibility(View.VISIBLE);
                        rLay.setVisibility(View.VISIBLE);
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(DashboardCurrentActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(DashboardCurrentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setLV() {
        if (pollPojos != null && pollPojos.size() > 0) {
            prodAdap = new PollAdap(DashboardCurrentActivity.this, pollPojos, DashboardCurrentActivity.this);
            listView.setAdapter(prodAdap);
            noHistImageView.setVisibility(View.GONE);
            rLay.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setAdapter(null);
            listView.setVisibility(View.GONE);
            noHistImageView.setVisibility(View.VISIBLE);
            rLay.setVisibility(View.VISIBLE);
        }
    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(DashboardCurrentActivity.this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }

    private void initDataObj() {
        cal = Calendar.getInstance();
        sCal = Calendar.getInstance();
        Date date = cal.getTime();
        sdFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        sDateTime = simpleFormat.format(date);
        //dateTVStart.setText(getResources().getString(R.string.date_) + " " + sDateTime);
    }

    public void onAdapItemClicked(PollPojo pojo) {
        if (pojo.getIsprivate().equalsIgnoreCase(UtilConstants.YES)) {
            //ask for password dialog
            showPollDetails(pojo);
            Log.i("TAG", "onAdapItemClicked: " + pojo.getIsprivate());
        } else {
            allowAccessToPrivatePoll(pojo);
        }
    }

    private void showPollDetails(final PollPojo pollPojo) {
        dialogShowPollDetails = new Dialog(this);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        View view = getLayoutInflater().inflate(R.layout.dialog_enterpass_private, null);
        sCodeET = view.findViewById(R.id.tv_fromto);
        btnSendCode = view.findViewById(R.id.btn_sendcode);
        ivCrossCancel = view.findViewById(R.id.iv_cancel);
        ivCrossCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogShowPollDetails.isShowing()) dialogShowPollDetails.cancel();
            }
        });
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogShowPollDetails != null && dialogShowPollDetails.isShowing()) {
                    sCode = sCodeET.getText().toString();
                    //check if passcode is correct
                    if (pollPojo != null && !pollPojo.getScode().isEmpty()) {
                        if (pollPojo.getScode().equalsIgnoreCase(sCode)) {
                            dialogShowPollDetails.dismiss();
                            allowAccessToPrivatePoll(pollPojo);
                        } else {
                            UtilConstants.showCustomToast("Security code entered is incorrect",
                                    DashboardCurrentActivity.this);
                        }
                    }
                }
            }
        });
        dialogShowPollDetails.setContentView(view);
        dialogShowPollDetails.show();
    }

    @Override
    protected void onDestroy() {
        if (dialogLoader != null && dialogLoader.isShowing())
            dialogLoader.dismiss();
        super.onDestroy();
    }
}
