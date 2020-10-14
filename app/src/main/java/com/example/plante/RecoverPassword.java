package com.example.plante;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plante.Activities.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPassword extends AppCompatActivity {
	
	private Button btn_rec_recover;
	private EditText edt_rec_username;
	private TextView txv_remembered;
	
	private FirebaseAuth mAuth;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recover_password);
		
		btn_rec_recover = findViewById(R.id.btn_rec_recover);
		txv_remembered = findViewById(R.id.txv_remembered);
		edt_rec_username = findViewById(R.id.edt_rec_username);
		
		mAuth = FirebaseAuth.getInstance();
		
		txv_remembered.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(RecoverPassword.this, Login.class);
				startActivity(intent);
				finish();
			}
		});
		
		btn_rec_recover.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = edt_rec_username.getText().toString().trim();
				
				if(!Patterns.EMAIL_ADDRESS.matcher(username).matches()){
					edt_rec_username.setError("Enter Valid username");
					edt_rec_username.setBackgroundColor(Color.RED);
					edt_rec_username.setFocusable(true);
				}else{
					executePasswordRecover(username);
				}
			}
		});
		
	}
	
	private void executePasswordRecover(String username) {
		mAuth.sendPasswordResetEmail(username).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if(task.isSuccessful()){
					Toast.makeText(getApplicationContext(), "Recover mail sent! \n Please check your email...", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "System Failure! \n Please try again later...", Toast.LENGTH_SHORT).show();
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "System Failure!" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}
