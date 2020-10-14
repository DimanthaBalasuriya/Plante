package com.example.plante.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.GroupChatFrame;
import com.example.plante.Base_module.ModelGroupChatList;
import com.example.plante.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {
	
	private Context context;
	private ArrayList<ModelGroupChatList> groupChatLists;
	
	public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
		this.context = context;
		this.groupChatLists = groupChatLists;
	}
	
	@NonNull
	@Override
	public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_group_chat, parent, false);
		return new HolderGroupChatList(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
		ModelGroupChatList model = groupChatLists.get(position);
		String groupId = model.getGroupId();
		String groupIcon = model.getGroupIcon();
		String groupTitle = model.getGroupTitle();
		
		holder.txv_grup_groupname.setText("");
		holder.txv_grup_time.setText("");
		holder.txv_grup_message.setText("");
		
		loadLastMessage(model, holder);
		
		holder.txv_grup_groupname.setText(groupTitle);
		try {
			Picasso.get().load(groupIcon).placeholder(R.drawable.ic_usernot).into(holder.imv_grup_image);
		} catch (Exception e) {
			holder.imv_grup_image.setImageResource(R.drawable.ic_usernot);
		}
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, GroupChatFrame.class);
				intent.putExtra("groupId", groupId);
				context.startActivity(intent);
			}
		});
	}
	
	private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
		ref.child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String message = "" + d.child("message").getValue();
					String timestamp = "" + d.child("timestamp").getValue();
					String sender = "" + d.child("sender").getValue();
					String messageType = "" + d.child("type").getValue();
					
					Calendar cal = Calendar.getInstance(Locale.ENGLISH);
					cal.setTimeInMillis(Long.parseLong(timestamp));
					String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
					
					if (messageType.equals("image")) {
						holder.txv_grup_message.setText("Sent photo");
					} else {
						holder.txv_grup_message.setText(message);
					}
					holder.txv_grup_time.setText(dateTime);
					
					DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
					ref.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot d : dataSnapshot.getChildren()) {
								String name = "" + d.child("name").getValue();
								holder.txv_grup_sendername.setText(name);
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return groupChatLists.size();
	}
	
	class HolderGroupChatList extends RecyclerView.ViewHolder {
		
		private ImageView imv_grup_image;
		private TextView txv_grup_groupname, txv_grup_sendername, txv_grup_message, txv_grup_time;
		
		public HolderGroupChatList(View itemView) {
			super(itemView);
			imv_grup_image = itemView.findViewById(R.id.imv_grup_image);
			txv_grup_groupname = itemView.findViewById(R.id.txv_grup_groupname);
			txv_grup_sendername = itemView.findViewById(R.id.txv_grup_sendername);
			txv_grup_message = itemView.findViewById(R.id.txv_grup_message);
			txv_grup_time = itemView.findViewById(R.id.txv_grup_time);
		}
		
	}
	
}
