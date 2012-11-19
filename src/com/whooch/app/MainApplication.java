package com.whooch.app;

import android.app.Application;

import com.actionbarsherlock.R;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.CustomPushNotificationBuilder;
import com.urbanairship.push.PushManager;


public class MainApplication extends Application {

    @Override
    public void onCreate() {
		//This should only be called once
		AirshipConfigOptions options = AirshipConfigOptions
				.loadDefaultOptions(this);
		UAirship.takeOff(this, options);	
		PushManager.shared().setIntentReceiver(IntentReceiver.class);
		PushManager.enablePush();
		
		
        CustomPushNotificationBuilder nb = new CustomPushNotificationBuilder();

        nb.statusBarIconDrawableId = R.drawable.ic_menu;

        nb.layout = R.layout.notification; // The layout resource to use
        nb.layoutIconDrawableId = R.drawable.ic_menu; // The icon you want to display
        nb.layoutIconId = R.id.notification_icon; // The icon's layout 'id'
        nb.layoutSubjectId = R.id.notification_subject; // The id for the 'subject' field
        nb.layoutMessageId = R.id.notification_message; // The id for the 'message' field

        //set this ID to a value > 0 if you want a new notification to replace the previous one
        nb.constantNotificationId = 0;

        // Set the builder
        PushManager.shared().setNotificationBuilder(nb);
    }

}
