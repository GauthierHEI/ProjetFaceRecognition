package com.example.projets8;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AdminActivity extends AppCompatActivity{

    private TextView LoginTextView;
    private TextView PasswordTextView;
    private Button AuthentificationButton;

    @Override
    public void OnCreate(Bundle savedInstanceState){

        super.onCreate( savedInstanceState);

        setContentView(R.layout.activity_admin);
        LoginTextView = findViewById(R.id.login);
        PasswordTextView = findViewById(R.id.password);
        AuthentificationButton = findViewById(R.id.ButtonLogin);

        AuthentificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AdminActivity.this, "Authentification à faire", Toast.LENGTH_LONG).show();
            }
        });

    }
}
