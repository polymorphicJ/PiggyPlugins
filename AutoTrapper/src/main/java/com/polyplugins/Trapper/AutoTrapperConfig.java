package com.polyplugins.Trapper;


import net.runelite.client.config.*;

@ConfigGroup("AutoTrapperConfig")
public interface AutoTrapperConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = "",
            position = 0
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigSection(
            name = "Tick Delay",
            description = "",
            position = 1

    )
    String tickDelaySection = "Tick Delay";

    @ConfigItem(
            name = "Tick Delay",
            keyName = "tickDelay",
            description = "Slow down dialogue",
            position = 1,
            section = tickDelaySection
    )
    default int tickDelay() {
        return 0;
    }
}

