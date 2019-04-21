package com.example.projets8;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    String TAG = "MonTag";
    String ACTIVITY_TAG = "MainActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("PositionsPorte");
    private static final String COMMA_DELIMITER = ",";
    JSONArray jsonArray = new JSONArray();
    private FirebaseAuth mAuth;

    private double latitudeTelphone;
    private double longitudeTelephone;
    private int nombrePortes;
    private ArrayList<Porte> portes;
    private ArrayList<Double> distanceToPortes;
    private Integer porteProche = null;

    LocationManager locationManager = null;
    private String fournisseur;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    Button submit;
    Button test;
    private ArrayList<TextView> distancePortesTextViews;

    SharedPreferences sharedPreferences;

    LocationListener ecouteurGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location localisation) {
            Log.d(ACTIVITY_TAG,"On location changed");

            //Toast.makeText(MainActivity.this, fournisseur + " localisation", Toast.LENGTH_SHORT).show();

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

            if (portes.size()!=0) {
                distancesPorte(localisation);

                porteProche = choixPorte();

                if (distanceToPortes.get(porteProche) < 20){
                    for (int i=0; i< nombrePortes; i++) {
                        distancePortesTextViews.get(i).setTextColor(Color.parseColor("black"));
                    }
                    distancePortesTextViews.get(porteProche).setTextColor(Color.parseColor("green"));
                }
                else {
                    for (int i=0; i< nombrePortes; i++) {
                        distancePortesTextViews.get(i).setTextColor(Color.parseColor("black"));
                    }
                    distancePortesTextViews.get(porteProche).setTextColor(Color.parseColor("red"));
                }
            }
            else {
                Toast.makeText(MainActivity.this, "Veuillez patienter, nous récupérons votre localisation...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStatusChanged(String fournisseur, int status, Bundle extras) {
            switch(status){
                case LocationProvider
                        .AVAILABLE:
                    //Toast.makeText(MainActivity.this, fournisseur + "état disponible!", Toast.LENGTH_SHORT).show();
                    Log.d("GPS", fournisseur + " état disponible!");
                    break;
                case LocationProvider
                        .OUT_OF_SERVICE:
                    //Toast.makeText(MainActivity.this, fournisseur + "état indisponible!", Toast.LENGTH_SHORT).show();
                    Log.d("GPS", fournisseur + " état indisponible!");
                    break;
                case LocationProvider
                        .TEMPORARILY_UNAVAILABLE:
                    //Toast.makeText(MainActivity.this, fournisseur + "état temporairement indisponible !", Toast.LENGTH_SHORT).show();
                    Log.d("GPS", fournisseur + " état temporairement indisponible!");
                    break;

                default:
                    //Toast.makeText(MainActivity.this, fournisseur + "état : "+ status, Toast.LENGTH_SHORT).show();
                    Log.d("GPS", fournisseur + "état : "+ status);
            }

        }

        @Override
        public void onProviderEnabled(String provider) {
            //Toast.makeText(MainActivity.this, fournisseur + "activé !", Toast.LENGTH_SHORT).show();
            Log.d("GPS", fournisseur+ " activé !");

        }

        @Override
        public void onProviderDisabled(String provider) {
            //Toast.makeText(MainActivity.this, fournisseur + " désactivé !", Toast.LENGTH_SHORT).show();
            Log.d("GPS", fournisseur+ " désactivé !");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(ACTIVITY_TAG, "OnCreate");
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        final EditText editMatricule = findViewById(R.id.matricule);
        final TextView result = findViewById(R.id.tvResult);

        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String matricule = editMatricule.getText().toString();
                sharedPreferences
                        .edit()
                        .putString("matricule", matricule)
                        .apply();
                Toast.makeText(MainActivity.this, "Sauvegardé !", Toast.LENGTH_SHORT).show();

                String savedMatricule = sharedPreferences.getString("matricule", null);
                editMatricule.setText(savedMatricule);

                // CHANGE ACTIVITY
                if (porteProche==null) {
                    for (int i=0; i< nombrePortes; i++) {
                        distancePortesTextViews.get(i).setTextColor(Color.parseColor("black"));
                    }
                    Toast.makeText(MainActivity.this, "Impossible de récupérer la position des portes", Toast.LENGTH_LONG).show();
                }
                else if( distanceToPortes.get(porteProche) < 2000){

                    distancePortesTextViews.get(porteProche).setTextColor(Color.parseColor("green"));
                    Toast.makeText(MainActivity.this, "Vous êtes proche de : " + portes.get(porteProche).getName(), Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(MainActivity.this, FaceRecon.class);
                    Bundle porteBundle = new Bundle();
                    porteBundle.putInt("intPorte", porteProche);
                    porteBundle.putString("nomPorte", portes.get(porteProche).getName());
                    i.putExtras(porteBundle);

                    startActivity(i);
                }
                else {
                    for (int i=0; i< nombrePortes; i++) {
                        distancePortesTextViews.get(i).setTextColor(Color.parseColor("black"));
                    }
                    distancePortesTextViews.get(porteProche).setTextColor(Color.parseColor("red"));
                    Toast.makeText(MainActivity.this, "Raprochez-vous de : "+portes.get(porteProche).getName()+", vous êtes trop loin", Toast.LENGTH_LONG).show();
                }
            }
        });

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            portes = new ArrayList<Porte>();
            portes = extras.getParcelableArrayList("listePortes");
        }
        else {
            Toast.makeText(MainActivity.this, "Impossible de récupérer la position des portes", Toast.LENGTH_LONG).show();
        }

        /*portes.add(new Porte("Porte d'entree"));
        portes.add(new Porte("Porte de derriere"));
        portes.add(new Porte("Salle de classe"));

        portes.get(0).setLatitude(50.633769d);
        portes.get(0).setLongitude(3.045075d);
        portes.get(1).setLatitude(50.633297d);
        portes.get(1).setLongitude(3.045993d);
        portes.get(2).setLatitude(50.634005d);
        portes.get(2).setLongitude(3.045535d);*/

        Log.d("Portes", portes.toString());

        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);

        test = findViewById(R.id.ButtonTest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ADD CODE HERE

            }
        });

        distancePortesTextViews = new ArrayList<TextView>();

        distancePortesTextViews.add((TextView) findViewById(R.id.distancePorteEntree));
        distancePortesTextViews.add((TextView) findViewById(R.id.distancePorteDerriere));
        distancePortesTextViews.add((TextView) findViewById(R.id.distanceSalleClasse));

        distanceToPortes = new ArrayList<Double>();

        initialiserLocalisation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final EditText editMatricule = findViewById(R.id.matricule);
        String savedMatricule = sharedPreferences.getString("matricule", null);
        editMatricule.setText(savedMatricule);

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
        Log.d(ACTIVITY_TAG, "On initialiserLocalisation");

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

            locationManager.requestLocationUpdates(fournisseur, 2000, 0, ecouteurGPS);
            //Mise à jour de la position automatiquement ( au moins 10s et 0m )

        }
    }

    private void distancesPorte(Location location) {
        // A noter que les portes sont dans l'ordre suivant :
        // 0- Porte d'entre
        // 1- Porte de derriere
        // 2- Salle de classe <-> Test

        distanceToPortes = new ArrayList<>();

        for (int i =0; i<portes.size(); i++) {
            Location locationi = new Location(fournisseur);
            locationi.setLatitude(portes.get(i).getLocation().getLatitude());
            locationi.setLongitude(portes.get(i).getLocation().getLongitude());

            distanceToPortes.add((double) location.distanceTo(locationi));
        }

        distancePortesTextViews.get(0).setText(distanceToPortes.get(0).toString());
        distancePortesTextViews.get(1).setText(distanceToPortes.get(1).toString());
        distancePortesTextViews.get(2).setText(distanceToPortes.get(2).toString());
    }

    private int choixPorte() {
        int porte;
        porte = distanceToPortes.indexOf(Collections.min(distanceToPortes));
        return porte;
    }

}
