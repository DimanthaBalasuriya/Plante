package com.example.plante;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class RegisterDisease extends AppCompatActivity {
	
	private ImageView imv_rgds_back, imv_rgds_fertilizer, imv_rgds_addimage;
	private EditText edt_rgds_diseasename, edt_rgds_diseasedescription;
	private Button btn_rgds_add;
	private TextView textView, textView1, textView2;
	
	private FirebaseAuth mAuth;
	private Intent intent;
	
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
		setContentView(R.layout.activity_register_disease);
		
		pd = new ProgressDialog(this);
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_rgds_back = findViewById(R.id.imv_rgds_back);
		imv_rgds_fertilizer = findViewById(R.id.imv_rgds_fertilizer);
		imv_rgds_addimage = findViewById(R.id.imv_rgds_addimage);
		edt_rgds_diseasename = findViewById(R.id.edt_rgds_diseasename);
		edt_rgds_diseasedescription = findViewById(R.id.edt_rgds_diseasedescription);
		btn_rgds_add = findViewById(R.id.btn_rgds_add);
		textView = findViewById(R.id.textView);
		textView1 = findViewById(R.id.textView1);
		textView2 = findViewById(R.id.textView2);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_rgds_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_rgds_fertilizer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterFertilizer.class);
				startActivity(intent);
				finish();
			}
		});
		
		imv_rgds_addimage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
		btn_rgds_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeDiseaseAddProcess();
			}
		});
		
	}
	
	private void changeToEnglish() {
		edt_rgds_diseasename.setHint(R.string.putanameofdiseasehere);
		edt_rgds_diseasedescription.setHint(R.string.putadescriptionofdiseasehere);
		btn_rgds_add.setText(R.string.add);
		textView.setText(R.string.name);
		textView1.setText(R.string.description);
		textView2.setText(R.string.howitlooklike);
	}
	
	private void changeToSinhala() {
		edt_rgds_diseasename.setHint(R.string.sinhala_putanameofdiseasehere);
		edt_rgds_diseasedescription.setHint(R.string.sinhala_putadescriptionofdiseasehere);
		btn_rgds_add.setText(R.string.sinhala_add);
		textView.setText(R.string.sinhala_name);
		textView1.setText(R.string.sinhala_description);
		textView2.setText(R.string.sinhala_howitlooklike);
	}
	
	private void executeDiseaseAddProcess() {
		pd.setMessage("Publishing...");
		String diseasename = edt_rgds_diseasename.getText().toString().trim();
		String diseaeDescription = edt_rgds_diseasedescription.getText().toString().trim();
		if (TextUtils.isEmpty(diseasename)) {
			Toast.makeText(getApplicationContext(), "Please add disease name!", Toast.LENGTH_SHORT).show();
			return;
		}
		String timestamp = "" + System.currentTimeMillis();
		if (image_uri == null) {
			addDisease("" + timestamp, "" + diseasename, "" + diseaeDescription, "");
		} else {
			String filename = "Disease_Imgs/" + "image" + timestamp;
			StorageReference storageReference = FirebaseStorage.getInstance().getReference(filename);
			storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
					while (!uriTask.isSuccessful()) ;
					Uri downloaduri = uriTask.getResult();
					if (uriTask.isSuccessful()) {
						addDisease("" + timestamp, "" + diseasename, "" + diseaeDescription, "" + downloaduri);
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
	
	private void addDisease(String timestamp, String name, String description, String image) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("diseaseId", "" + timestamp);
		hashMap.put("name", "" + name);
		hashMap.put("description", "" + description);
		hashMap.put("image", "" + image);
		hashMap.put("addedby", "" + mAuth.getUid());
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Diseases");
		ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				pd.dismiss();
				intent = new Intent(getApplicationContext(), AddTreatment.class);
				intent.putExtra("did", timestamp);
				startActivity(intent);
				finish();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				pd.setMessage("Not Published!");
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
				imv_rgds_addimage.setImageURI(image_uri);
			} else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				imv_rgds_addimage.setImageURI(image_uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
