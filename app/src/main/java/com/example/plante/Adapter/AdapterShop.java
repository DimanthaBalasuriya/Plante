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

import com.example.plante.Base_module.ModelShop;
import com.example.plante.R;
import com.example.plante.Shop_detail;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.MyViewHolder> {
	
	private List<ModelShop> shopList;
	private Context context;
	
	public AdapterShop(List<ModelShop> shopList, Context context) {
		this.shopList = shopList;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shop, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelShop modelShop = shopList.get(position);
		holder.name.setText(modelShop.getName());
		holder.number.setText(modelShop.getNumber());
		holder.city.setText(modelShop.getCity());
		
		try {
			Picasso.get().load(modelShop.getImage()).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, Shop_detail.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("shopID", modelShop.getId());
				context.startActivity(intent);
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return shopList.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, number, city;
		public ImageView image;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_shop_name);
			number = view.findViewById(R.id.txv_shop_contact);
			city = view.findViewById(R.id.txv_shop_city);
			image = view.findViewById(R.id.imv_shop_image_profi);
		}
	}
	
	
}
