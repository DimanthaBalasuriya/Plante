package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plante.Activities.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
	
	private SharedPreferences sp;
	private FirebaseAuth mAuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		mAuth = FirebaseAuth.getInstance();
		
		next_activity_loader();
		
	}
	
	private void next_activity_loader() {
		try {
			Thread.sleep(50);
			for (int i = 0; i <= 100; i++) {
				if (i == 100) {
					SharedPreferences sp = getSharedPreferences("Credential", MODE_PRIVATE);
					String username = sp.getString("username", "null");
					String password = sp.getString("password", "null");
					
					if (username.equalsIgnoreCase("null") || password.equalsIgnoreCase("null")) {
						Intent intent = new Intent(SplashScreen.this, Login.class);
						startActivity(intent);
					} else {
						mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
							@Override
							public void onComplete(@NonNull Task<AuthResult> task) {
								if (task.isSuccessful()) {
									Intent intent = new Intent(SplashScreen.this, Parent.class);
									startActivity(intent);
								} else {
									Toast.makeText(getApplicationContext(), "Error With Credentials!", Toast.LENGTH_LONG).show();
								}
							}
						});
					}
				} else {
					//Show interuption message
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}