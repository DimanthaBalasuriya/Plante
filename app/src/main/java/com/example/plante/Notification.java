package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterNotification;
import com.example.plante.Base_module.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Notification extends AppCompatActivity {
	
	private ImageView imv_notif_back;
	private TextView notification;
	private Intent intent;
	
	private FirebaseAuth firebaseAuth;
	private ArrayList<ModelNotification> notificationList;
	private AdapterNotification adapterNotification;
	
	private RecyclerView rcv_noti_list;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		
		imv_notif_back = findViewById(R.id.imv_notif_back);
		notification = findViewById(R.id.notification);
		rcv_noti_list = findViewById(R.id.rcv_noti_list);
		
		getAllNotifications();
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_notif_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(Notification.this, Parent.class);
				startActivity(intent);
				finish();
			}
		});
		
	}
	
	private void getAllNotifications() {
		notificationList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
		ref.child(firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				notificationList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ModelNotification model = ds.getValue(ModelNotification.class);
					notificationList.add(model);
				}
				adapterNotification = new AdapterNotification(getApplicationContext(), notificationList);
				rcv_noti_list.setAdapter(adapterNotification);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void changeToEnglish() {
		notification.setText(R.string.notifiication);
	}
	
	private void changeToSinhala() {
		notification.setText(R.string.sinhala_notification);
	}
	
}
