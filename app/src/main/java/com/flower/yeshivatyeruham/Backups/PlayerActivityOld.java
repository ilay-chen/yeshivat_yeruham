package com.flower.yeshivatyeruham.Backups;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flower.yeshivatyeruham.R;


public class PlayerActivityOld extends AppCompatActivity {

    private ArrayList<Song> songList;
    private ListView songView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_old);
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }


    public class Song {
        private long id;
        private String title;
        private String artist;

        public Song(long songID, String songTitle, String songArtist) {
            id = songID;
            title = songTitle;
            artist = songArtist;
        }

        public long getID(){return id;}
        public String getTitle(){return title;}
        public String getArtist(){return artist;}

    }
    public class SongAdapter extends BaseAdapter {

        private ArrayList<Song> songs;
        private LayoutInflater songInf;

        public SongAdapter(Context c, ArrayList<Song> theSongs){
            songs=theSongs;
            songInf=LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return songs.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //map to song layout
            LinearLayout songLay = (LinearLayout)songInf.inflate
                    (R.layout.song, parent, false);
            //get title and artist views
            TextView songView = (TextView)songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
            //get song using position
            Song currSong = songs.get(position);
            //get title and artist strings
            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            //set position as tag
            songLay.setTag(position);
            return songLay;
        }
    }
    public class MusicService extends Service implements
            MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        //media player
        private MediaPlayer player;
        //song list
        private ArrayList<Song> songs;
        //current position
        private int songPosn;

        @Override
        public void onCreate() {
            //create the service
            //create the service
            super.onCreate();
            //initialize position
            songPosn = 0;
            //create player
            player = new MediaPlayer();
            initMusicPlayer();
        }

        public void setList(ArrayList<Song> theSongs) {
            songs = theSongs;
        }

        public void initMusicPlayer() {
            //set player properties

//            player.setWakeMode(getApplicationContext(),
//                    PowerManager.PARTIAL_WAKE_LOCK);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
        }

        @Override
        public IBinder onBind(Intent arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {

        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {

        }


        public class MusicBinder extends Binder {
            MusicService getService() {
                return MusicService.this;
            }
        }
    }
}
