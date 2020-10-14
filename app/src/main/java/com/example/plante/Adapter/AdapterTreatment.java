package com.example.plante.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.R;
import com.example.plante.Base_module.ModelTreatments;

import java.util.List;

public class AdapterTreatment extends RecyclerView.Adapter<AdapterTreatment.MyViewHolder> {
	
	private List<ModelTreatments> modelTreatmentsList;
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, description;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_tret_name);
			description = view.findViewById(R.id.txv_tret_explan);
		}
	}
	
	public AdapterTreatment(List<ModelTreatments> treatlist) {
		this.modelTreatmentsList = treatlist;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_treatment, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelTreatments modelTreatments = modelTreatmentsList.get(position);
		holder.name.setText(modelTreatments.getName());
		holder.description.setText(modelTreatments.getDescription());
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ModelTreatments t = modelTreatmentsList.get(position);
				System.out.println("Name " +t.getName());
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return modelTreatmentsList.size();
	}
}
