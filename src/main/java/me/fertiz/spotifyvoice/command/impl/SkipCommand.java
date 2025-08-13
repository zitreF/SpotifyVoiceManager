package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.Collection;
import java.util.Set;
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

    @Override
    public Collection<String> prefixes() {
        return Set.of("skip", "next", "next track");
    }
}


