package com.example.beacon;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ImagesDisplay extends LinearLayout implements ResultChangeListener {

	protected static final String TAG = "BEACON";
	private Handler handler = new Handler();

	public ImagesDisplay(Context context) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP);
	}

	ConcurrentHashMap<String, List<ImageView>> map = new ConcurrentHashMap<String, List<ImageView>>();

	public void add(String name) {
		Log.i(TAG, "ADD " + name);
		if (map.containsKey(name)) {
			Log.i(TAG, "HAVE CACHED " + name);
			List<ImageView> imageViews = map.get(name);
			for (Iterator<ImageView> iterator = imageViews.iterator(); iterator
					.hasNext();) {
				ImageView imageView = (ImageView) iterator.next();
				//TODO Change this to keep track of view that is already in. Could have a 2 sets. Moving object around.
				try {
					this.addView(imageView);
				} catch (Exception e) {
				}
			}
		} else {
			Log.i(TAG, "NEW " + name);
			// this.addView(imageView);
			List<ImageView> imageViews = new ArrayList<ImageView>();
			map.put(name, imageViews);
			getImage(name, imageViews);
		}
	}

	private void getImage(String name, final List<ImageView> imageViews) {
		final ParseQuery query = new ParseQuery("Beacon");
		query.whereEqualTo("name", name);
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> beacons, ParseException arg1) {
				Log.i(TAG, "PARSE RETURN");
				if (beacons != null) {
					for (Iterator<ParseObject> iterator = beacons.iterator(); iterator
							.hasNext();) {
						ParseObject beacon = (ParseObject) iterator.next();
						Log.i(TAG, "HAVE RESULT");
						final ParseFile image = (ParseFile) beacon.get("image");
						image.getDataInBackground(new GetDataCallback() {
							@Override
							public void done(byte[] bytes, ParseException arg1) {
								Log.i(TAG, "IMAGE LOADED");
								if (bytes != null) {
									Log.i(TAG, "IMAGE DATA EXISTS "
											+ imageViews.toString());
									final Drawable d = Drawable
											.createFromStream(
													new ByteArrayInputStream(
															bytes), UUID
															.randomUUID()
															.toString());
									handler.post(new Runnable() {
										public void run() {
											ImageView imageView = new ImageView(
													getContext());
											imageView
													.setLayoutParams(new LayoutParams(
															LayoutParams.MATCH_PARENT,
															LayoutParams.WRAP_CONTENT));
											imageView.setImageDrawable(d);
											imageView.invalidate();
											imageViews.add(imageView);
											ImagesDisplay.this
													.addView(imageView);
										}
									});
								} else {
									image.getDataInBackground(this);
								}
								// ImagesDisplay.this.invalidate();
							}
						});
					}

				} else {
					query.findInBackground(this);
					Log.i(TAG, "NO RESULT");
				}
			}
		});
	}

	public void remove(final String name) {
		Log.i(TAG, "REMOVE " + name);
		handler.post(new Runnable() {
			public void run() {
				List<ImageView> imageViews = map.get(name);
				for (Iterator<ImageView> iterator = imageViews.iterator(); iterator
						.hasNext();) {
					ImageView imageView = (ImageView) iterator.next();
					ImagesDisplay.this.removeView(imageView);
				}
			}
		});

	}
}
