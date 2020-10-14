package com.example.plante.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.plante.R;

public class ActivityLog extends AppCompatActivity {
	
	private ImageView imv_atvl_back;
	private TextView txv_title;
	private Intent intent;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		imv_atvl_back = findViewById(R.id.imv_act_back);
		txv_title = findViewById(R.id.txv_title);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			changeToSinhala();
		} else {
			changeToEnglish();
		}
		
		imv_atvl_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void changeToSinhala() {
		txv_title.setText(R.string.sinhala_notification);
	}
	
	private void changeToEnglish() {
		txv_title.setText(R.string.notifiication);
	}
	
}
