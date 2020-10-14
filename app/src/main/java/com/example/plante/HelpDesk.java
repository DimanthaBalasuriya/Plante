package com.example.plante;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class HelpDesk extends AppCompatActivity {
	
	private ImageView imv_hdck_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_desk);
		
		imv_hdck_back = findViewById(R.id.imv_hdck_back);
		
		imv_hdck_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
}
