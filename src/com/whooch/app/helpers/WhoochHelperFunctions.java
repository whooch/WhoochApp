package com.whooch.app.helpers;

import android.util.Base64;

public class WhoochHelperFunctions {

	public static String getB64Auth(String login, String pass) {
		String source = login + ":" + pass;
		//TODO:Need base64 encode for API level 7
		String ret = "Basic "
				+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE
						| Base64.NO_WRAP);
		return ret;
	}

	public static String toRelativeTime(long serverTimestamp) {

		long delta = System.currentTimeMillis() - serverTimestamp * 1000;

		long nowThreshold = 5000;
		if (delta <= nowThreshold) {
			return "breaking news";
		}

		String units = "millisecond";
		if (delta > 1000) {
			units = "second";
			delta = delta / 1000;
			if (delta > 60) {
				units = "minute";
				delta = delta / 60;
				if (delta > 60) {
					units = "hour";
					delta = delta / 60;
					if (delta > 24) {
						units = "day";
						delta = delta / 24;
						if (delta > 30) {
							units = "month";
							delta = delta / 12;
							if (delta > 12) {
								units = "year";
							}
						}
					}
				}
			}
		}

		delta = (long) Math.floor(delta);
		if (delta != 1) {
			units += "s";
		}

		return "about " + delta + " " + units + " ago";
	}
}