package com.example.plante.Navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.plante.Activities.CreatePost;
import com.example.plante.Activities.Login;
import com.example.plante.Activities.Setting;
import com.example.plante.Activities.UserList;
import com.example.plante.Diseases;
import com.example.plante.Fertilizers;
import com.example.plante.HelpDesk;
import com.example.plante.Profile;
import com.example.plante.R;
import com.example.plante.Stores;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class Menu extends Fragment {
	
	private View v;
	private FirebaseAuth firebaseAuth;
	private FirebaseUser user;
	private Intent intent;
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private RelativeLayout rlt_menu_profile_chip, rel_store, rel_ferti,
			rel_disease, rel_users, rel_createpost, rel_help, rel_advertisement;
	private ImageView imv_menu_setting;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	private TextView disease, fertilizer, stores, tcv_textview1, community, askfromcommunity, advertising, notification, helpdesk;
	
	public Menu() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_menu, container, false);
		
		firebaseAuth = FirebaseAuth.getInstance();
		user = firebaseAuth.getCurrentUser();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("User");
		
		rlt_menu_profile_chip = v.findViewById(R.id.rlt_menu_profile_chip);
		rel_store = v.findViewById(R.id.rel_store);
		rel_ferti = v.findViewById(R.id.rel_ferti);
		rel_disease = v.findViewById(R.id.rel_disease);
		rel_users = v.findViewById(R.id.rel_users);
		rel_createpost = v.findViewById(R.id.rel_createpost);
		rel_advertisement = v.findViewById(R.id.rel_advertisement);
		rel_help = v.findViewById(R.id.rel_help);
		imv_menu_setting = v.findViewById(R.id.imv_menu_setting);
		stores = v.findViewById(R.id.stores);
		disease = v.findViewById(R.id.disease);
		fertilizer = v.findViewById(R.id.fertilizer);
		community = v.findViewById(R.id.community);
		askfromcommunity = v.findViewById(R.id.askfromcommunity);
		advertising = v.findViewById(R.id.advertising);
		notification = v.findViewById(R.id.notification);
		helpdesk = v.findViewById(R.id.helpdesk);
		tcv_textview1 = v.findViewById(R.id.tcv_textview1);
		
		languagesp = getActivity().getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		rel_createpost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), CreatePost.class);
				startActivity(intent);
			}
		});
		
		rlt_menu_profile_chip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), Profile.class);
				startActivity(intent);
			}
		});
		
		rel_store.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), Stores.class);
				startActivity(intent);
			}
		});
		
		
		rel_ferti.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), Fertilizers.class);
				startActivity(intent);
			}
		});
		
		rel_disease.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), Diseases.class);
				startActivity(intent);
			}
		});
		
		rel_users.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), UserList.class);
				startActivity(intent);
			}
		});
		
		rel_help.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), HelpDesk.class);
				startActivity(intent);
			}
		});
		
		imv_menu_setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), Setting.class);
				startActivity(intent);
			}
		});
		
/*		Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
		
		query.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					String name = "" + ds.child("name").getValue();
					String image = "" + ds.child("image").getValue();
					
					txv_menu_name.setText(name + "");
					
					if (!image.equalsIgnoreCase("null")) {
						try {
							Picasso.get().load(image).into(imv_menu_image);
						} catch (Exception e) {
							System.out.println("PIC" + e);
						}
					} else {
						try {
							Picasso.get().load(R.drawable.ic_usernot).into(imv_menu_image);
						} catch (Exception e) {
							System.out.println("PIC" + e);
						}
					}
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});*/
		
		
		return v;
	}
	
	private void changeToEnglish() {
		disease.setText(R.string.diseases);
		stores.setText(R.string.stores);
		fertilizer.setText(R.string.fertilizers);
		community.setText(R.string.community);
		askfromcommunity.setText(R.string.askfromcommunity);
		advertising.setText(R.string.advertising);
		notification.setText(R.string.notifiication);
		helpdesk.setText(R.string.helpdesk);
		tcv_textview1.setText(R.string.menu);
	}
	
	private void changeToSinhala() {
		disease.setText(R.string.sinhala_disease);
		stores.setText(R.string.sinhala_stores);
		fertilizer.setText(R.string.sinhala_fertilizer);
		community.setText(R.string.sinhala_community);
		askfromcommunity.setText(R.string.sinhala_askfromcommunity);
		advertising.setText(R.string.sinhala_advertising);
		notification.setText(R.string.sinhala_notification);
		helpdesk.setText(R.string.sinhala_help);
		tcv_textview1.setText(R.string.sinhala_menu);
	}
	
	private void checkUserStatus() {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
		
		} else {
			startActivity(new Intent(getActivity(), Login.class));
		}
	}
	
	@Override
	public void onStart() {
		checkUserStatus();
		super.onStart();
	}
}
