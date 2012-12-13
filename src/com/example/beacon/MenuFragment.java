package com.example.beacon;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.ParseUser;

public class MenuFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_menu, container, false);
		main = (MainActivity) getActivity();
		toggleButtonView = (ToggleButton) view
				.findViewById(R.id.toggleButtonBluetooth);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		return view;
	}

	private BluetoothAdapter mBtAdapter;
	private ToggleButton toggleButtonView;
	private MainActivity main;

	@Override
	public void onResume() {
		super.onResume();
		if (mBtAdapter != null) {
			if (mBtAdapter.isEnabled()) {
				toggleButtonView.setChecked(true);
			} else {
				toggleButtonView.setChecked(false);
			}
		}
	}

	public void toggleBluetooth(View _view) {
		ToggleButton toggleButtonView = (ToggleButton) _view;
		if (mBtAdapter != null) {
			if (toggleButtonView.isChecked()) {
				mBtAdapter.enable();
				mBtAdapter.cancelDiscovery();
				mBtAdapter.startDiscovery();
			} else {
				mBtAdapter.disable();
			}
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
