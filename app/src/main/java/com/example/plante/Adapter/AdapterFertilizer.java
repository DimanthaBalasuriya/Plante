package com.example.plante.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelFertilizer;
import com.example.plante.FertilizerDetails;
import com.example.plante.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFertilizer extends RecyclerView.Adapter<AdapterFertilizer.MyViewHolder> {
	
	private List<ModelFertilizer> fertilizerList;
	private Context context;
	
	public AdapterFertilizer(List<ModelFertilizer> fertilizerList, Context context) {
		this.fertilizerList = fertilizerList;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_fetilizer_from_shop_details, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelFertilizer modelFertilizer = fertilizerList.get(position);
		holder.name.setText(modelFertilizer.getName());
		holder.type.setText(modelFertilizer.getType());
		
		try {
			Picasso.get().load(modelFertilizer.getImage()).placeholder(R.drawable.ic_image).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FertilizerDetails.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("fid", modelFertilizer.getId());
				context.startActivity(intent);
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return fertilizerList.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, type;
		public ImageView image;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_ffsd_name);
			type = view.findViewById(R.id.txv_ffsd_type);
			image = view.findViewById(R.id.imv_ffsd_image);
		}
	}
	
}
