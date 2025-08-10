package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayCommand implements VoiceCommand {

    public static final Pattern PATTERN = Pattern.compile("^play(?:\\s+(.+))?$", Pattern.CASE_INSENSITIVE);

    private final SpotifyClient spotify;
    private final String query;

    public PlayCommand(SpotifyClient spotify) {
        this.spotify = spotify;
        this.query = null;
    }

    public PlayCommand(SpotifyClient spotify, String query) {
        this.spotify = spotify;
        this.query = (query.isBlank()) ? null : query;
    }

    @Override
    public CommandResult execute() throws Exception {
        if (query == null) {
            spotify.play();
            return new CommandResult(true, "Playing");
        }
        Optional<String> uriOpt = spotify.searchTrack(query);
        if (uriOpt.isPresent()) {
            spotify.play(uriOpt.get());
            return new CommandResult(true, "Playing '" + query + "'");
        }
        return new CommandResult(false, "Song not found: " + query);
    }

    @Override
    public String name() {
        return "play";
    }

    public static CommandFactory factory(SpotifyClient spotify) {
        return matcher -> {
            String arg = null;
            if (matcher.groupCount() >= 1) {
                arg = matcher.group(1);
            }
            return new PlayCommand(spotify, arg);
        };
    }
}