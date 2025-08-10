package me.fertiz.spotifyvoice.command.impl;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.CommandFactory;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.regex.Pattern;

public class SkipMultipleCommand implements VoiceCommand {

    public static final Pattern PATTERN = Pattern.compile("^skip\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final SpotifyClient spotify;
    private final int times;

    public SkipMultipleCommand(SpotifyClient spotify, int times) {
        this.spotify = spotify;
        this.times = Math.max(1, times);
    }

    @Override
    public CommandResult execute() throws Exception {
        for (int i = 0; i < times; i++) {
            spotify.next();
        }
        return new CommandResult(true, "Skipped " + times + " tracks");
    }

    @Override
    public String name() {
        return "skip-multiple";
    }

    public static CommandFactory factory(SpotifyClient spotify) {
        return matcher -> {
            int times = Integer.parseInt(matcher.group(1));
            return new SkipMultipleCommand(spotify, times);
        };
    }
}


