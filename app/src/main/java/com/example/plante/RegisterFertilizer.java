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

public class RegisterFertilizer extends AppCompatActivity {
	
	private ImageView imv_afet_back, imv_afet_list, imv_afet_image;
	private EditText edt_afet_fertiname, edt_afet_fertitype, edt_afet_fertiprize, edt_afet_fertidesc;
	private Button btn_afet_add;
	private TextView textview, name, textView1, textView2, textView3, textView4;
	
	private Intent intent;
	private FirebaseAuth mAuth;
	
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
		setContentView(R.layout.activity_register_fertilizer);
		
		pd = new ProgressDialog(this);
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_afet_back = findViewById(R.id.imv_afet_back);
		imv_afet_list = findViewById(R.id.imv_afet_list);
		imv_afet_image = findViewById(R.id.imv_afet_image);
		edt_afet_fertiname = findViewById(R.id.edt_afet_fertiname);
		edt_afet_fertitype = findViewById(R.id.edt_afet_fertitype);
		edt_afet_fertiprize = findViewById(R.id.edt_afet_fertiprize);
		edt_afet_fertidesc = findViewById(R.id.edt_afet_fertidesc);
		btn_afet_add = findViewById(R.id.btn_afet_add);
		name = findViewById(R.id.name);
		textView1 = findViewById(R.id.textView1);
		textView2 = findViewById(R.id.textView2);
		textView3 = findViewById(R.id.textView3);
		textView4 = findViewById(R.id.textView4);
		textview = findViewById(R.id.textview);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_afet_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_afet_list.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), Fertilizers.class);
				startActivity(intent);
			}
		});
		
		imv_afet_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showImagePickDialog();
			}
		});
		
		btn_afet_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				executeFertilizerAddProcess();
			}
		});
	}
	
	private void changeToEnglish() {
		btn_afet_add.setText(R.string.add);
		name.setText(R.string.name);
		textView1.setText(R.string.type);
		textView3.setText(R.string.description);
		textView4.setText(R.string.howitlooklike);
		textview.setText(R.string.addfertilizer);
	}
	
	private void changeToSinhala() {
		btn_afet_add.setText(R.string.sinhala_add);
		name.setText(R.string.sinhala_name);
		textView1.setText(R.string.sinhala_type);
		textView3.setText(R.string.sinhala_description);
		textView4.setText(R.string.sinhala_howitlooklike);
		textview.setText(R.string.sinhala_addfertilizer);
	}
	
	private void executeFertilizerAddProcess() {
		pd.setMessage("Publishing...");
		pd.show();
		String fertilizername = edt_afet_fertiname.getText().toString().trim();
		String fertilizertype = edt_afet_fertitype.getText().toString().trim();
		String fertilizerprize = edt_afet_fertiprize.getText().toString().trim();
		String fertilizerDescription = edt_afet_fertidesc.getText().toString().trim();
		if (TextUtils.isEmpty(fertilizername)) {
			Toast.makeText(getApplicationContext(), "Please add fertilizer name!", Toast.LENGTH_SHORT).show();
			return;
		}
		String timestamp = "" + System.currentTimeMillis();
		if (image_uri == null) {
			addFertilizer("" + timestamp, "" + fertilizername, "" + fertilizertype, "" + fertilizerprize, "" + fertilizerDescription, "");
		} else {
			String filename = "Fertilizer_Imgs/" + "image" + timestamp;
			StorageReference storageReference = FirebaseStorage.getInstance().getReference(filename);
			storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
					while (!uriTask.isSuccessful()) ;
					Uri downloaduri = uriTask.getResult();
					if (uriTask.isSuccessful()) {
						addFertilizer("" + timestamp, "" + fertilizername, "" + fertilizertype, "" + fertilizerprize, "" + fertilizerDescription, "" + downloaduri);
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
	
	private void addFertilizer(String timestmp, String name, String type, String price, String description, String image) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("fertilizerId", timestmp);
		hashMap.put("name", name);
		hashMap.put("type", type);
		hashMap.put("price", price);
		hashMap.put("description", description);
		hashMap.put("image", image);
		hashMap.put("addeduser", mAuth.getUid());
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Fertilizers");
		ref.child(timestmp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				pd.dismiss();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				pd.setMessage("Not Published!");
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
				imv_afet_image.setImageURI(image_uri);
			} else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				imv_afet_image.setImageURI(image_uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
