package com.example.plante;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.example.plante.Activities.GroupInfoActivity;
import com.example.plante.Activities.GroupParticipantAdd;
import com.example.plante.Adapter.AdapterGroupChat;
import com.example.plante.Base_module.ModelGroupChat;
import com.example.plante.Base_module.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatFrame extends AppCompatActivity {
	
	private FirebaseAuth firebaseAuth;
	
	private String groupId;
	private String myGroupRole;
	private Toolbar toolbar;
	private ImageView imv_gcfm_image, add_participants, about_group, imv_gcfm_back;
	private ImageButton imb_gcfm_attach, imb_gcfm_send;
	private TextView txv_gcfm_groupname;
	private EditText edt_gcfm_message;
	private RecyclerView rcv_gcfm_list;
	private ArrayList<ModelGroupChat> groupChatList;
	private AdapterGroupChat adapterGroupChat;
	
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_CAMERA_CODE = 300;
	private static final int IMAGE_PICK_GALLERY_CODE = 400;
	
	String[] cameraPermissions;
	String[] storagePermissions;
	
	Uri image_uri = null;
	String hisUid;
	String myUid;
	String hisImage;
	
	private RequestQueue requestQueue;
	boolean notify = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_chat_frame);
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		toolbar = findViewById(R.id.tbr_gcfm_toolbar);
		imv_gcfm_image = findViewById(R.id.imv_gcfm_image);
		imv_gcfm_back = findViewById(R.id.imv_gcfm_back);
		imb_gcfm_attach = findViewById(R.id.imb_gcfm_attach);
		imb_gcfm_send = findViewById(R.id.imb_gcfm_sendMessage);
		txv_gcfm_groupname = findViewById(R.id.txv_gcfm_groupname);
		edt_gcfm_message = findViewById(R.id.edt_gcfm_message);
		rcv_gcfm_list = findViewById(R.id.rcv_gcfm_list);
		add_participants = findViewById(R.id.add_paticipants);
		about_group = findViewById(R.id.about_group);
		
		setSupportActionBar(toolbar);
		
		Intent intent = getIntent();
		groupId = intent.getStringExtra("groupId");
		System.out.println("GroupID" + groupId);
		
		add_participants.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent addParticipant = new Intent(getApplicationContext(), GroupParticipantAdd.class);
				addParticipant.putExtra("groupId", groupId);
				startActivity(addParticipant);
			}
		});
		
		about_group.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent addGroup = new Intent(getApplicationContext(), GroupInfoActivity.class);
				addGroup.putExtra("groupId", groupId);
				startActivity(addGroup);
			}
		});
		
		imv_gcfm_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		loadGroupInfo();
		loadGroupMessages();
		loadMyGroupRole();
		
		imb_gcfm_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = edt_gcfm_message.getText().toString().trim();
				if (TextUtils.isEmpty(message)) {
					Toast.makeText(getApplicationContext(), "Can't send empty message...", Toast.LENGTH_SHORT).show();
				} else {
					sendMessage(message);
					edt_gcfm_message.setText("");
				}
			}
		});
		
		imb_gcfm_attach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
