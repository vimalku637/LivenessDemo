package com.ctl.clientlivenessdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ctl.clientlivenessdemo.rest_apis.APIClient;
import com.ctl.clientlivenessdemo.rest_apis.ApisInterface;
import com.ctl.clientlivenessdemo.rest_apis.DialogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdk.karzalivness.KLivenessView;
import com.sdk.karzalivness.enums.CameraFacing;
import com.sdk.karzalivness.enums.FaceStatus;
import com.sdk.karzalivness.enums.FaceTypeStatus;
import com.sdk.karzalivness.enums.KEnvironment;
import com.sdk.karzalivness.enums.KLiveStatus;
import com.sdk.karzalivness.interfaces.KLivenessCallbacks;
import com.sdk.karzalivness.models.KLiveResult;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements KLivenessCallbacks {
    private static final String TAG = "MainActivity>>";
    KLivenessView kLivenessView;
    TextView tvHelpUsVerifyItsYou;
    private String karzaToken = "";
    private String imageBase64String = "";
    private double livenessScore = 0.0;
    //CircleImageView ivSelfie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callGetTokenApis();
            }
        }, 1000);

        findViewById(R.id.btnOpenCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCameraPermissionGranted()) {
                    requestCameraPermission();
                }
                //ivSelfie.setVisibility(View.GONE);
                //PRODUCTION=>xnPGTjjjtQESKAG
                //TESTING=>ia5r4fD8zTID2QH8rot5
                kLivenessView.initialize(getSupportFragmentManager(), MainActivity.this,
                        karzaToken, KEnvironment.TEST, CameraFacing.FRONT);
            }
        });
    }

    private void callGetTokenApis() {
        DialogUtils.showDialog(this);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("productId", "liveness");

        Gson gson = new Gson();
        String data = gson.toJson(jsonObject);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, data);

        APIClient.getClient().create(ApisInterface.class)
                .getTokenForSilentLivenessSDK(body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {
                            JSONObject jsonObjectRes = new JSONObject(String.valueOf(response.body()));
                            Log.e("TAG>>>", "onResponse: " + jsonObjectRes);
                            DialogUtils.dismissDialog();
                            if (jsonObjectRes != null) {
                                String statusCode = jsonObjectRes.getString("statusCode");
                                if (statusCode!=null) {
                                    JSONObject jsonObjectResult = jsonObjectRes.getJSONObject("result");
                                    JSONObject jsonObjectData = jsonObjectResult.getJSONObject("data");
                                    karzaToken = jsonObjectData.getString("karzaToken");
                                    Log.e("TAG>>>", "Token: " + karzaToken);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        DialogUtils.dismissDialog();
                        Toast.makeText(MainActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void initViews() {
        kLivenessView = new KLivenessView(this);
        tvHelpUsVerifyItsYou = findViewById(R.id.tvHelpUsVerifyItsYou);
        //ivSelfie=findViewById(R.id.ivSelfie);
    }

    @Override
    public void needPermissions(@NonNull String... strings) {
        // ask for required permission
        Log.e("TAG>>>", "needPermissions: ");
    }

    @Override
    public void showLoader() {
        // show loader
        Log.e("TAG>>>", "showLoader: ");
        DialogUtils.showDialog(this);
    }

    @Override
    public void hideLoader() {
        // hide loader
        Log.e("TAG>>>", "hideLoader: ");
        DialogUtils.dismissDialog();
    }

    @Override
    public void onReceiveKLiveResult(KLiveStatus status, @Nullable KLiveResult kLiveResult) {
        if (status == KLiveStatus.SUCCESS) {
            // handle success
            Toast.makeText(this, "Liveness check successful.", Toast.LENGTH_SHORT).show();
            tvHelpUsVerifyItsYou.setText("Success");
            livenessScore=kLiveResult.getLivenessScore();
            Log.e("liveness>>>", "livenessScore: "+livenessScore);
            imageBase64String=kLiveResult.getImageB64String();
            Log.e("Imagess>>>", "imageBase64String: "+imageBase64String);
            /*if (imageBase64String!=null){
                byte[] decodedString = Base64.decode(imageBase64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Log.e(TAG, "onReceiveKLiveResult: "+bitmap);
                ivSelfie.setVisibility(View.VISIBLE);
                ivSelfie.setImageBitmap(bitmap);
            }*/
        }
        else if (status == KLiveStatus.CAMERA_TIMEOUT) {
            Toast.makeText(this, "Camera got timed-out (based on time-out value given in view or else default timeout).", Toast.LENGTH_SHORT).show();
        }
        else if (status == KLiveStatus.INSUFFICIENT_CREDITS) {
            Toast.makeText(this, "Credits provided by Karza exhausted.", Toast.LENGTH_SHORT).show();
        }
        else if (status == KLiveStatus.INTERNAL_SERVER_ERROR) {
            Toast.makeText(this, "Internal error on Karza Server.", Toast.LENGTH_SHORT).show();
        }
        else if (status == KLiveStatus.UNAUTHORIZED) {
            Toast.makeText(this, "Karza token is not valid.", Toast.LENGTH_SHORT).show();
        }
        else if (status == KLiveStatus.REQUEST_TIMED_OUT) {
            Toast.makeText(this, "Request time out.", Toast.LENGTH_SHORT).show();
        }
        else {
            //handle failure
            Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
            tvHelpUsVerifyItsYou.setText("Failed");
        }
    }

    @Override
    public void faceStatus(FaceStatus faceStatus, @Nullable FaceTypeStatus faceTypeStatus) {
        Log.e("TAG>>>", "faceStatus: " + faceStatus);
        switch (faceStatus) {
            case VALID_FACE:
                // do valid face logic
                tvHelpUsVerifyItsYou.setText("Hold still...");
                break;
            case NO_FACE:
                // do no face logic
                break;
            case INVALID_FACE:
                // do invalid face logic, which also has the type reason in faceTypeStatus
                if (faceTypeStatus != null) {
                    switch (faceTypeStatus) {
                        case LOW_BRIGHTNESS:
                            // do low brightness face logic
                            Toast.makeText(this, "Low brightness conditions. Please move to a well-lit environment.", Toast.LENGTH_SHORT).show();
                            break;
                        case HIGH_BRIGHTNESS:
                            // do high brightness face logic
                            Toast.makeText(this, "High brightness conditions. Please move to an area with more shade.", Toast.LENGTH_SHORT).show();
                            break;
                        case TOO_FAR:
                            // do too far face logic
                            Toast.makeText(this, "Your face is too far.", Toast.LENGTH_SHORT).show();
                            break;
                        case TOO_CLOSE:
                            // do too close face logic
                            Toast.makeText(this, "Your face is too close.", Toast.LENGTH_SHORT).show();
                            break;
                        case LIES_OUTSIDE:
                            // do lies outside face logic
                            Toast.makeText(this, "Face lies outside the frame.", Toast.LENGTH_SHORT).show();
                            break;
                        case MULTIPLE_FACES:
                            // do multiple faces logic
                            Toast.makeText(this, "Multiple Faces Detected.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
        }
    }

    @Override
    public void onError(String s) {
        Log.e("TAG>>>", "onError: " + s);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
        Log.e("TAG>>>", "onPointerCaptureChanged: " + hasCapture);
    }

    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
    }

    public boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.v("permission", "Camera permission is granted");
                return true;
            } else {
                Log.v("permission", "Camera permission is revoked");
                return false;
            }
        } else {
            Log.v("permission", "Camera permission is granted");
            return true;
        }
    }
}