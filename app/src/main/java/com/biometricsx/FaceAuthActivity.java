package com.biometricsx;

import static com.biometricsx.utils.UtilConstants.mPersonGroupId;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.biometricsx.utils.ClientApi;
import com.biometricsx.utils.ImageHelper;
import com.biometricsx.utils.ImagePath_MarshMallow;
import com.biometricsx.utils.LoginSharedPref;
import com.biometricsx.utils.PermissionUtils;
import com.biometricsx.utils.UtilConstants;
import com.biometricsx.webservices.JSONParser;
import com.biometricsx.webservices.RestAPI;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class FaceAuthActivity extends AppCompatActivity {
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final String FILE_NAME = "temp.jpg";
    private TextView tvLogoutPunch, tvLoginPunch;
    private ImageView takePicIV;
    private boolean loginPunch;
    private static final String BG_LOC_TAG = "BG_LOC_TAG";
    private static final int CAMERA_PERMISSIONS_REQUEST = 2;
    private String encode;
    private static final int MAX_DIMENSION = 500;
    private String name;
    private LinearLayout lLay;
    private Dialog dialogLoader;
    private Uri fileUri;
    private Uri fileUri1;
    private AlertDialog.Builder ad;
    private AlertDialog alertDialog;
    private String status_, msg_;
    private boolean detected;
    private ProgressDialog progressDialog;
    private boolean changePhoto, status_NA;
    private Face[] face;
    private Face[] face1;
    private Bitmap mBitmap;
    private UUID faceId;
    private boolean statusCheck;
    private Button btnUploadforApproval;
    private boolean identifyPic;
    private FaceRectangle faceRectangle;
    private String faceRectString;
    private String faceUri;
    private String faceUri1;
    private Calendar cal;
    private SimpleDateFormat simpledateFormat, sTimeFormat;
    private String d, t;
    private String pid, cid;
    private UUID faceIdu;
    String photo;
    String getImageUrl1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);


        new GetFaceTask().execute(LoginSharedPref.getUIdKey(FaceAuthActivity.this));


        if ((cid = getIntent().getStringExtra(UtilConstants.CID_toFACE)) != null) {
            pid = getIntent().getStringExtra(UtilConstants.PID_toFACE);
            initUI();
            startCamera();
        }
    }


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
                AlertDialog.Builder ad = new AlertDialog.Builder(FaceAuthActivity.this);
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
//                        photo = array.getJSONObject(0).getString("data1");
//                        Log.d("photo", photo);
//                        status = array.getJSONObject(0).getString("data2");
//                        msg = array.getJSONObject(0).getString("data3");
//                        mPId = array.getJSONObject(0).getString("data4");
//                        LoginSharedPref.setPidKey(SplashCumLoginActivity.this,
//                                mPId);
//                        mPGId = array.getJSONObject(0).getString("data5");
                        try {

//                            if (!array.getJSONObject(0).getString("data6").equalsIgnoreCase(UtilConstants.NA))
//
//                                faceIdu = UUID.fromString(array.getJSONObject(0).getString("data6"));
//                            Log.d("fid", faceIdu.toString());


                            if (!array.getJSONObject(0).getString("data7").equalsIgnoreCase(UtilConstants.NA))

//                                faceIdu = UUID.fromString(array.getJSONObject(0).getString("data6"));
                                Log.d("fid", array.getJSONObject(0).getString("data7"));
                            fileUri1 = Uri.parse(array.getJSONObject(0).getString("data7"));

                            faceIdu = UUID.fromString(String.valueOf(array.getJSONObject(0).getString("data6")));


//                            ContentValues values = new ContentValues(1);
//                            values.put(MediaStore.Images.Media.MIME_TYPE, "*/*");
//                            fileUri1 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
////                            Bitmap mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri1, getContentResolver());
////                            Log.d("bitmap", mBitmap1.toString());
//
//
//                            Bitmap mBitmap1 = null;
//                            try {
//                                mBitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri1);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//
////                            Bitmap mBitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri1);
//
//
////                            ImageView img = (ImageView) findViewById(R.id.iv_loginout_click1);
////                            img.setImageURI(fileUri1);
//
//
////                            encode = UtilConstants.encodeBitmap(mBitmap1);


//                            Bitmap mBitmap1 = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri1, getContentResolver());
//                            if (mBitmap1 != null) {
//                                // mBitmap = Bitmap.createScaledBitmap(mBitmap, 500, 500, false);
//                    /*mBitmap =scaleBitmapDown(
//                            MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri),
//                            MAX_DIMENSION);*/
////                    img.setImageBitmap(mBitmap);
//                                String getImageUrl;
//                                getImageUrl = ImagePath_MarshMallow.getPath(FaceAuthActivity.this, fileUri1);
//                                faceUri1 = getImageUrl;
//                                //Bitmap bt = RotateImg(getImageUrl, mBitmap);
//                                encode = null;
////                                takePicIV.setImageBitmap(mBitmap1);
////                                this.mBitmap1 = mBitmap1;
//                                Log.i("TAG", "getFace: " + mBitmap1);
//
//                                encode = UtilConstants.encodeBitmap(mBitmap1);
//                                Log.i("TAG", "getFace: ENCODE SENT" + encode.length());
//
//
//                                Log.d("detect1", "Working");
//                                detect1(mBitmap1);
//                            }


                        } catch (IllegalArgumentException ex) {
                            Log.d("fid", ex.getMessage());
                            ex.printStackTrace();
                        }
