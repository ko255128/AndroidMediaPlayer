package com.example.android.musicplayer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class videoPlayer extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private static final String TAG = "MediaPlayer";
	private MediaPlayer mMediaPlayer;
	private SeekBar mSeekBar;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private TextView mTitleTextView;
	private ImageButton mPlayPauseImageButton;
	private ImageButton mLibraryButton;
	private ImageButton mVolumButton;
	private AudioManager mAudioManager;

	private Handler mHandler;
	private boolean mIsSeekBarTrackingTouch = false;
	private String mPath = "";
	private String title = "";
	private int showingLayOut = 1;
	private ListView listView;
	private int layoutVisibility = View.VISIBLE;
	private LinearLayout timeBarLayout;
	private LinearLayout controlButtonLayout;
	private LinearLayout fileTitlLayout;

	private boolean LoadNewFile = false;;

	Timer timer = new Timer();
	Handler timeHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				layoutVisibility = View.GONE;
				setLayoutVisibility();
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void setLayoutVisibility() {
		timeBarLayout.setVisibility(layoutVisibility);
		controlButtonLayout.setVisibility(layoutVisibility);
		fileTitlLayout.setVisibility(layoutVisibility);
	}
	
	TimerTask task = new TimerTask() {

		public void run() {
			Message message = new Message();
			message.what = 1;
			timeHandler.sendMessage(message);
			timer.cancel();
		}

	};

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.video_option_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.switchToMusic:
    		Log.e("TAG","Running HERE");
    		Intent intent = new Intent();
    		intent.setClass(videoPlayer.this, MainActivity.class);
    		videoPlayer.this.startActivity(intent);
    		finish();
    		break;
    	case R.id.videoQuite:
    		android.os.Process.killProcess(android.os.Process.myPid());
    		super.onDestroy();
    		break;
    	}
    	return true;
    }
	
	
	private ArrayList<VideoInfo> videoList = new ArrayList<VideoInfo>();

	static class VideoInfo {
		long ID;
		String filePath;
		String mimeType;
		String thumbPath;
		String title;

		public VideoInfo(long ID, String filePath, String mimeType, String title) {
			this.ID = ID;
			this.filePath = filePath;
			this.mimeType = mimeType;
			this.title = title;
		}

		public String getFilePath() {
			return filePath;
		}

		public String getMimeType() {
			return mimeType;
		}

		public String getTitle() {
			return title;
		}
	}
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		layoutVisibility = View.VISIBLE;
		setLayoutVisibility();
		Runtimer();
		return true;
	}
	
	
	
	private void setLayout() {
		if (showingLayOut == 1) {
			setContentView(R.layout.main2);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

			this.mPlayPauseImageButton = (ImageButton) findViewById(R.id.video_media_play_Button);
			this.mSeekBar = (SeekBar) findViewById(R.id.videoSeekBar);
			this.mTitleTextView = (TextView) findViewById(R.id.videoTitletextView);

			this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			this.mTitleTextView.setText(title);

			this.mLibraryButton = (ImageButton) findViewById(R.id.videoMediaLibraryimageButton);
			this.mVolumButton = (ImageButton) findViewById(R.id.videovolumnimageButton);

			timeBarLayout = (LinearLayout) findViewById(R.id.videotimeBarLayOut);
			controlButtonLayout = (LinearLayout) findViewById(R.id.videocontrolButtons);
			fileTitlLayout = (LinearLayout) findViewById(R.id.videoFileTitleLayout);
			
			mLibraryButton.setOnClickListener(this);
			mPlayPauseImageButton.setOnClickListener(this);
			mVolumButton.setOnClickListener(this);
			
			this.initSeekBar();

			this.mMediaPlayer = new MediaPlayer();
			this.mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
			this.mSurfaceHolder = this.mSurfaceView.getHolder();
			this.mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
				public void surfaceDestroyed(SurfaceHolder holder) {
					Log.d(TAG, "surfaceDestroyed called");
				}

				public void surfaceCreated(SurfaceHolder holder) {
					Log.d(TAG, "surfaceCreated called");
					DisplayMetrics dm = new DisplayMetrics();

					getWindowManager().getDefaultDisplay().getMetrics(dm);
					setVideo();
				}

				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
					Log.d(TAG, "surfaceChanged called");
				}
			});

			this.mHandler = new Handler();
		}

		else if (showingLayOut == 2) {
			setContentView(R.layout.listview2);
			listView = (ListView) findViewById(R.id.listView2);
			videoList.clear();
			captureFile();
			String[] mStrings = new String[videoList.size()];
			for (int i = 0; i < videoList.size(); i++)
				mStrings[i] = videoList.get(i).getTitle();

			listView.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, mStrings));
			listView.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mPath = videoList.get(arg2).getFilePath();
					title = videoList.get(arg2).getTitle();
					mMediaPlayer.stop();
					showingLayOut = 1;
					setLayout();
					LoadNewFile = true;
				}
			});

		}
	}

	public void captureFile() {
		String[] mediaColumns = new String[] { MediaStore.Video.Media.DATA,
				MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
				MediaStore.Video.Media.MIME_TYPE };

		Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Cursor cur = getContentResolver().query(uri, mediaColumns, null, null,
				null);
		int idColumn = cur.getColumnIndex(MediaStore.Video.Media._ID);
		int filePahColumn = cur.getColumnIndex(MediaStore.Video.Media.DATA);
		int mimeTypeColumn = cur
				.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
		int titleColumn = cur.getColumnIndex(MediaStore.Video.Media.TITLE);
		cur.moveToFirst();
		do {
			Log.i(TAG,
					"ID: " + cur.getString(idColumn) + " Title: "
							+ cur.getString(titleColumn));
			// long ID,String filePath,String mimeType,String title,long
			// duration
			videoList.add(new VideoInfo(cur.getLong(idColumn), cur
					.getString(filePahColumn), cur.getString(mimeTypeColumn),
					cur.getString(titleColumn)));
		} while (cur.moveToNext());
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setLayout();

	}

	public void Runtimer()
	{
		timer.cancel();
		task.cancel();
		timer = new Timer();
		task = new TimerTask() {

			public void run() {
				Message message = new Message();
				message.what = 1;
				timeHandler.sendMessage(message);
				timer.cancel();
			}

		};
		timer.schedule(task, 5000);
	}
	
	public void onClick(View v) {
		{
			if (v == mPlayPauseImageButton) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					mPlayPauseImageButton
							.setImageResource(android.R.drawable.ic_media_play);
				} else {
					if(mPath=="")
						return;
					this.mMediaPlayer.start();
					this.mHandler.post(seekBarThread);
					mPlayPauseImageButton
							.setImageResource(android.R.drawable.ic_media_pause);
					Runtimer();
				}

			} else if (v == mVolumButton)
				((AudioManager) getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
			else if (v == mLibraryButton) {
				showingLayOut = 2;
				setLayout();
			}
		}
	}

	private void setVideo() {
		Log.d(TAG, "playVideo called");
		if (!LoadNewFile)
			return;
		LoadNewFile = false;
		this.mMediaPlayer.stop();
		this.mMediaPlayer.reset();
		this.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			this.mMediaPlayer.setDataSource(this.mPath);
			Log.e("mPath at seVideo", mPath);
			this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
			this.mMediaPlayer.prepare();
			this.mSeekBar.setMax(this.mMediaPlayer.getDuration());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.e("IllegalArgumentException", e.toString());
			Toast.makeText(videoPlayer.this, "此檔案損毀無法撥放", Toast.LENGTH_SHORT)
					.show();
		} catch (SecurityException e) {
			Log.e("SecurityException", e.toString());
			// TODO Auto-generated catch block
			Toast.makeText(videoPlayer.this, "此檔案損毀無法撥放", Toast.LENGTH_SHORT)
					.show();
		} catch (IllegalStateException e) {
			Log.e("IllegalStateException", e.toString());
			// TODO Auto-generated catch block
			Toast.makeText(videoPlayer.this, "此檔案損毀無法撥放", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			Log.e("IOException", e.toString());
			// TODO Auto-generated catch block
			Toast.makeText(videoPlayer.this, "此檔案損毀無法撥放", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (showingLayOut == 2) {
				showingLayOut = 1;
				setLayout();
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			this.mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_SHOW_UI);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			this.mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_SHOW_UI);
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	Runnable seekBarThread = new Runnable() {
		public void run() {
			if (!mIsSeekBarTrackingTouch && mMediaPlayer.isPlaying())
			{
				mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
			}
			mHandler.postDelayed(seekBarThread, 100);
		}
	};

	private void initSeekBar() {
		this.mSeekBar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						mIsSeekBarTrackingTouch = false;
						Log.e("seekBar","false");
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						mIsSeekBarTrackingTouch = true;
						Log.e("seekBar","true");
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (mIsSeekBarTrackingTouch)
						{
							mMediaPlayer.seekTo(mSeekBar.getProgress());
							Log.e("seekBar","Change Error");
						}
						if (!mMediaPlayer.isPlaying()&&mMediaPlayer.getDuration()>0) {
							mMediaPlayer.seekTo(mSeekBar.getProgress());
							mMediaPlayer.start();
							mMediaPlayer.pause();
						}
					}
				});
	}
}
