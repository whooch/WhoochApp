package com.whooch.app;

import android.app.Application;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;


public class MainApplication extends Application {

    @Override
    public void onCreate() {
		//This should only be called once
		AirshipConfigOptions options = AirshipConfigOptions
				.loadDefaultOptions(this);
		UAirship.takeOff(this, options);	
		PushManager.shared().setIntentReceiver(IntentReceiver.class);
    }

}
