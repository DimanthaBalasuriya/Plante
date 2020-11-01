package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterShop;
import com.example.plante.Base_module.ModelShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Stores extends AppCompatActivity {
	
	private ImageView imv_store_back, imv_store_add;
	private RecyclerView rcv_store_list;
	private Intent intent;
	private TextView stores;
	
	private List<ModelShop> modelShopList = new ArrayList<>();
	private AdapterShop mAdapter;
	
	private FirebaseAuth mAuth;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	String city = "";
	String country = "";
	
	private String userType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stores);
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_store_back = findViewById(R.id.imv_store_back);
		imv_store_add = findViewById(R.id.imv_store_add);
		stores = findViewById(R.id.stores);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_store_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterShop.class);
				startActivity(intent);
				finish();
			}
		});
		
		imv_store_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		rcv_store_list = findViewById(R.id.rcv_store_list);
		
		mAdapter = new AdapterShop(modelShopList, getApplicationContext());
		rcv_store_list.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_store_list.setLayoutManager(mLayoutManager);
		rcv_store_list.setItemAnimator(new DefaultItemAnimator());
		rcv_store_list.setAdapter(mAdapter);
		
		
		fetchListItemsToList();
		
	}
	
	private void changeToEnglish() {
		stores.setText(R.string.stores);
	}
	
	private void changeToSinhala() {
		stores.setText(R.string.sinhala_stores);
	}
	
	private void fetchListItemsToList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shops");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelShopList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String id = d.child("shopId").getValue(String.class);
					String name = d.child("name").getValue(String.class);
					String city = d.child("city").getValue(String.class);
					String contact = d.child("contact").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					modelShopList.add(new ModelShop(id, name, city, contact, image));
					mAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	@Override
	protected void onStart() {
		FirebaseUser user = mAuth.getCurrentUser();
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference myRef = database.getReference("User").child(user.getUid()).child("type");
		myRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				userType = dataSnapshot.getValue(String.class) + "";
				
				if (userType.equalsIgnoreCase("Admin")) {
					imv_store_add.setVisibility(View.VISIBLE);
				} else {
					imv_store_add.setVisibility(View.INVISIBLE);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(), "Can't Load User Data!", Toast.LENGTH_LONG).show();
			}
		});
		
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
