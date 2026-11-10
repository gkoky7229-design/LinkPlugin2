package dev.linkbot;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkPlugin extends JavaPlugin {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();
    private static final Pattern CODE = Pattern.compile("\"code\"\\s*:\\s*\"(\\d{6})\"");

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - cooldown.getOrDefault(p.getUniqueId(), 0L) < 5000) {
            p.sendMessage("§cPlease wait a few seconds.");
            return true;
        }
        cooldown.put(p.getUniqueId(), now);

        String url = getConfig().getString("api-url");
        String secret = getConfig().getString("secret");
        String body = "{\"uuid\":\"" + p.getUniqueId() + "\",\"name\":\"" + p.getName().replace("\"", "") + "\"}";

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String msg;
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .header("x-secret", secret)
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                Matcher m = CODE.matcher(res.body());
                if (res.body().contains("\"already\"")) msg = "§aYour account is already linked.";
                else if (res.statusCode() == 200 && m.find())
                    msg = "§aYour code: §e§l" + m.group(1) + "§a (valid for 1 minute). Enter it in Discord using the link button.";
                else msg = "§cCould not get a code (error " + res.statusCode() + ").";
            } catch (Exception e) {
                getLogger().warning("Link API error: " + e);
                msg = "§cLink service is unavailable right now.";
            }
            final String out = msg;
            Bukkit.getScheduler().runTask(this, () -> p.sendMessage(out));
        });
        return true;
    }
}
