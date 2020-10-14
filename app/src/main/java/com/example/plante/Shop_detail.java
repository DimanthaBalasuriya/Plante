package com.example.plante;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterFertilizer;
import com.example.plante.Base_module.ModelFertilizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Shop_detail extends AppCompatActivity {
	
	private TextView txv_shdt_city, txv_shdt_contact, txv_shdt_name, txv_head_title;
	private ImageView imv_shdt_back, imv_shdt_rate, imv_shdt_image;
	private RecyclerView rcv_fet_list;
	private FirebaseAuth mAuth;
	private String shopId = "";
	
	private List<ModelFertilizer> modelFertilizers = new ArrayList<>();
	private AdapterFertilizer fertilizerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_detail);
		
		mAuth = FirebaseAuth.getInstance();
		
		shopId = getIntent().getStringExtra("shopID");
		System.out.println(shopId);
		
		txv_shdt_city = findViewById(R.id.txv_shdt_city);
		txv_shdt_contact = findViewById(R.id.txv_shdt_contact);
		txv_shdt_name = findViewById(R.id.txv_shdt_name);
		txv_head_title = findViewById(R.id.txv_head_title);
		imv_shdt_back = findViewById(R.id.imv_shdt_back);
		imv_shdt_rate = findViewById(R.id.imv_shdt_rate);
		imv_shdt_image = findViewById(R.id.imv_shdt_image);
		
		rcv_fet_list = findViewById(R.id.rcv_fet_list);
		
		fertilizerAdapter = new AdapterFertilizer(modelFertilizers, getApplicationContext());
		rcv_fet_list.setHasFixedSize(true);
		RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
		rcv_fet_list.setLayoutManager(layoutManager1);
		rcv_fet_list.setItemAnimator(new DefaultItemAnimator());
		rcv_fet_list.setAdapter(fertilizerAdapter);
		
		
		getShopDetails();
		fetchFertilizersToListView();
		
		imv_shdt_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_shdt_rate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
			}
		});
	}
	
	private void fetchFertilizersToListView() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
		reference.child("Shops").child(shopId).child("Fertilizers").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelFertilizers.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String id = d.child("id").getValue(String.class);
					String name = d.child("name").getValue(String.class);
					String type = d.child("type").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					modelFertilizers.add(new ModelFertilizer(id, name, type, image));
					fertilizerAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getShopDetails() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shops");
		Query query = reference.orderByChild("shopId").equalTo(shopId);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("name").getValue(String.class);
					String contact = d.child("contact").getValue(String.class);
					String city = d.child("city").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					txv_shdt_name.setText("" + name);
					txv_shdt_contact.setText("" + contact);
					txv_shdt_city.setText("" + city);
					txv_head_title.setText("" + name);
					
					try {
						Picasso.get().load(image).into(imv_shdt_image);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				
			}
		});
	}
}
