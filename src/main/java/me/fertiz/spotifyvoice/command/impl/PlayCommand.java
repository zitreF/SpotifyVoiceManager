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
    private final Optional<String> query;

    public PlayCommand(SpotifyClient spotify) {
        this.spotify = spotify;
        this.query = Optional.empty();
    }

    public PlayCommand(SpotifyClient spotify, String query) {
        this.spotify = spotify;
        this.query = Optional.ofNullable(query).filter(s -> !s.isBlank());
    }

    @Override
    public CommandResult execute() throws Exception {
        if (query.isEmpty()) {
            spotify.play();
            return new CommandResult(true, "Playing");
        } else {
            String q = query.get();
            Optional<String> uriOpt = spotify.searchTrack(q);
            if (uriOpt.isPresent()) {
                spotify.play(uriOpt.get());
                return new CommandResult(true, "Playing '" + q + "'");
            } else {
                return new CommandResult(false, "Song not found: " + q);
            }
        }
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



