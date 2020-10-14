package com.example.plante.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelFertilizerShop;
import com.example.plante.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFertilizerShop extends RecyclerView.Adapter<AdapterFertilizerShop.MyViewHolder> {
	
	private List<ModelFertilizerShop> modelFertilizerShops;
	private Context context;
	
	public AdapterFertilizerShop(List<ModelFertilizerShop> modelFertilizerShops, Context context) {
		this.modelFertilizerShops = modelFertilizerShops;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ferti_shop, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelFertilizerShop modelFertilizerShop = modelFertilizerShops.get(position);
		holder.name.setText(modelFertilizerShop.getName());
		holder.city.setText(modelFertilizerShop.getCity());
		holder.contact.setText(modelFertilizerShop.getContact());
		
		try {
			Picasso.get().load(modelFertilizerShop.getImage()).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int getItemCount() {
		return modelFertilizerShops.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, city, contact;
		public ImageView image;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_fets_name);
			city = view.findViewById(R.id.txv_fets_city);
			contact = view.findViewById(R.id.txv_fets_contact);
			image = view.findViewById(R.id.imv_fets_image);
		}
	}
	
}
