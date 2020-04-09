package me.cookle.portalCore.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("PortalCache")
public class PortalCache extends HashMap<List<String>, Location> implements ConfigurationSerializable {

    //TODO: add Serialization: https://bukkit.gamepedia.com/Configuration_API_Reference#Getting_Values

    public PortalCache(){
    }

    public String version;

    //Note: Cloning the location in the overridden methods since Locations behave as if they are passed by reference.
    // i.e. The add and subtract methods will change the original's value, not return the result of the operation.

    @Override
    public Location get(Object key) {
        return (super.containsKey(key))? super.get(key).clone():null;
    }
    @Override
    public Location getOrDefault(Object key, Location def) {
        return super.getOrDefault(key, def).clone();
    }

    public static boolean checkBeacon(@NotNull Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.BEACON) return true;

        Beacon beacon = (Beacon) block.getState();
        return beacon.getTier() == 0;
    }

    @Override
    public Location put(List<String> key, Location value){
        return super.put(key, value.clone());
    }

    @Nullable
    private static String getBlockData(Block block) {
        StringBuilder blockData = new StringBuilder();

        if (block instanceof Container){
            Container container = (Container)block.getState();
            blockData.append("Inventory = ");
            for (ItemStack i : container.getInventory()) {
                if (i != null) {
                    blockData.append(i.getType().name()).append(":").append(i.getAmount()).append(",");
                    continue;
                }
                blockData.append("AIR:0,");
            }
        } else if (block instanceof Sign){
            Sign sign = (Sign)block.getState();
            blockData.append("Lines = ");
            blockData.append(String.join(",", sign.getLines()));
        }
        if (blockData.length() != 0) { return blockData.toString(); }
        return null;
    }

    @Nullable
    public static List<String> getPortalID(@NotNull Location location) {
        Location loc = location.clone().subtract(0.0, 1.0, 0.0);

        ArrayList<String> ID = new ArrayList<>();
        loc = loc.subtract(1.0, 0.0, 2.0);
        boolean encountered_block = false;

        for (int i = 0; i < 16; ++i) {
            String tileEntity = getBlockData(loc.getBlock());

            if (tileEntity != null) {
                ID.add(loc.getBlock().getType().name() + ", [" + tileEntity + "]");
            }
            else {
                ID.add(loc.getBlock().getType().name());
                if (loc.getBlock().getType() != Material.AIR) { encountered_block = true; }
            }

            if (i < 3) loc.add(1.0, 0.0, 0.0);
            if (3 <= i & i < 7) loc.add(0.0, 0.0, 1.0);
            if (7 <= i & i < 11) loc.subtract(1.0, 0.0, 0.0);
            if (!(11 <= i & i < 15)) continue;loc.subtract(0.0, 0.0, 1.0);
        }

        if (!encountered_block) { return null; }
        return ID;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> ymlObject = new HashMap<>();
        ymlObject.put("version", this.version);
        ymlObject.put("portals",
                this.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().toString(),
                                entry  -> entry.getValue().serialize())));

        return ymlObject;
    }

    public static PortalCache deserialize(@NotNull Map<String, Object> ymlObject){
        PortalCache cache = new PortalCache();
        // Unsafe, but testing, shouldn't ever be anything but a memory section
        MemorySection rawObj = MemorySection.class.cast(ymlObject);
        cache.version = rawObj.getString("version");
        Map<List<String>,Location> deserializedEntries = rawObj.getObject("portals", HashMap.class);
        cache.putAll(deserializedEntries);
        return cache;
    }
}
