package com.example.plante.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Activities.CreatePost;
import com.example.plante.Post;
import com.example.plante.PostDetail;
import com.example.plante.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyViewHolder> {
	
	Context context;
	List<Post> postList;
	String userUID;
	
	private DatabaseReference likesRef;
	private DatabaseReference postsRef;
	boolean mProcessLike = false;
	
	public AdapterPost(Context context, List<Post> postList) {
		this.context = context;
		this.postList = postList;
		userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
		postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_home, parent, false);
		return new MyViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		String uid = postList.get(position).getUid();
		String uEmail = postList.get(position).getUemail();
		String uName = postList.get(position).getUname();
		String uDp = postList.get(position).getUprofile();
		String pId = postList.get(position).getPid();
		String pTitle = postList.get(position).getPtitle();
		String pDescription = postList.get(position).getPcontent();
		String pimage = postList.get(position).getpImage();
		String pTimeStamp = postList.get(position).getPtime();
		String pLikes = postList.get(position).getpLikes();
		String pComments = postList.get(position).getpComments();
		
		Toast.makeText(this.context, "UDP + " + uDp, Toast.LENGTH_LONG);
		
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
		String pTime = DateFormat.format("hh:mm: aa", calendar).toString();
		
		holder.userName.setText(uName);
		holder.postTime.setText(pTime);
		holder.postTitle.setText(pTitle);
		holder.postContent.setText(pDescription);
		holder.postLikes.setText(pLikes + "Votes");
		holder.postComments.setText(pComments + "Answers");
		setLikes(holder, pId);
		
		try {
			Picasso.get().load(uDp).placeholder(R.drawable.ic_usernot).into(holder.userImage);
		} catch (Exception e) {
			e.getMessage();
		}
		
		if ((pimage == null) || (pimage.equalsIgnoreCase("NoImage"))) {
			holder.postImage.setVisibility(View.GONE);
		} else {
			holder.postImage.setVisibility(View.VISIBLE);
			try {
				Picasso.get().load(pimage).into(holder.postImage);
			} catch (Exception e) {
				e.getMessage();
			}
		}
		
		holder.like.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pLikes = Integer.parseInt(postList.get(position).getpLikes());
				mProcessLike = true;
				String postIde = postList.get(position).getPid();
				likesRef.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (mProcessLike) {
							if (dataSnapshot.child(postIde).hasChild(userUID)) {
								postsRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
								likesRef.child(postIde).child(userUID).removeValue();
								mProcessLike = false;
							} else {
								postsRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
								likesRef.child(postIde).child(userUID).setValue("Liked");
								mProcessLike = false;
								addToHisNotifications("" + uid, "" + pId, "Liked your post!");
							}
						}
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		});
		
		holder.comment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PostDetail.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("postId", pId);
				context.startActivity(intent);
			}
		});
		
		holder.more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreOptions(holder.more, uid, userUID, pId, pimage);
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
	
	private void setLikes(MyViewHolder holder, String pId) {
		likesRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(pId).hasChild(userUID)) {
					holder.like.setTextColor(Color.GREEN);
					holder.like.setText("Voted");
				} else {
					holder.like.setTextColor(Color.BLACK);
					holder.like.setText("Vote");
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void showMoreOptions(ImageView more, String uid, String userUID, String pId, String pimage) {
		PopupMenu popupMenu = new PopupMenu(context, more, Gravity.END);
		if (uid.equals(userUID)) {
			popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
			popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
		}
		
		popupMenu.getMenu().add(Menu.NONE, 2, 0, "Comments");
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				if (id == 0) {
					beginDelete(pId, pimage);
				} else if (id == 1) {
					Intent intent = new Intent(context, CreatePost.class);
					intent.putExtra("key", "editPost");
					intent.putExtra("editPostId", pId);
					context.startActivity(intent);
				} else if (id == 2) {
					Intent intent = new Intent(context, PostDetail.class);
					intent.putExtra("postId", pId);
					context.startActivity(intent);
				}
				return false;
			}
		});
		popupMenu.show();
	}
	
	private void beginDelete(String pId, String pimage) {
		if ((pimage == null) || (pimage.equals("NoImage"))) {
			deleteWithoutImage(pId);
		} else {
			deleteWithImage(pId, pimage);
		}
	}
	
	private void deleteWithImage(String pId, String pimage) {
		StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pimage);
		picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pId);
				fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						for (DataSnapshot ds : dataSnapshot.getChildren()) {
							ds.getRef().removeValue();
						}
						Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				e.getMessage();
			}
		});
	}
	
	private void deleteWithoutImage(String pId) {
		Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pId);
		fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ds.getRef().removeValue();
				}
				Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return postList.size();
	}
	
	class MyViewHolder extends RecyclerView.ViewHolder {
		
		ImageView userImage, postImage, more;
		TextView userName, postTime, postTitle, postContent, postLikes, postComments;
		Button like, comment;
		
		public MyViewHolder(View itemView) {
			super(itemView);
			
			userImage = itemView.findViewById(R.id.imv_post_user_picture);
			more = itemView.findViewById(R.id.imv_post_more);
			postImage = itemView.findViewById(R.id.imv_post_image);
			userName = itemView.findViewById(R.id.txv_post_user_name);
			postTime = itemView.findViewById(R.id.txv_post_time);
			postTitle = itemView.findViewById(R.id.txv_post_title);
			postContent = itemView.findViewById(R.id.txv_post_content);
			postLikes = itemView.findViewById(R.id.txv_post_like_count);
			like = itemView.findViewById(R.id.btn_post_like);
			comment = itemView.findViewById(R.id.btn_post_comment);
			postComments = itemView.findViewById(R.id.txv_post_comment_count);
		}
	}
}
