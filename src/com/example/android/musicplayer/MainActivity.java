/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.musicplayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/** 
 * Main activity: shows media player buttons. This activity shows the media player buttons and
 * lets the user click them. No media handling is done here -- everything is done by passing
 * Intents to our {@link MusicService}.
 * */
public class MainActivity extends Activity implements OnClickListener {
    /**
     * The URL we suggest as default when adding by URL. This is just so that the user doesn't
     * have to find an URL to test this sample.
     */

    ImageButton mPlayButton;
    ImageButton mSkipButton;
    ImageButton mRewindButton;
    ImageButton mVolumnButton;
    ImageButton mLibraryButton;
    ImageButton mQueueButton;
    TextView duringTimeTextView;
    TextView remainTimeTextView;
    TextView titleTextView;
    TextView detialTextView;
    TextView statusTextView;
    ImageView albumImageView;
    LinearLayout controlButtonLayOut;
    LinearLayout timeBarLayOut;
    ProgressBar seekBar;
    ArrayList<HashMap<String,String>> libraryList = new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String,String>> queueList = new ArrayList<HashMap<String,String>>();
    ListView listView;
    boolean randomPlay = false;

    private MusicService musicPlayer;
    boolean isPlay = false;
    int sowingLayout = 1;
    private Handler handler = new Handler()
    {
    	public void handleMessage(Message msg)
        {
    		
    	    int duringTime = msg.getData().getInt("nowTime");
    	    int fullTime = msg.getData().getInt("fullTime");
    	    int remainTime = fullTime - duringTime;
    	    int nowPlay = msg.getData().getInt("nowPlay");
    	    int queueSize = msg.getData().getInt("queueSize");
    	    String albumArt = msg.getData().getString("AlbumArt");
    	    isPlay = msg.getData().getBoolean("isPlay");
    		remainTimeTextView.setText("-"+Integer.toString(remainTime/60000)+":"+new DecimalFormat("00").format((remainTime/1000)%60));
			duringTimeTextView.setText(Integer.toString(duringTime/60000)+":"+new DecimalFormat("00").format((duringTime/1000)%60));
			statusTextView.setText(Integer.toString(nowPlay) + " / " + Integer.toString(queueSize));
			seekBar.setMax(fullTime);
			
			if(queueSize == 0)
			{				
				albumImageView.setImageResource(R.drawable.nofile);
				timeBarLayOut.setVisibility(View.GONE);
				controlButtonLayOut.setVisibility(View.GONE);
				statusTextView.setVisibility(View.GONE);
			}
			else
			{
				timeBarLayOut.setVisibility(View.VISIBLE);
				controlButtonLayOut.setVisibility(View.VISIBLE);
				statusTextView.setVisibility(View.VISIBLE);
				titleTextView.setText(msg.getData().getString("Title"));
				detialTextView.setText(msg.getData().getString("Detial"));
				if(albumArt == null)
				    albumImageView.setImageResource(R.drawable.dummy_album_art);
				else
				{
					Uri uri = Uri.parse(albumArt);
		    	    albumImageView.setImageURI(uri);
				}
			}
			
			if(fullTime == 0)
				seekBar.setProgress(0);
			else
			{
				seekBar.setProgress(duringTime);
			}
			if(isPlay)
				mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
			else
				mPlayButton.setImageResource(android.R.drawable.ic_media_play);
        }
    };
    
