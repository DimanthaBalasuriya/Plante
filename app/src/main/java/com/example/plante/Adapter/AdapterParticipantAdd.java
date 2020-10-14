package com.example.plante.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {
	
	private Context context;
	private ArrayList<ModelUser> userList;
	private String groupId, myGroupRole;
	
	@NonNull
	@Override
	public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_participant_list, parent, false);
		return new HolderParticipantAdd(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
		ModelUser modelUser = userList.get(position);
		String name = modelUser.getName();
		String job = modelUser.getPosition();
		String image = modelUser.getImage();
		String uid = modelUser.getUid();
		
		holder.txv_lpai_name.setText(name);
		holder.txv_lpai_position.setText(job);
		try {
			Picasso.get().load(image).placeholder(R.drawable.ic_usernot).into(holder.imv_lpai_userimage);
		} catch (Exception e) {
			holder.imv_lpai_userimage.setImageResource(R.drawable.ic_usernot);
		}
		
		checkIfAlreadyExists(modelUser, holder);
		
		System.out.println("ONVH" + myGroupRole);
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
				ref.child(groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.exists()) {
							String hisPerviousRole = "" + dataSnapshot.child("role").getValue();
							String[] options;
							
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle("Choose Option");
							if (myGroupRole.equals("creator")) {
								if (hisPerviousRole.equals("admin")) {
									options = new String[]{"Remove Admin", "Remove User"};
									builder.setItems(options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (which == 0) {
												removeAdmin(modelUser);
											} else {
												removeParticipant(modelUser);
											}
										}
									}).show();
								} else if (hisPerviousRole.equals("participant")) {
									options = new String[]{"Make Admin", "Remove User"};
									builder.setItems(options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (which == 0) {
												makeAdmin(modelUser);
											} else {
												removeParticipant(modelUser);
											}
										}
									}).show();
								}
							} else if (myGroupRole.equals("admin")) {
								if (hisPerviousRole.equals("creator")) {
									Toast.makeText(context, "Creator of group...", Toast.LENGTH_SHORT).show();
								} else if (hisPerviousRole.equals("admin")) {
									options = new String[]{"Remove Admin", "Remove User"};
									builder.setItems(options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (which == 0) {
												removeAdmin(modelUser);
											} else {
												removeParticipant(modelUser);
											}
										}
									}).show();
								} else if (hisPerviousRole.equals("participant")) {
									options = new String[]{"Remove Admin", "Remove User"};
									builder.setItems(options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (which == 0) {
												removeAdmin(modelUser);
											} else {
												removeParticipant(modelUser);
											}
										}
									}).show();
								}
							}
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							builder.setTitle("Add Participant").setMessage("Add this user in this group?").setPositiveButton("ADD", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									addParticipant(modelUser);
								}
							}).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).show();
						}
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		});
		
	}
	
	private void removeParticipant(ModelUser modelUser) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
		reference.child(groupId).child("Participants").child(modelUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
			
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
			
			}
		});
		
	}
	
	private void makeAdmin(ModelUser modelUser) {
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("role", "admin");
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
		reference.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(context, "User is now admin", Toast.LENGTH_SHORT).show();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void removeAdmin(ModelUser modelUser) {
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("role", "participant");
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
		reference.child(groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(context, "User is no longer admin...", Toast.LENGTH_SHORT).show();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void addParticipant(ModelUser modelUser) {
		String timestamp = "" + System.currentTimeMillis();
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("uid", modelUser.getUid());
		hashMap.put("role", "participant");
		hashMap.put("timestamp", "" + timestamp);
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void checkIfAlreadyExists(ModelUser modelUser, HolderParticipantAdd holder) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Participants").child(modelUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					String hisRole = "" + dataSnapshot.child("role").getValue();
					holder.txv_lpai_status.setText(hisRole);
				} else {
					holder.txv_lpai_status.setText("");
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return userList.size();
	}
	
	public AdapterParticipantAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
		this.context = context;
		this.userList = userList;
		this.groupId = groupId;
		this.myGroupRole = myGroupRole;
	}
	
	class HolderParticipantAdd extends RecyclerView.ViewHolder {
		
		private ImageView imv_lpai_userimage;
		private TextView txv_lpai_name, txv_lpai_position, txv_lpai_status;
		
		public HolderParticipantAdd(View itemView) {
			super(itemView);
			
			imv_lpai_userimage = itemView.findViewById(R.id.imv_lpai_userimage);
			txv_lpai_name = itemView.findViewById(R.id.txv_lpai_username);
			txv_lpai_position = itemView.findViewById(R.id.txv_lpai_usertype);
			txv_lpai_status = itemView.findViewById(R.id.txv_lpai_state);
		}
		
	}
	
}
