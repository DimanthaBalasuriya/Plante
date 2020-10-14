package com.example.plante;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Activities.Login;
import com.example.plante.Adapter.AdapterGroupChatList;
import com.example.plante.Base_module.ModelGroupChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserGroupChat extends Fragment {
	
	private View view;
	private RecyclerView rcv_ugch_list;
	private FirebaseAuth firebaseAuth;
	
	private ArrayList<ModelGroupChatList> groupChatLists;
	private AdapterGroupChatList adapterGroupChatList;
	
	public UserGroupChat() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.activity_user_group_chat, container, false);
		
		rcv_ugch_list = view.findViewById(R.id.rcv_ugch_list);
		
		firebaseAuth = FirebaseAuth.getInstance();
		
		loadGroupChatList();
		
		return view;
	}
	
	private void loadGroupChatList() {
		groupChatLists = new ArrayList<>();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				groupChatLists.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					if (d.child("Participants").child(firebaseAuth.getUid()).exists()) {
						ModelGroupChatList model = d.getValue(ModelGroupChatList.class);
						groupChatLists.add(model);
					}
				}
				adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
				rcv_ugch_list.setAdapter(adapterGroupChatList);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void searchGroupChatList(final String query) {
		groupChatLists = new ArrayList<>();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				groupChatLists.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					if (d.child("Participants").child(firebaseAuth.getUid()).exists()) {
						if (d.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
							ModelGroupChatList model = d.getValue(ModelGroupChatList.class);
							groupChatLists.add(model);
						}
					}
				}
				adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
				rcv_ugch_list.setAdapter(adapterGroupChatList);
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
		
		} else {
			startActivity(new Intent(getActivity(), Login.class));
			getActivity().finish();
		}
	}
	
}
