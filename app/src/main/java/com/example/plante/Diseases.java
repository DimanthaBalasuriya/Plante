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

import com.example.plante.Adapter.AdapterDiseaseList;
import com.example.plante.Base_module.ModelDiseasesList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Diseases extends AppCompatActivity {
	
	private ImageView imv_disease_back, imv_disease_add;
	private EditText edt_search_bar;
	private RecyclerView rcv_disease_list;
	
	private Intent intent;
	
	private List<ModelDiseasesList> modelDiseasesLists = new ArrayList<>();
	private AdapterDiseaseList mAdapter;
	
	private FirebaseAuth mAuth;
	
	private TextView title;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diseases);
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_disease_back = findViewById(R.id.imv_disease_back);
		imv_disease_add = findViewById(R.id.imv_disease_add);
		edt_search_bar = findViewById(R.id.edt_search_bar);
		title = findViewById(R.id.title);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		rcv_disease_list = findViewById(R.id.rcv_disease_list);
		
		mAdapter = new AdapterDiseaseList(modelDiseasesLists, getApplicationContext());
		rcv_disease_list.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_disease_list.setLayoutManager(mLayoutManager);
		rcv_disease_list.setItemAnimator(new DefaultItemAnimator());
		rcv_disease_list.setAdapter(mAdapter);
		
		imv_disease_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_disease_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterDisease.class);
				startActivity(intent);
			}
		});
		
		fetchListItemsToList();
		
	}
	
	private void changeToEnglish() {
		edt_search_bar.setText(R.string.search);
		title.setText(R.string.adddisease);
	}
	
	private void changeToSinhala() {
		edt_search_bar.setText(R.string.sinhala_search);
		title.setText(R.string.sinhala_adddisease);
	}
	
	private void fetchListItemsToList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelDiseasesLists.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("name").getValue(String.class);
					String desc = d.child("description").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					modelDiseasesLists.add(new ModelDiseasesList(name, image, desc));
					mAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
}
