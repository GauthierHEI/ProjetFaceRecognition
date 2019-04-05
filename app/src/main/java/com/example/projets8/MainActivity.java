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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.location.LocationProvider.OUT_OF_SERVICE;

public class MainActivity extends AppCompatActivity {

    String TAG = "GPS";

    private double latitudeTelphone;
    private double longitudeTelephone;
    private ArrayList<Porte> portes;
    private ArrayList<Double> distanceToPortes;

    LocationManager locationManager = null;
    private String fournisseur;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private Button Test;
    private Button Submit;
    private TextView distancePorteEntreeTextView;
    private TextView distancePorteDerriereTextView;
    private TextView distanceSalleClasseTextView;

    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location localisation) {
            Log.d(TAG,"On location changed");

            Toast.makeText(MainActivity.this, fournisseur + " localisation", Toast.LENGTH_SHORT).show();

            Log.d("GPS", "localisation : " + localisation.toString());

            latitudeTelphone = localisation.getLatitude();
            longitudeTelephone = localisation.getLongitude();
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
            latitudeTextView.setText(strLatitude);
            longitudeTextView.setText(strLongitude);
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

        final EditText editMatricule =  findViewById(R.id.matricule);
        final TextView result = findViewById(R.id.tvResult);
        Submit = findViewById(R.id.submit);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matricule = editMatricule.getText().toString();
                if (matricule.equals("")) {
                    result.setText("Veuillez entrer un matricule valide");
                }
                else {
                    result.setText("Matricule:\t" + matricule);
                }
            }
        });

        portes = new ArrayList<Porte>();
        portes.add(new Porte("porteEntree", 50.633792d,3.044914d));
        portes.add(new Porte("porteDerriere", 50.633187d,3.046571d));
        portes.add(new Porte("salleDeClasse", 50.631919d, 3.041823d));

        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        Test = findViewById(R.id.ButtonTest);

        Test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Test de géolocalisation", Toast.LENGTH_LONG).show();
            }
        });

        Log.d(TAG, "OnCreate");

        initialiserLocalisation();

        distancePorteEntreeTextView = findViewById(R.id.distancePorteEntree);
        distancePorteDerriereTextView = findViewById(R.id.distancePorteDerriere);
        distanceSalleClasseTextView = findViewById(R.id.distanceSalleClasse);

        distanceToPortes = new ArrayList<Double>();
        distancesPorte();

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
        Log.d(TAG, "On initialiserLocalisation");

        if(locationManager==null){
            locationManager = (LocationManager)
                    MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();
            criteres.setAccuracy(Criteria.ACCURACY_FINE); //précision de la localisation
            criteres.setAltitudeRequired(true); //altitude actuelle
            criteres.setCostAllowed(true);
            criteres.setPowerRequirement(Criteria.POWER_HIGH); // consommation d'énergie autorisée
            fournisseur = locationManager.getBestProvider(criteres, true);

            Log.d("GPS", "fournisseur "+fournisseur);
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

    private void distancesPorte() {
        // A noter que les portes sont dans l'ordre suivant :
        // 0- Porte d'entre
        // 1- Porte de derriere
        // 2- Salle de classe <-> Test

        Location phonePosition = new Location("position Telephone");
        phonePosition.setLatitude(latitudeTelphone);
        phonePosition.setLongitude(longitudeTelephone);

        Location frontDoorPosition = new Location("position porte entree");
        frontDoorPosition.setLatitude(portes.get(0).getLatitude());
        frontDoorPosition.setLongitude(portes.get(0).getLongitude());

        Location backDoorPosition = new Location("position porte derriere");
        backDoorPosition.setLatitude(portes.get(1).getLatitude());
        backDoorPosition.setLongitude(portes.get(1).getLongitude());

        Location classroomPosition = new Location("position salle de classe");
        classroomPosition.setLatitude(portes.get(2).getLatitude());
        classroomPosition.setLongitude(portes.get(2).getLongitude());

        distanceToPortes.add((double) phonePosition.distanceTo(frontDoorPosition));
        distanceToPortes.add((double) phonePosition.distanceTo(backDoorPosition));
        distanceToPortes.add((double) phonePosition.distanceTo(classroomPosition));

        distancePorteEntreeTextView.setText(distanceToPortes.get(0).toString());
        distancePorteDerriereTextView.setText(distanceToPortes.get(1).toString());
        distanceSalleClasseTextView.setText(distanceToPortes.get(2).toString());
    }

}
