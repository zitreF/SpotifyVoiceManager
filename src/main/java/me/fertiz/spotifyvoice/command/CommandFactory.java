package me.fertiz.spotifyvoice.command;

import java.util.regex.Matcher;

@FunctionalInterface
public interface CommandFactory {
    VoiceCommand create(Matcher matcher);
}

