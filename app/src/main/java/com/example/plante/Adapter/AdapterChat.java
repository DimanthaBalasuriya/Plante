package com.example.plante.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelChat;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
	
	private static final int MSG_TYPE_LEFT = 0;
	private static final int MSG_TYPE_RIGHT = 1;
	
	Context context;
	List<ModelChat> chatList;
	String imageUrl;
	
	FirebaseUser firebaseUser;
	
	public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
		this.context = context;
		this.chatList = chatList;
		this.imageUrl = imageUrl;
	}
	
	@NonNull
	@Override
	public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == MSG_TYPE_LEFT) {
			View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
			return new MyHolder(view);
		} else {
			View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
			return new MyHolder(view);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyHolder holder, int position) {
		String message = chatList.get(position).getMessage();
		String timeStamp = chatList.get(position).getTimestamp();
		String type = chatList.get(position).getType();
		
		if (type.equals("text")) {
			holder.message.setVisibility(View.VISIBLE);
			holder.messageImage.setVisibility(View.GONE);
			holder.message.setText(message);
		} else {
			holder.message.setVisibility(View.GONE);
			holder.messageImage.setVisibility(View.VISIBLE);
			Picasso.get().load(message).placeholder(R.drawable.ic_image).into(holder.messageImage);
		}
		
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(timeStamp));
		String dateTime = DateFormat.format("hh:mm aa", cal).toString();
		
		holder.message.setText(message);
		holder.time.setText(dateTime);
		
		try {
			Picasso.get().load(imageUrl).into(holder.userImage);
		} catch (Exception e) {
		
		}
		
		holder.lyt_msg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Delete");
				builder.setMessage("Are you sure to delete this?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteMessage(position);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		});
		
		if (position == chatList.size() - 1) {
			if (chatList.get(position).isSeen()) {
				holder.isSeen.setText("Seen");
			} else {
				holder.isSeen.setText("Delivered");
			}
		} else {
			holder.isSeen.setVisibility(View.GONE);
		}
	}
	
	private void deleteMessage(int position) {
		
		String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
		
		String msgTimeStamp = chatList.get(position).getTimestamp();
		DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
		Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					if (d.child("sender").getValue().equals(myUID)) {
						d.getRef().removeValue();
						HashMap<String, Object> hashMap = new HashMap<>();
						hashMap.put("message", "This message was deleted...");
						d.getRef().updateChildren(hashMap);
						Toast.makeText(context, "This message was deleted...", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, "You cannot delete this message...", Toast.LENGTH_SHORT).show();
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return chatList.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
			return MSG_TYPE_RIGHT;
		} else {
			return MSG_TYPE_LEFT;
		}
	}
	
	class MyHolder extends RecyclerView.ViewHolder {
		ImageView userImage, messageImage;
		TextView message, time, isSeen;
		LinearLayout lyt_msg;
		
		public MyHolder(View itemView) {
			super(itemView);
			userImage = itemView.findViewById(R.id.imv_profileImage);
			message = itemView.findViewById(R.id.txv_message);
			time = itemView.findViewById(R.id.txv_time);
			isSeen = itemView.findViewById(R.id.txv_msgState);
			lyt_msg = itemView.findViewById(R.id.lyt_msg);
			messageImage = itemView.findViewById(R.id.imv_msg_image);
		}
	}
}
