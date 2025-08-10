package me.fertiz.spotifyvoice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.fertiz.spotifyvoice.auth.*;
import me.fertiz.spotifyvoice.client.SimpleHttpSpotifyClient;
import me.fertiz.spotifyvoice.command.CommandParser;
import me.fertiz.spotifyvoice.command.CommandResult;
import me.fertiz.spotifyvoice.command.VoiceCommand;
import me.fertiz.spotifyvoice.recognition.VoiceRecognizer;
import me.fertiz.spotifyvoice.recognition.impl.VoskVoiceRecognizer;
import me.fertiz.spotifyvoice.util.ResourceUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

public class SpotifyVoiceApp {

    private static final String CLIENT_ID = System.getenv("SPOTIFY_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("SPOTIFY_CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://127.0.0.1:9090/spotifyvoice";
    private static final int CALLBACK_PORT = 9090;
    private static final String SCOPES = "user-modify-playback-state user-read-playback-state user-read-currently-playing playlist-read-private playlist-modify-private user-library-read user-library-modify user-read-recently-played user-top-read";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Path tokenFile = Path.of("tokens.json");
        TokenStore tokenStore = new TokenStore(tokenFile);

        String refreshToken = tokenStore.getRefreshToken();
        if (refreshToken == null) {
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow(
                    CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, CALLBACK_PORT, SCOPES);
            System.out.println("[AUTH] No refresh token found. Starting authorization flow...");
            String code = flow.run();
            refreshToken = exchangeCodeForTokens(code, tokenStore);
            System.out.println("[AUTH] Refresh token obtained.");
        }

        OAuthTokenManager tokenManager = new OAuthTokenManager(CLIENT_ID, CLIENT_SECRET, refreshToken, tokenStore);
        SimpleHttpSpotifyClient spotifyClient = new SimpleHttpSpotifyClient(tokenManager);

        Path modelPath = ResourceUtil.extractResourceDirectory("model-vosk", SpotifyVoiceApp.class);
        VoiceRecognizer recognizer = new VoskVoiceRecognizer(modelPath.toString());

        CommandParser commandParser = new CommandParser(spotifyClient);

        System.out.println("[INFO] Available prefixes: " + String.join(", ", CommandParser.PREFIXES));
        System.out.println("[INFO] Say commands like 'Hey spotify play', 'Hey spotify pause', 'Hey spotify skip'");

        while (true) {
            System.out.println("[LISTENING] Please speak now...");
            String jsonResult = recognizer.listenAndRecognize();

            if (jsonResult == null || jsonResult.isBlank()) continue;

            String recognizedText;
            try {
                JsonNode root = OBJECT_MAPPER.readTree(jsonResult);
                recognizedText = root.path("text").asText("");
            } catch (Exception e) {
                recognizedText = jsonResult;
            }

            if (recognizedText.isBlank()) continue;

            System.out.println("[RECOGNIZED TEXT] " + recognizedText);

            Optional<VoiceCommand> commandOpt = commandParser.parse(recognizedText);

            commandOpt.ifPresentOrElse(command -> {
                try {
                    CommandResult response = command.execute();
                    System.out.println("[COMMAND] " + response.message());
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to execute command: " + e.getMessage());
                }
            }, () -> {
                System.out.println("[INFO] No known command recognized.");
            });
        }
    }

    private static String exchangeCodeForTokens(String code, TokenStore tokenStore) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String form = "grant_type=authorization_code" +
                "&code=" + java.net.URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&redirect_uri=" + java.net.URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Failed to exchange code: " + resp.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(resp.body());

        String accessToken = node.get("access_token").asText();
        String refreshToken = node.get("refresh_token").asText();
        int expiresIn = node.get("expires_in").asInt();

        tokenStore.saveTokens(accessToken, refreshToken, expiresIn);

        client.close();
        return refreshToken;
    }
}
