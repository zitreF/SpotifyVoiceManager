package me.fertiz.spotifyvoice.client;

import java.util.Optional;

public interface SpotifyClient {

    // songs
    void play() throws Exception;
    void play(String trackUri) throws Exception;
    void pause() throws Exception;
    void next() throws Exception;
    Optional<String> searchTrack(String query) throws Exception;

    // playlists
    void playPlaylist(String playlistUri) throws Exception;

}