//
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FaceAuthActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {
        // The IDs of two face to verify.


        @Override
        protected VerifyResult doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
                publishProgress("Verifying...");


                Log.d("check", faceId.toString());
                Log.d("check1", faceIdu.toString());


                // Start verification.
                return faceServiceClient.verify(
                        faceId,      /* The first face ID to verify */
                        faceIdu);     /* The second face ID to verify */
            } catch (Exception e) {
                Log.d("verify", e.getMessage());
            }
//                publishProgress(e.getMessage());
//                addLog(e.getMessage());
            return null;
        }


        @Override
        protected void onPreExecute() {
//        progressDialog.show();
//            addLog("Request: Verifying face " + mFaceId0 + " and face " + mFaceId1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
//            progressDialog.setMessage(progress[0]);
//            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {
                Log.d("verify", "Response: Success. Face " + faceId + " and face "
                        + faceIdu + (result.isIdentical ? " " : " don't ")
                        + "belong to the same person");


                if (result.isIdentical) {
                    castVote();
                } else {
                    Toast.makeText(FaceAuthActivity.this, "Face not Match", Toast.LENGTH_SHORT).show();
                }
            }

            // Show the result on screen when verification is done.
//            setUiAfterVerification(result);
        }
    }


    private void initUI() {
        lLay = findViewById(R.id.ll_camera_ui);
        name = LoginSharedPref.getUnameKey(this);
        takePicIV = findViewById(R.id.iv_loginout_click);
        btnUploadforApproval = findViewById(R.id.btn_reupload);
        progressDialog = new ProgressDialog(FaceAuthActivity.this);
        progressDialog.setTitle("Please wait..");
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
           /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);*/
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "*/*");
                fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permissions needed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            /*Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            if (photoUri != null) {
                setImg(photoUri);
            }*/
            try {
                Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri, getContentResolver());
                if (mBitmap != null) {
                    // mBitmap = Bitmap.createScaledBitmap(mBitmap, 500, 500, false);
                    /*mBitmap =scaleBitmapDown(
                            MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri),
                            MAX_DIMENSION);*/
//                    img.setImageBitmap(mBitmap);
                    String getImageUrl;
                    getImageUrl = ImagePath_MarshMallow.getPath(FaceAuthActivity.this, fileUri);
                    faceUri = getImageUrl;
                    //Bitmap bt = RotateImg(getImageUrl, mBitmap);
                    encode = null;
                    takePicIV.setImageBitmap(mBitmap);
                    this.mBitmap = mBitmap;
                    Log.i("TAG", "onActivityResult: " + mBitmap);

                    encode = UtilConstants.encodeBitmap(mBitmap);
                    Log.i("TAG", "onActivityResult: ENCODE SENT" + encode.length());
                    //detect face in selected image
                    detect(mBitmap);
                    //if no face detected then snackbar:


                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Start detecting in image.
    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        // Start a background task to detect faces in the image.
        detectionApi(inputStream);
    }

    private void detect1(Bitmap bitmap1) {

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(output.toByteArray());
        // Start a background task to detect faces in the image.
        Log.d("input1", inputStream1.toString());
        detectionApi1(inputStream1);


    }


    public void onFaceAuth(View view) {
        startCamera();
    }

    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            } catch (Exception e) {
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.

//            was not commented
//            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            //stopAnimation();
            //  setUiAfterDetection(?;
            progressDialog.dismiss();
            if (result != null) {
                if (result.length == 0) {
                    face = null;
                    detected = false;
                    showCustomToast("No faces detected!");
                } else {
                    Log.i("TAG", "onPostExecute: " + result);
                    face = result;
                    Snackbar.make(lLay, "Face detected", Snackbar.LENGTH_LONG).show();
                    detected = true;
                    //if (faceId == null)
                    faceId = result[0].faceId;
                    Log.i("TAG", "onPostExecute: " + faceId);

                    //
                    faceRectString = getFaceRectStringJson();
                    new VerificationTask().execute();


//                    afterFaceDetection();
//                    was not commented
                }
            } else {
                detected = false;
            }
        }
    }

    private class DetectionTask1 extends AsyncTask<InputStream, String, Face[]> {
        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
//                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            } catch (Exception e) {
                publishProgress(e.getMessage());
                Log.d("detectiontask1", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.

//            was not commented
//            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            //stopAnimation();
            //  setUiAfterDetection(?;
            progressDialog.dismiss();
            if (result != null) {
                if (result.length == 0) {
                    face1 = null;
//                    detected = false;
                    showCustomToast("No faces detected!");
                } else {
                    Log.i("TAG", "onPostExecute: " + result);
                    face1 = result;
                    Snackbar.make(lLay, "Face detected", Snackbar.LENGTH_LONG).show();
//                    detected = true;
                    //if (faceId == null)
                    faceIdu = result[0].faceId;
                    Log.i("TAG1", "onPostExecute: " + faceIdu);
                    Log.i("face", "faceidu " + faceIdu);

                    //
                    faceRectString = getFaceRectStringJson();
                    new VerificationTask().execute();


//                    afterFaceDetection();
//                    was not commented
                }
            } else {
                detected = false;
            }
        }
    }

    private String getFaceRectStringJson() {
        String jsonString = null;
        if (face != null) {
            Gson gsons = new Gson();
            jsonString = gsons.toJson(face[0].faceRectangle);
            Log.i("TAG", "getFaceRectStringJson: " + jsonString);

//            text = jsonString;
        }
        return jsonString;
    }

    private void detectionApi(ByteArrayInputStream inputStream) {
        //detection face api
        new DetectionTask().execute(inputStream);
        //afterFaceDetection();
    }

    private void detectionApi1(ByteArrayInputStream inputStream1) {
        //detection face api
        new DetectionTask1().execute(inputStream1);
        //afterFaceDetection();
    }


    private void afterFaceDetection() {
        //set in detection api
        if (detected) {
            Log.i("TAG", "afterFaceDetection: " + status_NA);
            identifyPic = true;
            identificationApi();
            identifyPic = false;
            Log.i("TAG", "afterFaceDetection: no match, else");
        } else {
            Snackbar.make(lLay, "Your face is not detected, click again", Snackbar.LENGTH_LONG).show();
        }
        detected = false;
    }

    private void identificationApi() {
        Log.i("TAG", "identificationApi: " + faceId/*[0].faceId*/);
        new IdentificationTask(mPersonGroupId).execute(
                /*face[0].*/new UUID[]{faceId});
        //faceIds.toArray(new UUID[faceIds.size()])
    }

    private void initDataObj() {
        cal = Calendar.getInstance();
        Date date = cal.getTime();
        simpledateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        sTimeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        d = simpledateFormat.format(date);
        t = sTimeFormat.format(date);
    }


    class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        private boolean mSucceed = true;
        String mPersonGroupId;

        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            String logString = "Request: Identifying faces ";
            /*for (UUID faceId : params) {
                logString += faceId.toString() + ", ";
            }*/
            logString += " in group " + mPersonGroupId;
