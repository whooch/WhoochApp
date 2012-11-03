package com.whooch.app.json;


public class NavigationEntry {

	public String navigationName = null;
	public int navigationType = 0;

	public NavigationEntry() {
	}

	public NavigationEntry(int type) {

		navigationType = type;
		
		if (navigationType == 1) {
			navigationName = "Stream";
		} else if (navigationType == 2) {
			navigationName = "Whooch";
		} else if (navigationType == 3) {
			navigationName = "Feedback";
		} else if (navigationType == 4) {
			navigationName = "Reactions";
		} else if (navigationType == 5) {
			navigationName = "You";
		}
	}

}