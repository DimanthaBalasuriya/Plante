package com.example.plante.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterModelUser;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Useruser extends AppCompatActivity {
	
	private TextView txv_uusr_title;
	private ImageView imv_uusr_back;
	private Intent intent;
	
	private String id;
	private String title;
	
	private List<String> idList;
	private RecyclerView rcv_uusr_list;
	private AdapterModelUser userAdapter;
	private List<ModelUser> userList;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_useruser);
		
		txv_uusr_title = findViewById(R.id.txv_uusr_title);
		imv_uusr_back = findViewById(R.id.imv_uusr_back);
		rcv_uusr_list = findViewById(R.id.rcv_uusr_users);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		intent = getIntent();
		id = intent.getStringExtra("id");
		title = intent.getStringExtra("title");
		
		rcv_uusr_list.setHasFixedSize(true);
		rcv_uusr_list.setLayoutManager(new LinearLayoutManager(this));
		userList = new ArrayList<>();
		userAdapter = new AdapterModelUser(this, userList);
		rcv_uusr_list.setAdapter(userAdapter);
		
		imv_uusr_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		idList = new ArrayList<>();
		
		switch (title) {
			case "Following":
				txv_uusr_title.setText("Following");
				getFollowing();
				break;
			case "Followers":
				txv_uusr_title.setText("Followers");
				getFollowers();
				break;
		}
	}
	
	private void changeToSinhala() {
		txv_uusr_title.setText(R.string.sinhala_findpeople);
	}
	
	private void changeToEnglish() {
		txv_uusr_title.setText(R.string.findpeople);
	}
	
	
	private void getFollowers() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("Followers");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				idList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					idList.add(d.getKey());
				}
				showUsers();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getFollowing() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("Following");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				idList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					idList.add(d.getKey());
				}
				showUsers();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void showUsers() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelUser user = d.getValue(ModelUser.class);
					for (String id : idList) {
						if (user.getUid().equals(id)) {
							userList.add(user);
						}
					}
				}
				userAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
}
