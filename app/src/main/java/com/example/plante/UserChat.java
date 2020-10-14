package com.example.plante;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterChatList;
import com.example.plante.Base_module.ModelChat;
import com.example.plante.Base_module.ModelChatList;
import com.example.plante.Base_module.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserChat extends Fragment {
	
	private View view;
	private FirebaseAuth firebaseAuth;
	private RecyclerView rcv_ucht_list;
	private List<ModelChatList> chatListList;
	private List<ModelUser> userList;
	private DatabaseReference reference;
	private FirebaseUser currentUser;
	private AdapterChatList adapterChatList;
	private String uid;
	
	public UserChat() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.activity_user_chat, container, false);
		firebaseAuth = FirebaseAuth.getInstance();
		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		
		rcv_ucht_list = view.findViewById(R.id.rcv_ucht_list);
		chatListList = new ArrayList<>();
		reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				chatListList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelChatList chatList = d.getValue(ModelChatList.class);
					chatListList.add(chatList);
				}
				loadChats();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		return view;
	}
	
	private void loadChats() {
		userList = new ArrayList<>();
		reference = FirebaseDatabase.getInstance().getReference("User");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelUser user = d.getValue(ModelUser.class);
					for (ModelChatList chatList : chatListList) {
						if (user.getUid() != null && user.getUid().equals(chatList.getId())) {
							userList.add(user);
							break;
						}
					}
					adapterChatList = new AdapterChatList(getContext(), userList);
					rcv_ucht_list.setAdapter(adapterChatList);
					for (int i = 0; i < userList.size(); i++) {
						lastMessage(userList.get(i).getUid());
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void lastMessage(String uid) {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String theLastMessage = "default";
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelChat chat = d.getValue(ModelChat.class);
					if (chat == null) {
						continue;
					}
					String sender = chat.getSender();
					String receiver = chat.getReceiver();
					if (sender == null || receiver == null) {
						continue;
					}
					if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(uid) || chat.getReceiver().equals(uid) && chat.getSender().equals(currentUser.getUid())) {
						if (chat.getType().equals("image")) {
							theLastMessage = "Sent a photo.";
						} else {
							theLastMessage = chat.getMessage();
						}
					}
				}
				adapterChatList.setLastMessageMap(uid, theLastMessage);
				adapterChatList.notifyDataSetChanged();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			uid = user.getUid();
		} else {
			startActivity(new Intent(getActivity(), Parent.class));
			getActivity().finish();
		}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
