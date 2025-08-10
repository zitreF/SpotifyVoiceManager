package me.fertiz.spotifyvoice.recognition;

public interface VoiceRecognizer {
    String listenAndRecognize() throws Exception;
    void close() throws Exception;
}

