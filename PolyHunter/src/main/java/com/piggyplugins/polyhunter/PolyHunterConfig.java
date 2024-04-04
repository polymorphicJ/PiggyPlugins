package com.piggyplugins.polyhunter;


import com.piggyplugins.polyhunter.data.ButterflyType;
import com.piggyplugins.polyhunter.data.HunterMode;
import com.piggyplugins.polyhunter.data.Salamander;
import net.runelite.client.config.*;

@ConfigGroup("PolyHunterConfig")
public interface PolyHunterConfig extends Config {
    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = "",
            position = 0
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            name = "Tick Delay",
            keyName = "tickDelay",
            description = "Slow down dialogue",
            position = 1
    )
    default int tickDelay() {
        return 0;
    }

    @ConfigItem(
            keyName = "hunterMode",
            name = "Hunter Mode",
            description = "",
            position = 2
    )
    default HunterMode hunterMode() {
        return HunterMode.AERIAL;
    }

    /*                       SALAMANDER CONFIG             */
    @ConfigSection(
            name = "Salamander Trapping",
            description = "",
            position = 20
    )
    String salamanderTrappingSection = "Salamander Trapping";

    @ConfigItem(
            keyName = "salamanderType",
            name = "Salamander",
            description = "",
            position = 21,
            section = salamanderTrappingSection
    )
    default Salamander salamander() {
        return Salamander.RED_SALAMANDER;
    }

    @ConfigItem(
            keyName = "maxArea",
            name = "Max dist",
            description = "Max distance from start tile to set traps",
            position = 22,
            section = salamanderTrappingSection
    )
    default int maxDist() {
        return 10;
    }

    /*                       BUTTERFLY CONFIG             */

    @ConfigSection(
            name = "Butterfly Catching",
            description = "",
            position = 30
    )
    String butterflyCatchingSection = "Butterfly Catching";

    @ConfigItem(
            keyName = "butterflyName",
            name = "Butterfly",
            description = "",
            position = 1,
            section = butterflyCatchingSection
    )
    default ButterflyType butterfly() {
        return ButterflyType.RUBY_HARVEST;
    }

}

