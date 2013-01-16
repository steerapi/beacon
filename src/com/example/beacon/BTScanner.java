package com.example.beacon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BTScanner {
	protected static final String TAG = "BEACON";
	List<ResultChangeListener> changeListeners = new ArrayList<ResultChangeListener>();
	List<FinishListener> finishListeners = new ArrayList<FinishListener>();
	List<StatusListener> statusListeners = new ArrayList<StatusListener>();

	private BluetoothAdapter mBtAdapter;
	private HashSet<String> set = new HashSet<String>();
	private HashSet<String> tmpset = new HashSet<String>();

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				tmpset.clear();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
				Toast.makeText(BTScanner.this.main,"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
				String name = device.getName();
				String address = device.getAddress();
				// TODO: Change to MAC Address
				tmpset.add(name);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.i(TAG, "SET " + tmpset.toString());
				for (Iterator<String> iterator = tmpset.iterator(); iterator
						.hasNext();) {
					String elm = iterator.next();
					if (!getSet().contains(elm)) {
						// elm add
						getSet().add(elm);
						for (Iterator<ResultChangeListener> iterator2 = changeListeners
								.iterator(); iterator2.hasNext();) {
							ResultChangeListener listener = (ResultChangeListener) iterator2
									.next();
							listener.add(elm);
						}
					}
				}
				for (Iterator<String> iterator = getSet().iterator(); iterator
						.hasNext();) {
					String elm = iterator.next();
					if (!tmpset.contains(elm)) {
						// elm remove
						iterator.remove();
						for (Iterator<ResultChangeListener> iterator2 = changeListeners
								.iterator(); iterator2.hasNext();) {
							ResultChangeListener listener = (ResultChangeListener) iterator2
									.next();
							listener.remove(elm);
						}
					}
				}
				for (Iterator<FinishListener> iterator2 = finishListeners
						.iterator(); iterator2.hasNext();) {
					FinishListener listener = (FinishListener) iterator2.next();
					listener.finish(getSet());
				}
			}
		}
	};
	private Context main;

	public BTScanner(Context _main) {
		main = _main;
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		onResume();
	}

	public void onResume() {
		mBtAdapter.enable();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		main.registerReceiver(mReceiver, filter);
	}

	public void startDiscovery() {
		mBtAdapter.cancelDiscovery();
		mBtAdapter.startDiscovery();
	}

	private Timer timer = null;
	private Handler handler = new Handler();

	public void start(int interval) {
		try {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							startDiscovery();
						}
					});
				}
			}, 0, interval);
		} catch (Exception e) {
		}
	}

	public void stopDiscovery() {
		mBtAdapter.cancelDiscovery();
	}

	public void stop() {
		if (timer != null) {
			timer.cancel();
		}
		stopDiscovery();
	}

	public void onPause() {
		stop();
		main.unregisterReceiver(mReceiver);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		main.unregisterReceiver(mReceiver);
	}

	public void addStatusChangeListener(StatusListener lis) {
		statusListeners.add(lis);
	}

	public void removeStatusChangeListener(FinishListener lis) {
		statusListeners.remove(lis);
	}

	public void addFinishListener(FinishListener lis) {
		finishListeners.add(lis);
	}

	public void removeFinishListener(FinishListener lis) {
		finishListeners.remove(lis);
	}

	public void addResultChangeListener(ResultChangeListener lis) {
		changeListeners.add(lis);
	}

	public void removeResultChangeListener(ResultChangeListener lis) {
		changeListeners.remove(lis);
	}

	public HashSet<String> getSet() {
		return set;
	}

}
