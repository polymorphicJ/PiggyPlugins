package com.piggyplugins.strategyexample.tasks;

import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import com.piggyplugins.strategyexample.StrategySmithPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Config;
import net.runelite.client.plugins.Plugin;

@Slf4j
public class Banking extends AbstractTask<StrategySmithPlugin> {
    public Banking(StrategySmithPlugin plugin, Config config) {
        super(plugin, config);
    }

    @Override
    public boolean validate() {
        return Bank.isOpen() && (!plugin.hasEnoughBars() || plugin.hasBarsButNotEnough() || Inventory.search().nameContains("Hammer").empty());
    }

    @Override
    public void execute() {
        log.info("Do Banking");
    }
}
