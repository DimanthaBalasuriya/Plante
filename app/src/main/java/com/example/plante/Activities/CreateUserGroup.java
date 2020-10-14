package com.example.plante.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import com.example.plante.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateUserGroup extends AppCompatActivity {
	
	private FirebaseAuth firebaseAuth;
	private ImageView imv_cugr_back, imv_cugr_image;
	private TextView txv_cugr_title;
	private EditText edt_cugr_groupname, edt_cugr_groupdescription;
	private Button btn_cugr_creategroup;
	
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	
	private static final int IMAGE_PICK_CAMERA_CODE = 300;
	private static final int IMAGE_PICK_GALLERY_CODE = 400;
	
	String[] cameraPermissions;
	String[] storagePermissions;
	
	Uri image_uri = null;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_user_group);
		
		pd = new ProgressDialog(this);
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		imv_cugr_back = findViewById(R.id.imv_cugr_back);
		imv_cugr_image = findViewById(R.id.imv_cugr_image);
		txv_cugr_title = findViewById(R.id.txv_cugr_title);
		edt_cugr_groupname = findViewById(R.id.edt_cugr_groupname);
		edt_cugr_groupdescription = findViewById(R.id.edt_cugr_groupdescription);
		btn_cugr_creategroup = findViewById(R.id.btn_cugr_creategroup);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		
		firebaseAuth = FirebaseAuth.getInstance();
		checkUser();
		
		imv_cugr_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
		btn_cugr_creategroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startCreateGroup();
			}
		});
		
		imv_cugr_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void changeToSinhala() {
		edt_cugr_groupname.setText(R.string.sinhala_group_name);
		edt_cugr_groupdescription.setText(R.string.sinhala_group_description);
		btn_cugr_creategroup.setText(R.string.sinhala_create);
	}
	
	private void changeToEnglish() {
		edt_cugr_groupname.setText(R.string.group_name);
		edt_cugr_groupdescription.setText(R.string.group_description);
		btn_cugr_creategroup.setText(R.string.create);
	}
	
	private void startCreateGroup() {
		pd.setMessage("Publishing Group...");
		pd.show();
		String groupTitle = edt_cugr_groupname.getText().toString().trim();
		String groupDescription = edt_cugr_groupdescription.getText().toString().trim();
		if (TextUtils.isEmpty(groupTitle)) {
			Toast.makeText(this, "Please enter group title...", Toast.LENGTH_SHORT).show();
			return;
		}
		String g_timestamp = "" + System.currentTimeMillis();
		if (image_uri == null) {
			createGroup("" + g_timestamp, "" + groupTitle, "" + groupDescription, "");
		} else {
			String fileNameAndPath = "Group_Imgs/" + "image" + g_timestamp;
			StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
			storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
					while (!p_uriTask.isSuccessful()) ;
					Uri p_downloadUri = p_uriTask.getResult();
					if (p_uriTask.isSuccessful()) {
						createGroup("" + g_timestamp, "" + groupTitle, "" + groupDescription, "" + p_downloadUri);
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	private void createGroup(String g_timestamp, String groupTitle, String groupDescription, String groupIcon) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("groupId", "" + g_timestamp);
		hashMap.put("groupTitle", "" + groupTitle);
		hashMap.put("groupDescription", "" + groupDescription);
		hashMap.put("groupIcon", "" + groupIcon);
		hashMap.put("timestamp", "" + g_timestamp);
		hashMap.put("createdBy", "" + firebaseAuth.getUid());
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(g_timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				HashMap<String, String> hashMap1 = new HashMap<>();
				hashMap1.put("uid", firebaseAuth.getUid());
				hashMap1.put("role", "creator");
				hashMap1.put("timestamp", g_timestamp);
				DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
				ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid()).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						pd.dismiss();
					}
				}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						pd.setMessage("Not published!");
						pd.show();
					}
				});
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				pd.setMessage("Not published!");
				pd.show();
			}
		});
	}
	
	private void showImagePickDialog() {
		String[] options = {"Camera", "Gallery"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					if (!checkCameraPermission()) {
						requestCameraPermission();
					} else {
						pickFromCamera();
					}
				} else {
					if (!checkStoragePermissions()) {
						requestStoragePermission();
					} else {
						pickFromGallery();
					}
				}
			}
		}).show();
	}
	
	private void pickFromCamera() {
		ContentValues cv = new ContentValues();
		cv.put(MediaStore.Images.Media.TITLE, "Group Image Icon Title");
		cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Description");
		image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
		startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
	}
	
	private boolean checkStoragePermissions() {
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
	
	private void pickFromGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
	}
	
	private void checkUser() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			txv_cugr_title.setText(user.getEmail());
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case CAMERA_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccepted && storageAccepted) {
						pickFromCamera();
					} else {
						Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
					}
				}
			}
			case STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (storageAccepted) {
						pickFromGallery();
					} else {
						Toast.makeText(this, "Storage permission are required", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK_GALLERY_CODE) {
				image_uri = data.getData();
				imv_cugr_image.setImageURI(image_uri);
			} else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				imv_cugr_image.setImageURI(image_uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
