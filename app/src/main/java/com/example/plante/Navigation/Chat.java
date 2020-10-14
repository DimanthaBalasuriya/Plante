package com.example.plante.Navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.plante.Activities.ActiveUserList;
import com.example.plante.Activities.CreateUserGroup;
import com.example.plante.Activities.UserList;
import com.example.plante.R;
import com.example.plante.UserChat;
import com.example.plante.UserGroupChat;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Chat extends Fragment {
	
	private View v;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	
	private ImageView imv_chat_userlist, imv_chat_addGroup;
	private Intent intent;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	private TextView textview, textview1;
	
	public Chat() {
	
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_chat, container, false);
		
		imv_chat_userlist = v.findViewById(R.id.imv_chat_allusers);
		imv_chat_addGroup = v.findViewById(R.id.imv_chat_addgroup);
		textview = v.findViewById(R.id.textview);
		viewPager = v.findViewById(R.id.vpr_chat);
		setupViewPager(viewPager);
		tabLayout = v.findViewById(R.id.tab_chat);
		tabLayout.setupWithViewPager(viewPager);
		textview1 = v.findViewById(R.id.textview1);
		
		languagesp = getActivity().getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_chat_userlist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), UserList.class);
				startActivity(intent);
			}
		});
		
		imv_chat_addGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent = new Intent(getActivity(), CreateUserGroup.class);
				startActivity(intent);
			}
		});
		
		return v;
	}
	
	private void changeToSinhala() {
		textview1.setText(R.string.sinhala_forum);
	}
	
	private void changeToEnglish() {
		textview1.setText(R.string.forum);
	}
	
	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter1 adapter1 = new ViewPagerAdapter1(getChildFragmentManager());
		adapter1.addFragment(new UserChat(), "Chat");
		adapter1.addFragment(new UserGroupChat(), "Group chat");
		adapter1.addFragment(new ActiveUserList(), "Active Usres");
		viewPager.setAdapter(adapter1);
	}
	
}

class ViewPagerAdapter1 extends FragmentPagerAdapter {
	
	private final List<Fragment> mFragmentList = new ArrayList<>();
	private final List<String> mFragmentTitleList = new ArrayList<>();
	
	public ViewPagerAdapter1(FragmentManager manager) {
		super(manager);
	}
	
	@NonNull
	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}
	
	@Override
	public int getCount() {
		return mFragmentList.size();
	}
	
	public void addFragment(Fragment fragment, String title) {
		mFragmentList.add(fragment);
		mFragmentTitleList.add(title);
	}
	
	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return mFragmentTitleList.get(position);
	}
}
