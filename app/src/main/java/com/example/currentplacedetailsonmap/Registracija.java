package com.example.currentplacedetailsonmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registracija extends AppCompatActivity {


    EditText emailId, password;
    Button btnSignUp;
    FirebaseAuth mFirebaseAuth;
    TextView tvSignIn;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registracija);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mFirebaseAuth= FirebaseAuth.getInstance();
        emailId=findViewById(R.id.email_space);
        password=findViewById(R.id.pass_space);
        tvSignIn=findViewById(R.id.textView1);
        btnSignUp=findViewById(R.id.imageButton1);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= emailId.getText().toString();
                String pwd= password.getText().toString();

                if (email.isEmpty()){

                    emailId.setError("Molimo Vas unesite e-mail adresu");
                    emailId.requestFocus();
                }

                else if(pwd.isEmpty()){

                    password.setError("Molimo Vas unesite lozinku");
                    password.requestFocus();
                }

                else if(email.isEmpty() && pwd.isEmpty()){

                    Toast.makeText(Registracija.this,"Polja su nepopunjena, ispunite ih prije nastavka", Toast.LENGTH_SHORT).show();
                }

                else if(!(email.isEmpty() && pwd.isEmpty())){

                    mFirebaseAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(Registracija.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful()){

                                Toast.makeText(Registracija.this,"Neuspješan pokušaj! Molimo Vas ponovite registraciju.", Toast.LENGTH_SHORT).show();
                            }

                            else{

                                startActivity(new Intent(Registracija.this, MapsActivity.class));
                            }
                        }
                    });
                }

                else{
                    Toast.makeText(Registracija.this,"Pojavila se pogreška", Toast.LENGTH_SHORT).show();

                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Registracija.this, Prijava.class);
                startActivity(i);
            }
        });
    }
}
