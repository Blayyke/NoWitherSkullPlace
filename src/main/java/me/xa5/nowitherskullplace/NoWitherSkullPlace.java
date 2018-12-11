package me.xa5.nowitherskullplace;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class NoWitherSkullPlace extends JavaPlugin implements Listener {
    private String permission;
    private String cancelMessage;
    private List<String> worldWhitelist;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        FileConfiguration config = getConfig();
        try {
            getLogger().info("Successfully loaded config!");
            config.load(new File(getDataFolder(), "config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load config. Disabling:");
            e.printStackTrace();
            return;
        }

        permission = config.getString("permission");
        cancelMessage = config.getString("cancel-message");
        worldWhitelist = config.getStringList("bypass-worlds");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        if (!(block.getState() instanceof Skull)) return;
        Skull skull = (Skull) block.getState();

        if (skull.getSkullType() != SkullType.WITHER) return;

        Location location = event.getBlockPlaced().getLocation();
        Block blockUnder = event.getBlockPlaced().getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
        if (blockUnder == null) return;

        Material typeUnder = blockUnder.getType();
        if (typeUnder != Material.SOUL_SAND) return;

        // If the world is in the whitelist don't cancel.
        if (worldWhitelist.contains(block.getWorld().getName().toLowerCase())) return;

        Player player = event.getPlayer();
        if (player != null) {
            if (player.hasPermission(permission)) return;
            else player.sendMessage(ChatColor.translateAlternateColorCodes('&', cancelMessage));
        }

        // cancel here to prevent other methods of placing
        event.setCancelled(true);
    }
}