    private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			musicPlayer = ((MusicService.MyBinder) service).getService();
			if(musicPlayer != null)
			{
			    randomPlay = musicPlayer.isRandomPlayMode();
			    Log.e("MainActivity","ConnectSucceed");
			}
			else
			{
				Log.e("MainActivity","ConnectFail");
			}
		}

		public void onServiceDisconnected(ComponentName name) {
            musicPlayer = null;
		}
	};
    
    /**
     * Called when the activity is first created. Here, we simply set the event listeners and
     * start the background service ({@link MusicService}) that will handle the actual media
     * playback.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(MusicService.ACTION_PAUSE), mServiceConnection, BIND_AUTO_CREATE);
        setLayout();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options_menu, menu);
    	return true;
    }
    @Override
    public boolean onMenuOpened (int featureId, Menu menu)
    {
    	if(menu != null)
    	{
    		MenuItem menuItem = menu.findItem(R.id.randomPlay);
        	menuItem.setChecked(randomPlay);
    	}
    	return true;    	
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.randomPlay:
    		randomPlay = !randomPlay;
    		musicPlayer.setRandomPlayMode(randomPlay);
    		item.setChecked(randomPlay);
    		break;
    	case R.id.quite:
    		android.os.Process.killProcess(android.os.Process.myPid());
    		super.onDestroy();
    		break;
    	}
    	return true;
    }
    
    private Runnable SetRunningView = new Runnable() {
    	public void run() {
    		if(sowingLayout == 1 && musicPlayer!=null && !musicPlayer.isPrepareing())
    		{
    			Message m = new Message();
    			Bundle data = m.getData();
    	          data.putInt("nowTime", musicPlayer.getNowTime());
    	          data.putInt("fullTime",musicPlayer.getDurationTime());
    	          data.putInt("nowPlay", musicPlayer.getNowPlay());
    	          data.putInt("queueSize", musicPlayer.getQueuSize());
    	          data.putBoolean("isPlay", musicPlayer.getIsPlaying());
    	          data.putString("Title", musicPlayer.getTitle());
    	          data.putString("Detial", musicPlayer.getDetial());
    	          data.putString("AlbumArt", musicPlayer.getAlbumArt());
    	          m.setData(data);
    	          handler.sendMessage(m);
    		}
    		handler.postDelayed(SetRunningView, 100);
    	}
    };
    public void setLayout()
    {
    	if(sowingLayout ==1)
    	{
    		setContentView(R.layout.main);
    		mPlayButton = (ImageButton) findViewById(R.id.media_play_Button);
            mSkipButton = (ImageButton) findViewById(R.id.media_next_Button);
            mRewindButton = (ImageButton) findViewById(R.id.media_back_Button);
            mVolumnButton = (ImageButton) findViewById(R.id.volumnimageButton);
            mLibraryButton = (ImageButton) findViewById(R.id.MediaLibraryimageButton);
            duringTimeTextView = (TextView) findViewById(R.id.nowTIme);
            remainTimeTextView = (TextView) findViewById(R.id.remainTime);
            seekBar = (ProgressBar) findViewById(R.id.progressBar1);
            mQueueButton = (ImageButton) findViewById(R.id.QueueimageButton);
            albumImageView = (ImageView) findViewById(R.id.AlbumimageView);
            titleTextView = (TextView) findViewById(R.id.TitletextView);
            detialTextView = (TextView) findViewById(R.id.detialtextView);
            statusTextView = (TextView) findViewById(R.id.StatusTextView);
            controlButtonLayOut = (LinearLayout) findViewById(R.id.controlButtons);
            timeBarLayOut  = (LinearLayout)findViewById(R.id.timeBarLayOut);
            
            albumImageView.setImageResource(R.drawable.dummy_album_art);
            mPlayButton.setOnClickListener(this);
            mSkipButton.setOnClickListener(this);
            mRewindButton.setOnClickListener(this);
            mVolumnButton.setOnClickListener(this);
            mLibraryButton.setOnClickListener(this);
            mQueueButton.setOnClickListener(this);
    	}
    	else if(sowingLayout ==2)
    	{
    		setContentView(R.layout.listview);
    		libraryList.clear();
    		listView = (ListView)findViewById(R.id.listView);
    		for(int i=0; i<musicPlayer.getNumberOfSong(); i++){
   			 HashMap<String,String> item = new HashMap<String,String>();
   			 item.put( "SongTitle", musicPlayer.getAllSongTitle()[i]);
   			 item.put( "SongArtist",musicPlayer.getAllArtist()[i] );
   			 libraryList.add( item );
    		}
    		
    		SimpleAdapter adapter = new SimpleAdapter( 
    				 this, 
    				 libraryList,
    				 android.R.layout.simple_list_item_2,
    				 new String[] { "SongTitle","SongArtist" },
    				 new int[] { android.R.id.text1, android.R.id.text2 } );
    		
    		listView.setAdapter(adapter);
    		
    		listView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
						musicPlayer.AddandPlay(arg2);
			            startService(new Intent(MusicService.ACTION_STOP));
					    startService(new Intent(MusicService.ACTION_PLAY));
					sowingLayout = 1;
					setLayout();
				}
			});
    		
    		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						final int arg2, long arg3) {
					final CharSequence[] items = {"新增至佇列","全部撥放"};
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle(musicPlayer.getAllSongTitle()[arg2]);
					builder.setItems(items, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							if(which == 0)
								musicPlayer.AddtoQueue(arg2);
							else if(which ==1)
							{
								musicPlayer.playAll();
								startService(new Intent(MusicService.ACTION_STOP));
								startService(new Intent(MusicService.ACTION_PLAY));
								sowingLayout = 1;
								setLayout();
							}
						}
					}
					);
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});
   		}
    	else if(sowingLayout ==3)
    	{
    		setContentView(R.layout.listview2);
    		listView = (ListView)findViewById(R.id.listView2);
    		queueList.clear();
    		for(int i=0; i<musicPlayer.getQueuSize(); i++){
   			 HashMap<String,String> item = new HashMap<String,String>();
   			 item.put( "SongTitle", musicPlayer.getQueueTitle()[i]);
   			 item.put( "SongArtist",musicPlayer.getQueueArtist()[i] );
   			 queueList.add( item );
    		}
    		
    		SimpleAdapter adapter = new SimpleAdapter( 
    				 this, 
    				 queueList,
    				 android.R.layout.simple_list_item_2,
    				 new String[] { "SongTitle","SongArtist" },
    				 new int[] { android.R.id.text1, android.R.id.text2 } );
    		
    		listView.setAdapter(adapter);
    		
    		listView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if(musicPlayer.getNowPlay() != arg2)
					{
					    musicPlayer.setNowPlay(arg2);
					    startService(new Intent(MusicService.ACTION_STOP));
						startService(new Intent(MusicService.ACTION_PLAY));
						sowingLayout = 1;
						setLayout();
					}
				}
			});
    		
    		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						final int arg2, long arg3) {
					final CharSequence[] items = {"從佇列刪除","清空佇列"};
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle(musicPlayer.getQueueTitle()[arg2]);
					builder.setItems(items, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							if(which == 0)
							{
								if(musicPlayer.getNowPlay() == arg2+1 && musicPlayer.getQueuSize() != 1)
									startService(new Intent(MusicService.ACTION_SKIP));
								else if(musicPlayer.getQueuSize() == 1)
								{
									startService(new Intent(MusicService.ACTION_STOP));
									sowingLayout = 1;
								}
								musicPlayer.deleteFromQueue(arg2);
								setLayout();
							}
							else if(which ==1)
							{
								musicPlayer.clearQueue();
								startService(new Intent(MusicService.ACTION_STOP));
								sowingLayout = 1;
								setLayout();
							}
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});
    	}
    		
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	SetRunningView.run();
    }
    
    
    
    
    public void onClick(View target) {
        // Send the correct intent to the MusicService, according to the button that was clicked
        if (target == mPlayButton)
        {
        	if(!isPlay)
        	{
                startService(new Intent(MusicService.ACTION_PLAY));
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);

        	}
        	else
            {
            	startService(new Intent(MusicService.ACTION_PAUSE));
            	mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            }
        	isPlay = !isPlay;
        }
        else if (target == mSkipButton)
            startService(new Intent(MusicService.ACTION_SKIP));
        else if (target == mRewindButton)
            startService(new Intent(MusicService.ACTION_REWIND));
        else if (target == mVolumnButton)
        {
        	((AudioManager) getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        }
        else if(target == mLibraryButton)
        {
        	sowingLayout = 2;
        	setLayout();
        }
        else if(target == mQueueButton)
        {
        	sowingLayout = 3;
        	setLayout();
        }
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    }


    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            	((AudioManager) getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,
    					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            	return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            	((AudioManager) getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,
    					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            	return true;
            case KeyEvent.KEYCODE_BACK:
            	if(sowingLayout == 2 || sowingLayout==3)
            	{
            		sowingLayout = 1;
            		setLayout();
            		return true;
            	}
            	else
            	{
            		return super.onKeyDown(keyCode, event);
            	}
            	 
        }
        return super.onKeyDown(keyCode, event);
    }
}
