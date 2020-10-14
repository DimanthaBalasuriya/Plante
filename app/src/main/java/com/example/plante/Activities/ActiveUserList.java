package com.example.plante.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterModelUser;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.Parent;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActiveUserList extends Fragment {
	
	View v;
	
	String id;
	String title;
	
	List<String> idList;
	RecyclerView rcv_ativ_userlist;
	AdapterModelUser userAdapter;
	List<ModelUser> userList;
	FirebaseAuth firebaseAuth;
	
	public ActiveUserList() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_active_user_list, container, false);
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		rcv_ativ_userlist = v.findViewById(R.id.rcv_ativ_list);
		rcv_ativ_userlist.setHasFixedSize(true);
		rcv_ativ_userlist.setLayoutManager(new LinearLayoutManager(getContext()));
		userList = new ArrayList<>();
		userAdapter = new AdapterModelUser(getContext(), userList);
		rcv_ativ_userlist.setAdapter(userAdapter);
		idList = new ArrayList<>();
		
		checkUserStatus();
		getFollowers();
		getFollowing();
		
		return v;
	}
	
	private void getFollowing() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("Following");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				idList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					idList.add(d.getKey());
				}
				showUsers();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getFollowers() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("Followers");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				idList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					idList.add(d.getKey());
				}
				showUsers();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void showUsers() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
		reference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelUser user = d.getValue(ModelUser.class);
					for (String id : idList) {
						if (user.getUid().equals(id)) {
							userList.add(user);
						}
					}
				}
				userAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			id = user.getUid();
		} else {
			startActivity(new Intent(getActivity(), Parent.class));
			getActivity().finish();
		}
	}
	
}
