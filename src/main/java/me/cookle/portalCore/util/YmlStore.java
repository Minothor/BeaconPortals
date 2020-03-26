package me.cookle.portalCore.util;

import me.cookle.portalCore.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class YmlStore implements PortalStore {

    private File portalStore;
    private FileConfiguration portalList;

    public YmlStore(File dataFolder) {
        this.portalStore = new File(dataFolder, String.format("%cportals.yml", File.separatorChar));
    }

    public YmlStore() {
        this(Main.plugin.getDataFolder());
    }

    @Override
    public PortalCache loadPortals() {
        portalList = YamlConfiguration.loadConfiguration(portalStore);
        return (portalList.contains("portals"))
                ? portalList.getObject("portals", PortalCache.class)
                : new PortalCache();
    }

    /**
     * Saves the portal cache to portals.yml
     *
     * @return boolean True or False for success status
     */
    @Override
    public boolean savePortals(PortalCache cache) {
        portalList.set("portals", cache);

        try {
            portalList.save(portalStore);
            return true;
        } catch (IOException e) {
            Main.LOG.warning(e.getLocalizedMessage());
            return false;
        }
    }
}
