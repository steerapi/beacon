package com.example.beacon;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

public class OffersFragment extends Fragment {
	private MainActivity main;
	private ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_offers,
				container, false);
		main = (MainActivity) getActivity();
		// Create a progress bar to display while the list loads
		ProgressBar progressBar = new ProgressBar(main);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		progressBar.setIndeterminate(true);
		listView = (ListView) view.findViewById(R.id.listView1);
		listView.setEmptyView(progressBar);
		view.addView(progressBar);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		main.getCoupons(new FindCallback() {
			@Override
			public void done(List<ParseObject> coupons, ParseException arg1) {
				if (coupons != null) {
					String[] values = new String[coupons.size()];
					int i = 0;
					for (ParseObject obj : coupons) {
						values[i++] = obj.getString("name");
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							main, android.R.layout.simple_list_item_1,
							android.R.id.text1, values);
					listView.setAdapter(adapter);
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

}
