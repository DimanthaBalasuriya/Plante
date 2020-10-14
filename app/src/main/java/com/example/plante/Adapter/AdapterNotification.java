package com.example.plante.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelNotification;
import com.example.plante.PostDetail;
import com.example.plante.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {
	
	private Context context;
	private ArrayList<ModelNotification> notificationList;
	private FirebaseAuth mAuth;
	
	public AdapterNotification(Context context, ArrayList<ModelNotification> notificationList) {
		this.context = context;
		this.notificationList = notificationList;
		
		mAuth = FirebaseAuth.getInstance();
	}
	
	@NonNull
	@Override
	public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false);
		return new HolderNotification(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
		ModelNotification model = notificationList.get(position);
		String name = model.getsName();
		String notification = model.getNotification();
		String timestamp = model.getTimestamp();
		String image = model.getsImage();
		String senderUid = model.getsUid();
		String pId = model.getpId();
		
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(Long.parseLong(timestamp));
		String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
		
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
		reference.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					String name = "" + ds.child("name").getValue();
					String image = "" + ds.child("image").getValue();
					String email = "" + ds.child("email").getValue();
					
					model.setsName(name);
					model.setsEmail(email);
					model.setsImage(image);
					
					holder.name.setText(name);
					
					try {
						Picasso.get().load(image).placeholder(R.drawable.ic_usernot).into(holder.image);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		holder.notification.setText(notification);
		holder.time.setText(pTime);
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PostDetail.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("postId", pId);
				context.startActivity(intent);
			}
		});
		
		holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete");
				builder.setMessage("Are you sure to delete this notification?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
						ref.child(mAuth.getUid()).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();
							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				return false;
			}
		});
		
	}
	
	@Override
	public int getItemCount() {
		return notificationList.size();
	}
	
	public class HolderNotification extends RecyclerView.ViewHolder {
		ImageView image;
		TextView name, notification, time;
		
		public HolderNotification(View view) {
			super(view);
			
			image = view.findViewById(R.id.imv_lint_image);
			name = view.findViewById(R.id.txv_lint_name);
			notification = view.findViewById(R.id.txv_lint_notification);
			time = view.findViewById(R.id.txv_lint_time);
		}
	}
}
