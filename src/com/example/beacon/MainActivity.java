package com.example.beacon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity {

	private String TAG = "BEACON";
	// private ScrollView scroll;
	private BTScanner btScanner;
	private BluetoothAdapter mBtAdapter;
	private MenuFragment menuFragment;
	private SettingsFragment settingsFragment;
	private SurroundingsFragment surroundingsFragment;
	private OffersFragment offersFragment;
	private ArrayList<TextView> textViews = new ArrayList<TextView>();
	private ProgressBar progressBar;

	public static final String PREFS_NAME = "aQuiSettings";

	public void createNotification(String couponName, String businessName) {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Builder builder = new NotificationCompat.Builder(this);

		Intent intent = new Intent(this, MainActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, 0);

		long[] patterns = { 0, 500, 250, 500 };
		builder.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(couponName)
				.setContentText(couponName + " from " + businessName)
				// setLights (int argb, int
				// onMs, int offMs)
				.setContentIntent(pendingIntent).setAutoCancel(true)
				.setVibrate(patterns);

		// TODO: set delete intent
		Notification notification = builder.build();
		notificationManager.notify(couponName.hashCode(), notification);
	}

	public BTScanner getBtScanner() {
		return btScanner;
	}

	public void toastLocation(View _view) {
		if (user != null) {
			String location = user.getString("location");
			Toast.makeText(getApplicationContext(),
					"Your current location is " + location, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void toastPoints(View _view) {
		if (user != null) {
			Number points = user.getNumber("points");
			Toast.makeText(getApplicationContext(),
					"Your current points is " + points, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void gotoOffers(View _view) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentById(R.id.fragment_content);
		if (fragment != null) {
			ft.remove(fragment);
		}
		ft.add(R.id.fragment_content, offersFragment);
		ft.commit();
	}

	public void gotoSurroundings(View _view) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentById(R.id.fragment_content);
		if (fragment != null) {
			ft.remove(fragment);
		}
		ft.add(R.id.fragment_content, surroundingsFragment);
		ft.commit();
	}

	public void gotoSettings(View _view) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentById(R.id.fragment_content);
		if (fragment != null) {
			ft.remove(fragment);
		}
		ft.add(R.id.fragment_content, settingsFragment);
		ft.commit();
	}

	class MutableInt {
		int i;

		public MutableInt(int _i) {
			i = _i;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						MainActivity.this);

				// set title
				alertDialogBuilder.setTitle("Manual Scan");

				// set dialog message
				alertDialogBuilder
						.setMessage("Restart Scan Now?")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										SharedPreferences settings = MainActivity.this
												.getSharedPreferences(PREFS_NAME, 0);
										String interval = settings.getString("interval", "15s");
										setTimerInterval(interval);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});
		
		FragmentManager fm = getSupportFragmentManager();

		menuFragment = new MenuFragment();
		settingsFragment = new SettingsFragment();
		offersFragment = new OffersFragment();
		surroundingsFragment = new SurroundingsFragment();

		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fragment_content, menuFragment);
		ft.commit();

		textViews.add((TextView) findViewById(R.id.textView1));
		textViews.add((TextView) findViewById(R.id.textView2));
		textViews.add((TextView) findViewById(R.id.textView3));

		addEventListener(textViews.get(0));
		addEventListener(textViews.get(1));
		addEventListener(textViews.get(2));

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter != null) {
			mBtAdapter.enable();
			mBtAdapter.cancelDiscovery();
		}

		Parse.initialize(this, "3XLYnHBU7Hc9sAKMvzdf34CVRAJVb3L05GVrAzDi",
				"mkehU0ITlySlea3XaGUgXNvz8XMf2BC0sFoHtRYh");

		btScanner = new BTScanner(MainActivity.this);
		btScanner.addFinishListener(new FinishListener() {
			public void finish(HashSet<String> set) {
				if (set.size() <= 0) {
					// textViews.get(0).setText("Discovering...");
					// for (int i = 1; i < set.size(); i++) {
					// textViews.get(i).setText("");
					// }
					return;
				}
				final MutableInt i = new MutableInt(0);
				for (String name : set) {
					// Query for business name
					ParseQuery query = new ParseQuery("Beacon");
					query.whereEqualTo("name", name);
					query.getFirstInBackground(new GetCallback() {
						@Override
						public void done(ParseObject object, ParseException arg1) {
							if (object != null) {
								Log.i(TAG, "Object : " + object.toString());
								Log.i(TAG,
										"Business : "
												+ object.getRelation("business"));
								if (i.i < textViews.size()) {
									ParseRelation relation = object
											.getRelation("business");
									ParseQuery query2 = relation.getQuery();
									query2.getFirstInBackground(new GetCallback() {
										@Override
										public void done(ParseObject business,
												ParseException arg1) {
											String place = business
													.getString("name");
											textViews.get(i.i).setText(place);
											i.i++;
										}
									});
								}
							}

						}
					});

				}
			}
		});

		ParseUser.logInInBackground("tester", "tester", new LogInCallback() {
			public void done(ParseUser _user, ParseException e) {
				Log.i(TAG, "DONE LOGGING IN");
				if (_user != null) {
					user = _user;
					Log.i(TAG, "LOGGED IN");
					// Hooray! The user is logged in.
					Logger logger = new Logger(user);
					btScanner.addFinishListener(logger);
					// Restore preferences
					SharedPreferences settings = MainActivity.this
							.getSharedPreferences(PREFS_NAME, 0);
					String interval = "15s";
					boolean auto = false;
					Calendar cal = Calendar.getInstance();
					int hour = cal.get(Calendar.HOUR_OF_DAY), min = cal
							.get(Calendar.MINUTE);
					interval = settings.getString("interval", "15s");
					auto = settings.getBoolean("autolaunch", false);
					hour = settings.getInt("hour", hour);
					min = settings.getInt("min", min);

					MainActivity.this.setTimerInterval(interval);
					if (auto) {
						MainActivity.this.setAutomaticLaunch(hour, min);
					}
				} else {
					Log.i(TAG, "LOGGED IN FAILED");
					ParseUser.logInInBackground("tester", "tester", this);
					// Login failed. Look at the ParseException to see
					// what happened.
				}
			}
		});
	}

	private ParseUser user;

	private void setLocation(String location) {
		if (user != null) {
			user.put("location", location);
			user.saveEventually();
		}
	}

	private void notifyCoupons(String location) {
		ParseQuery query = new ParseQuery("Business");
		Log.i(TAG, "location: " + location);
		query.whereEqualTo("name", location);
		query.getFirstInBackground(new GetCallback() {
			@Override
			public void done(final ParseObject business, ParseException arg1) {
				if (business == null)
					return;
				List<ParseObject> coupons;
				business.getRelation("coupons").getQuery()
						.findInBackground(new FindCallback() {
							@Override
							public void done(List<ParseObject> coupons,
									ParseException arg1) {
								Log.i(TAG, "Coupons: " + coupons.toString());
								Log.i(TAG, "Coupons: " + coupons.size());
								if (user != null) {
									ParseRelation relation = user
											.getRelation("coupons");
									for (ParseObject coupon : coupons) {
										relation.add(coupon);
										createNotification(
												(String) coupon.get("name"),
												(String) business.get("name"));
									}
									user.saveInBackground();
								}
							}
						});
			}
		});
	}

	private void addEventListener(TextView textView) {
		textView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						MainActivity.this);

				final String location = (String) ((TextView) v).getText()
						.toString();
				// set title
				alertDialogBuilder.setTitle(location);

				// set dialog message
				alertDialogBuilder
						.setMessage("Set as current location?")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, close
										// current activity
										setLocation(location);
										notifyCoupons(location);
									}

								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		btScanner.onResume();
	}

	public void toggleBluetooth(View _view) {
		menuFragment.toggleBluetooth(_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	protected void onPause() {
		super.onPause();
		btScanner.onPause();
	};

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_content);
		if (fragment == menuFragment) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);

			alertDialogBuilder.setTitle("Confirm Exit");

			// set dialog message
			alertDialogBuilder
					.setMessage("Are you sure?")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// if this button is clicked, close
									// current activity
									MainActivity.super.onBackPressed();
								}

							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									dialog.cancel();
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
			
		} else {
			FragmentTransaction ft = fm.beginTransaction();
			if (fragment != null) {
				ft.remove(fragment);
			}
			ft.add(R.id.fragment_content, menuFragment);
			ft.commit();
		}
	}
	
	public void setTimerInterval(String text) {
		if (text.equals("15s")) {
			setTimerInterval(15000);
		} else if (text.equals("30s")) {
			setTimerInterval(30000);
		} else if (text.equals("1m")) {
			setTimerInterval(60000);
		} else if (text.equals("2m")) {
			setTimerInterval(120000);
		} else if (text.equals("5m")) {
			setTimerInterval(300000);
		}
	}

	public void setTimerInterval(int interval) {
		btScanner.stop();
		btScanner.start(interval);
	}

	public PendingIntent setAutomaticLaunch(int hour, int min) {
		FragmentActivity activity = this;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);

		String text = String.format("%tl:%tM", calendar, calendar);
		Toast.makeText(activity.getApplicationContext(),
				"Automatic launch is at " + text, Toast.LENGTH_SHORT).show();
		Intent myIntent = new Intent(activity, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getService(activity, 0,
				myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager) activity
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				AlarmManager.INTERVAL_DAY, calendar.getTimeInMillis(),
				pendingIntent);
		return pendingIntent;
	}

	public void getCoupons(final FindCallback findCallback) {
		if (user != null) {
			ParseRelation coupons = user.getRelation("coupons");
			ParseQuery couponQuery = coupons.getQuery();
			couponQuery.findInBackground(findCallback);
		}
	}
	
}
