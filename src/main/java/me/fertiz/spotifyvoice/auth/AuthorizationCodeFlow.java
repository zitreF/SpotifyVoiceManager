package me.fertiz.spotifyvoice.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.fertiz.spotifyvoice.util.URIBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AuthorizationCodeFlow {

    private final String clientId;
    private final String redirectUri;
    private final int callbackPort;
    private final String scopes;
    private final String clientSecret;

    public AuthorizationCodeFlow(String clientId, String clientSecret, String redirectUri, int callbackPort, String scopes) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.callbackPort = callbackPort;
        this.scopes = scopes;
    }

    /**
     * Starts the authorization flow.
     * Opens a browser window and starts a local server to capture the code.
     * Blocks until user authorizes or timeout.
     */
    public String run() throws Exception {
        String state = UUID.randomUUID().toString();
        String authUrl = URIBuilder.create("https://accounts.spotify.com/authorize")
                .addParam("client_id", clientId)
                .addParam("response_type", "code")
                .addParam("redirect_uri", redirectUri)
                .addParam("scope", scopes)
                .addParam("state", state)
                .addParam("show_dialog", "true")
                .build();

        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", callbackPort), 0);
        server.createContext("/spotifyvoice", new CallbackHandler(codeFuture, state));
        server.start();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            System.out.println("[AUTH] Open this URL in a browser:\n" + authUrl);
        }

        String code = codeFuture.get(180, TimeUnit.SECONDS);

        Thread.sleep(500);

        server.stop(1);
        return code;
    }

    private record CallbackHandler(CompletableFuture<String> codeFuture, String expectedState) implements HttpHandler {

        @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> q = parseQuery(query);
                    String code = q.get("code");
                    String state = q.get("state");

                    String responseText;
                    if (code != null && (expectedState == null || expectedState.equals(state))) {
                        codeFuture.complete(code);
                        responseText = "<html><body><h3>Authorized! You can close this tab.</h3></body></html>";
                    } else {
                        codeFuture.completeExceptionally(new RuntimeException("No code in callback or state mismatch"));
                        responseText = "<html><body><h3>Authorization failed or canceled.</h3></body></html>";
                    }
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                    byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                        os.flush();
                    }
                } catch (Exception e) {
                    codeFuture.completeExceptionally(e);
                    String response = "<html><body><h3>Error: " + e.getMessage() + "</h3></body></html>";
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                        os.flush();
                    }
                }
            }
        }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> out = new HashMap<>();
        if (query == null || query.isBlank()) return out;
        var parts = query.split("&");
        for (String p : parts) {
            int i = p.indexOf('=');
            if (i > 0) {
                String k = URLDecoder.decode(p.substring(0, i), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(p.substring(i + 1), StandardCharsets.UTF_8);
                out.put(k, v);
            }
        }
        return out;
    }
}

