package io.github.vpgel.mcworldapi;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(MCWAPI.MODID)
public class MCWAPI {
    public static final String MODID = "mcworldapi";
    
    public MCWAPI() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
