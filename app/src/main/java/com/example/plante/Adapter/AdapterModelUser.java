package com.example.plante.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.MessageFrame;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterModelUser extends RecyclerView.Adapter<AdapterModelUser.MyHolder> {
	
	Context context;
	List<ModelUser> userList;
	
	private FirebaseUser firebaseUser;
	
	public AdapterModelUser(Context context, List<ModelUser> userList) {
		this.context = context;
		this.userList = userList;
	}
	
	@NonNull
	@Override
	public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_user_list, parent, false);
		return new MyHolder(view);
		
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyHolder holder, int position) {
		
		firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		final ModelUser user = userList.get(position);
		
		holder.btn_follow.setVisibility(View.VISIBLE);
		
		String hisuid = userList.get(position).getUid();
		String image = userList.get(position).getImage();
		String name = userList.get(position).getName();
		String type = userList.get(position).getPosition();
		
		holder.txv_auli_name.setText(name);
		holder.txv_auli_position.setText(type);
		
		try {
			Picasso.get().load(image).placeholder(R.drawable.ic_usernot).into(holder.imv_auli_profile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MessageFrame.class);
				intent.putExtra("hisuid", hisuid);
				context.startActivity(intent);
			}
		});
		
		isFollowing(user.getUid(), holder.btn_follow);
		
		if (user.getUid().equals(firebaseUser.getUid())) {
			holder.btn_follow.setVisibility(View.GONE);
		}
		
/*		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
				editor.putString("profileid", user.getUid());
				editor.apply();
				
				//((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Chat()).commit();
			}
		});*/
		
		holder.btn_follow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (holder.btn_follow.getText().toString().equalsIgnoreCase("Follow")) {
					FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(user.getUid()).setValue(true);
					FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid()).child("Followers").child(firebaseUser.getUid()).setValue(true);
				} else {
					FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(user.getUid()).removeValue();
					FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUid()).child("Followers").child(firebaseUser.getUid()).removeValue();
				}
			}
		});
		
	}
	
	@Override
	public int getItemCount() {
		return userList.size();
	}
	
	class MyHolder extends RecyclerView.ViewHolder {
		ImageView imv_auli_profile;
		Button btn_follow;
		TextView txv_auli_name, txv_auli_position;
		
		public MyHolder(View itemView) {
			super(itemView);
			imv_auli_profile = itemView.findViewById(R.id.imv_liul_userimage);
			txv_auli_name = itemView.findViewById(R.id.txv_liul_username);
			txv_auli_position = itemView.findViewById(R.id.txv_liul_usertype);
			btn_follow = itemView.findViewById(R.id.btn_follow);
		}
	}
	
	private void isFollowing(String userid, Button btn_follo) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.child(userid).exists()) {
					btn_follo.setText("Following");
				} else {
					btn_follo.setText("Follow");
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
}
