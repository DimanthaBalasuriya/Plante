package com.example.plante;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterFertilizerList;
import com.example.plante.Base_module.ModelFertilizerList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fertilizers extends AppCompatActivity {
	
	private ImageView imv_ferti_back, imv_ferti_add;
	private EditText edt_search_bar;
	private RecyclerView rcv_lifl_list;
	private TextView title;
	
	private Intent intent;
	
	private List<ModelFertilizerList> modelFertilizerLists = new ArrayList<>();
	private AdapterFertilizerList mAdapter;
	
	private FirebaseDatabase mAuth;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fertilizers);
		
		imv_ferti_back = findViewById(R.id.imv_ferti_back);
		imv_ferti_add = findViewById(R.id.imv_ferti_add);
		edt_search_bar = findViewById(R.id.edt_search_bar);
		title = findViewById(R.id.title);
		
		rcv_lifl_list = findViewById(R.id.rcv_lifl_list);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		mAdapter = new AdapterFertilizerList(modelFertilizerLists, getApplicationContext());
		rcv_lifl_list.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_lifl_list.setLayoutManager(mLayoutManager);
		rcv_lifl_list.setItemAnimator(new DefaultItemAnimator());
		rcv_lifl_list.setAdapter(mAdapter);
		
		imv_ferti_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_ferti_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterFertilizer.class);
				startActivity(intent);
				finish();
			}
		});
		
		fetchListItemsToList();
	}
	
	private void changeToEnglish() {
		title.setText(R.string.placesyoucanbuy);
		edt_search_bar.setHint(R.string.search);
	}
	
	private void changeToSinhala() {
		title.setText(R.string.sinhala_placesyoucanbuy);
		edt_search_bar.setHint(R.string.sinhala_search);
	}
	
	private void fetchListItemsToList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fertilizers");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelFertilizerLists.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("name").getValue(String.class);
					String type = d.child("type").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					modelFertilizerLists.add(new ModelFertilizerList(name, type, image));
					mAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
}
