package com.biometricsx;

import static com.biometricsx.utils.UtilConstants.mPersonGroupId;
import static com.biometricsx.utils.UtilConstants.showCustomToastNormal;

import android.Manifest;
import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.biometricsx.utils.ClientApi;
import com.biometricsx.utils.EmailValidation;
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
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText nameET, emailET, passET, contactET, idNumET;
    private Button regBtn;
    private LinearLayout llReg;
    private EmailValidation validate;
    private Dialog dialogLoader;
    private Button loginBtn;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 2;
    private String encode;
    private Uri fileUri;
    private boolean detected;
    private ProgressDialog progressDialog;
    private boolean changePhoto, status_NA;
    private Face[] face;
    private Bitmap mBitmap;
    private String mPId, mPGId, photo, status, msg, faceUri;
    private UUID faceId;
    private String faceRectString;
    private ImageView takePicIV;


    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initUI();
    }


    private void initUI() {
        nameET = findViewById(R.id.et_name_reg);
        emailET = findViewById(R.id.et_email_reg);
        loginBtn = findViewById(R.id.btn_login_screen);
        loginBtn.setOnClickListener(this);
        passET = findViewById(R.id.et_pass_reg);
        contactET = findViewById(R.id.et_contact_reg);
        idNumET = findViewById(R.id.et_pincode_reg);
        llReg = findViewById(R.id.ll_login_ui);
        takePicIV = findViewById(R.id.iv_loginout_click);

        regBtn = findViewById(R.id.btn_reg);
        regBtn.setOnClickListener(this);
        validate = new EmailValidation();
        progressDialog = new ProgressDialog(RegistrationActivity.this);
        progressDialog.setTitle("Please wait..");
    }

    private void stopAnimation() {
        if (dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    private void startAnimation() {
        dialogLoader = new Dialog(RegistrationActivity.this, R.style.AppTheme_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = RegistrationActivity.this.getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        dialogLoader.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_reg) {
            if (nameET.getText().toString().isEmpty()) {
                Snackbar.make(llReg, "Please Enter Name", Snackbar.LENGTH_SHORT).show();
            } else if (emailET.getText().toString().isEmpty()) {
                Snackbar.make(llReg, "Please Enter Email address", Snackbar.LENGTH_SHORT).show();
            } else if (!validate.validateEmail(emailET.getText().toString())) {
                Snackbar.make(llReg, "Email address is invalid", Snackbar.LENGTH_SHORT).show();
            } else if (contactET.getText().toString().isEmpty()) {
                Snackbar.make(llReg, "Please Enter Contact", Snackbar.LENGTH_SHORT).show();
            } else if (idNumET.getText().toString().isEmpty()) {
                Snackbar.make(llReg, "Please Enter identification number", Snackbar.LENGTH_SHORT).show();
            } else if (idNumET.getText().toString().length() != 9) {
                Snackbar.make(llReg, "Please Enter 9 digit identification number", Snackbar.LENGTH_SHORT).show();
            } else if (passET.getText().toString().isEmpty()) {
                Snackbar.make(llReg, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
            } else {
                afterFaceDetection();
            }
        } else if (id == R.id.btn_login_screen) {
            goToLogin();
        }
        hideKeyboard(v);
    }

    private void loginAct() {
        Intent loginIntent = new Intent(RegistrationActivity.this, SplashCumLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        //finish();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onTakePicClicked(View view) {

        if (!nameET.getText().toString().isEmpty()) startCamera();
        else {
            showCustomToastNormal("Please enter a name to proceed", RegistrationActivity.this);
        }
    }

    private class RegTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.Uregister(strings[0], strings[1], strings[2], strings[3]
                        , strings[4], strings[5], strings[6], strings[7], strings[8]
                        , strings[9], strings[10]);
                Log.i("TAG", "doInBackground: " + strings[0] + strings[1] + strings[2] + strings[3]
                        + strings[4] +/* strings[5]+ */strings[6] + strings[7] + strings[8]
                        + strings[9] + strings[10]);
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
            Log.d("reply reg", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(RegistrationActivity.this);
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
                    //Log.d("reply::", ans);
                    if (ans.compareTo("true") == 0) {
                        Log.i("TAG", "UpdatePhotoForApprovalTask: " + mPId + faceRectString);
                       /* UpdatePhotoForApprovalTask task = new UpdatePhotoForApprovalTask();
                        task.execute(LoginSharedPref.getUIdKey(RegistrationActivity.this),
                                photo, mPId, mPGId, faceId.toString(), faceUri, faceRectString);*/
                        statusPendingDialog();
                    } else if (ans.compareTo("email") == 0) {
                        Snackbar.make(llReg, "Account with this email already exists", Snackbar.LENGTH_SHORT).show();
                    } else if (ans.compareTo("idno") == 0) {
                        Snackbar.make(llReg, "Account with this identification number already exists", Snackbar.LENGTH_SHORT).show();
                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(RegistrationActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RegistrationActivity.this, "Error registering: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void statusPendingDialog() {
        takePicIV.setEnabled(false);
        AlertDialog.Builder ad = new AlertDialog.Builder(RegistrationActivity.this);
        ad.setTitle("Registration successful");
        ad.setMessage("Your image is under pending state. You will be redirected to Login Screen for Authentication");
        AlertDialog.Builder ok = ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                goToLogin();
                //finish();
            }
        });
        ad.show();
        //dialogmsg: Sorry your image approval is pending-dialog-ok-close app
        //cannot click image or go to dashboard unfalsifiable
    }

    private void goToLogin() {
        clearSharedPref();
        Intent intentLogin = new Intent(RegistrationActivity.this, SplashCumLoginActivity.class);
        intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
    }

    private void clearSharedPref() {
        LoginSharedPref.setUnameKey(RegistrationActivity.this, "");
        LoginSharedPref.setPidKey(RegistrationActivity.this, "");
        LoginSharedPref.setUIdKey(RegistrationActivity.this, "");
        LoginSharedPref.setPunchInKey(RegistrationActivity.this, false);
    }


    //microsoft face detection api
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
            setUiDuringBackgroundTask(values[0]);
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
                    Snackbar.make(llReg, "No faces detected! Click again", Snackbar.LENGTH_LONG).show();
                } else {
                    Log.i("TAG", "onPostExecute: " + result);
                    face = result;
                    //Snackbar.make(llReg, "Face detected", Snackbar.LENGTH_LONG).show();
                    detected = true;
                    //if (faceId == null)
                    faceId = result[0].faceId;
                    //[TEST]
                    faceRectString = getFaceRectStringJson();

                    Log.i("TAG", "onPostExecute: faceid and face[], facerectstring " + faceId + face.length + faceRectString);

                    //afterfaceDetection was commented
//                    afterFaceDetection();
                }

            } else {
                detected = false;
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

    private void setUiBeforeBackgroundTask() {
        progressDialog.show();
    }

    // Show the status of background detection task on screen.
    private void setUiDuringBackgroundTask(String progress) {
        progressDialog.setMessage(progress);
        Log.i("TAG", "setUiDuringBackgroundTask: " + progress);

    }


    private void createPerson() {
        UpdateImageApi();


//        AddPersonTask task = new AddPersonTask();
//        Log.d("pg", mPersonGroupId);
//        task.execute(mPersonGroupId, nameET.getText().toString());

    }

    class AddPersonTask extends AsyncTask<String, String, String> {
        // Indicate the next step is to add face in this person, or finish editing this person.

        @Override
        protected String doInBackground(String... params) {
            // Get an instance of face service client.
            FaceServiceClient faceServiceClient = ClientApi.getFaceServiceClient();

            try {
                /*publishProgress("Syncing with server to add person...");
                addLog("Request: Creating Person in person group" + params[0]);*/

                // Start the request to creating person.
                Log.d("addpersontask", "working");


                CreatePersonResult createPersonResult = faceServiceClient.createPersonInLargePersonGroup(

                        params[0],
                        params[1], params[1]
//                        nameET.getText().toString(), faceId.toString(), mPersonGroupId

                );
                //unique description data- uid
                return createPersonResult.personId.toString();


            } catch (Exception e) {
                /*publishProgress(e.getMessage());
                addLog(e.getMessage());*/
                Log.d("error", "AddpersonTask :" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            setUiBeforeBackgroundTask();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            setUiDuringBackgroundTask(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result != null) {
                mPId = result;
                Log.i("TAG", "onPostExecute: mPid from createPerson" + result);
                UpdateImageApi();
            }
        }
    }

    private void UpdateImageApi() {
        if (encode != null /*&& faceRectString != null*/) {
            mPGId = mPersonGroupId;
            photo = encode;
            faceRectString = getFaceRectStringJson();
            //mPId = UUID.randomUUID().toString();
            RegisterTask();
            Log.d("pgid", mPGId);
        } else Log.i("TAG", "UpdateImageApi: " + encode.length());
    }

    private String getFaceRectStringJson() {
        String jsonString = null;
        if (face != null) {
            Gson gsons = new Gson();
            jsonString = gsons.toJson(face[0].faceRectangle);
            Log.i("TAG", "getFaceRectStringJson: " + jsonString);

            text = jsonString;
        }
        return jsonString;
    }

    private class UpdatePhotoForApprovalTask extends AsyncTask<String, Void, String> {
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
                        + strings[1] + strings[2] + " " + strings[3] + " " + strings[4] + " " + strings[5] + " " + strings[6]);
                JSONObject jsonObject =
                        //check update for faceRectString
                        restAPI.UpdateImage(strings[0], strings[1], strings[2], strings[3], strings[4],
                                strings[5], strings[6]);
                //Toast.makeText(RegistrationActivity.this, "" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                // Log.i(UtilConstants.TAG, "doInBackground: " + jsonObject.toString());
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
                AlertDialog.Builder ad = new AlertDialog.Builder(RegistrationActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection, Unable to connect the Server");
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
                        //Toast.makeText(RegistrationActivity.this, "updated photo in server", Toast.LENGTH_SHORT).show();
                        // apiImageTask();

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(RegistrationActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RegistrationActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void RegisterTask() {
        /*if (nameET.getText().toString().isEmpty()) {
            Snackbar.make(llReg, "Please Enter Name", Snackbar.LENGTH_SHORT).show();
        } else if (emailET.getText().toString().isEmpty()) {
            Snackbar.make(llReg, "Please Enter Email address", Snackbar.LENGTH_SHORT).show();
        } else if (!validate.validateEmail(emailET.getText().toString())) {
            Snackbar.make(llReg, "Email address is invalid", Snackbar.LENGTH_SHORT).show();
        } else if (contactET.getText().toString().isEmpty()) {
            Snackbar.make(llReg, "Please Enter Contact", Snackbar.LENGTH_SHORT).show();
        } else if (idNumET.getText().toString().isEmpty()) {
            Snackbar.make(llReg, "Please Enter identification number", Snackbar.LENGTH_SHORT).show();
        } else if (idNumET.getText().toString().length() == 9) {
            Snackbar.make(llReg, "Please Enter 9 digit identification number", Snackbar.LENGTH_SHORT).show();
        } else if (passET.getText().toString().isEmpty()) {
            Snackbar.make(llReg, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
        } else {*/
        //after reg succ - Reg API,
        // -->redirect to login,finish
        RegTask task = new RegTask();
        Log.i("TAG", "onClick: " + nameET.getText().toString() + emailET.getText().toString() + contactET.getText().toString() +
                idNumET.getText().toString() + passET.getText().toString() + photo.length());
        task.execute(nameET.getText().toString(), emailET.getText().toString(),
                contactET.getText().toString(),
                idNumET.getText().toString(),
                passET.getText().toString(), photo,
                "NA", "NA", faceId.toString(), faceUri, faceRectString);
        //}
    }

    private void afterFaceDetection() {
        if (detected) {
            createPerson();
            Log.i("TAG", "afterFaceDetection: createPerson called");
        } else {
            Snackbar.make(llReg, "Your face is not detected, click again", Snackbar.LENGTH_LONG).show();
        }
        // detected = false;
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
                    getImageUrl = ImagePath_MarshMallow.getPath(RegistrationActivity.this, fileUri);
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

}

