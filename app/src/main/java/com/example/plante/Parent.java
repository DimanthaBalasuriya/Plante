package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.plante.Activities.Login;
import com.example.plante.Navigation.DiseaseRecognition;
import com.example.plante.Navigation.Home;
import com.example.plante.Navigation.Menu;
import com.example.plante.NotificationManagement.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Parent extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
	
	FirebaseAuth firebaseAuth;
	FirebaseUser user;
	String mUid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent);
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		loadFragment(new Home());
		BottomNavigationView navigation = findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(Parent.this);
		
//		checkUserStatus();
//		updateToken(FirebaseInstanceId.getInstance().getToken());
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Fragment fragment = null;
		
		switch (item.getItemId()) {
			case R.id.nav_home:
				fragment = new Home();
				break;
			
			case R.id.nav_disease:
				Intent intent = new Intent(Parent.this, DiseaseRecognition.class);
				startActivity(intent);
				break;
			
			case R.id.nav_menu:
				fragment = new Menu();
				break;
			
		}
		return loadFragment(fragment);
	}
	
	private boolean loadFragment(Fragment fragment) {
		if (fragment != null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, fragment)
					.commit();
			return true;
		}
		return false;
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			mUid = user.getUid();
			SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("Current_USERID", mUid);
			editor.apply();
		} else {
			startActivity(new Intent(Parent.this, Login.class));
			finish();
		}
	}
	
	public void updateToken(String token) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
		Token mToken = new Token(token);
		ref.child(mUid).setValue(mToken);
	}
	
	@Override
	protected void onStart() {
		checkUserStatus();
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		checkUserStatus();
		super.onResume();
	}
}
