package com.example.beacon;

import java.util.HashSet;

import com.example.beacon.MainActivity.MutableInt;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BTService extends IntentService {
	public static final String TAG = BTService.class.getSimpleName();
	private Handler mHandler;

	public BTService() {
		super("com.aquimobile.BTService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Intent received.");
		mHandler.post(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(BTService.this, "Scanning...",
						Toast.LENGTH_SHORT).show();
			}
		});
		BTScanner btScanner = new BTScanner(this.getApplicationContext());
		btScanner.addFinishListener(new FinishListener() {
			public void finish(HashSet<String> set) {
				for (final String name : set) {
					mHandler.post(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(BTService.this, "Detect " + name,
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
		btScanner.startDiscovery();
	}
}
