package me.fertiz.spotifyvoice.recognition.impl;

import me.fertiz.spotifyvoice.command.CommandParser;
import me.fertiz.spotifyvoice.recognition.VoiceRecognizer;
import me.fertiz.spotifyvoice.util.GrammarBuilder;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

public class VoskVoiceRecognizer implements VoiceRecognizer {

    private final Model model;
    private final Recognizer recognizer;
    private final TargetDataLine microphone;

    public VoskVoiceRecognizer(CommandParser commandParser, String modelPath) throws IOException, LineUnavailableException {
        this.model = new Model(modelPath);

        String grammar = GrammarBuilder.buildGrammar(commandParser);
        System.out.println("[GRAMMAR] " + grammar);
        this.recognizer = new Recognizer(model, 16000.0f, grammar);

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        this.microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
    }

    @Override
    public String listenAndRecognize() {
        byte[] buffer = new byte[4096];
        StringBuilder result = new StringBuilder();
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 5000) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead < 0) break;

            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String partial = recognizer.getResult();
                result.append(partial);
                break;
            } else {
                // you can get partial result:
                // String partial = recognizer.getPartialResult();
            }
        }
        return result.toString();
    }

    @Override
    public void close() {
        microphone.stop();
        microphone.close();
        recognizer.close();
        model.close();
    }
}
