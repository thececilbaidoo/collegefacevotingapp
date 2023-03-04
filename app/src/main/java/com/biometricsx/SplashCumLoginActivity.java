package com.biometricsx;

import static com.biometricsx.utils.UtilConstants.mPersonGroupId;
import static com.biometricsx.utils.UtilConstants.showCustomToast;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.biometricsx.pojo.FacePojo;
import com.biometricsx.utils.ClientApi;
import com.biometricsx.utils.EmailValidation;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.utils.UtilConstants;
import com.biometricsx.webservices.JSONParser;
import com.biometricsx.webservices.RestAPI;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SplashCumLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout cLayout;
    private LinearLayoutCompat ll;
    private FingerprintManagerCompat fingerprintManagerCompat;
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricPrompt biometricPrompt;
    private Handler handler;
    private TextInputEditText passET, emailET;
    private Button btnLogin, btnReg;
    private ProgressDialog progressDialog;
    private Dialog dialogLoader;
    private boolean changePhoto, status_NA;
    private Bitmap mBitmap;
    private String mPId, mPGId, photo, status, msg, faceUri;
    private UUID faceId;
    private boolean statusCheck;
    private String faceRectString;
    private FaceRectangle faceRectangle;
    private boolean isFaceApproved;
    private FacePojo facePojo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LoginSharedPref.getUIdKey(SplashCumLoginActivity.this).isEmpty()
                && LoginSharedPref.getPunchInKey(SplashCumLoginActivity.this)) {

            setContentView(R.layout.activity_splashcumlogin);
            fingerprintInit();
            initUI();
            isFaceApproved = true;
            startFingerprintScanning();
            /*Intent regIntent = new Intent(SplashCumLoginActivity.this, DashboardCurrentActivity.class);
            startActivity(regIntent);
            finish();*/


        } else {

            setContentView(R.layout.activity_splashcumlogin);
            fingerprintInit();
            initUI();
            statusCheck = true;
            getFaceApi();


        }
    }

    private void fingerprintInit() {

        Executor executor = Executors.newSingleThreadExecutor();
        fingerprintManagerCompat =
                FingerprintManagerCompat.from(this);
        Log.i("TAG", "isHardwareDetected: " + fingerprintManagerCompat.isHardwareDetected());
        biometricPrompt =
                new androidx.biometric.BiometricPrompt(this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                            finish();

                        } else {

                            // TODO: Called when an unrecoverable error has been encountered and the operation is complete.
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        //TODO: Called when a biometric is recognized.
                        goToDashBoard();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        //failed show message
                        Log.i("TAG", "onAuthenticationFailed:  Called when a biometric is valid but not recognized.");
                    }
                });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                // .setSubtitle("")
                .setDescription("Place your finger to verify your identity")
                .setNegativeButtonText("cancel")
                .build();

    }

    private void getFaceApi() {
        if (!LoginSharedPref.getUIdKey(SplashCumLoginActivity.this).isEmpty()) {  //not punched in
            // if (!LoginSharedPref.getUIdKey(SplashCumLoginActivity.this).isEmpty()) {
            Log.i("TAG", "getFaceApi: " + LoginSharedPref.getUIdKey(SplashCumLoginActivity.this));
            new GetFaceTask().execute(LoginSharedPref.getUIdKey(SplashCumLoginActivity.this));
            // }
        } else if (LoginSharedPref.getPunchInKey(SplashCumLoginActivity.this)) {
            Log.i("TAG", "getFaceApi: not punched in");
            isFaceApproved = true;
            startFingerprintScanning();
        } else {
            showNormalLoginOption();
        }
    }

    private void startFingerprintScanning() {
        if (fingerprintManagerCompat.isHardwareDetected()) {
            //if fingerprint not registered in device then inform user and return
            if (!fingerprintManagerCompat.hasEnrolledFingerprints()) {
                showCustomToast("Fingerprint is not registered. Add a fingerprint and try again",
                        SplashCumLoginActivity.this);
                return;
            }
            //if isFaceApproved: go ahead
            if (isFaceApproved)
                askForFingerAuth();
            else if (!LoginSharedPref.getUIdKey(SplashCumLoginActivity.this).isEmpty())
                statusPendingDialog();
            else showNormalLoginOption();
            if (changePhoto) {
                showReuploadNowDialog();
                //open in another activity for
            }
        } else {
            UtilConstants.showCustomToast("Your device does not support fingerprint scanner, try alternate login",
                    SplashCumLoginActivity.this);
            showNormalLoginOption();
        }
    }

    private void showReuploadNowDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
        ad.setTitle("Request for re-upload of your photo");
        ad.setMessage("Your photo was not approved. Admin message: " + msg);
        ad.setNeutralButton("take a picture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //launch ReUploadActivity with upload details for api from getFaceApi();
                Intent intent = new Intent(SplashCumLoginActivity.this, ReUploadActivity.class);
                intent.putExtra(UtilConstants.LOGIN_AS_BTN_VISIBLE_KEY, true);
                intent.putExtra(UtilConstants.FACE_KEY, facePojo);
                startActivity(intent);
            }
        });
        ad.setNegativeButton("later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad.show();
    }

    private void askForFingerAuth() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                biometricPrompt.authenticate(promptInfo);
            }
        }, 2000);
    }

    private void showNormalLoginOption() {
        //ll setVisibility true
        ll.setVisibility(View.VISIBLE);
    }

    private void initUI() {
        cLayout = findViewById(R.id.cLayout);
        ll = findViewById(R.id.ll);
        emailET = findViewById(R.id.et_email_log);
        passET = findViewById(R.id.et_pass_log);
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        btnReg = findViewById(R.id.btn_reg);
        btnReg.setOnClickListener(this);
        progressDialog = new ProgressDialog(SplashCumLoginActivity.this);
        progressDialog.setTitle("Please wait..");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_login) {
            loginThroughEmailPass();
        } else if (view.getId() == R.id.btn_reg) {
            goToRegister();
        }
    }

    private void loginThroughEmailPass() {
        EmailValidation validate = new EmailValidation();
        if (emailET.getText().toString().isEmpty()) {
            Snackbar.make(ll, "Please Enter Email address", Snackbar.LENGTH_SHORT).show();
        } else if (!validate.validateEmail(emailET.getText().toString())) {
            Snackbar.make(ll, "Email address is invalid", Snackbar.LENGTH_SHORT).show();
        } else if (passET.getText().toString().isEmpty()) {
            Snackbar.make(ll, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
        } else {
            LoginTask task = new LoginTask();
            task.execute(emailET.getText().toString(), passET.getText().toString());
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
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
                Log.i("TAG", "doInBackground: " + strings[0] + strings[1]);
                JSONObject jsonObject = restAPI.Ulogin(strings[0], strings[1]);
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
            Log.d("reply Login", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
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
                    if (ans.compareTo("false") == 0) {
                        Snackbar.make(ll, "Login failed. Password/user Id do not match.", Snackbar.LENGTH_LONG).show();
                    } else if (ans.compareTo("ok") == 0) {
                        //name,status,message
                        JSONArray array = json.getJSONArray("Data");
                        LoginSharedPref.setUnameKey(SplashCumLoginActivity.this,
                                array.getJSONObject(0).getString("data1"));
                        LoginSharedPref.setUIdKey(SplashCumLoginActivity.this, array.getJSONObject(0).getString("data0"));
                        //check for status if approved
                        getFaceApiApprovedCheck();

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(SplashCumLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SplashCumLoginActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getFaceApiApprovedCheck() {
        new GetFaceApprovalTask().execute(LoginSharedPref.getUIdKey(SplashCumLoginActivity.this));
    }

    //
    private class GetFaceTask extends AsyncTask<String, Void, String> {
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
                //Log.i(UtilConstants.TAG, "doInBackground: " + strings[1] + strings[2] + strings[3] + strings[0]);
                JSONObject jsonObject = restAPI.getFace(strings[0]);
                //Toast.makeText(SplashCumLoginActivity.this, "" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                Log.i(UtilConstants.TAG, "doInBackground: getface " + jsonObject.toString());
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
            Log.d("reply getface", s);
            //onuithread
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
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
                        //photo,status,message,pid,pgid,faceid,faceuri
                        //details from this api save in instance variables
                        JSONArray array = json.getJSONArray("Data");
                        photo = array.getJSONObject(0).getString("data1");
                        status = array.getJSONObject(0).getString("data2");
                        msg = array.getJSONObject(0).getString("data3");
                        mPId = array.getJSONObject(0).getString("data4");
                        LoginSharedPref.setPidKey(SplashCumLoginActivity.this,
                                mPId);
                        mPGId = array.getJSONObject(0).getString("data5");
                        try {
                            if (!array.getJSONObject(0).getString("data6").equalsIgnoreCase(UtilConstants.NA))
                                faceId = UUID.fromString(array.getJSONObject(0).getString("data6"));
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                        faceUri = array.getJSONObject(0).getString("data7");
                        if (faceId == null)
                            facePojo = new FacePojo(mPId, mPGId, null, faceUri, faceRectString);
                        else
                            facePojo = new FacePojo(mPId, mPGId, faceId.toString(), faceUri, faceRectString);
                        faceRectString = array.getJSONObject(0).getString("data8");
                        if (!faceRectString.equalsIgnoreCase("NA")) {
                            Gson gson = new Gson();
                            faceRectangle = gson.fromJson(faceRectString, FaceRectangle.class);
                        }
                        Log.i("TAG", "onPostExecute: " +
                                mPId + " " + mPGId + " " + status + " " + msg + " "
                                + faceUri + " " + faceId + " " + faceRectString);
                        if (statusCheck) {
                            if (status.equalsIgnoreCase(UtilConstants.STATUS_PENDING)) {
                                if (!msg.equalsIgnoreCase(UtilConstants.NA)) {
                                    //change photo
                                    Snackbar.make(ll, msg.toUpperCase() + ", please click your pic", Snackbar.LENGTH_INDEFINITE).show();
                                    changePhoto = true;
                                } else {
                                    //still pending, handled in startFingerprintScanning
                                    isFaceApproved = false;
                                }
                            } else if (!status.equalsIgnoreCase(UtilConstants.NA)) {   //approved
                                if (!msg.equalsIgnoreCase(UtilConstants.NA)) {
                                    //direct identify if msg not NA-"Uploded image"
                                    isFaceApproved = true;
                                    Log.i("TAG", "onCreate: msg Uploaded image");
                                } else {
                                    Log.i("TAG", "onPostExecute: msg is NA");
                                    //upload
                                    uploadToMSRepoApi();
                                    // and update msg in status
                                    statusCheck = false;
                                    return;
                                }
                            } else {
                                status_NA = true;
                                Snackbar.make(ll, "To set up face authentication, please click your picture first", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        startFingerprintScanning();
                        statusCheck = false;
                    } else if (ans.compareTo("false") == 0) {
                        Toast.makeText(SplashCumLoginActivity.this, "Database(Face) not added by admin", Toast.LENGTH_SHORT).show();
                        clearSharedPref();
                        finish();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(SplashCumLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SplashCumLoginActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class GetFaceApprovalTask extends AsyncTask<String, Void, String> {
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
                //Log.i(UtilConstants.TAG, "doInBackground: " + strings[1] + strings[2] + strings[3] + strings[0]);
                JSONObject jsonObject = restAPI.getFace(strings[0]);
                //Toast.makeText(SplashCumLoginActivity.this, "" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                Log.i(UtilConstants.TAG, "doInBackground: getface " + jsonObject.toString());
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
            Log.d("reply getfaceappr", s);
            //onuithread
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
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
                        //photo,status,message,pid,pgid,faceid,faceuri
                        //details from this api save in instance variables
                        JSONArray array = json.getJSONArray("Data");
                        photo = array.getJSONObject(0).getString("data1");
                        status = array.getJSONObject(0).getString("data2");
                        msg = array.getJSONObject(0).getString("data3");
                        mPId = array.getJSONObject(0).getString("data4");
                        LoginSharedPref.setPidKey(SplashCumLoginActivity.this,
                                mPId);
                        mPGId = array.getJSONObject(0).getString("data5");
                        try {
                            if (!array.getJSONObject(0).getString("data6").equalsIgnoreCase(UtilConstants.NA))
                                faceId = UUID.fromString(array.getJSONObject(0).getString("data6"));
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                        faceUri = array.getJSONObject(0).getString("data7");
                        if (faceId == null)
                            facePojo = new FacePojo(mPId, mPGId, null, faceUri, faceRectString);
                        else
                            facePojo = new FacePojo(mPId, mPGId, faceId.toString(), faceUri, faceRectString);
                        faceRectString = array.getJSONObject(0).getString("data8");
                        if (!faceRectString.equalsIgnoreCase("NA")) {
                            Gson gson = new Gson();
                            faceRectangle = gson.fromJson(faceRectString, FaceRectangle.class);
                        }
                        Log.i("TAG", "onPostExecute: " +
                                mPId + " " + mPGId + " " + status + " " + msg + " "
                                + faceUri + " " + faceId + " " + faceRectString);
                        if (status.equalsIgnoreCase(UtilConstants.STATUS_PENDING)) {
                            if (!msg.equalsIgnoreCase(UtilConstants.NA)) {
                                //change photo
                                Snackbar.make(ll, msg.toUpperCase() + ", please click your pic", Snackbar.LENGTH_INDEFINITE).show();
                                changePhoto = true;
                                showReuploadNowDialog();
                            } else {
                                //still pending
                                statusPendingDialog();
                                isFaceApproved = false;
                            }
                        } else if (!status.equalsIgnoreCase(UtilConstants.NA)) {   //approved
                            if (!msg.equalsIgnoreCase(UtilConstants.NA)) {
                                //direct identify if msg not NA-"Uploded image"
                                isFaceApproved = true;
                                LoginSharedPref.setPunchInKey(SplashCumLoginActivity.this, true);
                                goToDashBoard();
                                Log.i("TAG", "onCreate: msg Uploaded image");
                            } else {
                                Log.i("TAG", "onPostExecute: msg is NA");
                                uploadToMSRepoApi();
                                // and update msg in status
                            }
                        } else {
                            status_NA = true;
                            Snackbar.make(ll, "To set up face authentication, please click your picture first", Snackbar.LENGTH_LONG).show();
                        }
                    } else if (ans.compareTo("false") == 0) {
                        Toast.makeText(SplashCumLoginActivity.this, "Database(Face) not added by admin", Toast.LENGTH_SHORT).show();
                        clearSharedPref();
                        finish();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(SplashCumLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SplashCumLoginActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (dialogLoader != null && dialogLoader.isShowing())
            dialogLoader.dismiss();
        super.onDestroy();
    }

    private void statusPendingDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
        ad.setTitle("Approval pending");
        ad.setCancelable(false);
        ad.setMessage("Your image is under pending state. You cannot use face authentication or access the app");
        ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //clearSharedPref();
                finishAffinity();
            }
        });
        ad.setPositiveButton("Register here", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToRegister();
            }
        });
        ad.show();
        //dialogmsg: Sorry your image approval is pending-dialog-ok-close app
        //cannot click image or go to dashboard setenablefalse
    }

    private void goToRegister() {
        Intent regIntent = new Intent(SplashCumLoginActivity.this, RegistrationActivity.class);
        startActivity(regIntent);
        finish();
    }

    //approved so upload to MS repository
    private void uploadToMSRepoApi() {
        //create before adding
        UploadToMSTask();
    }

    private void clearSharedPref() {
        LoginSharedPref.setUnameKey(SplashCumLoginActivity.this, "");
        LoginSharedPref.setUIdKey(SplashCumLoginActivity.this, "");
        LoginSharedPref.setPunchInKey(SplashCumLoginActivity.this, false);
        LoginSharedPref.setPidKey(SplashCumLoginActivity.this, "");
    }

    private void UploadToMSTask() {
        //not uploading since facerect not gettign saved in reg
        if (faceRectangle == null) {
            Log.i("TAG", "UploadToMSTask:msg: face not detected");
            return;
        }
        //mPId = UUID.randomUUID().toString();
        UploadToMSTask task = new UploadToMSTask();
        task.execute();
    }


    class UploadToMSTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
                if (mPId == null)
                    return false;
                UUID personId = UUID.fromString(mPId);
                if (mBitmap == null) {
                    mBitmap = UtilConstants.decodeBitmap(photo);
                    Log.i("TAG", "UtilConstants.decodeBitmap: " + mBitmap);
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());
                // Start the request to add face.

//                Log.i("TAG", "doInBackground: " + mPersonGroupId + " " +
//                        personId + " " +
//                        imageInputStream + " " +
//                        timestamp() + " " +
//                        faceRectangle);

                AddPersistedFaceResult result = faceServiceClient.addPersonFaceInLargePersonGroup(
                        mPersonGroupId,
                        personId,
                        imageInputStream,
                        timestamp(),
                        faceRectangle);

                return true;
            } catch (Exception e) {
                publishProgress(e.getMessage());
                //addLog(e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {

//            setUiDuringBackgroundTask(progress[0]);

            //it was not commented
            UpdateImageStatusApi();
            Log.i("TAG", "onProgressUpdate: " + progress);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //setUiAfterAddingFace(result, mFaceIndices);
            Log.i("TAG", "UploadToMStask onPostExecute: " + result);
            if (result) UpdateImageStatusApi();
            // after upload:
        }
    }

    private void UpdateImageStatusApi() {
        UpdateStatusTask task = new UpdateStatusTask();
        task.execute(LoginSharedPref.getUIdKey(SplashCumLoginActivity.this));
    }

    private String timestamp() {
        Date d = new Date();
        String uniqueUserData = LoginSharedPref.getUIdKey(SplashCumLoginActivity.this) +
                LoginSharedPref.getUnameKey(SplashCumLoginActivity.this) +
                d;
        if (uniqueUserData.length() >= 64) {
            return LoginSharedPref.getUIdKey(SplashCumLoginActivity.this) +
                    LoginSharedPref.getUnameKey(SplashCumLoginActivity.this);
        } else
            return uniqueUserData;
    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(SplashCumLoginActivity.this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }

    private class UpdateStatusTask extends AsyncTask<String, Void, String> {
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
                JSONObject jsonObject = restAPI.UpdateImageStatus(strings[0]);
                //Toast.makeText(SplashCumLoginActivity.this, "" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                Log.i(UtilConstants.TAG, "doInBackground: " + jsonObject.toString());
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
            Log.d("reply updatestatus", s);
            //onuithread
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(SplashCumLoginActivity.this);
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
                    if (ans.compareTo("true") == 0) {
                        // enable the click image btn, prompt the user to click his pic, identify the clicked image by user
                       /* Snackbar.make(ll, "To Authenticate: please click your pic now",
                                Snackbar.LENGTH_INDEFINITE).show();*/
                        //not getting trained. make new acc n check , but check flow once logging
                        trainingGroup();
                        Log.i("TAG", "onPostExecute: updatestatus");
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(SplashCumLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SplashCumLoginActivity.this, "Error : "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void trainingGroup() {
        new TrainPersonGroupTask().execute(mPersonGroupId);
    }

    class TrainPersonGroupTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            // addLog("Request: Training group " + params[0]);

            // Get an instance of face service client.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
                // publishProgress("Training person group...");

                faceServiceClient.trainLargePersonGroup(params[0]);
                return params[0];
            } catch (Exception e) {
                // publishProgress(e.getMessage());
                /// addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
//            i commented this
//            setUiDuringBackgroundTask(progress[0]);

            UpdateImageStatusApi();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                Log.i("TAG", "onPostExecute:TrainPersonGroup ");
              /*  identifyPic = true;
                startCamera();*/
                LoginSharedPref.setPunchInKey(SplashCumLoginActivity.this, true);
                goToDashBoard();
            }
        }
    }

    private void setUiBeforeBackgroundTask() {
        progressDialog.show();
    }

    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);
        Log.i("TAG", "setUiDuringBackgroundTask: " + progress);
    }

    private void goToDashBoard() {
        Intent intentDashboard = new Intent(SplashCumLoginActivity.this, DashboardCurrentActivity.class);
        startActivity(intentDashboard);
        finish();
    }
}

