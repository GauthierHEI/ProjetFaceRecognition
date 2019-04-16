package com.example.projets8;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private TextView LoginTextView;
    private TextView PasswordTextView;
    private Button AuthentificationButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Administrateurs");

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.admin_activity);
        LoginTextView = findViewById(R.id.Login);
        PasswordTextView = findViewById(R.id.Password);
        AuthentificationButton = findViewById(R.id.ButtonAuthentification);

        AuthentificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String login = LoginTextView.getText().toString();
                String mdp = PasswordTextView.getText().toString();

                if (login != "" && mdp != ""){
                    ref.document("login");
                }else{
                    Toast.makeText(AdminActivity.this, "Champs mal remplis", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
