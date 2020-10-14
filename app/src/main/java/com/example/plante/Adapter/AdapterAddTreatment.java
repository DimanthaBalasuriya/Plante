package com.example.plante.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelAddTreatment;
import com.example.plante.R;

import java.util.List;

public class AdapterAddTreatment extends RecyclerView.Adapter<AdapterAddTreatment.MyViewHolder> {
	
	private List<ModelAddTreatment> modelAddTreatmentList;
	private Context context;
	
	public AdapterAddTreatment(List<ModelAddTreatment> modelAddTreatmentList, Context context) {
		this.modelAddTreatmentList = modelAddTreatmentList;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_add_treatment, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelAddTreatment modelAddTreatment = modelAddTreatmentList.get(position);
		holder.treatmentname.setText(modelAddTreatment.getTreatmentname());
		holder.description.setText(modelAddTreatment.getDescription());
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ModelAddTreatment t = modelAddTreatmentList.get(position);
				Toast.makeText(context, t.getTreatmentname(), Toast.LENGTH_SHORT).show();
			}
		});
		
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return modelAddTreatmentList.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView treatmentname, description;
		public ImageView delete;
		
		public MyViewHolder(View view) {
			super(view);
			treatmentname = view.findViewById(R.id.txv_adlt_treatment);
			description = view.findViewById(R.id.txv_adlt_description);
			delete = view.findViewById(R.id.imv_adtl_delete);
		}
	}
	
	
}
