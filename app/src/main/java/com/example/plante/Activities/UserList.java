package com.example.plante.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterModelUser;
import com.example.plante.Base_module.ModelUser;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity {
	
	private RecyclerView rcv_auli_users;
	private AdapterModelUser userAdapter;
	private List<ModelUser> userList;
	private ImageView imv_auli_back;
	private EditText edt_auli_search;
	private TextView txv_auli_title;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	public UserList() {
	
	}
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		
		rcv_auli_users = findViewById(R.id.rcv_auli_users);
		rcv_auli_users.setHasFixedSize(true);
		rcv_auli_users.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		
		
		edt_auli_search = findViewById(R.id.edt_search_bar);
		imv_auli_back = findViewById(R.id.imv_auli_back);
		txv_auli_title = findViewById(R.id.txv_auli_title);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_auli_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		userList = new ArrayList<>();
		userAdapter = new AdapterModelUser(getApplicationContext(), userList);
		rcv_auli_users.setAdapter(userAdapter);
		
		readUsers();
		edt_auli_search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				searchUser(s.toString());
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
	}
	
	private void changeToSinhala() {
		txv_auli_title.setText(R.string.sinhala_your_connection);
	}
	
	private void changeToEnglish() {
		txv_auli_title.setText(R.string.your_connection);
	}
	
	private void searchUser(String s) {
		Query query = FirebaseDatabase.getInstance().getReference("User").orderByChild("name").startAt(s).endAt(s + "\uf8ff");
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					ModelUser user = d.getValue(ModelUser.class);
					userList.add(user);
				}
				userAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void readUsers() {
		FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
		ref.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userList.clear();
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					ModelUser modelUser = ds.getValue(ModelUser.class);
					if (!modelUser.getUid().equals(fuser.getUid())) {
						userList.add(modelUser);
					}
					userAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
}