//            addLog(logString);

            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();
            try {
                //publishProgress("Getting person group status...");

                TrainingStatus trainingStatus = faceServiceClient.getLargePersonGroupTrainingStatus(
                        this.mPersonGroupId);     /* personGroupId */
                Log.i("TAG", "doInBackground: " + trainingStatus.status);
                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
                    //publishProgress("Person group training status is " + trainingStatus.status);
                    mSucceed = false;
                    return null;
                }

                //publishProgress("Identifying...");
                // Toast.makeText(FaceAuthActivity.this, "Identifying...", Toast.LENGTH_SHORT).show();
                // Start identification.
                return faceServiceClient.identityInLargePersonGroup(
                        this.mPersonGroupId,   /* personGroupId */
                        params,                  /* faceIds */
                        1);  /* maxNumOfCandidatesReturned */
            } catch (Exception e) {
                e.printStackTrace();
                mSucceed = false;
                // publishProgress(e.getMessage());
                //addLog(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.a
            setUiDuringBackgroundTask(values[0]);
        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            // Show the result on screen when detection is done.
            setUiAfterIdentification(result, mSucceed);

//            was not commented
            // IdentificationTask

//            castVote();


        }
    }

    private class CastAVoteTask extends AsyncTask<String, Void, String> {
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
                JSONObject jsonObject = restAPI.CastVote(strings[0], strings[1], strings[2], d, t);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(FaceAuthActivity.this);
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
                    if (ans.compareTo("already") == 0) {
                        Snackbar.make(lLay, "Login failed. Password/user Id do not match.", Snackbar.LENGTH_LONG).show();
                    } else if (ans.compareTo("true") == 0) {
                        Intent intentDashboard = new Intent(FaceAuthActivity.this, SuccesVoteActivity.class);
                        intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentDashboard);
                        //after castVote-> info that successfully voted
                        finish();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(FaceAuthActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(FaceAuthActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(IdentifyResult[] result, boolean succeed) {
        progressDialog.dismiss();

        Log.i("TAG", "setUiAfterIdentification: " + succeed);
        if (succeed) {
            // Set the information about the detection result.
            Snackbar.make(lLay, "Detection is done", Snackbar.LENGTH_LONG).show();

            if (result != null) {

                String logString = "Response: Success. ";
                for (IdentifyResult identifyResult : result) {
                    logString += "Face " + identifyResult.faceId.toString() + " is identified as "
                            + (identifyResult.candidates.size() > 0
                            ? identifyResult.candidates.get(0).personId.toString()
                            : "Unknown Person")
                            + ". " + result.length;
                }
                //if (result.length > 0)
                /*Log.i("TAG", "candidates.size: " +
                        result[0].candidates.size());*/
                if (result.length > 0)
                    if (result[0].candidates.size() > 0) {
                        if (result[0].candidates.get(0).confidence > 0.8
                                && LoginSharedPref.getPidKey(FaceAuthActivity.this).
                                equalsIgnoreCase(result[0].candidates.get(0).personId.toString())) {
                            //crm

                            Log.i("TAG", "setUiAfterIdentification: " + logString + "\n" + result[0].candidates.get(0).confidence);
                            castVote();
                        } else {
                            showCustomToast("face authentication failed");
                            // Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                            //authentication failed
                            //finish();
                        }
                    } else {
                        showCustomToast("face authentication failed");
                        // Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                        //authentication failed
                        //finish();
                    }
                //addLog(logString);
                Log.i("TAG", "setUiAfterIdentification: result.length " + logString
                        + result[0].candidates.get(0).confidence);
            }
        } else {
            Snackbar.make(lLay, "Couldn't proceed, Check if you have trained the group or no", Snackbar.LENGTH_LONG).show();
        }
    }

    private void castVote() {
        initDataObj();
        CastAVoteTask task = new CastAVoteTask();
        task.execute(LoginSharedPref.getUIdKey(FaceAuthActivity.this), pid, cid);
    }

    private void showCustomToast(String toastText) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);
        TextView text = layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void onTakePicClicked(View view) {
        startCamera();
    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    protected void onDestroy() {
        if (dialogLoader != null && dialogLoader.isShowing())
            dialogLoader.dismiss();
        super.onDestroy();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(FaceAuthActivity.this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = FaceAuthActivity.this.getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }
}
