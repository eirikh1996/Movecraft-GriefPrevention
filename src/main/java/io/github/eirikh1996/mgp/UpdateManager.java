package io.github.eirikh1996.mgp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateManager extends BukkitRunnable {
    private static UpdateManager instance;
    private boolean running = false;

    private UpdateManager(){}
    @Override
    public void run() {
        final double currentVersion = getCurrentVersion();
        final double newVersion = checkUpdate(currentVersion);
        MGP.getInstance().getLogger().info("Checking for updates");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (newVersion > currentVersion){

                    for (Player p : Bukkit.getOnlinePlayers()){
                        sendUpdateMessage(p);
                    }
                    return;
                }
                MGP.getInstance().getLogger().info("You are up to date");
            }
        }.runTaskLaterAsynchronously(MGP.getInstance(), 100);
    }

    public static void initialize(){
        instance = new UpdateManager();
    }

    public static synchronized UpdateManager getInstance() {
        return instance;
    }

    public void start(){
        if (running)
            return;
        runTaskTimerAsynchronously(MGP.getInstance(), 0, 100000000);
        running = true;
    }

    public void sendUpdateMessage(Player player){
        if (!player.hasPermission("mgp.update"))
            return;
        if (checkUpdate(getCurrentVersion()) <= getCurrentVersion()){
            return;
        }
        player.sendMessage(String.format("A new update of Movecraft-GriefPrevention (v%1f) is available.", checkUpdate(getCurrentVersion())));
        player.sendMessage(String.format("You are currently on v%1f", getCurrentVersion()));
        player.sendMessage("Download at: https://dev.bukkit.org/projects/movecraft-griefprevention/files");
    }

    public double getCurrentVersion(){
        return Double.parseDouble(MGP.getInstance().getDescription().getVersion());
    }

    public double checkUpdate(double currentVersion){
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=342391");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Movecraft-WorldBorder Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final JSONArray jsonArray = (JSONArray) JSONValue.parse(response);
            if (jsonArray.size() == 0) {
                MGP.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return currentVersion;
            }
            JSONObject jsonObject = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            String versionName = ((String) jsonObject.get("name"));
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            return Double.parseDouble(newVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return currentVersion;
        }
    }
}
