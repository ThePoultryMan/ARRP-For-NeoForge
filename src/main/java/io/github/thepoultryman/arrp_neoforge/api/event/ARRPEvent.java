package io.github.thepoultryman.arrp_neoforge.api.event;

import net.minecraft.server.packs.PackResources;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public abstract class ARRPEvent extends Event {
    private final List<PackResources> packs = new ArrayList<>();

    public void addPack(PackResources pack) {
        this.packs.add(pack);
    }

    public List<PackResources> getPacks() {
        return this.packs;
    }

    // I can't figure out how to run anything before game launch, so these first
    // three are not implemented yet.
//    public static class BeforeVanillaEvent extends ARRPEvent {}
//    public static class BetweenVanillaAndModsEvent extends ARRPEvent {}
//    public static class BetweenModsAndUserEvent extends ARRPEvent {}
    public static class BeforeUserEvent extends ARRPEvent {}
    public static class AfterVanillaEvent extends ARRPEvent {}
}
