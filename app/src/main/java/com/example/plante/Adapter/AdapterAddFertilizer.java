package com.example.plante.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelAddFertilizer;
import com.example.plante.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterAddFertilizer extends RecyclerView.Adapter<AdapterAddFertilizer.MyViewHolder> {
	
	private List<ModelAddFertilizer> modelAddFertilizerList;
	private Context context;
	private String diseaseID = "";
	private String processTT = "";
	
	private ArrayList list = new ArrayList();
	
	public AdapterAddFertilizer(List<ModelAddFertilizer> modelAddFertilizerList, Context context, String diseaseID, String processTT) {
		this.modelAddFertilizerList = modelAddFertilizerList;
		this.context = context;
		this.diseaseID = diseaseID;
		this.processTT = processTT;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_fertilizer_verticle, parent, false);
		return new MyViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelAddFertilizer modelAddFertilizer = modelAddFertilizerList.get(position);
		
		holder.name.setText(modelAddFertilizer.getName());
		holder.type.setText(modelAddFertilizer.getType());
		
		try {
			Picasso.get().load(modelAddFertilizer.getImage()).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ModelAddFertilizer t = modelAddFertilizerList.get(position);
				String name = t.getName();
				String type = t.getType();
				String image = t.getImage();
				String ftid = t.getId();
				HashMap<String, String> hashMap = new HashMap<>();
				hashMap.put("name", name);
				hashMap.put("type", type);
				hashMap.put("id", ftid);
				hashMap.put("image", image);
				String timestamp = "" + System.currentTimeMillis();
				if (processTT.equalsIgnoreCase("Shop")) {
					DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shops");
					reference.child(diseaseID).child("Fertilizers").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(context, "Fertilizer added Successfully!", Toast.LENGTH_SHORT).show();
							HashMap<String, String> fertilizermap = new HashMap<>();
							fertilizermap.put("sid", diseaseID);
							DatabaseReference internalReference = FirebaseDatabase.getInstance().getReference("Fertilizers");
							internalReference.child(ftid).child("AvailableShop").child(timestamp).setValue(fertilizermap).addOnSuccessListener(new OnSuccessListener<Void>() {
								@Override
								public void onSuccess(Void aVoid) {
									System.out.println("Hello");
								}
							}).addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
								
								}
							});
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				} else if (processTT.equalsIgnoreCase("Disease")) {
					DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Diseases");
					reference.child(diseaseID).child("Fertilizers").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Toast.makeText(context, "Fertilizer added Successfully!", Toast.LENGTH_SHORT).show();
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
		
/*		holder.state.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ModelAddFertilizer t = modelAddFertilizerList.get(position);
				Toast.makeText(context, "" + t.getName(), Toast.LENGTH_SHORT).show();
			}
		});*/
	}
	
	@Override
	public int getItemCount() {
		return modelAddFertilizerList.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, type;
		public ImageView image;
		public Button state;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_adfet_fetname);
			type = view.findViewById(R.id.txv_adfet_type);
			image = view.findViewById(R.id.imv_adfet_fetimage);
			state = view.findViewById(R.id.btn_adfet_state);
		}
	}
	
	
}
