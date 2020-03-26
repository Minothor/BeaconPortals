package me.cookle.portalCore.util;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;

@SerializableAs("PortalCache")
public class PortalCache extends HashMap<String, Location> {}
