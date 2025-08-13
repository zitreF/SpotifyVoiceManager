package me.fertiz.spotifyvoice.util;

import me.fertiz.spotifyvoice.command.CommandParser;
import me.fertiz.spotifyvoice.command.VoiceCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GrammarBuilder {

    public static String buildGrammar(CommandParser commandParser) {
        List<String> grammarPhrases = new ArrayList<>();

        for (String prefix : CommandParser.PREFIXES) {
            for (VoiceCommand command : commandParser.getCommands().values()) {
                for (String cmdPrefix : command.prefixes()) {
                    grammarPhrases.add(prefix + " " + cmdPrefix);
                }
            }
        }
        return grammarPhrases.stream()
                .map(p -> "\"" + p + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }
}
