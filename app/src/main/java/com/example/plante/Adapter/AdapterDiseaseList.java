package com.example.plante.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelDiseasesList;
import com.example.plante.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterDiseaseList extends RecyclerView.Adapter<AdapterDiseaseList.MyViewHolder> {
	
	private List<ModelDiseasesList> diseasesLists;
	private Context context;
	
	public AdapterDiseaseList(List<ModelDiseasesList> diseasesLists, Context context) {
		this.diseasesLists = diseasesLists;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_disease_list, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelDiseasesList diseasesList = diseasesLists.get(position);
		holder.name.setText(diseasesList.getName());
		holder.description.setText(diseasesList.getDescription());
		
		try {
			Picasso.get().load(diseasesList.getImage()).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int getItemCount() {
		return diseasesLists.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, description;
		public ImageView image;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_lidl_name);
			description = view.findViewById(R.id.txv_lidl_description);
			image = view.findViewById(R.id.imv_lidl_image);
		}
	}
}
