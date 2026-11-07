# WebSocket gateway (no open ports)

Lets clients reach the server through Cloudflare Tunnel instead of a
port-forward. The client wraps its TCP traffic in WebSocket frames; on the
server, `websockify` unwraps them and passes the bytes to the normal game ports.

```
client  --wss://game.damon.nobeggar.com/43594-->  Cloudflare  --tunnel-->  cloudflared
                                                                              |
        websockify 127.0.0.1:53594  <--------------------------------------- /43594
        websockify 127.0.0.1:53595  <--------------------------------------- /43595
                |                         |
        tcp 127.0.0.1:43594       tcp 127.0.0.1:43595
        (game + on-demand)        (JAGGRAB)
```

## Server setup

1. Run the 2006Scape server as normal (game on 43594, JAGGRAB on 43595).
2. Bridges:
   ```bash
   sudo apt install websockify
   sudo cp 2006scape-ws@.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now 2006scape-ws@43594 2006scape-ws@43595
   ```
3. Tunnel: follow the comments in `cloudflared-config.yml`.

## Client

Point the client at the gateway, either with the flag:

```bash
java -jar client.jar -ws wss://game.damon.nobeggar.com
```

or by setting `ClientSettings.WEBSOCKET_URL`. When it is set, every
connection goes to `<url>/<port>` and `SERVER_IP` is ignored.

Without `-ws` the client behaves exactly as before (plain TCP).

## Caveats

- The server sees every player as `127.0.0.1`. IP bans and IP mutes
  (`data/bans/IpsBanned.txt`, `IpsMuted.txt`) can't tell players apart -
  banning that address would ban everyone. Use username bans.
- `IPS_ALLOWED` / per-IP limits are currently commented out in the server
  (`HostList`), so this does not cap logins today. If they are re-enabled,
  they will need to account for the gateway.
