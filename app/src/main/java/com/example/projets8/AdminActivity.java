package com.example.projets8;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    private TextView LoginTextView;
    private TextView PasswordTextView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference ref = db.collection("Administrateurs");

    public void GoingToAdminAdd(){
        Intent i = new Intent(AdminActivity.this, AdminAddImageActivity.class);
        startActivity(i);
    }

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.admin_activity);
        LoginTextView = findViewById(R.id.Login);
        PasswordTextView = findViewById(R.id.Password);
        Button authentificationButton = findViewById(R.id.ButtonAuthentification);

        authentificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String login = LoginTextView.getText().toString();
                final String mdp = PasswordTextView.getText().toString();

                if (!login.equals("") && !mdp.equals("")) {
                    ref.document(login).get() 
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull  Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().get("mdp").equals(mdp)) {
                                                GoingToAdminAdd();
                                            } else {
                                                Toast.makeText(AdminActivity.this, "Erreur d'authentification, vérifiez vos informations !", Toast.LENGTH_LONG).show();
                                            }
                                        } else{
                                            Toast.makeText(AdminActivity.this, "Erreur d'authentification, vérifiez vos informations !", Toast.LENGTH_LONG).show();
                                            try {
                                                throw task.getException();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                }
                            });
                }
                else{
                    Toast.makeText(AdminActivity.this, "Champs mal remplis", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
