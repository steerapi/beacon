package com.example.beacon;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		Intent btIntent = new Intent(context.getApplicationContext(),
				BTService.class);
		AlarmManager alarmManager = (AlarmManager) context
				.getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getService(
				context.getApplicationContext(), 987654321, btIntent, 0);
		try {
			alarmManager.cancel(pendingIntent);
		} catch (Exception e) {

		}
		int timeForAlarm = 30000;

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + timeForAlarm, timeForAlarm,
				pendingIntent);
	}
}
