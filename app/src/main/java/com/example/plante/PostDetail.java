package com.example.plante;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Activities.CreatePost;
import com.example.plante.Activities.Login;
import com.example.plante.Adapter.AdapterComment;
import com.example.plante.Base_module.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetail extends AppCompatActivity {
	
	private ImageView userImage, postImage, more;
	private ImageView back;
	private TextView title, time, content, username, likes, comments;
	private Button likeBtn;
	private LinearLayout profileLayout;
	private RecyclerView recyclerView;
	
	private List<ModelComment> modelCommentList;
	private AdapterComment adapterComment;
	
	private boolean mProcessComment = false;
	private boolean mProcessLike = false;
	
	private EditText comment;
	private ImageButton sendBtn;
	private ImageView userProfileImage;
	
	private String myUid, myEmail, myName, myDp, postId, pLikes, hisUid, hisDp, hisName, pImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_detail);
		
		Intent intent = getIntent();
		postId = intent.getStringExtra("postId");
		
		back = findViewById(R.id.imv_comments_back);
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		userImage = findViewById(R.id.imv_post_user_picture);
		postImage = findViewById(R.id.imv_post_image);
		more = findViewById(R.id.imv_post_more);
		title = findViewById(R.id.txv_post_title);
		time = findViewById(R.id.txv_post_time);
		content = findViewById(R.id.txv_post_content);
		username = findViewById(R.id.txv_post_user_name);
		likes = findViewById(R.id.txv_post_like_count);
		comments = findViewById(R.id.txv_post_comment_count);
		likeBtn = findViewById(R.id.btn_post_like);
		profileLayout = findViewById(R.id.profilelayout);
		comment = findViewById(R.id.edt_comment_content);
		sendBtn = findViewById(R.id.btn_comment_send);
		userProfileImage = findViewById(R.id.imv_comment_user);
		recyclerView = findViewById(R.id.rcv_comment_list);
		
		loadPostInfo();
		
		checkUserStatus();
		
		loadUserInfo();
		
		setLikes();
		
		loadComments();
		
		sendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postComment();
			}
		});
		
		likeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				likePost();
			}
		});
		
		more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreOptions();
			}
		});
		
	}
	
	private void addToHisNotifications(String hisUid, String pId, String notification) {
		String timestamp = "" + System.currentTimeMillis();
		
		HashMap<Object, String> hashMap = new HashMap<>();
		hashMap.put("pId", pId);
		hashMap.put("timestamp", timestamp);
		hashMap.put("pUid", hisUid);
		hashMap.put("notification", notification);
		hashMap.put("sUid", pId);
		
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
		reference.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
			
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
			
			}
		});
	}
	
	private void loadComments() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		recyclerView.setLayoutManager(layoutManager);
		
		modelCommentList = new ArrayList<>();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelCommentList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ModelComment modelComment = ds.getValue(ModelComment.class);
					modelCommentList.add(modelComment);
					adapterComment = new AdapterComment(getApplicationContext(), modelCommentList, myUid, postId);
					recyclerView.setAdapter(adapterComment);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void showMoreOptions() {
		PopupMenu popupMenu = new PopupMenu(getApplicationContext(), more, Gravity.END);
		if (hisUid.equals(myUid)) {
			popupMenu.getMenu().add(android.view.Menu.NONE, 0, 0, "Delete");
			popupMenu.getMenu().add(android.view.Menu.NONE, 1, 0, "Edit");
		}
		
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				if (id == 0) {
					beginDelete();
				} else if (id == 1) {
					Intent intent = new Intent(getApplicationContext(), CreatePost.class);
					intent.putExtra("key", "editPost");
					intent.putExtra("editPostId", postId);
					startActivity(intent);
				}
				return false;
			}
		});
		popupMenu.show();
	}
	
	private void beginDelete() {
		System.out.println("DFLIP");
		if ((pImage == null) || (pImage.equals("NoImage"))) {
			System.out.println("MFLIP");
			deleteWithoutImage();
		} else {
			System.out.println("KFLIP");
			deleteWithImage();
		}
	}
	
	private void deleteWithoutImage() {
		Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(postId);
		fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ds.getRef().removeValue();
				}
				Toast.makeText(getApplicationContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void deleteWithImage() {
		StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
		picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
				fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						for (DataSnapshot ds : dataSnapshot.getChildren()) {
							ds.getRef().removeValue();
						}
						Toast.makeText(getApplicationContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "Hello1" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void setLikes() {
		final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
		likesRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(postId).hasChild(myUid)) {
					likeBtn.setText("Voted");
				} else {
					likeBtn.setText("Vote");
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void likePost() {
		mProcessLike = true;
		DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
		DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
		likesRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (mProcessLike) {
					if (dataSnapshot.child(postId).hasChild(myUid)) {
						postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
						likesRef.child(postId).child(myUid).removeValue();
						mProcessLike = false;
					} else {
						postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
						likesRef.child(postId).child(myUid).setValue("Liked");
						mProcessLike = false;
						addToHisNotifications("" + hisUid, "" + postId, "Liked your post!");
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void postComment() {
		String commentContent = comment.getText().toString().trim();
		if (TextUtils.isEmpty(commentContent)) {
			Toast.makeText(this, "Answer is empty...", Toast.LENGTH_SHORT).show();
			return;
		}
		String timeStamp = String.valueOf(System.currentTimeMillis());
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("cId", timeStamp);
		hashMap.put("comment", comment.getText().toString());
		hashMap.put("timestamp", timeStamp);
		hashMap.put("uid", myUid);
		hashMap.put("uEmail", myEmail);
		hashMap.put("uDp", myDp);
		hashMap.put("uName", myName);
		
		ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Toast.makeText(getApplicationContext(), "Answer Added...", Toast.LENGTH_SHORT).show();
				comment.setText("");
				updateCommentCount();
				
				addToHisNotifications("" + hisUid, "" + postId, "Answer to your post!");
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void updateCommentCount() {
		mProcessComment = true;
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (mProcessComment) {
					String comments = "" + dataSnapshot.child("pComments").getValue();
					int newCommentVal = Integer.parseInt(comments) + 1;
					ref.child("pComments").setValue("" + newCommentVal);
					mProcessComment = false;
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadUserInfo() {
		Query myRef = FirebaseDatabase.getInstance().getReference("User");
		myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					myName = "" + ds.child("name").getValue();
					myDp = ds.child("image").getValue().toString();
					try {
						Picasso.get().load(myDp).placeholder(R.drawable.ic_usernot).into(userImage);
					} catch (Exception e) {
						Picasso.get().load(R.drawable.ic_usernot).into(userImage);
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void loadPostInfo() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		Query query = ref.orderByChild("pid").equalTo(postId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					String pTitle = "" + ds.child("ptitle").getValue();
					String pDescr = "" + ds.child("pcontent").getValue();
					pLikes = "" + ds.child("pLikes").getValue();
					String pTimeStamp = "" + ds.child("ptime").getValue();
					pImage = "" + ds.child("pImage").getValue();
					hisDp = "" + ds.child("uDp").getValue();
					hisUid = "" + ds.child("uid").getValue();
					String uEmail = "" + ds.child("uemail").getValue();
					hisName = "" + ds.child("uname").getValue();
					String commentCount = "" + ds.child("pComments").getValue();
					
					Calendar calendar = Calendar.getInstance(Locale.getDefault());
					calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
					String pTime = DateFormat.format("dd/MM/yyyy hh:mm: aa", calendar).toString();
					
					title.setText(pTitle);
					content.setText(pDescr);
					likes.setText(pLikes + "Likes");
					time.setText(pTime);
					comments.setText(commentCount + "Comments");
					
					username.setText(hisName);
					
					if (pImage.equals("NoImage")) {
						postImage.setVisibility(View.GONE);
					} else {
						postImage.setVisibility(View.VISIBLE);
						try {
							Picasso.get().load(pImage).into(postImage);
						} catch (Exception e) {
						
						}
					}
					
					try {
						Picasso.get().load(hisDp).placeholder(R.drawable.ic_usernot).into(userImage);
					} catch (Exception e) {
						Picasso.get().load(R.drawable.ic_usernot).into(userImage);
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void checkUserStatus() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			myEmail = user.getEmail();
			myUid = user.getUid();
		} else {
			startActivity(new Intent(this, Login.class));
		}
	}
}
