package com.example.beacon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class SurroundingsFragment extends Fragment {
	private MainActivity main;
	private ImagesDisplay imagesDisplay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.activity_surroundings, container, false);
		imagesDisplay = new ImagesDisplay(view.getContext());
		ScrollView scroll = (ScrollView) view.findViewById(R.id.scrollView1);
		scroll.addView(imagesDisplay);
		main = (MainActivity) getActivity();
		return view;
	}

	@Override
	public void onResume() {
		imagesDisplay.removeAllViews();
		for (String device : main.getBtScanner().getSet()) {
			imagesDisplay.add(device);
		}
		main.getBtScanner().addResultChangeListener(imagesDisplay);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		main.getBtScanner().removeResultChangeListener(imagesDisplay);
		imagesDisplay.removeAllViews();
	}

}
