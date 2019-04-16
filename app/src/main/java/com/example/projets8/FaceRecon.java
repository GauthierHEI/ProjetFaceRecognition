package com.example.projets8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FaceRecon extends AppCompatActivity {

    private static final String TAG = "FaceTrackerDemo";
    private CameraSource mCameraSource = null;
    private CameraSurfacePreview mPreview;
    private CameraOverlay cameraOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private int bundlePorte;
    private DatabaseReference mDatabase;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        //RealTimeDatabase
        mDatabase= FirebaseDatabase.getInstance().getReference("porte");
        Intent i=getIntent();
        Bundle bundle=getIntent().getExtras();
        bundlePorte=bundle.getInt("intPorte");


        setContentView(R.layout.activity_main);
        mPreview = (CameraSurfacePreview) findViewById(R.id.preview);
        cameraOverlay = (CameraOverlay) findViewById(R.id.faceOverlay);
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        int rw = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rw == PackageManager.PERMISSION_GRANTED) {
            Log.d("Pierre","Already granted");
        } else {
            Log.d("Pierre","Need to request");
            requestWritePermission();
        }
    }
    private void changeOuverturePorte(){
        if (bundlePorte==0){
            mDatabase.child("porte1").setValue(true);
        }
        if(bundlePorte==1){
            mDatabase.child("porte2").setValue(true);
        }
        if(bundlePorte==-1){
            mDatabase.child("porte1").setValue(false);
            mDatabase.child("porte2").setValue(false);
            mDatabase.child("porte3").setValue(false);
        }
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(cameraOverlay, "Camera permission is required",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();

    }

    private void requestWritePermission() {

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d("Pierre","Requesting");
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_WRITE_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_WRITE_PERM);
            }
        };

        Snackbar.make(cameraOverlay, "Write permission is required",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();

    }


    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        detector.setProcessor(
                new MultiProcessor.Builder<>(new FaceRecon.GraphicFaceTrackerFactory())
                        .build());
        if (!detector.isOperational()) {
            Log.e(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    @Override

    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override

    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM && requestCode != RC_HANDLE_WRITE_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  ) {
            if(requestCode == RC_HANDLE_CAMERA_PERM){
                Log.d(TAG, "Camera permission granted - initialize the camera source");
                createCameraSource();
            }else if(requestCode == RC_HANDLE_WRITE_PERM){
                Log.d(TAG, "Write permission granted");
            }
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FaceTrackerDemo")
                .setMessage("Need all permissions")
                .setPositiveButton("OK", listener)
                .show();

    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();

        }
        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, cameraOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new FaceRecon.GraphicFaceTracker(cameraOverlay);
        }
    }

    class faceRecon extends TimerTask {
        public void run() {
            mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        /*Log.d("Pierre", bmp.getWidth() + "x" + bmp.getHeight());
                        try {
                            filecon[0] = new FileOutputStream(file);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 90, filecon[0]);
                            Log.d("Pierre","fotoCreated");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.d("Pierre",e.getMessage());
                        }
                        if(filecon[0] !=null) {
                            try {
                                filecon[0].close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("Pierre",e.getMessage());
                            }
                        }*/


                    RequestQueue queue = Volley.newRequestQueue(FaceRecon.this);
                    String url = "http://www.facexapi.com/compare_faces?face_det=1";
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    Log.d("Pierre", response);
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Pierre", error.getMessage());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Log.d("Pierre", "get Params");
                            Map<String, String>  params = new HashMap<>();
                            params.put("img_1", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS7rxyUdhH_jvgUXGDcsb_KP5Si4uBHmRD5M39h2pTAiPEcB27v5w");
                            params.put("img_2", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS7rxyUdhH_jvgUXGDcsb_KP5Si4uBHmRD5M39h2pTAiPEcB27v5w");
                            return params;
                        }

                        /** Passing some request headers* */
                        @Override
                        public Map getHeaders() {
                            Log.d("Pierre", "get Header");
                            HashMap headers = new HashMap();
                            headers.put("user_id", "3cd031bea84b5097c384");
                            headers.put("user_key", "085fbb614106a1b414c5");
                            return headers;
                        }
                    };
                    queue.add(postRequest);
                }
            });
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private CameraOverlay mOverlay;
        private FaceOverlayGraphics faceOverlayGraphics;
        GraphicFaceTracker(CameraOverlay overlay) {
            mOverlay = overlay;
            faceOverlayGraphics = new FaceOverlayGraphics(overlay);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            faceOverlayGraphics.setId(faceId);
            Log.d("Pierre","faceDetected");
            Timer t = new Timer();
            faceRecon Task = new faceRecon();
            t.schedule(Task, 3000L);
        }


        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(faceOverlayGraphics);
            faceOverlayGraphics.updateFace(face);
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(faceOverlayGraphics);
        }

        @Override

        public void onDone() {
            mOverlay.remove(faceOverlayGraphics);
        }
    }

}

