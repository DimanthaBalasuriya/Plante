package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterFertilizerShop;
import com.example.plante.Base_module.ModelFertilizerShop;
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

public class FertilizerDetails extends AppCompatActivity {
	
	private ImageView imv_fdet_back, imv_fdet_image;
	private TextView txv_fdet_name, textView1;
	private RecyclerView rcv_fdet_list;
	
	private Intent intent;
	
	private FirebaseAuth mAuth;
	private String fertilizerID = "";
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	private List<ModelFertilizerShop> modelFertilizerShops = new ArrayList<>();
	private AdapterFertilizerShop adapterFertilizerShop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fertilizer_details);
		
		mAuth = FirebaseAuth.getInstance();
		
		fertilizerID = getIntent().getStringExtra("fid");
		
		imv_fdet_back = findViewById(R.id.imv_fdet_back);
		imv_fdet_image = findViewById(R.id.imv_fdet_image);
		txv_fdet_name = findViewById(R.id.txv_fdet_name);
		textView1 = findViewById(R.id.textView1);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		rcv_fdet_list = findViewById(R.id.rcv_fdet_list);
		
		adapterFertilizerShop = new AdapterFertilizerShop(modelFertilizerShops, getApplicationContext());
		rcv_fdet_list.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_fdet_list.setLayoutManager(mLayoutManager);
		rcv_fdet_list.setItemAnimator(new DefaultItemAnimator());
		rcv_fdet_list.setAdapter(adapterFertilizerShop);
		
		imv_fdet_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		getFertilizerData();
		fetchDataToShopList();
	}
	
	private void changeToEnglish() {
		textView1.setText(R.string.placesyoucanbuy);
	}
	
	private void changeToSinhala() {
		textView1.setText(R.string.sinhala_placesyoucanbuy);
	}
	
	private void fetchDataToShopList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fertilizers");
		reference.child(fertilizerID).child("AvailableShop").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelFertilizerShops.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String sid = d.child("sid").getValue(String.class);
					DatabaseReference internalReference = FirebaseDatabase.getInstance().getReference("Shops");
					Query query = internalReference.orderByChild("shopId").equalTo(sid);
					query.addValueEventListener(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for (DataSnapshot d : dataSnapshot.getChildren()) {
								String id = d.child("shopId").getValue(String.class);
								String name = d.child("name").getValue(String.class);
								String contact = d.child("contact").getValue(String.class);
								String city = d.child("city").getValue(String.class);
								String image = d.child("image").getValue(String.class);
								System.out.println(id + " " + name + " " + contact + " ");
								modelFertilizerShops.add(new ModelFertilizerShop(id, name, contact, city, image));
								adapterFertilizerShop.notifyDataSetChanged();
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {
						
						}
					});
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getFertilizerData() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fertilizers");
		Query query = reference.orderByChild("fertilizerId").equalTo(fertilizerID);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("name").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					
					txv_fdet_name.setText(name + "");
					
					try {
						Picasso.get().load(image).into(imv_fdet_image);
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
