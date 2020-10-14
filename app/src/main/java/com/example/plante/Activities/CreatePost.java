package com.example.plante.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.plante.Parent;
import com.example.plante.R;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CreatePost extends AppCompatActivity {
	
	FirebaseAuth mAuth;
	DatabaseReference userDb;
	private ImageView imv_post_back, imv_post_cover, imv_post_image;
	private EditText edt_post_title, edt_post_content;
	private Button btn_post_post;
	private TextView txv_head, textView1, textView2;
	
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	
	private static final int IMAGE_PICK_CAMERA_CODE = 300;
	private static final int IMAGE_PICK_GALLERY_CODE = 400;
	
	String[] cameraPermissions;
	String[] storagePermissions;
	
	Uri image_uri = null;
	String name, email, job, uid, dp;
	String editTitle, editDescription, editImage;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_post);
		
		pd = new ProgressDialog(this);
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		mAuth = FirebaseAuth.getInstance();
		checkUserStatus();
		
		imv_post_back = findViewById(R.id.imv_post_back);
		imv_post_cover = findViewById(R.id.imv_post_cover);
		imv_post_image = findViewById(R.id.imv_post_image);
		edt_post_title = findViewById(R.id.edt_post_title);
		edt_post_content = findViewById(R.id.edt_post_content);
		btn_post_post = findViewById(R.id.btn_post_post);
		txv_head = findViewById(R.id.txv_head);
		textView1 = findViewById(R.id.textView1);
		textView2 = findViewById(R.id.textView2);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		Intent intent = getIntent();
		
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent);
			} else if (type.startsWith("image")) {
				handleSendImage(intent);
			}
		}
		
		String isUpdateKey = "" + intent.getStringExtra("key");
		String editPostId = "" + intent.getStringExtra("editPostId");
		if (isUpdateKey.equals("editPost")) {
			loadPostData(editPostId);
		} else {
		
		}
		
		imv_post_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_post_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
		btn_post_post.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = edt_post_title.getText().toString().trim();
				String content = edt_post_content.getText().toString().trim();
				if (TextUtils.isEmpty(title)) {
					Toast.makeText(getApplicationContext(), "Enter Title...", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(content)) {
					Toast.makeText(getApplicationContext(), "Enter description...", Toast.LENGTH_SHORT).show();
					return;
				}
				if (isUpdateKey.equals("editPost")) {
					beginUpdate(title, content, editPostId);
				} else {
					uploadData(title, content);
				}
/*				if (image_uri == null) {
					uploadData(title, content, "NoImage");
				} else {
					System.out.println("MYTAG " + image_uri);
					uploadData(title, content, String.valueOf(image_uri));
				}*/
			}
		});
		
		userDb = FirebaseDatabase.getInstance().getReference("User");
		Query query = userDb.orderByChild("email").equalTo(email);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					name = "" + ds.child("name").getValue();
					email = "" + ds.child("email").getValue();
					job = "" + ds.child("position").getValue();
					dp = "" + ds.child("image").getValue();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void changeToSinhala() {
		edt_post_title.setHint(R.string.sinhala_put_a_caption_here);
		edt_post_content.setHint(R.string.sinhala_enter_caption_here);
		btn_post_post.setText(R.string.sinhala_publish);
		txv_head.setText(R.string.sinhala_ask_your_problem);
		textView1.setText(R.string.sinhala_caption);
		textView2.setText(R.string.sihnala_what_you_need_to_know);
	}
	
	private void changeToEnglish() {
		edt_post_title.setHint(R.string.enter_caption_here);
		edt_post_content.setHint(R.string.enter_question_here);
		btn_post_post.setText(R.string.publish);
		txv_head.setText(R.string.ask_your_problem);
		textView1.setText(R.string.caption);
		textView2.setText(R.string.ask_your_problem);
	}
	
	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			image_uri = imageUri;
			imv_post_image.setImageURI(image_uri);
		}
	}
	
	private void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText != null) {
			edt_post_content.setText(sharedText);
		}
	}
	
	private void beginUpdate(String title, String content, String editPostId) {
		if (!editImage.equals("NoImage") || (editImage != null)) {
			updateWasWithImage(title, content, editPostId);
		} else {
			//updateWithNowImage(title, content, editPostId);
			updateWithoutImage(title, content, editPostId);
		}
	}
	
	private void updateWithoutImage(String title, String content, String editPostId) {
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("uid", uid);
		hashMap.put("uName", name);
		hashMap.put("uEmail", email);
		hashMap.put("uDp", dp);
		hashMap.put("pTitle", title);
		hashMap.put("pDescr", content);
		hashMap.put("pImage", "NoImage");
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	private void updateWithNowImage(String title, String content, String editPostId) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String filePathAndName = "Posts/" + "post_" + timestamp;
		
		Bitmap bitmap = ((BitmapDrawable) imv_post_cover.getDrawable()).getBitmap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] data = baos.toByteArray();
		
		StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
		ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
				while (!uriTask.isSuccessful()) ;
				String downloadUri = uriTask.getResult().toString();
				if (uriTask.isSuccessful()) {
					HashMap<String, Object> hashMap = new HashMap<>();
					hashMap.put("uid", uid);
					hashMap.put("uName", name);
					hashMap.put("uEmail", email);
					hashMap.put("uDp", dp);
					hashMap.put("pTitle", title);
					hashMap.put("pDescr", content);
					hashMap.put("pImage", downloadUri);
					DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
					ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void updateWasWithImage(String title, String content, String editPostId) {
		StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
		mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				String timestamp = String.valueOf(System.currentTimeMillis());
				String filePathAndName = "Posts/" + "post_" + timestamp;
				
				Bitmap bitmap = ((BitmapDrawable) imv_post_cover.getDrawable()).getBitmap();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				byte[] data = baos.toByteArray();
				
				StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
				ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
						Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
						while (!uriTask.isSuccessful()) ;
						String downloadUri = uriTask.getResult().toString();
						if (uriTask.isSuccessful()) {
							HashMap<String, Object> hashMap = new HashMap<>();
							hashMap.put("uid", uid);
							hashMap.put("uName", name);
							hashMap.put("uEmail", email);
							hashMap.put("uDp", dp);
							hashMap.put("pTitle", title);
							hashMap.put("pDescr", content);
							hashMap.put("pImage", downloadUri);
							hashMap.put("pComments", "0");
							hashMap.put("pLikes", "0");
							DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
							ref.child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
								}
							}).addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void loadPostData(String editPostId) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
		Query fQuery = reference.orderByChild("pid").equalTo(editPostId);
		fQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					editTitle = "" + ds.child("ptitle").getValue();
					editDescription = "" + ds.child("pcontent").getValue();
					editImage = "" + ds.child("pimage").getValue();
					if (!editImage.equals("NoImage") || (editImage != null)) {
						try {
							Picasso.get().load(editImage).into(imv_post_cover);
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void uploadData(String title, String content) {
		pd.setMessage("Publishing...");
		pd.show();
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String filePathAndName = "Posts/" + "post_" + timeStamp;
		
		if (imv_post_cover.getDrawable() != null) {
			Bitmap bitmap = ((BitmapDrawable) imv_post_cover.getDrawable()).getBitmap();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] data = baos.toByteArray();
			StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
			ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
					while (!uriTask.isSuccessful()) ;
					String downloadUri = uriTask.getResult().toString();
					if (uriTask.isSuccessful()) {
						HashMap<Object, String> hashMap = new HashMap<>();
						hashMap.put("uid", uid);
						hashMap.put("uname", name);
						hashMap.put("uemail", email);
						hashMap.put("uprofile", dp);
						hashMap.put("pid", timeStamp);
						hashMap.put("ptitle", title);
						hashMap.put("pcontent", content);
						hashMap.put("pImage", downloadUri);
						hashMap.put("ptime", timeStamp);
						hashMap.put("pLikes", "0");
						hashMap.put("pComments", "0");
						
						DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
						ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								pd.dismiss();
								edt_post_title.setText("");
								edt_post_content.setText("");
								imv_post_cover.setImageURI(null);
								image_uri = null;
								
								prepareNotification("" + timeStamp, "" + name + " added new post", "" + title + "\n" + content, "PostNotification", "POST");
							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								pd.setMessage("Not Published!");
								pd.show();
							}
						});
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			HashMap<Object, String> hashMap = new HashMap<>();
			hashMap.put("uid", uid);
			hashMap.put("uname", name);
			hashMap.put("uemail", email);
			hashMap.put("uprofile", dp);
			hashMap.put("pid", timeStamp);
			hashMap.put("ptitle", title);
			hashMap.put("pcontent", content);
			hashMap.put("pImage", "NoImage");
			hashMap.put("ptime", timeStamp);
			hashMap.put("pLikes", "0");
			hashMap.put("pComments", "0");
			
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
			ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
				@Override
				public void onSuccess(Void aVoid) {
					pd.dismiss();
					edt_post_title.setText("");
					edt_post_content.setText("");
					imv_post_cover.setImageURI(null);
					image_uri = null;
					
					prepareNotification("" + timeStamp, "" + name + " added new post", "" + title + "\n" + content, "PostNotification", "POST");
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					pd.setMessage("Not Published!");
					pd.show();
				}
			});
		}
	}
	
	private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
		String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
		String NOTIFICATION_TITLE = title;
		String NOTIFICATION_MESSAGE = description;
		String NOTIFICATION_TYPE = notificationType;
		
		JSONObject notificationJO = new JSONObject();
		JSONObject notificationBodyJo = new JSONObject();
		try {
			notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
			notificationBodyJo.put("sender", uid);
			notificationBodyJo.put("pId", pId);
			notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
			notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
			notificationJO.put("to", NOTIFICATION_TOPIC);
			notificationJO.put("data", notificationBodyJo);
			
		} catch (JSONException e) {
			Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
		sendPostNotification(notificationJO);
	}
	
	private void sendPostNotification(JSONObject notificationJO) {
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJO, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				Log.d("FCM_RESPONSE", "onResponse : " + response.toString());
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(getApplicationContext(), "" + error.toString(), Toast.LENGTH_SHORT).show();
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
		Volley.newRequestQueue(this).add(jsonObjectRequest);
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
	
	@Override
	protected void onStart() {
		super.onStart();
		checkUserStatus();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		checkUserStatus();
	}
	
	private void checkUserStatus() {
		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			email = user.getEmail();
			uid = user.getUid();
		} else {
			startActivity(new Intent(this, Parent.class));
			finish();
		}
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
				imv_post_cover.setImageURI(image_uri);
			} else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				imv_post_cover.setImageURI(image_uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
