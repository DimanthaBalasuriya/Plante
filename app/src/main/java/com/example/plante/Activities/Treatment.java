package com.example.plante.Activities;

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

import com.example.plante.Adapter.AdapterFertilizer;
import com.example.plante.Adapter.AdapterTreatment;
import com.example.plante.Base_module.ModelFertilizer;
import com.example.plante.Base_module.ModelTreatments;
import com.example.plante.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Treatment extends AppCompatActivity {
	
	private ImageView imv_desc_back;
	private Intent intent;
	
	private List<ModelTreatments> modelTreatmentsList = new ArrayList<>();
	private List<ModelFertilizer> modelFertilizerList = new ArrayList<>();
	private RecyclerView rcv_tret_treatments;
	private RecyclerView rcv_tret_fertilizers;
	private AdapterTreatment tretAdapter;
	private AdapterFertilizer fetAdapter;
	private TextView head, sub_head1, sub_head2;
	
	private String diseaseID = "";
	
	private FirebaseAuth mAuth;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_treatment);
		
		mAuth = FirebaseAuth.getInstance();
		
		diseaseID = getIntent().getStringExtra("did");
		
		imv_desc_back = findViewById(R.id.imv_tret_back);
		head = findViewById(R.id.head);
		sub_head1 = findViewById(R.id.sub_head1);
		sub_head2 = findViewById(R.id.sub_head2);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_desc_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		rcv_tret_treatments = findViewById(R.id.rcv_tret_list);
		tretAdapter = new AdapterTreatment(modelTreatmentsList);
		rcv_tret_treatments.setHasFixedSize(true);
		RecyclerView.LayoutManager tretLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_tret_treatments.setLayoutManager(tretLayoutManager);
		rcv_tret_treatments.setItemAnimator(new DefaultItemAnimator());
		rcv_tret_treatments.setAdapter(tretAdapter);
		
		fetchTreatments();
		
		rcv_tret_fertilizers = findViewById(R.id.rcv_ferti_list);
		fetAdapter = new AdapterFertilizer(modelFertilizerList, getApplicationContext());
		RecyclerView.LayoutManager fertLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
		rcv_tret_fertilizers.setLayoutManager(fertLayoutManager);
		rcv_tret_fertilizers.setItemAnimator(new DefaultItemAnimator());
		rcv_tret_fertilizers.setAdapter(fetAdapter);
		
		fetchFertilizers();
		
	}
	
	private void changeToSinhala() {
		head.setText(R.string.sinhala_treatments);
		sub_head1.setText(R.string.sinhala_treatment_for_this);
		sub_head2.setText(R.string.sinhala_fertilizer_for_this);
	}
	
	private void changeToEnglish() {
		head.setText(R.string.treatment);
		sub_head1.setText(R.string.treatment_for_this);
		sub_head2.setText(R.string.fertilizer_for_this);
	}
	
	
	private void fetchTreatments() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
		reference.child(diseaseID).child("Treatments").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelTreatmentsList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("treatment").getValue(String.class);
					String type = d.child("description").getValue(String.class);
					System.out.println(name);
					modelTreatmentsList.add(new ModelTreatments(name, type));
					tretAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void fetchFertilizers() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
		reference.child(diseaseID).child("Fertilizers").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelFertilizerList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String id = d.child("id").getValue(String.class);
					String name = d.child("name").getValue(String.class);
					String type = d.child("type").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					System.out.println(name);
					modelFertilizerList.add(new ModelFertilizer(id, name, type, image));
					fetAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
}
