package me.fertiz.spotifyvoice.command;

import me.fertiz.spotifyvoice.client.SpotifyClient;
import me.fertiz.spotifyvoice.command.impl.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

    public static final Set<String> PREFIXES = Set.of(
            "hey spotify",
            "hi spotify",
            "ok spotify",
            "okay spotify"
    );

    private final Map<Pattern, CommandFactory> commandFactories = new LinkedHashMap<>();

    public CommandParser(SpotifyClient spotify) {
        this.registerCommand(PlayCommand.PATTERN, PlayCommand.factory(spotify));
        this.registerCommand(PauseCommand.PATTERN, (matcher) -> new PauseCommand(spotify));
        this.registerCommand(SkipMultipleCommand.PATTERN, SkipMultipleCommand.factory(spotify));
        this.registerCommand(SkipCommand.PATTERN, SkipCommand.factory(spotify));
        this.registerCommand(PlayLikedSongsCommand.PATTERN, PlayLikedSongsCommand.factory(spotify));
    }

    private void registerCommand(Pattern pattern, CommandFactory factory) {
        commandFactories.put(pattern, factory);
    }

    public Optional<VoiceCommand> parse(String phrase) {
        if (phrase == null) return Optional.empty();

        String trimmed = phrase.trim().toLowerCase(Locale.ROOT);

        String matchedPrefix = null;
        for (String prefix : PREFIXES) {
            if (trimmed.startsWith(prefix)) {
                matchedPrefix = prefix;
                break;
            }
        }
        if (matchedPrefix == null) {
            return Optional.empty();
        }

        String commandPart = trimmed.substring(matchedPrefix.length()).trim();

        for (var entry : commandFactories.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(commandPart);
            if (matcher.matches()) {
                VoiceCommand cmd = entry.getValue().create(matcher);
                return Optional.of(cmd);
            }
        }
        return Optional.empty();
    }
}

