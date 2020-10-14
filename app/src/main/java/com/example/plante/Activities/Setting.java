package com.example.plante.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.plante.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class Setting extends AppCompatActivity {
	
	private ImageView imv_sett_back;
	private Intent intent;
	private SwitchCompat swc_sett_postNotification, swc_sett_tosinhala;
	private Button btn_sett_logout, btn_sett_show;
	private TextView textview, textView1, textView2, textView3;
	
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	
	SharedPreferences languagesp;
	SharedPreferences.Editor language;
	
	private FirebaseAuth mAuth;
	
	private static final String TOPIC_POST_NOTIFICATION = "POST";
	private static final String SINHALA_FONT = "SINHALA";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		mAuth = FirebaseAuth.getInstance();
		
		textview = findViewById(R.id.textview);
		textView1 = findViewById(R.id.textView1);
		textView2 = findViewById(R.id.textView2);
		textView3 = findViewById(R.id.textView3);
		
		imv_sett_back = findViewById(R.id.imv_sett_back);
		btn_sett_logout = findViewById(R.id.btn_sett_logout);
		btn_sett_show = findViewById(R.id.btn_sett_show);
		swc_sett_postNotification = findViewById(R.id.swc_sett_postNotification);
		swc_sett_tosinhala = findViewById(R.id.swc_sett_tosinhala);
		
		sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
		boolean isPostEnables = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
		
		languagesp = getSharedPreferences("Language", MODE_PRIVATE);
		boolean isSinhala = languagesp.getBoolean("" + SINHALA_FONT, false);
		
		if (isSinhala) {
			swc_sett_tosinhala.setChecked(true);
			changeToSinhala();
		} else {
			swc_sett_tosinhala.setChecked(false);
			changeToEnglish();
		}
		
		if (isPostEnables) {
			swc_sett_postNotification.setChecked(true);
		} else {
			swc_sett_postNotification.setChecked(false);
		}
		
		imv_sett_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		btn_sett_logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAuth.signOut();
				checkUserStatus();
			}
		});
		
		swc_sett_tosinhala.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				language = languagesp.edit();
				language.putBoolean("" + SINHALA_FONT, isChecked);
				language.apply();
				if (isChecked) {
					changeToSinhala();
				} else {
					changeToEnglish();
				}
			}
		});
		
		swc_sett_postNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				editor = sp.edit();
				editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
				editor.apply();
				
				if (isChecked) {
					subscribePostNotification();
				} else {
					unSubscribePostNotification();
				}
			}
		});
		
	}
	
	private void changeToEnglish() {
		textview.setText(R.string.setting);
		textView1.setText(R.string.notifiication);
		textView2.setText(R.string.language_support);
		textView3.setText(R.string.activitylog);
		btn_sett_show.setText(R.string.show);
		btn_sett_logout.setText(R.string.logout);
	}
	
	private void changeToSinhala() {
		textview.setText(R.string.sinhala_setting);
		textView1.setText(R.string.sinhala_notification);
		textView2.setText(R.string.sinhala_language_support);
		textView3.setText(R.string.sinhala_activity_log);
		btn_sett_show.setText(R.string.sinhala_show);
		btn_sett_logout.setText(R.string.sinhala_log_out);
	}
	
	private void checkUserStatus() {
/*		FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			
		} else {
			startActivity(new Intent(getApplicationContext(), Login.class));
			finish();
		}*/
	}
	
	private void subscribePostNotification() {
		FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				String msg = "You will receive post notification";
				if (!task.isSuccessful()) {
					msg = "Subscription faild";
				}
				Toast.makeText(Setting.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void unSubscribePostNotification() {
		FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				String msg = "You will not receive post notification";
				if (!task.isSuccessful()) {
					msg = "Un  Subscription faild";
				}
				Toast.makeText(Setting.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
