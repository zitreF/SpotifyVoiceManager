# Voice-Controlled Spotify Player

A Java application that allows you to control Spotify playback using voice commands.  
Supports commands like play, pause, skip, play liked songs, and more.  
Uses Vosk for offline voice recognition and Spotify Web API for playback control.

---
## Configuration

This project uses environment variables for Spotify credentials for easy customization:

```bash
export SPOTIFY_CLIENT_ID=your_client_id_here
export SPOTIFY_CLIENT_SECRET=your_client_secret_here
```

Make sure to set these before running the application.

---

## OAuth Redirect URL

Spotify OAuth redirect URI is set to:

```
http://127.0.0.1:9090/spotifyvoice
```

Ensure you add this URI in your Spotify Developer Dashboard for your app's Redirect URIs.

---

## How to Use

1. Clone and build the project.
2. Set the environment variables for Spotify client ID and secret.
3. Run the application. On first run, it will open a browser window to authenticate your Spotify account.
4. After authentication, speak commands starting with one of the prefixes, for example:

   - **"Hey Spotify, play [song or artist]"**  
   - **"Hi Spotify, pause"**  
   - **"Ok Spotify, skip"**  
   - **"Hey Spotify, play liked songs"**

5. The app will recognize your voice command and control your Spotify playback accordingly.

---

## TODO
- [ ] Better and more convenient voice manager
- [ ] Improved exception handling with detailed user feedback  
- [ ] Support for more natural language variations and additional commands
- [ ] Add volume control 
- [ ] Add better playlist manager
- [ ] (Optional) Add gui 
- [ ] Easier configuration (eventually add script that will run on first time app run)
- [ ] Add more voice recognizers for optional
- [ ] Add command arguments (ex.: "repeat", "no repeat", "shuffle", "no shuffle")
---

**Contributions, feedback, and feature requests are welcome!**

