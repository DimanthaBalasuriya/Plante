package com.example.plante.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelGroupChat;
import com.example.plante.R;
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

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
	
	private static final int MSG_TYPE_LEFT = 0;
	private static final int MSG_TYPE_RIGHT = 1;
	
	private Context context;
	private ArrayList<ModelGroupChat> modelGroupChats;
	
	private FirebaseAuth firebaseAuth;
	
	public AdapterGroupChat() {
	}
	
	public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChats) {
		this.context = context;
		this.modelGroupChats = modelGroupChats;
		firebaseAuth = FirebaseAuth.getInstance();
	}
	
	@NonNull
	@Override
	
	public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == MSG_TYPE_RIGHT) {
			View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
			return new HolderGroupChat(view);
		} else {
			View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
			return new HolderGroupChat(view);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
		ModelGroupChat model = modelGroupChats.get(position);
		String message = model.getMessage();
		String timestamp = model.getTimestamp();
		String senderUid = model.getSender();
		String messageType = model.getType();
		
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(Long.parseLong(timestamp));
		String dateTime = DateFormat.format("hh:mm aa", cal).toString();
		
		if (messageType.equals("text")) {
			holder.imageMessage.setVisibility(View.GONE);
			holder.message.setVisibility(View.VISIBLE);
			holder.message.setText(message);
		} else {
			holder.imageMessage.setVisibility(View.VISIBLE);
			holder.message.setVisibility(View.GONE);
			try {
				Picasso.get().load(message).placeholder(R.drawable.ic_image).into(holder.imageMessage);
			} catch (Exception e) {
				holder.imageMessage.setImageResource(R.drawable.ic_image);
			}
		}
		
		//holder.message.setText(message);
		holder.time.setText(dateTime);
		setUserName(model, holder);
	}
	
	private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
		ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = "" + d.child("name").getValue();
					holder.name.setText(name);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return modelGroupChats.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		if (modelGroupChats.get(position).getSender().equals(firebaseAuth.getUid())) {
			return MSG_TYPE_RIGHT;
		} else {
			return MSG_TYPE_LEFT;
		}
	}
	
	class HolderGroupChat extends RecyclerView.ViewHolder {
		
		private TextView name, message, time;
		private ImageView imageMessage;
		
		public HolderGroupChat(View itemView) {
			super(itemView);
			
			name = itemView.findViewById(R.id.txv_gct_name);
			message = itemView.findViewById(R.id.txv_gct_message);
			time = itemView.findViewById(R.id.txv_gct_time);
			imageMessage = itemView.findViewById(R.id.imv_gct_imagemessage);
		}
	}
	
}
