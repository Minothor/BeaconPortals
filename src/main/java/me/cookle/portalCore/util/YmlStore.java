package me.cookle.portalCore.util;

import me.cookle.portalCore.PortalCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class YmlStore implements PortalStore {

    private ConfigurationSection config;
    private File portalStore;
    private FileConfiguration portalList;
    private int storeHash;

    public YmlStore(@NotNull PortalCore plugin, @NotNull ConfigurationSection config) {
        this.config = config;
        File dataFolder = plugin.getDataFolder();
        this.portalStore = new File(dataFolder, String.format("%cportals.yml", File.separatorChar));
    }

    @Override
    public PortalCache loadPortals() {
        portalList = YamlConfiguration.loadConfiguration(portalStore);
//        (portalList.contains("portals")) // TODO: add sanity/integrity checks.
        PortalCache cache = portalList.getObject("", PortalCache.class);
        if (cache == null) {
            PortalCore.LOG.warning(String.format("[%s] failed to load, supplying empty cache.", portalStore));
            cache = new PortalCache();
        }
        if(cache.version == null) {
            cache.version = portalList.getString("version", "1.0");
        }
        storeHash = cache.hashCode();
        return cache;
    }

    /**
     * Saves the portal cache to portals.yml
     *
     * @return boolean True or False for success status
     */
    @Override
    public boolean savePortals(PortalCache cache) {
        // Early return in the case that the Cache and Store are identical.
        int cacheHash = cache.hashCode();
        if (cacheHash == storeHash){
            PortalCore.LOG.info("Portal Cache unchanged - saving skipped.");
            return true;
        }
        PortalCore.LOG.info("Portal Cache modified - saving...");
        portalList.set("version", cache.version);
        portalList.set("portals", cache);

        boolean result = false;
        try {
            portalList.save(portalStore);
            result = true;
            storeHash = cacheHash;
        } catch (IOException e) {
            PortalCore.LOG.warning(e.getLocalizedMessage());
            result = false;
        } finally {
          return result;
        }
    }
}
