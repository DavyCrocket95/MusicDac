package com.example.musicdac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Music DAC";

    private MediaPlayer mPlayer;
    private ImageView btnPrev, btnPlay, btnNext;
    private TextView tvSongTitle, tvCurrentPosition, tvTotalDuration;
    private SeekBar sbPosition;
    private RecyclerView recyclerView;

    private int audioIndex = 0;
    double currentPos, totalDuration;

    private ArrayList<ModelSong> songArrayList;

    private static final int PERMISSION_READ = 0;

    public void init() {
        btnPrev = findViewById(R.id.btnPrev);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);

        sbPosition = findViewById(R.id.sbPosition);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        songArrayList = new ArrayList<>();

        mPlayer = new MediaPlayer();
    }

    //Vérification des autorisations d'acces -> exigé par Google
    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                        Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT).show();
                    else
                        setSong();
                }
            }
        }

    }

    //Fin verification des autorisations d'accès

    //Recup la liste music de l'appareil
    public void getAudioFile() {
        ContentResolver crAudio = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = crAudio.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                //Recup les info de la musique
                String title = cursor.getString((int) cursor.getColumnIndex((MediaStore.Audio.Media.TITLE)));
                String artist = cursor.getString((int) cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString((int) cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                ;
                String url = cursor.getString((int) cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                long albumId = cursor.getLong((int) cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                Uri coverFolder = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(coverFolder, albumId);

                ModelSong ms = new ModelSong();
                ms.setSongTitle(title);
                ms.setSongArtist(artist);
                ms.setSongDuration(duration);
                ms.setSongCover(albumArtUri);
                ms.setSongUrl(Uri.parse(url));

                songArrayList.add(ms);
                //Log.i(TAG, "Music  : " + title);

            } while (cursor.moveToNext());
        }

        AdapterSong adapterSong = new AdapterSong(this, songArrayList);
        recyclerView.setAdapter(adapterSong);

        adapterSong.setOnItemClickListener((new AdapterSong.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v1) {
                playSong(pos);
            }
        }));
    }

    private void playSong(int pos) {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(this, songArrayList.get(pos).getSongUrl());
            mPlayer.prepare();      //mise en cache
            mPlayer.start();

            //Changer le boutton play en pause - Titre
            btnPlay.setImageResource(R.drawable.ic_pause_circle_w_48);
            tvSongTitle.setText(songArrayList.get(pos).getSongTitle());

            audioIndex = pos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSongProgress();
    }

    private void setSongProgress() {
        currentPos = mPlayer.getCurrentPosition();
        totalDuration = mPlayer.getDuration();

        tvTotalDuration.setText(timerConversion((long) totalDuration));
        tvCurrentPosition.setText(timerConversion((long) currentPos));
        sbPosition.setMax((int) totalDuration);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    currentPos = mPlayer.getCurrentPosition();
                    tvCurrentPosition.setText(timerConversion((long) currentPos));
                    sbPosition.setProgress((int) currentPos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed) {
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public String timerConversion(long value) {
        String songDuration;
        int dur = (int) value;        //Temps en milliseconde
        int hrs = dur / 3600000;
        int mns = (dur / 60000) % 60000;
        int scs = (dur % 60000) / 1000;

        if (hrs > 0)
            songDuration = String.format("%02d:%02d:%02d", hrs, mns, scs);
        else
            songDuration = String.format("%02d:%02d", mns, scs);

        return songDuration;
    }

    public void prevSong() {
        btnPrev.setOnClickListener(new View.OnClickListener() {     //Fait le lien avec le design
            @Override
            public void onClick(View view) {
                if (audioIndex > 0)
                    audioIndex--;
                else
                    audioIndex = songArrayList.size() - 1;

                playSong(audioIndex);
            }

        });
    }

    public void nextSong() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioIndex < songArrayList.size() - 1)
                    audioIndex++;
                else
                    audioIndex = 0;

                playSong(audioIndex);
            }

        });
    }

    public void pause_play() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    btnPlay.setImageResource(R.drawable.ic_play_circle_w_48);
                } else {
                    mPlayer.start();
                    btnPlay.setImageResource(R.drawable.ic_pause_circle_w_48);
                }
            }
        });

    }

    public void setSong() {
        init();
        getAudioFile();     //Charger la liste music

        sbPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentPos = seekBar.getProgress();
                mPlayer.seekTo((int) currentPos);
            }
        });

        //Music fini, on passe next
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioIndex++;
                if (audioIndex > songArrayList.size() - 1)
                    audioIndex = 0;
                playSong(audioIndex);
            }
        });

        if (!songArrayList.isEmpty()) {
            //playSong(audioIndex);       //Prendre le 1er music au lancement
            prevSong();
            nextSong();
            pause_play();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (checkPermission())
            setSong();

    }

    public void play(View v1) {
        //mPlayer.start();

    }
}