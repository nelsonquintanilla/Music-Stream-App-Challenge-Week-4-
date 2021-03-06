package com.applaudostudios.musicstreamappchallenge;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

import static com.applaudostudios.musicstreamappchallenge.Constants.CHANNEL_ID.PRIMARY_CHANNEL_ID;

public class ForegroundService extends Service implements MediaPlayer.OnPreparedListener {
    MediaPlayer mMediaPlayer = null;
    // Boolean that it's set to true only when the service is created, then in the onStartCommand
    // method it will prepare the media player and create/starts the notification just the first time
    // the user hits play.
    boolean mMark;
    boolean mState;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private StateSwitcher mStateSwitcher;

    public void method(StateSwitcher stateSwitcher) {
        mStateSwitcher = stateSwitcher;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ForegroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForegroundService.this;
        }
    }

    // Called the first time the service is created
    @Override
    public void onCreate() {
        super.onCreate();
        // Declaring and initializing the media player.
        mMediaPlayer = new MediaPlayer();
        String url = "http://us5.internet-radio.com:8110/listen.pls&t=.m3u";
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Because the file I am referencing might not exist.
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMark = true;
    }

    // Triggered when the service starts
    // Called every time startService is called in the service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((Constants.ACTION.ACTION_PLAY).equals(intent.getAction())) {

            // The boolean variable 'mMark' is to make sure the Media Player will prepare itself
            // just the first time after it is created. After a call to the pause() method, it wont
            // prepare itself again, instead it will call the start() method (through the onPrepared()
            // method) to resume.
            if (mMark) {
                mMediaPlayer.prepareAsync(); // prepare async to not block main thread
                mMediaPlayer.setOnPreparedListener(this);

                // Intent for go back to the activity when we touch the notification.
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);

                // Intent passed in to the pending intent that is assigned to the play button in
                // the notification.
                Intent playMusicIntent = new Intent(this, ForegroundService.class);
                playMusicIntent.setAction(Constants.ACTION.ACTION_PLAY);
                PendingIntent playMusicPendingIntent = PendingIntent.getService(this,
                        0, playMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Intent passed in to the pending intent that is assigned to the pause button in
                // the notification.
                Intent pauseMusicIntent = new Intent(this, ForegroundService.class);
                pauseMusicIntent.setAction(Constants.ACTION.ACTION_PAUSE);
                PendingIntent pauseMusicPendingIntent = PendingIntent.getService(this,
                        0, pauseMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Mandatory for notification in Android Oreo or higher
                android.app.Notification notification = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                        .setContentTitle("You're listening to")
                        .setContentText(getString(R.string.url))
                        .setSmallIcon(R.drawable.ic_android)
                        .setColor(Color.BLUE)
                        .setContentIntent(pendingIntent)
                        .addAction(R.mipmap.ic_launcher, "Play", playMusicPendingIntent)
                        .addAction(R.mipmap.ic_launcher, "Pause", pauseMusicPendingIntent)
                        .build();

                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

            } else {
                onPrepared(mMediaPlayer);
                mState = false;
                // Using the method of the interface implemented in the main activity.
                mStateSwitcher.switcher(false);
            }

        } else if ((Constants.ACTION.ACTION_PAUSE).equals(intent.getAction())) {
            mMediaPlayer.pause();
            mMark = false;
            mState = true;
            // Using the method of the interface implemented in the main activity.
            mStateSwitcher.switcher(true);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
    }

    // Needed for bound services (In this case, a started and bound service).
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Called when the service stops to release an nullify the media player.
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * Interface that it's used to update the play/pause button when we play/pause from the
     * notification buttons.
     */
    public interface StateSwitcher {
        void switcher(boolean state);
    }

}
