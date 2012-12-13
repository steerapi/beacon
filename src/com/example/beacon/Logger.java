package com.example.beacon;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class Logger implements FinishListener {
	protected static final String TAG = "BEACON";
	private ParseUser user = null;

	public Logger(ParseUser _user) {
		user = _user;
	}

	public void finish(HashSet<String> set) {
		ParseQuery query = new ParseQuery("Beacon");
		query.whereContainedIn("name", set);
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> beacons, ParseException arg1) {
				if (beacons != null && user != null) {
					ParseObject log = new ParseObject("log");
					for (Iterator<ParseObject> iterator = beacons.iterator(); iterator
							.hasNext();) {
						ParseObject beacon = (ParseObject) iterator.next();
						ParseRelation relation = log.getRelation("beacons");
						relation.add(beacon);
					}
					ParseRelation userRelation = log.getRelation("user");
					userRelation.add(user);
					log.saveInBackground();
				}
			}
		});
	}
}
