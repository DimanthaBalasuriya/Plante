package com.example.plante.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.plante.RecoverPassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Login extends AppCompatActivity {
	
	private Button btn_login;
	private EditText edt_username, edt_password;
	private TextView txv_forgetpassword, txv_createnewaccount, txv_state;
	private Intent intent;
	
	private FirebaseAuth mAuth;
	
	ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		btn_login = findViewById(R.id.btn_login);
		txv_createnewaccount = findViewById(R.id.txv_createnewaccount);
		txv_forgetpassword = findViewById(R.id.txv_forgetpassword);
		edt_username = findViewById(R.id.edt_username);
		edt_password = findViewById(R.id.edt_password);
		txv_state = findViewById(R.id.txv_state);
		
		mAuth = FirebaseAuth.getInstance();
		
		pd = new ProgressDialog(this);
		
		txv_createnewaccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(Login.this, Signup.class);
				startActivity(intent);
				finish();
			}
		});
		
		txv_forgetpassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(Login.this, RecoverPassword.class);
				startActivity(intent);
				finish();
			}
		});
		
		btn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pd.setMessage("Signing in...");
				pd.show();
				String username = edt_username.getText().toString().trim();
				String password = edt_password.getText().toString().trim();
				if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
					Toast.makeText(getApplicationContext(), "Enter Valid user name!", Toast.LENGTH_SHORT).show();
					pd.dismiss();
				} else if (password.length() < 8) {
					Toast.makeText(getApplicationContext(), "Must be greater than 8 charcters!", Toast.LENGTH_SHORT).show();
					pd.dismiss();
				} else {
					executeLogin(username, password);
				}
			}
		});
		
		setUserGreet();
	}
	
	private void setUserGreet() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("a");
		if (sdf.format(new Date()).equalsIgnoreCase("AM")) {
			txv_state.setText(R.string.good_morning);
		} else if (sdf.format(new Date()).equalsIgnoreCase("PM")) {
			txv_state.setText(R.string.good_evening);
		}
	}
	
	private void executeLogin(String username, String password) {
		mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					FirebaseUser user = mAuth.getCurrentUser();
					
					if (task.getResult().getAdditionalUserInfo().isNewUser()) {
						
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
						hashMap.put("type", "member");
						hashMap.put("image", "");
						
						FirebaseDatabase database = FirebaseDatabase.getInstance();
						DatabaseReference reference = database.getReference("User");
						reference.child(uid).setValue(hashMap);
						
					}
					
					host_user_in_device(username, password);
					
					pd.dismiss();
					
					intent = new Intent(Login.this, Parent.class);
					startActivity(intent);
					finish();
					
				} else {
					Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
				}
			}
		}).addOnFailureListener(this, new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void host_user_in_device(String username, String password) {
		SharedPreferences sp = getSharedPreferences("Credential", MODE_PRIVATE);
		SharedPreferences.Editor edt = sp.edit();
		edt.putString("username", username);
		edt.putString("password", password);
		edt.commit();
	}
}
