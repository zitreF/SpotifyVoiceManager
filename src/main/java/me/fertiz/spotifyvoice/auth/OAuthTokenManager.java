package me.fertiz.spotifyvoice.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public class OAuthTokenManager {

    private final String clientId;
    private final String clientSecret;
    private volatile String accessToken;
    private volatile Instant expiry = Instant.EPOCH;
    private final String refreshToken;
    private final TokenStore tokenStore;
    private final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OAuthTokenManager(String clientId, String clientSecret, String refreshToken, TokenStore store) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.tokenStore = store;
    }

    public synchronized String getAccessToken() throws IOException, InterruptedException {
        if (accessToken == null || Instant.now().isAfter(expiry.minusSeconds(30))) {
            refresh();
        }
        return accessToken;
    }

    private synchronized void refresh() throws IOException, InterruptedException {
        String form = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Failed to refresh token: " + resp.body());
        }
        ObjectNode n = (ObjectNode) MAPPER.readTree(resp.body());
        this.accessToken = n.get("access_token").asText();
        int expires = n.has("expires_in") ? n.get("expires_in").asInt() : 3600;
        this.expiry = Instant.now().plusSeconds(Math.max(30, expires));

        String newRefresh = n.has("refresh_token") ? n.get("refresh_token").asText() : null;
        tokenStore.saveTokens(this.accessToken, Objects.requireNonNullElse(newRefresh, refreshToken), expires);
        System.out.println("[TOKEN] refreshed access token (expires in " + expires + "s)");
    }
}

