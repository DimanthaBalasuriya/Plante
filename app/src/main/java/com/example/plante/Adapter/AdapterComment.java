package com.example.plante.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelComment;
import com.example.plante.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder> {
	
	Context context;
	List<ModelComment> modelCommentAdapterList;
	String myUid, postId;
	
	public AdapterComment(Context context, List<ModelComment> modelCommentAdapterList, String myUid, String postId) {
		this.context = context;
		this.modelCommentAdapterList = modelCommentAdapterList;
		this.myUid = myUid;
		this.postId = postId;
	}
	
	@NonNull
	@Override
	public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_comment, parent, false);
		return new MyHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyHolder holder, int position) {
		String uid = modelCommentAdapterList.get(position).getuId();
		String name = modelCommentAdapterList.get(position).getuName();
		String email = modelCommentAdapterList.get(position).getuEmail();
		String image = modelCommentAdapterList.get(position).getuDp();
		String cid = modelCommentAdapterList.get(position).getcId();
		String comment = modelCommentAdapterList.get(position).getComment();
		String timestamp = modelCommentAdapterList.get(position).getTimestamp();
		
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(Long.parseLong(timestamp));
		String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
		
		holder.txv_comment_username.setText(name);
		holder.txv_comment_content.setText(comment);
		holder.txv_comment_time.setText(pTime);
		
		try {
			Picasso.get().load(image).placeholder(R.drawable.ic_usernot).into(holder.imv_comment_list_userProfileImage);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("MYUID" + myUid);
				System.out.println("UID" + uid);
				if (myUid.equals(uid)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
					builder.setTitle("Delete...");
					builder.setMessage("Are you sure?");
					builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteComment(cid);
						}
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				} else {
					Toast.makeText(context, "Cant' Delete this ModelComment!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
	
	private void deleteComment(String cid) {
		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
		ref.child("Comments").child(cid).removeValue();
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String comments = "" + dataSnapshot.child("pComments").getValue();
				int newCommentVal = Integer.parseInt(comments) - 1;
				ref.child("pComments").setValue("" + newCommentVal);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return modelCommentAdapterList.size();
	}
	
	class MyHolder extends RecyclerView.ViewHolder {
		ImageView imv_comment_list_userProfileImage;
		TextView txv_comment_username, txv_comment_content, txv_comment_time;
		
		public MyHolder(View itemView) {
			super(itemView);
			imv_comment_list_userProfileImage = itemView.findViewById(R.id.imv_comment_lit_user_image);
			txv_comment_username = itemView.findViewById(R.id.txv_comment_list_username);
			txv_comment_content = itemView.findViewById(R.id.txv_comment_list_content);
			txv_comment_time = itemView.findViewById(R.id.txv_comment_list_time);
		}
	}
	
}
