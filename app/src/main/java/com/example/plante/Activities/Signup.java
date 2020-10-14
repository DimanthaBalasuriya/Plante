package com.example.plante.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plante.Parent;
import com.example.plante.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Signup extends AppCompatActivity {
	
	private Button btn_signup;
	private EditText edt_sign_username, edt_sign_password, edt_sign_confirm_password;
	private TextView txv_alreadyuser;
	private Intent intent;
	
	private FirebaseAuth mAuth;
	
	ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		pd = new ProgressDialog(this);
		
		mAuth = FirebaseAuth.getInstance();
		
		btn_signup = findViewById(R.id.btn_signup);
		edt_sign_username = findViewById(R.id.edt_sign_username);
		edt_sign_password = findViewById(R.id.edt_sign_password);
		edt_sign_confirm_password = findViewById(R.id.edt_sign_password_con);
		txv_alreadyuser = findViewById(R.id.txv_alreadyuser);
		
		txv_alreadyuser.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(Signup.this, Login.class);
				startActivity(intent);
				finish();
			}
		});
		
		btn_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = edt_sign_username.getText().toString().trim();
				String password = edt_sign_password.getText().toString().trim();
				String conpaswword = edt_sign_confirm_password.getText().toString().trim();
				if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
					edt_sign_username.setText("Enter valid username!");
					edt_sign_username.setFocusable(true);
					edt_sign_username.setBackgroundColor(Color.RED);
				} else if (!password.equalsIgnoreCase(conpaswword)) {
					Toast.makeText(getApplicationContext(), "Password Not Match!", Toast.LENGTH_SHORT).show();
				} else if (password.length() < 8 || conpaswword.length() < 8) {
					Toast.makeText(getApplicationContext(), "Must be greater than 8 charcters!", Toast.LENGTH_SHORT).show();
				} else {
					executeRegistration(username, password);
				}
			}
		});
	}
	
	private void executeRegistration(String username, String password) {
		pd.setMessage("Registering...");
		pd.show();
		mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					FirebaseUser user = mAuth.getCurrentUser();
					String email = user.getEmail();
					String uid = user.getUid();
					
					HashMap<Object, String> hashMap = new HashMap<>();
					hashMap.put("email", email);
					hashMap.put("uid", uid);
					hashMap.put("name", "");
					hashMap.put("onlineStatus", "online");
					hashMap.put("typingTo", "noOne");
					hashMap.put("position", "");
					hashMap.put("bio", "");
					hashMap.put("image", "");
					
					FirebaseDatabase database = FirebaseDatabase.getInstance();
					DatabaseReference reference = database.getReference("User");
					reference.child(uid).setValue(hashMap);
					pd.dismiss();
					intent = new Intent(Signup.this, Parent.class);
					startActivity(intent);
					finish();
				} else {
					pd.setMessage("Password Not Match!");
					pd.show();
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				pd.setMessage("Not Signup!");
				pd.show();
			}
		});
	}
}
