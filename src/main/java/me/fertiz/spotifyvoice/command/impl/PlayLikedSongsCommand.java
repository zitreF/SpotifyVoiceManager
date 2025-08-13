package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayLikedSongsCommand implements VoiceCommand {

    public static final Pattern PATTERN = Pattern.compile("^playlist liked songs$", Pattern.CASE_INSENSITIVE);

    private final SpotifyClient spotify;

    public PlayLikedSongsCommand(SpotifyClient spotify) {
        this.spotify = spotify;
    }

    @Override
    public CommandResult execute() throws Exception {
        spotify.playPlaylist("spotify:collection");
        return new CommandResult(true, "Playing your liked songs playlist");
    }

    @Override
    public String name() {
        return "play-liked-songs";
    }

    public static CommandFactory factory(SpotifyClient spotify) {
        return matcher -> new PlayLikedSongsCommand(spotify);
    }

    @Override
    public Collection<String> prefixes() {
        return Set.of("playlist", "liked", "songs");
    }
}

