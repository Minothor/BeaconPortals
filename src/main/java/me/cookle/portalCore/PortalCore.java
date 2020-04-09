package me.cookle.portalCore;

import me.cookle.portalCore.listeners.PortalListener;
import me.cookle.portalCore.listeners.WorldSaveListener;
import me.cookle.portalCore.util.PortalCache;
import me.cookle.portalCore.util.PortalStore;
import me.cookle.portalCore.util.YmlStore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class PortalCore extends JavaPlugin {

    public static @Nullable PortalCache PORTAL_CACHE;
    public static Logger LOG;
    public static PortalStore portalStore;
    private FileConfiguration config;

    public void onEnable() {
        ConfigurationSerialization.registerClass(PortalCache.class);
        config = this.getConfig();
        LOG = this.getLogger();
        // Temporarily hard-coding the YAML storage method until others are complete.
        ConfigurationSection storageConfig = config.getConfigurationSection("storage");
        portalStore = new YmlStore(this, storageConfig);

        this.PORTAL_CACHE = portalStore.loadPortals();

        //TODO: Validate Cache version and update if necessary.

        this.getServer().getPluginManager().registerEvents(new PortalListener(this), this);

        this.getServer().getPluginManager().registerEvents(new WorldSaveListener(portalStore), this);
    }

    public void onDisable() {
        portalStore.savePortals(PORTAL_CACHE);
        ConfigurationSerialization.unregisterClass(PortalCache.class);
    }
}

