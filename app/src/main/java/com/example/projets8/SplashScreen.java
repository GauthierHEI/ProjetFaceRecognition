package com.example.projets8;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;

import java.util.ArrayList;

public class SplashScreen extends Activity {

    String TAG = "MonTag";
    String ACTIVITY_TAG = "SplashScreen";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("PositionsPorte");
    private static final String COMMA_DELIMITER = ",";
    JSONArray jsonArray = new JSONArray();
    private FirebaseAuth mAuth;

    String fournisseur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(ACTIVITY_TAG, "OnCreate");

        setContentView(R.layout.splash);

        fournisseur = "";

        final ArrayList<Porte> portes = new ArrayList<Porte>();

        ref= db.collection("PositionsPorte");
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot document : task.getResult()) {

                    Log.d("Portes", document.getString("nom"));

                    Double latitude = document.getDouble("latitude");
                    Double longitude = document.getDouble("longitude");
                    GeoPoint point = document.getGeoPoint("location");

                    Porte porte = new Porte(document.getString("nom"), new Location(fournisseur));
                    porte.getLocation().setLatitude(point.getLatitude());
                    porte.getLocation().setLongitude(point.getLongitude());

                    portes.add(porte);
                }
                Log.d(ACTIVITY_TAG+"Portes", portes.toString());
            }
        });


        Log.d("Portes",portes.toString());

        final Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        Bundle porteBundle = new Bundle();

        porteBundle.putParcelableArrayList("listePortes", portes);
        intent.putExtras(porteBundle);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }



    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }



}