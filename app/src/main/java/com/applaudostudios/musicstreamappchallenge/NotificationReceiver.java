package com.applaudostudios.musicstreamappchallenge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

    // This method can execute it's body without the app haven't to be open
    @Override
    public void onReceive(Context context, Intent intent) {
        if((Constants.ACTION.ACTION_EXAMPLE).equals(intent.getAction())){
            String message = intent.getStringExtra("toastmessage");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }


}
