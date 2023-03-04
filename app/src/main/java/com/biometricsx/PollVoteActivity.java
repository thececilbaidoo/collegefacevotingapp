package com.biometricsx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.biometricsx.adap.CandAssignAdap;
import com.biometricsx.pojo.CandidatePojo;
import com.biometricsx.pojo.CandidatePojoExpired;
import com.biometricsx.pojo.PollPojo;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.utils.UtilConstants;
import com.biometricsx.webservices.JSONParser;
import com.biometricsx.webservices.RestAPI;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class PollVoteActivity extends AppCompatActivity {

    private ArrayList<CandidatePojo> candidatePojos;
    private ArrayList<CandidatePojoExpired> candidatePojosEx;
    private PollPojo pojo;
    private Dialog dialogLoader;
    private Dialog dialogShowPollDetails;
    private TextInputEditText pollDurationTV, pollDescET, fromtoTV;
    private TextInputEditText userNameET, descriptionET, scoreET;
    private ImageView restuaPhotoIV;

    private String isPrivate = UtilConstants.NO; //yes or no
    private TextView tvTitleInfo, tvTitle;
    private boolean isVotingLive;   //for which poll, isVotinLive = false for expired
    private CandAssignAdap candiAdap;
    private ListView listView;
    private Dialog dialogCandiDetails;
    private ImageView ivCrossCancel;
    private ArrayList<Boolean> listSingleSelection;
    private Button btnVoteSend;
    private String cid;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list_detail);
        Toolbar toolbar = findViewById(R.id.toolbar_add_order);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        setSupportActionBar(toolbar);
        //getintent
        pojo = (PollPojo) getIntent().getSerializableExtra(UtilConstants.POLLDETAIL_ID);
        initUI();
        //list of candidates with ui info icon
        getCandidates();
    }

    private void initUI() {
        if (pojo != null) {
            tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText(pojo.getPname());
            tvTitleInfo = findViewById(R.id.tv_title_info);
            listView = findViewById(R.id.lv_current);
            btnVoteSend = findViewById(R.id.btn_votesend);
            if (pojo.getIsprivate().equalsIgnoreCase("yes")) {
                isPrivate = "yes";
            } else {
                isPrivate = "no";
            }
            //getSupportActionBar().setTitle("#Poll id " + pojo.getVotedByUid());
            //update poll
            String whichFrag = null;
            //common activity for expired and current detail poll
            if ((whichFrag = getIntent().getStringExtra(UtilConstants.WHICH_POLL)) != null) {
                switch (whichFrag) {
                    case "DashboardExpiredActivity":
                        //cannot vote(btn invisible), only add title info as winner name with votes%
                        isVotingLive = false;
                        break;
                    case "DashboardCurrentActivity":
                        //show vote btn visible
                        isVotingLive = true;
                        break;
                }
            }
            getSupportActionBar().setTitle("Poll Id " + pojo.getPid());
        }
    }

    private String getTheWinner() {
        if (candidatePojosEx != null) {
            //get the highest score
            int candiScore, max = 0, indexOfCandiWithMaxScores = -1;
            candiScore = Integer.parseInt(candidatePojosEx.get(0).getScoredCount());
            Log.i(TAG, "getTheWinner: total " + candidatePojosEx.get(0).getOutOfTot());
            for (int i = 0; i < candidatePojosEx.size(); i++) {
                if (candiScore > max) {
                    max = candiScore;
                    indexOfCandiWithMaxScores = i;
                }
            }
            //Log.i(TAG, "getTheWinner: winner " + candidatePojosEx.get(indexOfCandiWithMaxScores));
            if (indexOfCandiWithMaxScores != -1)
                return candidatePojosEx.get(indexOfCandiWithMaxScores).getCname();
            else return null;
        }
        return null;
    }

    public void onPersonSelected(CandidatePojo candidatePojo) {
        viewThisCandidate(candidatePojo);
    }

    private void viewThisCandidate(CandidatePojo candidatePojo) {
        candidateDetailDialog(candidatePojo);
    }

    private void candidateDetailDialog(CandidatePojo candidatePojo) {
        dialogCandiDetails = new Dialog(this);
        // dialogCandiDetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        View view = getLayoutInflater().inflate(R.layout.dialog_candi_details, null);
        userNameET = view.findViewById(R.id.et_user_name);
        descriptionET = view.findViewById(R.id.et_msg_for_photouploaded);
        restuaPhotoIV = view.findViewById(R.id.iv_user_photo);
        scoreET = view.findViewById(R.id.et_score);
        ivCrossCancel = view.findViewById(R.id.iv_cancel);
        ivCrossCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogCandiDetails.isShowing()) dialogCandiDetails.cancel();
            }
        });
        setData(candidatePojo);
        dialogCandiDetails.setContentView(view);
        dialogCandiDetails.show();
    }

    private void candidateExDetailDialog(CandidatePojoExpired pojo) {
        dialogCandiDetails = new Dialog(this);
        // dialogCandiDetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        View view = getLayoutInflater().inflate(R.layout.dialog_candi_details, null);
        userNameET = view.findViewById(R.id.et_user_name);
        descriptionET = view.findViewById(R.id.et_msg_for_photouploaded);
        scoreET = view.findViewById(R.id.et_score);
        restuaPhotoIV = view.findViewById(R.id.iv_user_photo);
        ivCrossCancel = view.findViewById(R.id.iv_cancel);
        ivCrossCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogCandiDetails.isShowing()) dialogCandiDetails.cancel();
            }
        });
        userNameET.setText(pojo.getCname());
        descriptionET.setText(pojo.getCdesc());
        scoreET.setText(pojo.getScoredCount()+"/"+pojo.getOutOfTot());
        if (pojo.getCphoto() != null) {
            restuaPhotoIV.setImageBitmap(UtilConstants.decodeBitmap(pojo.getCphoto()));
        }
        //enabled false
        userNameET.setEnabled(false);
        descriptionET.setEnabled(false);
        scoreET.setEnabled(false);
        restuaPhotoIV.setEnabled(false);
        Log.i(TAG, "setData: photo" + pojo.getCphoto().length());
        dialogCandiDetails.setContentView(view);
        dialogCandiDetails.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void setData(CandidatePojo pojo) {
        userNameET.setText(pojo.getCname());
        descriptionET.setText(pojo.getCdesc());
        if (pojo.getCphoto() != null) {
            restuaPhotoIV.setImageBitmap(UtilConstants.decodeBitmap(pojo.getCphoto()));
        }
        //enabled false
        userNameET.setEnabled(false);
        descriptionET.setEnabled(false);
        restuaPhotoIV.setEnabled(false);
        scoreET.setVisibility(View.GONE);
        Log.i(TAG, "setData: photo" + pojo.getCphoto().length());
    }

    public void getCandidates() {
        new ViewCandidatesTask().execute(pojo.getPid(),
                LoginSharedPref.getUIdKey(PollVoteActivity.this), isPrivate);
    }

    public void onCandidateSelected(String cId) {
        //send this candi's id for voting
        cid = cId;
        Log.i(TAG, "onCandidateSelected: " + cid);
    }

    public void onVoteSendClicked(View view) {
        //send to next screen for face auth
        //take to face authn page, if face not approved then ask to submit face dialog same as on login page
        if (pojo != null && cid != null) {
            UtilConstants.showCustomToastNormal("Face Authenticate to cast your vote. starting camera..", PollVoteActivity.this);
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intentDashboard = new Intent(PollVoteActivity.this, FaceAuthActivity.class);
                    intentDashboard.putExtra(UtilConstants.CID_toFACE, cid);
                    intentDashboard.putExtra(UtilConstants.PID_toFACE, pojo.getPid());
                    startActivity(intentDashboard);
                }
            }, 2000);
        } else {
            UtilConstants.showCustomToast("Please select at least one candidate to vote", PollVoteActivity.this);
        }
    }

    public void onPersonSelectedEx(CandidatePojoExpired candiPojoEx) {
        candidateExDetailDialog(candiPojoEx);
    }

    private class ViewCandidatesTask extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnimation();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            JSONObject json;
            try {
                if (isVotingLive)
                    json = api.Uviewcandidates(params[0], params[1], params[2]);
                else
                    json = api.viewcandidates(params[0]);
                Log.i("TAG", "doInBackground: viewcandidates" + params[0] + params[1] + params[2]);
                JSONParser jp = new JSONParser();
                a = jp.parseJSON(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("reply viewcandi", s);
            super.onPostExecute(s);
            stopAnimation();
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(PollVoteActivity.this);
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
                    candidatePojos = new ArrayList<>();
                    candidatePojosEx = new ArrayList<>();
                    listSingleSelection = new ArrayList<>();
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("ok") == 0) {
                        CandidatePojo candidatePojoLive = null;
                        CandidatePojoExpired candidatePojoEx = null;
                        JSONArray jarry = json.getJSONArray("Data");
                        Log.d("TAG", jarry.length() + " : main arr");
                        for (int i = 0; i < jarry.length(); i++) {
                            JSONObject jobj = jarry.getJSONObject(i);
                            if (isVotingLive) {
                                candidatePojoLive = new CandidatePojo(jobj.getString("data0"),
                                        jobj.getString("data1"),
                                        jobj.getString("data2"),
                                        jobj.getString("data3"),
                                        jobj.getString("data4"),
                                        jobj.getString("data5"),
                                        jobj.getString("data6"));
                                candidatePojos.add(candidatePojoLive);
                            } else {
                                candidatePojoEx = new CandidatePojoExpired(jobj.getString("data0"),
                                        jobj.getString("data1"),
                                        jobj.getString("data2"),
                                        jobj.getString("data3"),
                                        jobj.getString("data4"),
                                        jobj.getString("data5"),
                                        jobj.getString("data6"));
                                candidatePojosEx.add(candidatePojoEx);
                            }
                            listSingleSelection.add(false);
                        }
                        setLV();
                    } else if (ans.compareTo(UtilConstants.NO) == 0) {
                        listView.setVisibility(View.GONE);
                        listView.setAdapter(null);
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(PollVoteActivity.this, error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PollVoteActivity.this, ans, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PollVoteActivity.this, "catch - " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void setLV() {
        if ((candidatePojos != null && candidatePojos.size() > 0) || (candidatePojosEx != null && candidatePojosEx.size() > 0)) {
            // can vote or not:
            if (candidatePojos != null && candidatePojos.size() > 0) {
                if (!candidatePojos.get(0).getHavescored().equalsIgnoreCase(UtilConstants.NO)) {
                    //disable vote and click on adapter selection, only detail
                    btnVoteSend.setText("Already voted!");
                    btnVoteSend.setEnabled(false);
                }
                if (candidatePojos.get(0).getCanvote().equalsIgnoreCase(UtilConstants.NO)) {
                    //disable vote and click on adapter selection, only detail
                    btnVoteSend.setText("Not eligible for voting");
                    btnVoteSend.setEnabled(false);
                }
            }
            if (isVotingLive)   //current
                candiAdap = new CandAssignAdap(PollVoteActivity.this,
                        candidatePojos, listSingleSelection,
                        candidatePojos.get(0).getHavescored().equalsIgnoreCase("NO"),
                        PollVoteActivity.this);
            else {
                candiAdap = new CandAssignAdap(PollVoteActivity.this,
                        candidatePojosEx, listSingleSelection,
                        PollVoteActivity.this, !isVotingLive);
                btnVoteSend.setVisibility(View.GONE);
            }
            listView.setAdapter(candiAdap);
            listView.setVisibility(View.VISIBLE);
        } else {
            //selectTV.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            listView.setAdapter(null);
        }
        if (!isVotingLive) {
            String winner = "No winner";
            if ((winner = getTheWinner()) != null)
                tvTitleInfo.setText("The Winner of this poll is "+winner);
            else
                tvTitleInfo.setText("This poll has No winner");
        }
    }

    private void showPollDetails() {
        dialogShowPollDetails = new Dialog(this);
        // dialogShowPollDetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = getLayoutInflater().inflate(R.layout.dialog_poll_info, null);
        fromtoTV = view.findViewById(R.id.tv_fromto);
        pollDescET = view.findViewById(R.id.poll_desc_et);
        pollDurationTV = view.findViewById(R.id.tv_poll_duration);
        pollDescET.setText(pojo.getPdesc());
        pollDurationTV.setText(pojo.getPstart() + "-" + pojo.getPend());
        fromtoTV.setText(pojo.getPname());
        fromtoTV.setEnabled(false);
        pollDescET.setEnabled(false);
        pollDurationTV.setEnabled(false);
        dialogShowPollDetails.setContentView(view);
        dialogShowPollDetails.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pollinfo) {
            showPollDetails();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pollinfo, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onDestroy() {
        if (dialogShowPollDetails != null && dialogShowPollDetails.isShowing())
            dialogShowPollDetails.dismiss();
        if (dialogLoader != null && dialogLoader.isShowing())
            dialogLoader.dismiss();
        super.onDestroy();
    }

}
