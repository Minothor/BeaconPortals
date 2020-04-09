package me.cookle.portalCore.listeners;

import me.cookle.portalCore.PortalCore;
import me.cookle.portalCore.util.PortalCache;
import me.cookle.portalCore.util.PortalStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.NotNull;

public class WorldSaveListener implements Listener {
    private static PortalStore portalStore;

    public WorldSaveListener(PortalStore portalStore) {
        this.portalStore = portalStore;
    }

    @EventHandler
    public void onWorldSaveEvent(@NotNull WorldSaveEvent event) {
        PortalCore.LOG.info("Saving Portal Store...");
        //TODO: deep copy cache to help prevent simultaneous read/write conflicts.
        portalStore.savePortals(PortalCore.PORTAL_CACHE);
    }
}
