package com.biometricsx;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.biometricsx.pojo.FacePojo;
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

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import static com.biometricsx.utils.UtilConstants.mPersonGroupId;

public class ReUploadActivity extends AppCompatActivity {
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
    private boolean isLogout;
    private Uri fileUri;
    private AlertDialog.Builder ad;
    private AlertDialog alertDialog;
    private String status_, msg_;
    private boolean detected;
    private ProgressDialog progressDialog;
    private boolean changePhoto, status_NA;
    private Face[] face;
    private Bitmap mBitmap;
    private String mPId, mPGId, photo, status, msg, faceUri;
    private UUID faceId;
    private boolean statusCheck;
    private Button btnUploadforApproval;
    private boolean identifyPic;
    private FaceRectangle faceRectangle;
    private String faceRectString;
    private SharedPreferences pref;
    private FacePojo facePojo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reupload);
        //getintent and take params for update photo api
        initUI();
        if ((facePojo = (FacePojo) getIntent().getSerializableExtra(UtilConstants.FACE_KEY)) != null) {
            mPGId = facePojo.getPgid();
            mPId = facePojo.getPid();
        }
    }

    private void initUI() {
        lLay = findViewById(R.id.ll_camera_ui);
        name = LoginSharedPref.getUnameKey(this);
        takePicIV = findViewById(R.id.iv_loginout_click);
        btnUploadforApproval = findViewById(R.id.btn_reupload);
        //takePicIV.setEnabled(false);
    }

    public void onUploadforApprovalClicked(View view) {
        startCamera();
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
                    getImageUrl = ImagePath_MarshMallow.getPath(ReUploadActivity.this, fileUri);
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

    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        // Start a background task to detect faces in the image.
        detectionApi(inputStream);
    }

    private void detectionApi(ByteArrayInputStream inputStream) {
        //detection face api
        new DetectionTask().execute(inputStream);
        //afterFaceDetection();
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
            startAnimation();
        }

        @Override
        protected void onPostExecute(Face[] result) {
            stopAnimation();
            //  setUiAfterDetection(?;
            progressDialog.dismiss();
            if (result != null) {
                if (result.length == 0) {
                    face = null;
                    detected = false;
                    Snackbar.make(lLay, "No faces detected!", Snackbar.LENGTH_LONG).show();
                } else {
                    Log.i("TAG", "onPostExecute: " + result);
                    face = result;
                    Snackbar.make(lLay, "Face detected", Snackbar.LENGTH_LONG).show();
                    detected = true;
                    //if (faceId == null)
                    faceId = result[0].faceId;
                    Log.i("TAG", "onPostExecute: " + faceId);
                    UpdateImageApi();
                }
            } else {
                detected = false;
            }
        }
    }

    private void UpdateImageApi() {
        if (encode != null && faceRectString != null) {
            mPGId = mPersonGroupId;
            photo = encode;
            //mPId = UUID.randomUUID().toString();
            faceRectString = getFaceRectStringJson();
            Log.i("TAG", "UpdateImageApi: " + mPId);
            UpdateOnlyPhotoForApprovalTask task = new UpdateOnlyPhotoForApprovalTask();
            task.execute(LoginSharedPref.getUIdKey(ReUploadActivity.this),
                    photo, mPId, mPGId, faceId.toString(), faceUri, faceRectString);
        } else Log.i("TAG", "UpdateImageApi: " + encode);
    }

    public void onTakePicClicked(View view) {
        startCamera();
    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(ReUploadActivity.this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = ReUploadActivity.this.getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }

    private class UpdateOnlyPhotoForApprovalTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // startAnimation();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            try {
                Log.i(UtilConstants.TAG, "doInBackground: rApprovalT " + strings[0]
                        + strings[1] + strings[2] + " " + strings[3] + " " + strings[4] + " " + strings[5]+" "+strings[6]);
                JSONObject jsonObject =
                        //check update for faceRectString
                        restAPI.UpdateImage(strings[0], strings[1], strings[2], strings[3], strings[4],
                                strings[5], strings[6]);
                //Toast.makeText(ReUploadActivity.this, "" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
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
            //stopAnimation();
            Log.d("reply UpdateImg", s);
            //onuithread
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ReUploadActivity.this);
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
                        //Toast.makeText(ReUploadActivity.this, "updated photo in server", Toast.LENGTH_SHORT).show();
                        // apiImageTask();
                        statusPendingDialog();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(ReUploadActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReUploadActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void statusPendingDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(ReUploadActivity.this);
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
        ad.show();
        //dialogmsg: Sorry your image approval is pending-dialog-ok-close app
        //cannot click image or go to dashboard setenablefalse
    }

    private String getFaceRectStringJson() {
        String jsonString = null;
        if (face != null) {
            Gson gsons = new Gson();
            jsonString = gsons.toJson(face[0].faceRectangle);
            Log.i("TAG", "getFaceRectStringJson: " + jsonString);
        }
        return jsonString;
    }
}
