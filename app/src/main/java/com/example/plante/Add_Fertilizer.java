package com.example.plante;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.example.plante.Adapter.AdapterAddFertilizer;
import com.example.plante.Base_module.ModelAddFertilizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Add_Fertilizer extends AppCompatActivity {
	
	private Intent intent;
	private String processID = "";
	private String processTT = "";
	private ImageView imv_adfet_back, imv_adfet_selectedlist, imv_plafl_close, imv_adfet_regfet;
	private TextView txv_adfet_diseasename;
	private EditText edt_search_bar;
	
	private RecyclerView rcv_adfet_list;
	private RecyclerView rcv_plafl_list;
	private List<ModelAddFertilizer> modelAddFertilizerList = new ArrayList<>();
	private List<ModelAddFertilizer> modelAddFertilizerList2 = new ArrayList<>();
	private AdapterAddFertilizer mAdapter;
	
	private TextView txv_head_tit, dis_is, textView2;
	
	Dialog epicDialog;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_fertilizer);
		
		processID = getIntent().getStringExtra("did");
		processTT = getIntent().getStringExtra("prc");
		
		epicDialog = new Dialog(this);
		
		imv_adfet_back = findViewById(R.id.imv_adfet_back);
		imv_adfet_selectedlist = findViewById(R.id.imv_adfet_selectedlist);
		imv_adfet_regfet = findViewById(R.id.imv_adfet_regfet);
		txv_adfet_diseasename = findViewById(R.id.txv_adfet_diseasename);
		edt_search_bar = findViewById(R.id.edt_search_bar);
		txv_head_tit = findViewById(R.id.txv_head_tit);
		dis_is = findViewById(R.id.dis_is);
		textView2 = findViewById(R.id.textView2);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		rcv_adfet_list = findViewById(R.id.rcv_adfet_list);
		mAdapter = new AdapterAddFertilizer(modelAddFertilizerList, getApplicationContext(), processID, processTT);
		rcv_adfet_list.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_adfet_list.setLayoutManager(mLayoutManager);
		rcv_adfet_list.setItemAnimator(new DefaultItemAnimator());
		rcv_adfet_list.setAdapter(mAdapter);
		
		imv_adfet_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_adfet_regfet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterDisease.class);
				startActivity(intent);
			}
		});
		
		getDiseaseName();
		fetchDataToList();
		
		imv_adfet_selectedlist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				epicDialog.setContentView(R.layout.popup_layout_added_fertilizer_list);
				imv_plafl_close = epicDialog.findViewById(R.id.imv_plafl_close);
				rcv_plafl_list = epicDialog.findViewById(R.id.rcv_plafl_list);
				mAdapter = new AdapterAddFertilizer(modelAddFertilizerList2, getApplicationContext(), processID, processTT);
				rcv_plafl_list.setHasFixedSize(true);
				RecyclerView.LayoutManager mLayoutManagerPopup = new LinearLayoutManager(getApplicationContext());
				rcv_plafl_list.setLayoutManager(mLayoutManagerPopup);
				rcv_plafl_list.setItemAnimator(new DefaultItemAnimator());
				rcv_plafl_list.setAdapter(mAdapter);
				getAddedFertilizers();
				
				imv_plafl_close.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						epicDialog.dismiss();
					}
				});
				epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				epicDialog.show();
			}
		});
		
	}
	
	private void changeToSinhala() {
		txv_head_tit.setText(R.string.sinhala_addfertilizer);
		dis_is.setText(R.string.sinhala_diseaseis);
		textView2.setText(R.string.sinhala_listoffertilizer);
		edt_search_bar.setText(R.string.sinhala_search);
	}
	
	private void changeToEnglish() {
		txv_head_tit.setText(R.string.addfertilizer);
		dis_is.setText(R.string.diseaseis);
		textView2.setText(R.string.listoffertilizer);
		edt_search_bar.setText(R.string.search);
	}
	
	private void getAddedFertilizers() {
		if (processTT.equalsIgnoreCase("Shop")) {
			DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shops");
			reference.child(processID).child("Fertilizers").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					modelAddFertilizerList2.clear();
					for (DataSnapshot d : dataSnapshot.getChildren()) {
						String id = d.child("id").getValue(String.class);
						String name = d.child("name").getValue(String.class);
						String type = d.child("type").getValue(String.class);
						String image = d.child("image").getValue(String.class);
						modelAddFertilizerList2.add(new ModelAddFertilizer(id, name, type, image));
					}
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				
				}
			});
		} else if (processTT.equalsIgnoreCase("Disease")) {
			DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
			reference.child(processID).child("Fertilizers").addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					modelAddFertilizerList2.clear();
					for (DataSnapshot d : dataSnapshot.getChildren()) {
						String id = d.child("id").getValue(String.class);
						String name = d.child("name").getValue(String.class);
						String type = d.child("type").getValue(String.class);
						String image = d.child("image").getValue(String.class);
						modelAddFertilizerList2.add(new ModelAddFertilizer(id, name, type, image));
					}
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
				
				}
			});
		}
	}
	
	private void fetchDataToList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Fertilizers");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String id = d.child("fertilizerId").getValue(String.class);
					String name = d.child("name").getValue(String.class);
					String type = d.child("type").getValue(String.class);
					String image = d.child("image").getValue(String.class);
					modelAddFertilizerList.add(new ModelAddFertilizer(id, name, type, image));
					mAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				
			}
		});
	}
	
	private void getDiseaseName() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
		Query query = reference.child("Diseases").orderByChild("processID").equalTo(processID);
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("name").getValue(String.class);
					txv_adfet_diseasename.setText("" + name);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				
			}
		});
	}
}
