package com.example.plante;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Activities.Login;
import com.example.plante.Activities.Useruser;
import com.example.plante.Adapter.AdapterPost;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Profile extends AppCompatActivity {
	
	private ImageView imv_prof_back, imv_prof_image, imv_prof_editprofile;
	private TextView txv_prof_name, txv_prof_bio, txv_prof_position, txv_prof_follower, txv_prof_follwing, txv_prof_postcount, textView2;
	private LinearLayout lyt_prof_following, lyt_prof_follower;
	private Button btn_follow_user;
	private Intent intent;
	private RecyclerView recyclerView;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser user;
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private StorageReference storageReference;
	
	private static final int CAMERA_REQUEST_CODE = 100;
	private static final int STORAGE_REQUEST_CODE = 200;
	private static final int IMAGE_PICK_GALLERY_CODE = 300;
	private static final int IMAGE_PICK_CAMERA_CODE = 400;
	
	private String cameraPermissions[];
	private String storagePermissions[];
	
	private List<Post> postList;
	private AdapterPost adapterPost;
	private String uid;
	
	private Uri uri;
	
	private String profilephoto;
	
	private String storagePath = "User_Profile_Cover_Images/";
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		imv_prof_back = findViewById(R.id.imv_prof_back);
		imv_prof_image = findViewById(R.id.imv_prof_image);
		imv_prof_editprofile = findViewById(R.id.imv_prof_edit);
		txv_prof_name = findViewById(R.id.txv_prof_name);
		txv_prof_bio = findViewById(R.id.txv_prof_bio);
		txv_prof_position = findViewById(R.id.txv_prof_position);
		txv_prof_follower = findViewById(R.id.txv_prof_position);
		txv_prof_follwing = findViewById(R.id.txv_prof_following);
		textView2 = findViewById(R.id.textView2);
		btn_follow_user = findViewById(R.id.btn_follow_user);
		txv_prof_postcount = findViewById(R.id.txv_prof_postcount);
		lyt_prof_following = findViewById(R.id.lyt_prof_following);
		lyt_prof_follower = findViewById(R.id.lyt_prof_followers);
		recyclerView = findViewById(R.id.rcv_prof_view);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		firebaseAuth = FirebaseAuth.getInstance();
		user = firebaseAuth.getCurrentUser();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("User");
		storageReference = FirebaseStorage.getInstance().getReference();
		
		cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		
		Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
		
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					String name = "" + ds.child("name").getValue();
					String image = "" + ds.child("image").getValue();
					String position = "" + ds.child("position").getValue();
					String bio = "" + ds.child("bio").getValue();
					
					txv_prof_name.setText(name + "");
					txv_prof_position.setText(position + "");
					txv_prof_bio.setText(bio + "");
					
					if (!image.equalsIgnoreCase("null")) {
						try {
							Picasso.get().load(image).into(imv_prof_image);
						} catch (Exception e) {
							System.out.println("PIC" + e);
						}
					} else {
						try {
							Picasso.get().load(R.drawable.ic_usernot).into(imv_prof_image);
						} catch (Exception e) {
							System.out.println("PIC" + e);
						}
					}
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		imv_prof_editprofile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEditProfileDialog();
			}
		});
		
		imv_prof_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		checkUserStatus();
		
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		layoutManager.setStackFromEnd(true);
		layoutManager.setReverseLayout(true);
		recyclerView.setLayoutManager(layoutManager);
		postList = new ArrayList<>();
		loadMyPosts();
		getFollowers();
		getPostCount();
		checkFollow();
		
		btn_follow_user.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String btn = btn_follow_user.getText().toString();
				if (btn.equalsIgnoreCase("Follow")) {
					FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid()).child("Following").child(uid).setValue(true);
					FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("Followers").child(user.getUid()).setValue(true);
				} else if (btn.equalsIgnoreCase("Following")) {
					FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid()).child("Following").child(uid).removeValue();
					FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("Followers").child(user.getUid()).removeValue();
				}
			}
		});
		
		lyt_prof_following.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Useruser.class);
				intent.putExtra("id", uid);
				intent.putExtra("title", "Following");
				startActivity(intent);
			}
		});
		
		lyt_prof_follower.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Useruser.class);
				intent.putExtra("id", uid);
				intent.putExtra("title", "Followers");
				startActivity(intent);
			}
		});
		
	}
	
	private void changeToEnglish() {
		textView2.setText(R.string.profile);
	}
	
	private void changeToSinhala() {
		textView2.setText(R.string.sinhala_profile);
	}
	
	private void loadMyPosts() {
		System.out.println("Hello My Post");
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		Query query = ref.orderByChild("uid").equalTo(uid);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				System.out.println("Hello My Post1");
				postList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					Post p = ds.getValue(Post.class);
					postList.add(p);
					adapterPost = new AdapterPost(getApplicationContext(), postList);
					recyclerView.setAdapter(adapterPost);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private boolean checkStoragePermission() {
		boolean result = ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_DENIED);
		return result;
	}
	
	private void requestStoragePermission() {
		ActivityCompat.requestPermissions(Profile.this, storagePermissions, STORAGE_REQUEST_CODE);
	}
	
	private boolean checkCameraPermission() {
		boolean result1 = ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
		
		boolean result2 = ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
		return result1 && result2;
	}
	
	private void requestCameraPermission() {
		ActivityCompat.requestPermissions(Profile.this, cameraPermissions, CAMERA_REQUEST_CODE);
	}
	
	private void showEditProfileDialog() {
		String option[] = {"Profile picture ", "Position ", "Name ", "Bio"};
		AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
		builder.setTitle("Choose option");
		builder.setItems(option, new DialogInterface.OnClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					profilephoto = "image";
					showImagePicDialog();
				} else if (which == 1) {
					showPositionChangeDialog();
				} else if (which == 2) {
					showNameChangeDialog();
				} else if (which == 3) {
					showBioChangeDialog();
				}
			}
		});
		builder.create().show();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void showBioChangeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
		builder.setTitle("Update Bio");
		LinearLayout linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(40, 20, 40, 10);
		EditText editText = new EditText(getApplicationContext());
		editText.setHint("Enter Bio");
		editText.setPadding(10, 0, 0, 0);
		editText.setHintTextColor(getColor(R.color.colorWhite));
		editText.setTextColor(getColor(R.color.colorWhite));
		linearLayout.addView(editText);
		builder.setView(linearLayout);
		
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
				String value = editText.getText().toString().trim();
				if (!TextUtils.isEmpty(value)) {
					HashMap<String, Object> result = new HashMap<>();
					result.put("bio", value);
					databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(getApplicationContext(), "Not updated..." + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					Toast.makeText(getApplicationContext(), "Please enter Bio!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void showNameChangeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
		builder.setTitle("Update Name");
		LinearLayout linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(40, 20, 40, 10);
		EditText editText = new EditText(getApplicationContext());
		editText.setHint("Enter Name");
		editText.setPadding(10, 0, 0, 0);
		editText.setHintTextColor(getColor(R.color.colorWhite));
		editText.setTextColor(getColor(R.color.colorWhite));
		linearLayout.addView(editText);
		builder.setView(linearLayout);
		
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = editText.getText().toString().trim();
				if (!TextUtils.isEmpty(value)) {
					HashMap<String, Object> result = new HashMap<>();
					result.put("name", value);
					databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(getApplicationContext(), "Not updated..." + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
					
					DatabaseReference refe = FirebaseDatabase.getInstance().getReference("Posts");
					Query query = refe.orderByChild("uid").equalTo(uid);
					query.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot ds : dataSnapshot.getChildren()) {
								String child = ds.getKey();
								dataSnapshot.getRef().child(child).child("uname").setValue(value);
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
					refe.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot ds : dataSnapshot.getChildren()) {
								String child = ds.getKey();
								if (dataSnapshot.child(child).hasChild("Comments")) {
									String child1 = "" + dataSnapshot.child(child).getKey();
									Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
									child2.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											for (DataSnapshot ds : dataSnapshot.getChildren()) {
												String child = ds.getKey();
												dataSnapshot.getRef().child(child).child("uname").setValue(value);
											}
										}
										
										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {
										
										}
									});
								}
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
				} else {
					Toast.makeText(getApplicationContext(), "Please enter Name!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void showPositionChangeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
		builder.setTitle("Update Position");
		LinearLayout linearLayout = new LinearLayout(getApplicationContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setPadding(40, 20, 40, 10);
		EditText editText = new EditText(getApplicationContext());
		editText.setHint("Enter position");
		editText.setPadding(10, 0, 0, 0);
		editText.setHintTextColor(getColor(R.color.colorWhite));
		editText.setTextColor(getColor(R.color.colorWhite));
		linearLayout.addView(editText);
		builder.setView(linearLayout);
		
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = editText.getText().toString().trim();
				if (!TextUtils.isEmpty(value)) {
					HashMap<String, Object> result = new HashMap<>();
					result.put("position", value);
					databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(getApplicationContext(), "Updated...", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(getApplicationContext(), "Not updated..." + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					Toast.makeText(getApplicationContext(), "Please enter Position!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	private void showImagePicDialog() {
		String option[] = {"Take photo ", "Choose photo"};
		AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
		builder.setTitle("Upload Image");
		builder.setItems(option, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					if (!checkCameraPermission()) {
						requestCameraPermission();
					} else {
						pickFromCamera();
					}
				} else if (which == 1) {
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
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		
		switch (requestCode) {
			case CAMERA_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					if (cameraAccepted && writeStorageAccepted) {
						pickFromCamera();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Please enable camera & storage Permission.", Toast.LENGTH_SHORT).show();
				}
			}
			break;
			case STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0) {
					boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					if (writeStorageAccepted) {
						pickFromGallery();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Please enable storage Permission.", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}
	
	private void pickFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
	}
	
	private void pickFromCamera() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
		values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
		uri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_PICK_GALLERY_CODE) {
				uri = data.getData();
				uploadProfileImage(uri);
			}
			if (requestCode == IMAGE_PICK_CAMERA_CODE) {
				//uri = data.getData();
				uploadProfileImage(uri);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void uploadProfileImage(Uri uri) {
		String filePathAndName = storagePath + "" + profilephoto + "_" + user.getUid();
		StorageReference storageReference2nd = storageReference.child(filePathAndName);
		storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
				while (!uriTask.isSuccessful()) ;
				Uri downloadUri = uriTask.getResult();
				if (uriTask.isSuccessful()) {
					HashMap<String, Object> results = new HashMap<>();
					results.put(profilephoto, downloadUri.toString());
					databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(getApplicationContext(), "Image Updated...", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(getApplicationContext(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
						}
					});
					
					DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
					Query query = ref.orderByChild("uid").equalTo(uid);
					query.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot ds : dataSnapshot.getChildren()) {
								String child = ds.getKey();
								dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
					
					ref.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot ds : dataSnapshot.getChildren()) {
								String child = ds.getKey();
								if (dataSnapshot.child(child).hasChild("Comments")) {
									String child1 = "" + dataSnapshot.child(child).getKey();
									Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
									child2.addValueEventListener(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											for (DataSnapshot ds : dataSnapshot.getChildren()) {
												String child = ds.getKey();
												dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
											}
										}
										
										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {
										
										}
									});
								}
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
				} else {
					Toast.makeText(getApplicationContext(), "Some error ocurred", Toast.LENGTH_SHORT).show();
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			uid = user.getUid();
		} else {
			startActivity(new Intent(getApplicationContext(), Login.class));
			finish();
		}
	}
	
	private void getFollowers() {
		DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("Followers");
		ref1.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				txv_prof_follower.setText("" + dataSnapshot.getChildrenCount());
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("Following");
		ref2.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				txv_prof_follwing.setText("" + dataSnapshot.getChildrenCount());
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getPostCount() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				int i = 0;
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					Post post = snapshot.getValue(Post.class);
					if (post.getUid().equals(uid)) {
						i++;
					}
				}
				txv_prof_postcount.setText("" + i);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void checkFollow() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid()).child("Following");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(uid).exists()) {
					btn_follow_user.setText("Following");
				} else {
					btn_follow_user.setText("Follow");
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
}
