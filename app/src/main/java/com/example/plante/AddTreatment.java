package com.example.plante;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Adapter.AdapterAddTreatment;
import com.example.plante.Base_module.ModelAddTreatment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddTreatment extends AppCompatActivity {
	
	private ImageView imv_adtr_back, imv_adtr_addtohashmap, imv_adtr_fertilizerlist;
	private EditText edt_adtr_treatment, edt_adtr_howtodoit;
	private Button btn_adtr_next;
	private TextView txv_treatment, textview, txv_cap, textView1;
	
	private FirebaseAuth mAuth;
	private Intent intent;
	
	private String diseaseID = "";
	
	private RecyclerView rcv_adtr_list;
	private List<ModelAddTreatment> modelAddTreatmentList = new ArrayList<>();
	private AdapterAddTreatment mAdapter;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_treatment);
		
		pd = new ProgressDialog(this);
		
		diseaseID = getIntent().getStringExtra("did").toString();
		
		mAuth = FirebaseAuth.getInstance();
		
		imv_adtr_back = findViewById(R.id.imv_adtr_back);
		imv_adtr_addtohashmap = findViewById(R.id.imv_adtr_addtohashmap);
		imv_adtr_fertilizerlist = findViewById(R.id.imv_adtr_fertilizerlist);
		edt_adtr_treatment = findViewById(R.id.edt_adtr_treatment);
		edt_adtr_howtodoit = findViewById(R.id.edt_adtr_howtodoit);
		btn_adtr_next = findViewById(R.id.btn_adtr_next);
		txv_treatment = findViewById(R.id.txv_treatment);
		textview = findViewById(R.id.textview);
		txv_cap = findViewById(R.id.txv_cap);
		textView1 = findViewById(R.id.textView1);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		rcv_adtr_list = findViewById(R.id.rcv_adtr_list);
		mAdapter = new AdapterAddTreatment(modelAddTreatmentList, getApplicationContext());
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		rcv_adtr_list.setLayoutManager(mLayoutManager);
		rcv_adtr_list.setItemAnimator(new DefaultItemAnimator());
		rcv_adtr_list.setAdapter(mAdapter);
		
		imv_adtr_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		imv_adtr_addtohashmap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addTreatments();
			}
		});
		
		imv_adtr_fertilizerlist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), RegisterFertilizer.class);
				startActivity(intent);
			}
		});
		
		btn_adtr_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getApplicationContext(), Add_Fertilizer.class);
				intent.putExtra("did", diseaseID);
				intent.putExtra("prc", "Disease");
				startActivity(intent);
				finish();
			}
		});
		
		fetchDataToList();
	}
	
	private void changeToEnglish() {
		edt_adtr_treatment.setHint(R.string.addtreatment);
		edt_adtr_howtodoit.setHint(R.string.howtodoit);
		btn_adtr_next.setText(R.string.fertilizers);
		txv_treatment.setText(R.string.treatment_for_this);
		textview.setText(R.string.addtreatment);
		txv_cap.setText(R.string.treatment);
		textView1.setText(R.string.howtodoit);
	}
	
	private void changeToSinhala() {
		edt_adtr_treatment.setHint(R.string.sinhala_addtreatment);
		edt_adtr_howtodoit.setHint(R.string.sinhala_howtodoit);
		btn_adtr_next.setText(R.string.sinhala_fertilizer);
		txv_treatment.setText(R.string.sinhala_treatment_for_this);
		textview.setText(R.string.sinhala_addtreatment);
		txv_cap.setText(R.string.sinhala_treatments);
		textView1.setText(R.string.sinhala_howtodoit);
	}
	
	private void fetchDataToList() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
		reference.child(diseaseID).child("Treatments").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				modelAddTreatmentList.clear();
				for (DataSnapshot d : dataSnapshot.getChildren()) {
					String name = d.child("treatment").getValue(String.class);
					String description = d.child("description").getValue(String.class);
					modelAddTreatmentList.add(new ModelAddTreatment(name, description));
				}
				
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void addTreatments() {
		pd.setMessage("Publishing...");
		pd.show();
		String treatment = edt_adtr_treatment.getText().toString().trim();
		String description = edt_adtr_howtodoit.getText().toString().trim();
		if (TextUtils.isEmpty(treatment)) {
			Toast.makeText(getApplicationContext(), "Add Treatement name", Toast.LENGTH_SHORT).show();
			return;
		}
		String timestamp = "" + System.currentTimeMillis();
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("tid", timestamp);
		hashMap.put("treatment", treatment);
		hashMap.put("description", description);
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
		reference.child(diseaseID).child("Treatments").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				pd.dismiss();
				fetchDataToList();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				pd.setMessage("Not Published!");
				pd.show();
			}
		});
		
	}
}
