package me.cookle.portalCore;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import static me.cookle.portalCore.Main.PORTAL_CACHE;

public class PortalListener implements Listener {
    private Main plugin;

    PortalListener(Main main) {
        plugin = main;
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        Block block = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock();
        if (block.getType() != Material.BEACON) return;

        Beacon beacon = (Beacon) block.getState();
        if (beacon.getTier() == 0) return;

        Location originLocation = block.getLocation();
        String thisID = Main.getPortalID(originLocation);
        if (thisID == null) return;

        World world = player.getWorld();
        plugin.LOG.info(String.format("thisID: %s",thisID));
        plugin.LOG.info(String.format("originLocation: %s",originLocation));

        assert PORTAL_CACHE != null;
        if (!PORTAL_CACHE.containsKey(thisID)) {
            plugin.LOG.info(String.format("Creating Portal with Key: %s",thisID));
            PORTAL_CACHE.put(thisID, originLocation.clone());
            world.playSound(originLocation, Sound.ITEM_FLINTANDSTEEL_USE, 16, 1);
        }

        plugin.LOG.info("Checking for Destination ID...");

        // Check if destination is in portal cache,
        // teleport if present
        String otherID = null;
        for (int t = 1; t < 10; t++) {
            otherID = Main.getPortalID(originLocation.clone().add(0.0, t, 0.0));
            if (PORTAL_CACHE.containsKey(otherID)){
                break;
            }
            return;
        }

        Location destinationLocation = PORTAL_CACHE.get(otherID);
        plugin.LOG.info(String.format("destinationLocation: %s", destinationLocation));
        Block destinationBlock = destinationLocation.getBlock();

        if (destinationBlock.getType() != Material.BEACON) {
            invalidatePortal(otherID, String.format("Destination was not a beacon:\n%s",
                    destinationBlock.getType()));
            world.playSound(originLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 16, 16);
            return;
        }

        Beacon destinationBeacon = (Beacon) destinationBlock.getState();
        if (destinationBeacon.getTier() == 0) {
            invalidatePortal(otherID, String.format("Destination was not a functional beacon:\nTier: %s",
                    destinationBeacon.getTier()));
            world.playSound(originLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 16, 16);
            return;
        }

        String destinationID = Main.getPortalID(destinationLocation);
        if (destinationID == null) {
            invalidatePortal(otherID, String.format("Destination ID was:\n%s\nExpected:\n%s", destinationID,otherID));
            world.playSound(originLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 16, 16);
            return;
        }

        if (!destinationID.equals(otherID)) {
            invalidatePortal(otherID, String.format("Destination ID was:\n%s\nExpected:\n%s", destinationID,otherID));
            world.playSound(originLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 16, 16);
            return;
        }

        Location locationOffset = destinationLocation.clone().subtract(originLocation);

        Collection<Mob> leashedEntities = world.getNearbyEntities(
                BoundingBox.of(
                        originLocation, 3, 3, 3
                ))
                .parallelStream().filter(entity -> entity instanceof Mob)
                .map(entity -> (Mob) entity)
                .filter(mob -> mob.isLeashed() && mob.getLeashHolder().equals(player))
                .collect(Collectors.toSet());

        world.playSound(originLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);

            Location playerDestination = player.getLocation();
            playerDestination.add(locationOffset);
            player.teleport(playerDestination);
            world.playSound(playerDestination, Sound.ENTITY_ENDERMAN_TELEPORT, 16, 16);
            world.spawnParticle(Particle.FLASH, playerDestination, 20, 0.2, 1, 0.2);

            leashedEntities.forEach(mob -> {
                Location mobLocation = mob.getLocation();
                Location mobDestination = mobLocation.add(locationOffset);
                mob.teleport(mobDestination);
                world.spawnParticle(Particle.FLASH, mobDestination, 20, 0.2, 1, 0.2);
                mob.setLeashHolder(player);
            });
        player.setSneaking(false);
        }

    private void invalidatePortal(String otherID, String reason) {
        plugin.LOG.info(String.format("Portal address invalidated: \n%s\nReason: \n%s", otherID, reason));
        PORTAL_CACHE.remove(otherID);
    }
}

