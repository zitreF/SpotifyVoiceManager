package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.regex.Pattern;

public class PauseCommand implements VoiceCommand {

    public static final Pattern PATTERN = Pattern.compile("^(pause|stop)$", Pattern.CASE_INSENSITIVE);

    private final SpotifyClient spotify;

    public PauseCommand(SpotifyClient spotify) {
        this.spotify = spotify;
    }

    @Override
    public CommandResult execute() throws Exception {
        spotify.pause();
        return new CommandResult(true, "Paused");
    }

    @Override
    public String name() {
        return "pause";
    }

    public static CommandFactory factory(SpotifyClient spotify) {
        return (matcher) -> new PauseCommand(spotify);
    }
}


