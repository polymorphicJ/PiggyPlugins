package com.piggyplugins.PiggyUtils.strategy;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Config;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.worldhopper.WorldHopperPlugin;

public class ExampleTask extends AbstractTask {
    public ExampleTask( Plugin plugin, Config config) {
        super( plugin, config);
    }
    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public void execute() {

        interactNpc("Goblin", "Attack", true);

    }
}
