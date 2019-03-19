package com.example.projets8;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.location.LocationProvider.OUT_OF_SERVICE;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager = null;
    private String fournisseur;
    private TextView latitude;
    private TextView longitude;
    private Button button;

    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location localisation) {
            Toast.makeText(MainActivity.this, fournisseur + " localisation", Toast.LENGTH_SHORT).show();

            Log.d("GPS", "localisation : " + localisation.toString());
            String coordonnees = String.format("Latitude : %f - Longitude : %f\n", localisation.getLatitude(), localisation.getLongitude());
            Log.d("GPS", coordonnees);
            String autres = String.format("Vitesse : %f - Altitude : %f - Cap : %f\n", localisation.getSpeed(), localisation.getAltitude(), localisation.getBearing());
            Log.d("GPS", autres);
            //String timestamp = String.format("Timestamp : %d\n", localisation.getTime());
            //Log.d("GPS", "timestamp : " + timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date(localisation.getTime());
            Log.d("GPS", sdf.format(date));

            String strLatitude = String.format("Latitude : %f", localisation.getLatitude());
            String strLongitude = String.format("Longitude : %f", localisation.getLongitude());
            latitude.setText(strLatitude);
            longitude.setText(strLongitude);
        }

        @Override
        public void onStatusChanged(String fournisseur, int status, Bundle extras) {
            switch(status){
                case LocationProvider
                        .AVAILABLE:
                    Toast.makeText(MainActivity.this, fournisseur + "état disponible!", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider
                        .OUT_OF_SERVICE:
                    Toast.makeText(MainActivity.this, fournisseur + "état indisponible!", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider
                        .TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(MainActivity.this, fournisseur + "état temporairement indisponible !", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, fournisseur + "état : "+ status, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, fournisseur + "activé !", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, fournisseur + "désactivé !", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        button = findViewById(R.id.ButtonTest);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Test de géolocalisation", Toast.LENGTH_LONG).show();
            }
        });

        Log.d("GPS", "OnCreate");

        initialiserLocalisation();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        arreterLocalisation();

    }

    private void arreterLocalisation() {
        if(locationManager != null){
            locationManager.removeUpdates(ecouteurGPS);
            ecouteurGPS = null;
        }

    }


    private void initialiserLocalisation() {
        if(locationManager==null){
            locationManager = (LocationManager)
                    MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();
            criteres.setAccuracy(Criteria.ACCURACY_FINE); //précision de la localisation
            criteres.setAltitudeRequired(true); //altitude actuelle
            criteres.setCostAllowed(true);
            criteres.setPowerRequirement(Criteria.POWER_HIGH); // consommation d'énergie autorisée
            fournisseur = locationManager.getBestProvider(criteres, true);

            Log.d("GPS", "fournisseur"+fournisseur);
        }

        if (fournisseur != null){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED
               &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED)
            {
                Log.d("GPS", "no permissions !");
                return;
            }

            Location localisation = locationManager.getLastKnownLocation(fournisseur);
            if(localisation != null){
                ecouteurGPS.onLocationChanged(localisation); //notification de la localisation
            }

            locationManager.requestLocationUpdates(fournisseur, 10000, 0, ecouteurGPS);
            //Mise à jour de la position automatiquement ( au moins 10m et 15s )

        }
    }
}
