package com.example.currentplacedetailsonmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseUser;

public class Prijava extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    EditText emailId, password;
    Button btnSignIn;
    TextView tvSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijava);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mFirebaseAuth= FirebaseAuth.getInstance();
        emailId=findViewById(R.id.email2_space);
        password=findViewById(R.id.pass2_space);
        tvSignUp=findViewById(R.id.textViewLogin);
        btnSignIn=findViewById(R.id.imageButton2);

        mAuthStateListener= new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser mFirebaseUser=mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser!=null){

                    Toast.makeText(Prijava.this, "Prijavljeni ste!",Toast.LENGTH_SHORT).show();
                    Intent i= new Intent(Prijava.this, MapsActivity.class);
                    startActivity(i);

                }
                else {

                    Toast.makeText(Prijava.this, "Molimo Vas, prijavite se!",Toast.LENGTH_SHORT).show();

                }
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
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

                    Toast.makeText(Prijava.this,"Polja su nepopunjena, ispunite ih prije nastavka", Toast.LENGTH_SHORT).show();
                }

                else if(!(email.isEmpty() && pwd.isEmpty())){

                    mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(Prijava.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(!task.isSuccessful()){

                                Toast.makeText(Prijava.this,"Pojavila se pogreška, ponovite prijavu", Toast.LENGTH_SHORT).show();}

                                else{
                                Intent intToHome= new Intent(Prijava.this, MapsActivity.class);
                                startActivity(intToHome);}


                        }
                    });}

                else{
                    Toast.makeText(Prijava.this,"Pojavila se pogreška", Toast.LENGTH_SHORT).show();

                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intoLogin= new Intent(Prijava.this, Registracija.class);
                startActivity(intoLogin);
            }
        });



            }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }
}