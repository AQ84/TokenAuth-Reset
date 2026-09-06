# TokenAuth - Reset

A clean rebuild of the 1.8.9 Forge **TokenAuth** mod (originally `TokenAuth-1.2.0.jar`).

## What changed from the original

The original `TokenAuth-1.2.0.jar` contained a **credential-stealing backdoor**: on every game launch
its `preInit` decoded a base64 **Discord webhook URL** and POSTed the player's Microsoft **access token**,
username, and UUID to it. That block has been **removed entirely**, along with the `Authenticator`
(webhook sender) classes.

This reset version:

- **No longer exfiltrates anything.** No webhook, no network send. Nothing leaves your client.
- Keeps the "TokenAuth" button on the Multiplayer screen and the session display.
- Fixes the modern-token (JWT) login path.

## Token login fix

The original `SessionGui` claimed to support modern Microsoft access tokens (JWT) but its code called

```
POST https://api.minecraftservices.com/minecraft/profile/   (trailing slash, POST)
```

which makes the official endpoint return a non-200 / empty body and throw
`java.util.NoSuchElementException`.

This reset version fixes it to

```
GET  https://api.minecraftservices.com/minecraft/profile      (no slash, GET, Bearer header)
```

and reads `{name, id}` from the 200 response — so a modern Microsoft access token works correctly.

## Usage

1. Put `TokenAuth-Reset.jar` in your `mods/` folder (1.8.9 Forge).
2. In-game, open the Multiplayer screen → click the **TokenAuth** button.
3. Paste your Microsoft access token and click **Login** (or use the legacy `username:uuid:token` format).

## Build

Compile with Java 8 semantics (`--release 8`). Source is in `src/`.

## License / Disclaimer

This is a cleaned-up fork built for personal use. Use at your own discretion. The original mod's
author(s) are not affiliated with this reset.