/*		System.out.println("MYGROUPROLE1" + myGroupRole);
		if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
			add_participants.setVisibility(View.VISIBLE);
		} else {
			add_participants.setVisibility(View.GONE);
		}*/
		
	}
	
	private void loadMyGroupRole() {
		System.out.println(firebaseAuth.getUid());
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					myGroupRole = "" + d.child("role").getValue();
					System.out.println("MYGROUP1" + myGroupRole);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadGroupMessages() {
		groupChatList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				groupChatList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelGroupChat modelGroupChat = d.getValue(ModelGroupChat.class);
					groupChatList.add(modelGroupChat);
				}
				adapterGroupChat = new AdapterGroupChat(GroupChatFrame.this, groupChatList);
				rcv_gcfm_list.setAdapter(adapterGroupChat);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void sendMessage(String message) {
		String timestamp = "" + System.currentTimeMillis();
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", "" + firebaseAuth.getUid());
		hashMap.put("message", "" + message);
		hashMap.put("timestamp", "" + timestamp);
		hashMap.put("type", "" + "text");
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				edt_gcfm_message.setText("");
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void loadGroupInfo() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String groupTitle = "" + d.child("groupTitle").getValue();
					String groupDescription = "" + d.child("groupDescription").getValue();
					String groupIcon = "" + d.child("groupIcon").getValue();
					String timestamp = "" + d.child("timestamp").getValue();
					String createdBy = "" + d.child("createdBy").getValue();
					
					txv_gcfm_groupname.setText(groupTitle);
					try {
						Picasso.get().load(groupIcon).placeholder(R.drawable.ic_usernot).into(imv_gcfm_image);
					} catch (Exception e) {
						imv_gcfm_image.setImageResource(R.drawable.ic_usernot);
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				
			}
		});
	}
	
	private void showImagePickDialog() {
		String[] option = {"Take Photo", "Gallery"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Image from");
		builder.setItems(option, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					if (!checkCameraPermission()) {
						requestCameraPermission();
					} else {
						pickFromCamera();
					}
				}
				if (which == 1) {
					if (!checkStoragePermission()) {
						requestStoragePermission();
					} else {
						pickFromGallery();
					}
				}
			}
		});
		builder.create().show();
	}
	
	private void pickFromGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
	}
	
	private void pickFromCamera() {
		ContentValues cv = new ContentValues();
		cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
		cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
		image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
		startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
	}
	
	private boolean checkStoragePermission() {
		boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result;
	}
	
	private void requestStoragePermission() {
		ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
	}
	
	private boolean checkCameraPermission() {
		boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
		boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result1 && result2;
	}
	
	private void requestCameraPermission() {
		ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		switch (requestCode) {
			case CAMERA_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean cameraAccespted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccespted && storageAccepted) {
						pickFromCamera();
					} else {
						Toast.makeText(this, "Camera & Storage both permission are necessary...", Toast.LENGTH_SHORT).show();
					}
				} else {
				
				}
			}
			break;
			case STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (storageAccepted) {
						pickFromGallery();
					} else {
						Toast.makeText(this, "Storage permissions necessary...", Toast.LENGTH_SHORT);
					}
				} else {
				
				}
			}
			break;
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK_GALLERY_CODE) {
				image_uri = data.getData();
				try {
					setImageMessage(image_uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				try {
					setImageMessage(image_uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void setImageMessage(Uri image_uri) throws IOException {
		String timeStamp = "" + System.currentTimeMillis();
		String fileNameAndPath = "ChatImages/" + "" + timeStamp;
		Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] data = baos.toByteArray();
		StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
		ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
				while (!uriTask.isSuccessful()) ;
				String downloadUri = uriTask.getResult().toString();
				if (uriTask.isSuccessful()) {
					DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
					HashMap<String, Object> hashMap = new HashMap<>();
					hashMap.put("sender", myUid);
					hashMap.put("receiver", hisUid);
					hashMap.put("message", downloadUri);
					hashMap.put("timestamp", timeStamp);
					hashMap.put("isSeen", false);
					hashMap.put("type", "image");
					databaseReference.child("Chats").push().setValue(hashMap);
					DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(myUid);
					database.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							ModelUser user = dataSnapshot.getValue(ModelUser.class);
							if (notify) {
								//sendNotification(hisUid, user.getName(), "Sent you a Photo...");
							}
							notify = false;
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
					DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);
					chatRef1.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							if (!dataSnapshot.exists()) {
								chatRef1.child("id").setValue(hisUid);
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
					chatRef2.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							if (!dataSnapshot.exists()) {
								chatRef2.child("id").setValue(myUid);
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
			
			}
		});
	}
	
}
