package me.fertiz.spotifyvoice.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class TokenStore {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path path;

    public TokenStore(Path path) {
        this.path = path;
    }

    public synchronized void saveTokens(String access, String refresh, int expiresInSeconds) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            if (access != null) root.put("access_token", access);
            if (refresh != null) root.put("refresh_token", refresh);
            root.put("expires_at", Instant.now().getEpochSecond() + expiresInSeconds);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
            System.out.println("[TOKENS] tokens saved to " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[TOKENS] failed to save tokens: " + e.getMessage());
        }
    }

    public synchronized String getRefreshToken() {
        if (!Files.exists(path)) return null;
        try {
            var node = MAPPER.readTree(path.toFile());
            if (node.has("refresh_token")) return node.get("refresh_token").asText();
        } catch (IOException e) {
            System.err.println("[TOKENS] failed to read tokens file: " + e.getMessage());
        }
        return null;
    }
}

