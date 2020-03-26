package me.cookle.portalCore.util;

import org.jetbrains.annotations.NotNull;

public interface PortalStore {

    @NotNull
    PortalCache loadPortals();

    boolean savePortals(@NotNull PortalCache cache);
}
