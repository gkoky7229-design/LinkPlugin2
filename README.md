# LinkPlugin

Paper plugin: `/link` gives the player a 6-digit code (valid for 1 minute) from the Discord bot's API.
The player enters the code in Discord to link their Minecraft account with their Discord account.

## Get the jar
1. Go to the **Actions** tab on GitHub and open the latest run.
2. Download the **LinkPlugin** artifact (zip) and extract the `.jar`.
3. Put the jar in your server's `plugins/` folder and start the server once.

## Configure
Edit `plugins/LinkPlugin/config.yml`:
- `api-url`: address of the Discord bot API, e.g. `http://localhost:3000/code`
- `secret`: must match `API_SECRET` in the bot's `.env`

Restart the server.
