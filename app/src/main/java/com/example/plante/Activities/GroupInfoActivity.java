package com.example.plante.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
	
	private String groupId;
	private String myGroupRole;
	private ImageView imv_gia_image, imv_gia_back;
	private TextView txv_gia_description, txv_gia_createdBy, txv_gia_editGroup, txv_gia_addparticipant, txv_gia_leaveGroup, txv_gia_participant;
	
	private FirebaseAuth firebaseAuth;
	
	private ArrayList<ModelUser> userList;
	private AdapterParticipantAdd adapterParticipantAdd;
	private RecyclerView rcv_gia_list;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_info);
		
		groupId = getIntent().getStringExtra("groupId");
		imv_gia_image = findViewById(R.id.imv_gia_image);
		imv_gia_back = findViewById(R.id.imv_gia_back);
		txv_gia_description = findViewById(R.id.txv_gia_description);
		txv_gia_createdBy = findViewById(R.id.txv_gia_createdBy);
		txv_gia_editGroup = findViewById(R.id.txv_gia_edit);
		txv_gia_addparticipant = findViewById(R.id.txv_gia_add);
		txv_gia_leaveGroup = findViewById(R.id.txv_gia_leave);
		txv_gia_participant = findViewById(R.id.txv_gia_participantcount);
		rcv_gia_list = findViewById(R.id.rcv_gia_list);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		loadGroupInfo();
		loadMyGroupRole();
		
		imv_gia_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		txv_gia_addparticipant.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), GroupParticipantAdd.class);
				intent.putExtra("groupId", groupId);
				startActivity(intent);
			}
		});
	}
	
	private void changeToSinhala() {
		txv_gia_editGroup.setText(R.string.sinhala_edit_group);
		txv_gia_addparticipant.setText(R.string.sinhala_add_participants);
		txv_gia_leaveGroup.setText(R.string.sinhala_leave_group);
		txv_gia_participant.setText(R.string.sinhala_participant_count);
	}
	
	private void changeToEnglish() {
		txv_gia_editGroup.setText(R.string.edit_group);
		txv_gia_addparticipant.setText(R.string.add_participants);
		txv_gia_leaveGroup.setText(R.string.leave_group);
		txv_gia_participant.setText(R.string.participant_count);
	}
	
	private void loadGroupInfo() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String groupId = "" + d.child("goupId").getValue();
					String groupTitle = "" + d.child("groupTitle").getValue();
					String groupDescription = "" + d.child("groupDescription").getValue();
					String groupIcon = "" + d.child("groupIcon").getValue();
					String createdBy = "" + d.child("createdBy").getValue();
					String timestamp = "" + d.child("timestamp").getValue();
					
					Calendar cal = Calendar.getInstance(Locale.ENGLISH);
					cal.setTimeInMillis(Long.parseLong(timestamp));
					String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
					
					loadCreatorInfo(dateTime, createdBy);
					
					//txv_gia_createdBy.setText(createdBy);
					txv_gia_description.setText(groupDescription);
					
					try {
						Picasso.get().load(groupIcon).placeholder(R.drawable.ic_usernot).into(imv_gia_image);
					} catch (Exception e) {
						imv_gia_image.setImageResource(R.drawable.ic_usernot);
					}
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadCreatorInfo(String dateTime, String createdBy) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
		ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = "" + d.child("name").getValue();
					txv_gia_createdBy.setText("Created by " + name + " on " + dateTime);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadMyGroupRole() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					myGroupRole = "" + d.child("role").getValue();
					if (myGroupRole.equals("participant")) {
						txv_gia_editGroup.setVisibility(View.GONE);
						txv_gia_participant.setVisibility(View.GONE);
						txv_gia_leaveGroup.setText("Leave Group");
					} else if (myGroupRole.equals("admin")) {
						txv_gia_editGroup.setVisibility(View.GONE);
						txv_gia_participant.setVisibility(View.VISIBLE);
						txv_gia_leaveGroup.setText("Leave Group");
					} else if (myGroupRole.equals("creator")) {
						txv_gia_editGroup.setVisibility(View.VISIBLE);
						txv_gia_participant.setVisibility(View.VISIBLE);
						txv_gia_leaveGroup.setText("Delete Group");
					}
				}
				loadParticipants();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadParticipants() {
		userList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					String uid = "" + ds.child("uid").getValue();
					DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
					ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot d : dataSnapshot.getChildren()) {
								ModelUser modelUser = d.getValue(ModelUser.class);
								userList.add(modelUser);
							}
							adapterParticipantAdd = new AdapterParticipantAdd(getApplicationContext(), userList, groupId, myGroupRole);
							rcv_gia_list.setAdapter(adapterParticipantAdd);
							txv_gia_participant.setText("Participants(" + userList.size() + ")");
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
