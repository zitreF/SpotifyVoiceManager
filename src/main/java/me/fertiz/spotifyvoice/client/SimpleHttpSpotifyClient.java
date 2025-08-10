package me.fertiz.spotifyvoice.client;

import me.fertiz.spotifyvoice.auth.OAuthTokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SimpleHttpSpotifyClient implements SpotifyClient {

    private final OAuthTokenManager tokenManager;
    private final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SimpleHttpSpotifyClient(OAuthTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    private HttpRequest.Builder base(String url) throws Exception {
        String token = tokenManager.getAccessToken();
        return HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");
    }

    @Override
    public void play() throws Exception {
        var req = base("https://api.spotify.com/v1/me/player/play")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndHandle(req);
    }

    @Override
    public void play(String trackUri) throws Exception {
        // build JSON body with context_uri or uris array
        String json = "{\"uris\": [\"" + trackUri + "\"]}";

        var req = base("https://api.spotify.com/v1/me/player/play")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        sendAndHandle(req);
    }

    @Override
    public void pause() throws Exception {
        var req = base("https://api.spotify.com/v1/me/player/pause")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndHandle(req);
    }

    @Override
    public void next() throws Exception {
        var req = base("https://api.spotify.com/v1/me/player/next")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendAndHandle(req);
    }

    @Override
    public Optional<String> searchTrack(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=1";

        var req = base(url).GET().build();
        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new Exception("Spotify API error (search): " + resp.statusCode() + " - " + resp.body());
        }

        JsonNode root = MAPPER.readTree(resp.body());
        JsonNode tracks = root.path("tracks").path("items");
        if (tracks.isArray() && !tracks.isEmpty()) {
            JsonNode first = tracks.get(0);
            return Optional.ofNullable(first.get("uri").asText());
        }
        return Optional.empty();
    }

    private void sendAndHandle(HttpRequest req) throws Exception {
        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        if (code >= 200 && code < 300) return;
        if (code == 401) throw new Exception("Unauthorized (401) - token may be expired.");
        throw new Exception("Spotify API error " + code + " -> " + resp.body());
    }
}
