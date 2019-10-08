package io.github.eirikh1996.mgp;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MGP extends JavaPlugin implements Listener {
    private static MGP instance;
    private GriefPrevention griefPreventionPlugin;
    private Movecraft movecraftPlugin;

    @Override
    public void onLoad(){
        instance = this;
    }

    @Override
    public void onEnable() {

        loadConfig();
        if (!I18nSupport.initialize()){
            return;
        }

        Plugin gp = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp instanceof GriefPrevention){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - GriefPrevention found"));
            griefPreventionPlugin = (GriefPrevention) gp;
        }
        Plugin mp = getServer().getPluginManager().getPlugin("Movecraft");
        if (mp instanceof Movecraft){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft found"));
            movecraftPlugin = (Movecraft) mp;
        }
        if (griefPreventionPlugin == null || !griefPreventionPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - GriefPrevention not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (movecraftPlugin == null || !movecraftPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Movecraft not found or disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        UpdateManager.initialize();
        getServer().getPluginManager().registerEvents(this, this);
        UpdateManager.getInstance().start();
    }

    private void loadConfig(){
        saveDefaultConfig();
        Settings.locale = getConfig().getString("locale", "en");
        Settings.endSiegeOnSink = getConfig().getBoolean("endSiegeOnSink", true);
        Settings.allowSinkOnNoPvP = getConfig().getBoolean("allowSinkOnNoPvP", false);
        Settings.allowCraftEntryOnSiege = getConfig().getBoolean("allowCraftEntryOnSiege", true);
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event){
        if (event.getCraft().getSinking()){
            return;
        }
        for (MovecraftLocation ml : event.getNewHitBox()){
            if (event.getOldHitBox().contains(ml)){
                continue;
            }
            Claim claim = griefPreventionPlugin.dataStore.getClaimAt(ml.toBukkit(event.getCraft().getW()), false, null);
            if (claim == null){
                continue;
            }
            if (claim.siegeData != null && Settings.allowCraftEntryOnSiege){
                continue;
            }
            if (claim.allowAccess(event.getCraft().getNotificationPlayer()) != null){
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Not permitted to build"));
                event.setCancelled(true);
                break;
            }
        }

    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        if (event.getCraft().getSinking()){
            return;
        }
        for (MovecraftLocation ml : event.getNewHitBox()){
            if (event.getOldHitBox().contains(ml)){
                continue;
            }
            Claim claim = griefPreventionPlugin.dataStore.getClaimAt(ml.toBukkit(event.getCraft().getW()), false, null);
            if (claim == null){
                continue;
            }
            if (claim.siegeData != null && Settings.allowCraftEntryOnSiege){
                continue;
            }
            if (claim.allowAccess(event.getCraft().getNotificationPlayer()) != null){
                event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Not permitted to build"));
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onCraftSink(CraftSinkEvent event){
        Claim pvpClaim = Utils.getClaimCraftIsIn(event.getCraft());
        if (pvpClaim != null && getGriefPreventionPlugin().claimIsPvPSafeZone(pvpClaim) && !Settings.allowSinkOnNoPvP && event.getCraft().getNotificationPlayer() != null){
            event.getCraft().getNotificationPlayer().sendMessage(I18nSupport.getInternationalisedString("Sink - PvP is disabled"));
            event.setCancelled(true);
            return;
        }
        /*
        If end siege on sink is enabled, ignore crafts with null and dead pilots
         */
        if (Settings.endSiegeOnSink &&
                event.getCraft().getNotificationPlayer() != null &&
                !event.getCraft().getNotificationPlayer().isDead()){
            for (Claim claim : griefPreventionPlugin.dataStore.getClaims()){
                if (claim.siegeData == null){
                    continue;
                }
                if (event.getCraft().getNotificationPlayer().equals(claim.siegeData.attacker)){
                    griefPreventionPlugin.dataStore.endSiege(claim.siegeData, claim.siegeData.defender.getDisplayName(), event.getCraft().getNotificationPlayer().getDisplayName(), null);
                } else if (event.getCraft().getNotificationPlayer().equals(claim.siegeData.defender)){
                    griefPreventionPlugin.dataStore.endSiege(claim.siegeData, claim.siegeData.attacker.getDisplayName(), event.getCraft().getNotificationPlayer().getDisplayName(), null);
                }
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        UpdateManager.getInstance().sendUpdateMessage(event.getPlayer());
    }

    public Movecraft getMovecraftPlugin() {
        return movecraftPlugin;
    }

    public GriefPrevention getGriefPreventionPlugin() {
        return griefPreventionPlugin;
    }

    public static MGP getInstance() {
        return instance;
    }
}
