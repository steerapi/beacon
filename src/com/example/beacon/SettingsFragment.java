package com.example.beacon;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
	private MainActivity main;
	private RadioGroup radioGroup;
	private CheckBox checkBox;
	private TimePicker timePicker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_settings, container,
				false);
		main = (MainActivity) getActivity();

		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup1);
		checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
		timePicker = (TimePicker) view.findViewById(R.id.timePicker1);
		timePicker.setIs24HourView(true);

		loadSettings();

		return view;
	}

	private void saveSettings() {
		int checkedId = radioGroup.getCheckedRadioButtonId();
		RadioButton radioButton = (RadioButton) radioGroup
				.findViewById(checkedId);
		boolean auto = checkBox.isChecked();
		int hour = timePicker.getCurrentHour();
		int min = timePicker.getCurrentMinute();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("interval", radioButton.getText().toString());
		editor.putBoolean("autolaunch", auto);
		editor.putInt("hour", hour);
		editor.putInt("min", min);
		editor.commit();
	}

	private SharedPreferences settings;

	private void loadSettings() {
		// Restore preferences
		settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME,
				0);
		String interval = "15s";
		boolean auto = false;
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY), min = cal
				.get(Calendar.MINUTE);
		interval = settings.getString("interval", "15s");
		auto = settings.getBoolean("autolaunch", false);
		hour = settings.getInt("hour", hour);
		min = settings.getInt("min", min);

		// Toast.makeText(main.getApplicationContext(),
		// "INTERVAL " + interval, Toast.LENGTH_SHORT)
		// .show();

		main.setTimerInterval(interval);
		if (auto) {
			main.setAutomaticLaunch(hour, min);
		}
		updateSettingsUI(interval, auto, hour, min);
	}

	private void updateSettingsUI(String text, boolean auto, int hour, int min) {
		if (text.equals("15s")) {
			radioGroup.check(R.id.radio0);
		} else if (text.equals("30s")) {
			radioGroup.check(R.id.radio1);
		} else if (text.equals("1m")) {
			radioGroup.check(R.id.radio2);
		} else if (text.equals("2m")) {
			radioGroup.check(R.id.radio3);
		} else if (text.equals("5m")) {
			radioGroup.check(R.id.radio4);
		}
		if (auto) {
			checkBox.setChecked(auto);
			timePicker.setCurrentHour(hour);
			timePicker.setCurrentMinute(min);
		}
	}

	private PendingIntent pendingIntent = null;

	private OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			RadioButton radioButton = (RadioButton) group
					.findViewById(checkedId);
			String text = radioButton.getText().toString();
			Toast.makeText(main.getApplicationContext(),
					"Scan frequency is set to " + text, Toast.LENGTH_SHORT)
					.show();

			main.setTimerInterval(text);

			saveSettings();
		}

	};
	private OnTimeChangedListener onTimeChanged = new OnTimeChangedListener() {
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			if (checkBox.isChecked()) {
				if (pendingIntent != null) {
					pendingIntent.cancel();
				}
				pendingIntent = main.setAutomaticLaunch(hourOfDay, minute);
				saveSettings();
			}
		}
	};
	private android.widget.CompoundButton.OnCheckedChangeListener onCheckedBoxChange = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Integer hour = timePicker.getCurrentHour();
			Integer min = timePicker.getCurrentMinute();
			if (isChecked) {
				main.setAutomaticLaunch(hour, min);
			}
			saveSettings();
		}

	};

	@Override
	public void onResume() {
		radioGroup.setOnCheckedChangeListener(onCheckedChange);
		checkBox.setOnCheckedChangeListener(onCheckedBoxChange);
		timePicker.setOnTimeChangedListener(onTimeChanged);
		if (!checkBox.isChecked()) {
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);
			timePicker.setCurrentHour(hour);
			timePicker.setCurrentMinute(min);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		radioGroup.setOnCheckedChangeListener(null);
		checkBox.setOnCheckedChangeListener(null);
		timePicker.setOnTimeChangedListener(null);
		super.onPause();
	}
}
