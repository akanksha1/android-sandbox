package com.augusto.mymediaplayer;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

import com.augusto.mymediaplayer.services.AudioPlayer;

public class MyMediaPlayer extends TabActivity {
    private static String TAG="MyMediaPlayer";
    private TextView textView;
    private Handler handler = new Handler();
    
    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private static AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PlayQueueActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("artists").setIndicator("Playlist",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, BrowseActivity.class);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("browse").setIndicator("Browse",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
        

        //bind to service
        audioPlayerIntent = new Intent(this, AudioPlayer.class);
        bindService(audioPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
    

    private void getAlbums() {
        String[] columns = {MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS};

        Cursor managedQuery = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, columns, null, null, null);
        if( managedQuery == null) {
            textView.setText("no files found");
            return;
        }
        
        textView.append("\n--------------------------------\n");
        StringBuilder sb = new StringBuilder();
        int idColumn = managedQuery.getColumnIndex(MediaStore.Audio.Albums._ID);
        int artistColumn = managedQuery.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
        int albumColumn = managedQuery.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
        int numberOfSongsColumn = managedQuery.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
        while( managedQuery.moveToNext()) {
            int albumId = managedQuery.getInt(idColumn);
            sb.append("id: ='").append(albumId).append("';");
            sb.append("artist: ='").append(managedQuery.getString(artistColumn)).append("';");
            sb.append("album: ='").append(managedQuery.getString(albumColumn)).append("';");
            sb.append("songs: ='").append(managedQuery.getInt(numberOfSongsColumn)).append("'\n");
        }
        
        textView.append(sb.toString());
    }
    
    public static AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    
    private final class AudioPlayerServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service connected");
            MyMediaPlayer.audioPlayer = ((AudioPlayer.AudioPlayerBinder) baBinder).getService();
            startService(audioPlayerIntent);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service disconnected");
            MyMediaPlayer.audioPlayer = null;
        }
    }
}