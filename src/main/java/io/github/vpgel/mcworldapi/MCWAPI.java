package io.github.vpgel.mcworldapi;


import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(MCWAPI.MODID)
public class MCWAPI {
    static final String MODID = "mcworldapi";
    static final Logger logger = LogUtils.getLogger();
    static ServerThread server;
    
    public MCWAPI() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStop);
    }

    void onServerStart(ServerStartedEvent event) {
        server = new ServerThread(event.getServer());
        logger.info("Server starting");
        server.start();
    }

    void onServerStop(ServerStoppingEvent event) {
        logger.info("Server stopping");
        server.interrupt();
    }
}
