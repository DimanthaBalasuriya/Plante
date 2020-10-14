package com.example.plante.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plante.Base_module.ModelFertilizerList;
import com.example.plante.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFertilizerList extends RecyclerView.Adapter<AdapterFertilizerList.MyViewHolder> {
	
	private List<ModelFertilizerList> fertilizerLists;
	private Context context;
	
	public AdapterFertilizerList(List<ModelFertilizerList> fertilizerLists, Context context) {
		this.fertilizerLists = fertilizerLists;
		this.context = context;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_fertilizer_list, parent, false);
		return new MyViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		ModelFertilizerList fertilizerList = fertilizerLists.get(position);
		holder.name.setText(fertilizerList.getName());
		holder.type.setText(fertilizerList.getType());
		
		try {
			Picasso.get().load(fertilizerList.getImage()).into(holder.image);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getItemCount() {
		return fertilizerLists.size();
	}
	
	public class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView name, type;
		public ImageView image;
		
		public MyViewHolder(View view) {
			super(view);
			name = view.findViewById(R.id.txv_lifl_fetname);
			type = view.findViewById(R.id.txv_lifl_type);
			image = view.findViewById(R.id.imv_lifl_fetimage);
		}
	}
	
}
