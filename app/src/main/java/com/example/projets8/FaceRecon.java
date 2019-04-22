package com.example.projets8;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FaceRecon extends AppCompatActivity {

    private static final String TAG = "FaceTrackerDemo";
    private StorageReference mStorageRef;
    private CameraSource mCameraSource = null;
    private CameraSurfacePreview mPreview;
    private CameraOverlay cameraOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_PERM = 3;
    private int bundlePorte;
    private DatabaseReference mDatabase;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        //RealTimeDatabase
        mDatabase= FirebaseDatabase.getInstance().getReference("porte");
        Intent i=getIntent();
        Bundle bundle=getIntent().getExtras();
        bundlePorte=bundle.getInt("intPorte");


        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setContentView(R.layout.activity_facerecon);
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
                    // Create a reference to 'temp/photo.jpg'
                    final StorageReference mountainImagesRef = mStorageRef.child("temp/photo.jpg");

                    InputStream myInputStream = new ByteArrayInputStream(bytes);

                    final UploadTask uploadTask = mountainImagesRef.putStream(myInputStream);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful upload
                        }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("Pierre","fotoCreated");
                            checkImages(uploadTask, mountainImagesRef);
                        }
                    });

                }
            });
        }
    }

    class changeAct extends TimerTask {
        public void run() {
            FaceRecon.this.finish();
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
            t.schedule(Task, 1500L);
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


    public void deleteImage(StorageReference photoRef) {
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(TAG, "onFailure: did not delete file");
            }
        });
    }

    public void checkImages(UploadTask uploadTask, final StorageReference mountainImagesRef) {

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return mountainImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    final String image_url = downloadUri.toString();
                    Log.d("Pierre",image_url);
                    final SharedPreferences sharedPreferences;
                    sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

                    Log.d("Pierre", sharedPreferences.getString("matricule", null));

                    String savedMatricule = sharedPreferences.getString("matricule", null);


                    DocumentReference docRef = db.collection("Target").document(savedMatricule);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("Pierre", "DocumentSnapshot data: " + document.getString("visage"));
                                    final String img_check = document.getString("visage");
                                    Log.d("Pierre", img_check);

                                    RequestQueue queue = Volley.newRequestQueue(FaceRecon.this);

                                    String url = "http://www.facexapi.com/compare_faces?face_det=1";
                                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>()
                                            {
                                                @Override
                                                public void onResponse(String response) {
                                                    // response
                                                    Log.d("Pierre", response);
                                                    deleteImage(mountainImagesRef);
                                                    Log.d("Pierre", response.substring(15, 22));
                                                    float tx = Float.parseFloat(response.substring(15, 22));
                                                    responseHandler(tx);
                                                }
                                            },
                                            new Response.ErrorListener()
                                            {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    // error
                                                    Log.d("Pierre", error.toString());
                                                    deleteImage(mountainImagesRef);
                                                }
                                            }
                                    ) {
                                        @Override
                                        protected Map<String, String> getParams()
                                        {

                                            String savedMatricule = sharedPreferences.getString("matricule", null);
                                            DocumentReference docRef = db.collection("Target").document(savedMatricule);
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });


                                            Log.d("Pierre", "get Params");
                                            Map<String, String>  params = new HashMap<>();
                                            params.put("img_1", image_url);

                                            params.put("img_2", img_check);
                                            return params;
                                        }

                                        // Passing some request headers
                                        @Override
                                        public Map getHeaders() {
                                            Log.d("Pierre", "get Header");
                                            HashMap headers = new HashMap();
                                            headers.put("user_id", "3cd031bea84b5097c384");
                                            headers.put("user_key", "085fbb614106a1b414c5");
                                            return headers;
                                        }
                                    };

                                    postRequest.setRetryPolicy(new DefaultRetryPolicy(
                                            30000,
                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(postRequest);

                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

                } else {
                    Log.d("Pierre", "Failed");
                }
            }
        });
    }

    private void changeOuverturePorte(){
        if(bundlePorte==0){
            mDatabase.child("porte1").setValue(true);
            mDatabase.child("porte2").setValue(false);
            mDatabase.child("porte3").setValue(false);
        }
        if(bundlePorte==1){
            mDatabase.child("porte1").setValue(false);
            mDatabase.child("porte2").setValue(true);
            mDatabase.child("porte3").setValue(false);
        }
        if(bundlePorte==-1){
            mDatabase.child("porte1").setValue(false);
            mDatabase.child("porte2").setValue(false);
            mDatabase.child("porte3").setValue(false);
        }
    }

    //Manipule le taux de confiance renvoyé par l'API
    private void responseHandler(float tx) {
        if (tx > 0.5) {
            Toast.makeText(FaceRecon.this, "Bienvenue!", Toast.LENGTH_SHORT).show();
            changeOuverturePorte();
            Timer e = new Timer();
            changeAct Task = new changeAct();
            e.schedule(Task, 1500L);
        }
        else {
            Toast.makeText(FaceRecon.this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            Timer e = new Timer();
            changeAct Task = new changeAct();
            e.schedule(Task, 1500L);
        }
    }



}

