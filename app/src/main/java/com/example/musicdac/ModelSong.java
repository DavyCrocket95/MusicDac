package com.example.musicdac;

import android.net.Uri;

public class ModelSong {
    private String songTitle;
    private String songArtist;
    private String songDuration;
    private Uri songCover;
    private Uri songUrl;

    public ModelSong() {
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(String songDuration) {
        this.songDuration = songDuration;
    }

    public Uri getSongCover() {
        return songCover;
    }

    public void setSongCover(Uri songCover) {
        this.songCover = songCover;
    }

    public Uri getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(Uri songUrl) {
        this.songUrl = songUrl;
    }

}
