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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.plante.Adapter.AdapterChat;
import com.example.plante.Base_module.ModelChat;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.NotificationManagement.Data;
import com.example.plante.NotificationManagement.Sender;
import com.example.plante.NotificationManagement.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageFrame extends AppCompatActivity {
	
	private Toolbar toolbar;
	private RecyclerView recyclerView;
	private ImageView imv_mfr_userimage, imv_mfr_back;
	private TextView txv_mfr_name, txv_mfr_status;
	private EditText edt_mfr_message;
	private ImageButton imb_mfr_sendbutton, imb_mfr_attach;
	
	private FirebaseAuth firebaseAuth;
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	
	private ValueEventListener seenListener;
	private DatabaseReference userRefForSeen;
	
	private List<ModelChat> chatList;
	private AdapterChat adapterChat;
	
	private String hisUid;
	private String myUid;
	private String hisImage;
	
	private RequestQueue requestQueue;
	private boolean notify = false;
	
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_CAMERA_CODE = 300;
	private static final int IMAGE_PICK_GALLERY_CODE = 400;
	
	private String[] cameraPermissions;
	private String[] storagePermissions;
	
	private Uri image_uri = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_frame);
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		Toolbar toolbar = findViewById(R.id.mfr_toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle("");
		recyclerView = findViewById(R.id.rcv_mfr_msgList);
		imv_mfr_userimage = findViewById(R.id.imv_mfr_image);
		imb_mfr_sendbutton = findViewById(R.id.imb_mfr_send);
		txv_mfr_name = findViewById(R.id.txv_mfr_username);
		txv_mfr_status = findViewById(R.id.txv_mfr_userstate);
		edt_mfr_message = findViewById(R.id.edt_mfr_message);
		imb_mfr_attach = findViewById(R.id.imb_mfr_image);
		imv_mfr_back = findViewById(R.id.imv_mfr_back);
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		linearLayoutManager.setStackFromEnd(true);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(linearLayoutManager);
		
		Intent intent = getIntent();
		hisUid = intent.getStringExtra("hisuid");
		
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("User");
		
		Query userQuery = databaseReference.orderByChild("uid").equalTo(hisUid);
		userQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = "" + d.child("name").getValue();
					hisImage = "" + d.child("image").getValue();
					String typingStatus = "" + d.child("typingTo").getValue();
					
					if (typingStatus.equals(myUid)) {
						txv_mfr_status.setText("Typing...");
					} else {
						String onlineStatus = "" + d.child("onlineStatus").getValue();
						if (onlineStatus.equals("online")) {
							txv_mfr_status.setText(onlineStatus);
						} else {
							Calendar cal = Calendar.getInstance(Locale.ENGLISH);
							cal.setTimeInMillis(Long.parseLong(onlineStatus));
							String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:ss aa", cal).toString();
							txv_mfr_status.setText("Last seen at : " + dateTime);
						}
					}
					
					txv_mfr_name.setText(name);
					try {
						Picasso.get().load(hisImage).placeholder(R.drawable.ic_usernot).into(imv_mfr_userimage);
					} catch (Exception e) {
						Picasso.get().load(R.drawable.ic_usernot).into(imv_mfr_userimage);
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		imb_mfr_sendbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notify = true;
				String message = edt_mfr_message.getText().toString().trim();
				if (TextUtils.isEmpty(message)) {
					Toast.makeText(getApplicationContext(), "Text Field is Empty!", Toast.LENGTH_SHORT).show();
				} else {
					sendMessage(message);
				}
			}
		});
		
		imb_mfr_attach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
		edt_mfr_message.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().trim().length() == 0) {
					checkTypingStatus("noOne");
				} else {
					checkTypingStatus(hisUid);
				}
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		imv_mfr_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		readMessages();
		seenMessage();
	}
	
	private void seenMessage() {
		userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
		seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ModelChat chat = ds.getValue(ModelChat.class);
					if ((chat.getReceiver() == null) || (chat.getSender() == null)) {
					
					} else if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
						HashMap<String, Object> hasSeenHashMap = new HashMap<>();
						hasSeenHashMap.put("isSeen", true);
						ds.getRef().updateChildren(hasSeenHashMap);
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void readMessages() {
		chatList = new ArrayList<>();
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
		dbRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				chatList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ModelChat chat = ds.getValue(ModelChat.class);
					System.out.println("UIDL IDT " + hisUid);
					System.out.println("UIDL IDT " + myUid);
					System.out.println("UIDL IDT " + chat.getReceiver());
					System.out.println("UIDL IDT " + chat.getSender());
					if ((chat.getReceiver() == null) || (chat.getSender() == null)) {
						Toast.makeText(getApplicationContext(), "Currently no chat available...", Toast.LENGTH_SHORT).show();
					} else if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) || chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
						chatList.add(chat);
					}
					adapterChat = new AdapterChat(MessageFrame.this, chatList, hisImage);
					adapterChat.notifyDataSetChanged();
					recyclerView.setAdapter(adapterChat);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void sendMessage(String message) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", myUid);
		hashMap.put("receiver", hisUid);
		hashMap.put("message", message);
		hashMap.put("timestamp", timestamp);
		hashMap.put("isSeen", false);
		hashMap.put("type", "text");
		dbReference.child("Chats").push().setValue(hashMap);
		
		edt_mfr_message.setText("");
		
		String msg = message;
		DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(myUid);
		database.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				ModelUser user = dataSnapshot.getValue(ModelUser.class);
				if (notify) {
					sendNotification(hisUid, user.getName(), message);
				}
				notify = false;
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		System.out.println("UIDTEL" + myUid);
		System.out.println("UIDTEL" + hisUid);
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
	
	private void setImageMessage(Uri image_uri) throws IOException {
		notify = true;
		String timeStamp = "" + System.currentTimeMillis();
		String fileNameAndPath = "ChatImages/" + "post_" + timeStamp;
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
								sendNotification(hisUid, user.getName(), "Sent you a Photo...");
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
	
	private void sendNotification(String hisUid, String name, String message) {
		DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
		Query query = allTokens.orderByKey().equalTo(hisUid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					Token token = d.getValue(Token.class);
					Data data = new Data("" + myUid, "" + name + " : " + message, "New Message", "" + hisUid, "ChatNotification", R.drawable.ic_plante);
					
					Sender sender = new Sender(data, token.getToken());
					try {
						JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
						JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								Log.d("JSON_RESPONSE", "onResponse : " + response.toString());
							}
						}, new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d("JSON_RESPONSE", "onResponse : " + error.toString());
							}
						}) {
							@Override
							public Map<String, String> getHeaders() throws AuthFailureError {
								Map<String, String> headers = new HashMap<>();
								headers.put("Content-Type", "application/json");
								headers.put("Authorization", "key=AAAAVp4o4GY:APA91bFtYTe6He7hI8ixcQ3eiQw0F-dHIDrdf_5KSwnp-dNwcjca45v5Z77fhoSR-NhPziRJFGVSg6_0xy8iu7ABirjaDNG77b6ObfVXzfYp3_FOj2DasUvV0Zky8xKelfA5obEhmH_X");
								return headers;
							}
						};
						requestQueue.add(jsonObjectRequest);
					} catch (JSONException e) {
					
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
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
	
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			myUid = user.getUid();
		} else {
			startActivity(new Intent(MessageFrame.this, Parent.class));
			finish();
		}
	}
	
	private void checkOnlineStatus(String status) {
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("onlineStatus", status);
		dbRef.updateChildren(hashMap);
	}
	
	private void checkTypingStatus(String typing) {
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("User").child(myUid);
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("typingTo", typing);
		dbRef.updateChildren(hashMap);
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
	protected void onStart() {
		checkUserStatus();
		checkOnlineStatus("online");
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		String timeStamp = String.valueOf(System.currentTimeMillis());
		checkOnlineStatus(timeStamp);
		checkTypingStatus("noOne");
		userRefForSeen.removeEventListener(seenListener);
	}
	
	@Override
	protected void onResume() {
		checkOnlineStatus("online");
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
