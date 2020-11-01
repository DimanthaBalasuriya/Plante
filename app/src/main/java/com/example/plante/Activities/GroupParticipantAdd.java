package com.example.plante.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterParticipantAdd;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAdd extends AppCompatActivity {
	
	private RecyclerView group_participant_list;
	private FirebaseAuth firebaseAuth;
	private String groupId, myGroupRole;
	private ArrayList<ModelUser> userList;
	private AdapterParticipantAdd adapterParticipantAdd;
	private ImageView imv_adm_back;
	private TextView txv_title;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_participant_add);
		
		firebaseAuth = FirebaseAuth.getInstance();
		group_participant_list = findViewById(R.id.group_participant_list);
		
		imv_adm_back = findViewById(R.id.imv_adm_back);
		txv_title = findViewById(R.id.txv_title);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		
		groupId = getIntent().getStringExtra("groupId");
		loadGroupInfo();
		
		imv_adm_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	private void changeToSinhala() {
		txv_title.setText(R.string.sinhala_add_members);
	}
	
	private void changeToEnglish() {
		txv_title.setText(R.string.add_members);
	}
	
	private void getAllUsers() {
		userList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelUser modelUser = d.getValue(ModelUser.class);
					if (!firebaseAuth.getUid().equals(modelUser.getUid())) {
						userList.add(modelUser);
					}
				}
				System.out.println(myGroupRole);
				adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAdd.this, userList, "" + groupId, "" + myGroupRole);
				group_participant_list.setAdapter(adapterParticipantAdd);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadGroupInfo() {
		final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String groupId = "" + d.child("groupId").getValue();
					String groupTitle = "" + d.child("groupTitle").getValue();
					String groupDescription = "" + d.child("groupIcon").getValue();
					String createdBy = "" + d.child("createdBy").getValue();
					String timestamp = "" + d.child("timestamp").getValue();
					
					ref1.child(groupId).child("Participants").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							if (dataSnapshot.exists()) {
								myGroupRole = "" + dataSnapshot.child("role").getValue();
								getAllUsers();
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
}
