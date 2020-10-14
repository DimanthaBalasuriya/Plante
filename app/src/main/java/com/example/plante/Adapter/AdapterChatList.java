package com.example.plante.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.MessageFrame;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {
	
	Context context;
	List<ModelUser> userList;
	private HashMap<String, String> lastMessageMap;
	
	public AdapterChatList(Context context, List<ModelUser> userList) {
		this.context = context;
		this.userList = userList;
		lastMessageMap = new HashMap<>();
	}
	
	@NonNull
	@Override
	public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_chat_list, parent, false);
		return new MyHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyHolder holder, int position) {
		String hisUid = userList.get(position).getUid();
		String userImage = userList.get(position).getImage();
		String userName = userList.get(position).getName();
		String lastMessage = lastMessageMap.get(hisUid);
		
		holder.txv_licl_name.setText(userName);
		
		if (lastMessage == null || lastMessage.equals("default")) {
			holder.txv_licl_message.setVisibility(View.GONE);
		} else {
			holder.txv_licl_message.setVisibility(View.VISIBLE);
			holder.txv_licl_message.setText(lastMessage);
		}
		
		try {
			Picasso.get().load(userImage).placeholder(R.drawable.ic_usernot).into(holder.imv_licl_image);
		} catch (Exception e) {
			Picasso.get().load(R.drawable.ic_usernot).into(holder.imv_licl_image);
		}
		
		if (userList.get(position).getOnlineStatus().equals("online")) {
			holder.imv_licl_state.setImageResource(R.drawable.circle_online);
		} else {
			holder.imv_licl_state.setImageResource(R.drawable.circle_offline);
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MessageFrame.class);
				intent.putExtra("hisuid", hisUid);
				context.startActivity(intent);
			}
		});
	}
	
	public void setLastMessageMap(String userID, String lastMessage) {
		lastMessageMap.put(userID, lastMessage);
	}
	
	@Override
	public int getItemCount() {
		return userList.size();
	}
	
	class MyHolder extends RecyclerView.ViewHolder {
		ImageView imv_licl_image, imv_licl_state;
		TextView txv_licl_name, txv_licl_message;
		
		public MyHolder(View itemView) {
			super(itemView);
			imv_licl_image = itemView.findViewById(R.id.imv_licl_image);
			imv_licl_state = itemView.findViewById(R.id.imv_licl_state);
			txv_licl_name = itemView.findViewById(R.id.txv_licl_name);
			txv_licl_message = itemView.findViewById(R.id.txv_licl_message);
		}
	}
}
