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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Retrieves and organizes media to play. Before being used, you must call
 * {@link #prepare()}, which will retrieve all of the music on the user's device
 * (by performing a query on a content resolver). After that, it's ready to
 * retrieve a random song, with its title and URI, upon request.
 */
public class MusicRetriever {
	final String TAG = "MusicRetriever";

	ContentResolver mContentResolver;

	// the items (songs) we have queried
	List<Item> mItems = new ArrayList<Item>();
	List<Item> queue = new ArrayList<Item>();
	int nowPlay = 0;

	public int getQueueSize() {
		return queue.size();
	}

	public void clearQueue() {
		queue.clear();
	}

	public int getNowPlay() {
		if (queue.size() == 0)
			return 0;
		else
			return nowPlay + 1;
	}

	public void setNowPlay(int nowPlaying) {
		if (nowPlaying >= queue.size()) {
			this.nowPlay = queue.size() - 1;
		} else {
			this.nowPlay = nowPlaying;
		}
	}

	public void setPlayAll() {
		queue.clear();
		for (int i = 0; i < mItems.size(); i++) {
			queue.add(mItems.get(i));
			nowPlay = 0;
		}
	}

	public String[] getallQueueTitle() {
		String[] returnString;
		if (queue.size() == 0)
			returnString = null;
		else {
			returnString = new String[queue.size()];
			for (int i = 0; i < queue.size(); i++) {
				returnString[i] = queue.get(i).title;
			}
		}
		return returnString;
	}

	public String[] getallQueueArtist() {
		String[] returnString;
		if (queue.size() == 0)
			returnString = null;
		else {
			returnString = new String[queue.size()];
			for (int i = 0; i < queue.size(); i++) {
				returnString[i] = queue.get(i).artist;
			}
		}
		return returnString;
	}

	public void addToQueue(int index) {
		queue.add(mItems.get(index));
	}

	public void addAndPlay(int index) {
		if (!queue.contains(mItems.get(index))) {
			queue.add(mItems.get(index));
			nowPlay = queue.size() - 1;
		} else {
			nowPlay = queue.indexOf(mItems.get(index));
		}
	}

	public void deleteFromQueue(int index) {
		Item item = queue.get(nowPlay);
		queue.remove(index);
		nowPlay = queue.indexOf(item);
	}

	Random mRandom = new Random();

	public MusicRetriever(ContentResolver cr) {
		mContentResolver = cr;
	}

	/**
	 * Loads music data. This method may take long, so be sure to call it
	 * asynchronously without blocking the main thread.
	 */
	public void prepare() {
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Log.i(TAG, "Querying media...");
		Log.i(TAG, "URI: " + uri.toString());

		// Perform a query on the content resolver. The URI we're passing
		// specifies that we
		// want to query for all audio media on external storage (e.g. SD card)
		Cursor cur = mContentResolver.query(uri, null,
				MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
		Log.i(TAG, "Query finished. "
				+ (cur == null ? "Returned NULL." : "Returned a cursor."));

		if (cur == null) {
			// Query failed...
			Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
			return;
		}
		if (!cur.moveToFirst()) {
			// Nothing to query. There is no music on the device. How boring.
			Log.e(TAG, "Failed to move cursor to first row (no query results).");
			return;
		}

		Log.i(TAG, "Listing...");

		// retrieve the indices of the columns where the ID, title, etc. of the
		// song are
		int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
		int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
		int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
		int durationColumn = cur
				.getColumnIndex(MediaStore.Audio.Media.DURATION);
		int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
		int albumIDColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

		Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
		Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));

		// add each song to mItems
		do {
			Log.i(TAG,
					"ID: " + cur.getString(idColumn) + " Title: "
							+ cur.getString(titleColumn));
			mItems.add(new Item(cur.getLong(idColumn), cur
					.getString(artistColumn), cur.getString(titleColumn), cur
					.getString(albumColumn), cur.getLong(durationColumn), cur
					.getLong(albumIDColumn)));
		} while (cur.moveToNext());

		Log.i(TAG, "Done querying media. MusicRetriever is ready.");
	}

	public ContentResolver getContentResolver() {
		return mContentResolver;
	}

	/** Returns a random Item. If there are no items available, returns null. */
	public Item getRandomItem() {
		if (queue.size() <= 0)
			return null;
		nowPlay = mRandom.nextInt(queue.size());
		return queue.get(nowPlay);
	}

	public Item getNext() {
		if (queue.size() <= 0)
			return null;
		nowPlay++;
		if (nowPlay >= queue.size())
			nowPlay = 0;
		return queue.get(nowPlay);
	}

	public Item getNow() {
		if (queue.size() <= 0)
			return null;
		return queue.get(nowPlay);
	}

	public Item getPre() {
		if (queue.size() <= 0)
			return null;
		if (nowPlay == 0)
			nowPlay = queue.size() - 1;
		else
			nowPlay--;
		return queue.get(nowPlay);
	}

	public int getNumberOfSongs() {
		return mItems.size();
	}

	public String[] getallSongsName() {
		String[] returnString;
		if (mItems.size() == 0) {
			returnString = null;
			Log.d("Retriever", "NoSong");
		} else {
			Log.d("Retriever", "HasSongs");
			returnString = new String[mItems.size()];
			for (int i = 0; i < mItems.size(); i++) {
				returnString[i] = mItems.get(i).title;
			}
		}
		return returnString;

	}

	public String[] getallSongsArtist() {
		String[] returnString;
		if (mItems.size() == 0)
			returnString = null;
		else {
			returnString = new String[mItems.size()];
			for (int i = 0; i < mItems.size(); i++) {
				returnString[i] = mItems.get(i).artist;
			}
		}
		return returnString;
	}

	public String getAlbumArt() {
		String album_art = null;
		Cursor cursor = getContentResolver().query(
				ContentUris.withAppendedId(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, queue
								.get(nowPlay).getAlbumID()),
				new String[] { MediaStore.Audio.AlbumColumns.ALBUM_ART }, null,
				null, null);

		if (cursor.moveToFirst()) {
			album_art = cursor.getString(0);
		}

		if(album_art != null)
		    Log.e("Album_ART ADDRESS",album_art);
		
		cursor.close();
		return album_art;
	}

	public static class Item {
		long id;
		String artist;
		String title;
		String album;
		long duration;
		long albumID;

		public Item(long id, String artist, String title, String album,
				long duration, long albumID) {
			this.id = id;
			this.artist = artist;
			this.title = title;
			this.album = album;
			this.duration = duration;
			this.albumID = albumID;
		}

		public long getAlbumID() {
			return albumID;
		}

		public long getId() {
			return id;
		}

		public String getArtist() {
			return artist;
		}

		public String getTitle() {
			return title;
		}

		public String getAlbum() {
			return album;
		}

		public long getDuration() {
			return duration;
		}

		public Uri getURI() {
			return ContentUris
					.withAppendedId(
							android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							id);
		}
	}
}
