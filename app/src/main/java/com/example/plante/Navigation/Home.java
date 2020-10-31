package com.example.plante.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Activities.CreatePost;
import com.example.plante.Adapter.AdapterPost;
import com.example.plante.Post;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {
	
	private View v;
	private FirebaseAuth mAuth;
	private RecyclerView recyclerView;
	private List<Post> postList;
	private AdapterPost adapterPost;
	
	private ImageView imv_add_post, imv_post_user_picture, imv_post_more, imv_post_image, imv_home_community;
	private TextView txv_post_user_name, txv_post_time, txv_post_title, txv_post_content, txv_post_like_count;
	private Button btn_post_like, btn_post_comment;
	
	public Home() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_home, container, false);
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_add_post = v.findViewById(R.id.imv_home_createPost);
		imv_post_user_picture = v.findViewById(R.id.imv_post_user_picture);
		imv_post_more = v.findViewById(R.id.imv_post_more);
		imv_post_image = v.findViewById(R.id.imv_post_image);
		imv_home_community = v.findViewById(R.id.imv_home_community);
		txv_post_user_name = v.findViewById(R.id.txv_post_user_name);
		txv_post_time = v.findViewById(R.id.txv_post_time);
		txv_post_title = v.findViewById(R.id.txv_post_title);
		txv_post_content = v.findViewById(R.id.txv_post_content);
		txv_post_like_count = v.findViewById(R.id.txv_post_like_count);
		btn_post_like = v.findViewById(R.id.btn_post_like);
		btn_post_comment = v.findViewById(R.id.btn_post_comment);
		recyclerView = v.findViewById(R.id.rcv_home_post);
		
		imv_add_post.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CreatePost.class);
				startActivity(intent);
			}
		});
		
		imv_home_community.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), Chat.class);
				startActivity(intent);
			}
		});
		
		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
		layoutManager.setStackFromEnd(true);
		layoutManager.setReverseLayout(true);
		recyclerView.setLayoutManager(layoutManager);
		
		postList = new ArrayList<>();
		
		loadPost();
		
		return v;
	}
	
	private void loadPost() {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					Post post = ds.getValue(Post.class);
					postList.add(post);
					adapterPost = new AdapterPost(getActivity(), postList);
					recyclerView.setAdapter(adapterPost);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void searchPost(String query) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				postList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					Post post = ds.getValue(Post.class);
					//if (post.getPtitle().toLowerCase().contains(query.toLowerCase()) || post.getPcontent().toLowerCase().contains(query.toLowerCase())) {
					postList.add(post);
					//}
					adapterPost = new AdapterPost(getActivity(), postList);
					recyclerView.setAdapter(adapterPost);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
}
