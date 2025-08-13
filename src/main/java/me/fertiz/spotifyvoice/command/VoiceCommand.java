package me.fertiz.spotifyvoice.command;

import java.util.Collection;
import java.util.regex.Pattern;

public interface VoiceCommand {

    CommandResult execute() throws Exception;

    String name();

    Collection<String> prefixes();
}

