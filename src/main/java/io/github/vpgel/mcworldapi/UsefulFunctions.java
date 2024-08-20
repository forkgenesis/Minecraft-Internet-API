package io.github.vpgel.mcworldapi;

import java.net.Socket;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class UsefulFunctions {
    static String getAddress(Socket socket) {
        return (socket!=null) ? socket.getRemoteSocketAddress().toString().substring(1) : null;
    }
    
    static ResourceKey<Level> getDimension(@NotNull String arg) {
        return switch (arg) {
            case "overworld" -> Level.OVERWORLD;
            case "nether" -> Level.NETHER;
            case "end" -> Level.END;
            default -> null;
        };
    }
}
