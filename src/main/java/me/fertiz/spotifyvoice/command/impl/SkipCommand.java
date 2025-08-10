package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.regex.Pattern;

public class SkipCommand implements VoiceCommand {

    public static final Pattern PATTERN = Pattern.compile("^(skip|next|next track)$", Pattern.CASE_INSENSITIVE);

    private final SpotifyClient spotify;

    public SkipCommand(SpotifyClient spotify) {
        this.spotify = spotify;
    }

    @Override
    public CommandResult execute() throws Exception {
        spotify.next();
        return new CommandResult(true, "Skipped");
    }

    @Override
    public String name() {
        return "skip";
    }

    public static CommandFactory factory(SpotifyClient spotify) {
        return matcher -> new SkipCommand(spotify);
    }
}


