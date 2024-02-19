package com.piggyplugins.strategyexample;


import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.*;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.piggyplugins.PiggyUtils.API.InventoryUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import com.piggyplugins.PiggyUtils.strategy.TaskManager;
import com.piggyplugins.strategyexample.tasks.Banking;
import com.piggyplugins.strategyexample.tasks.OpenBank;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
        name = "<html><font color=\"#7ecbf2\">[PJ]</font>StrategySmith</html>",
        description = "",
        enabledByDefault = false,
        tags = {"poly", "plugin"}
)
@Slf4j
public class StrategySmithPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private StrategySmithConfig config;
    @Inject
    private StrategySmithOverlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    public boolean started = false;
    public int timeout = 0;
    public TaskManager taskManager = new TaskManager();

    @Provides
    private StrategySmithConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(StrategySmithConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);
        timeout = 0;
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
        timeout = 0;
    }


    @Subscribe
    private void onGameTick(GameTick event) {
        if (client.getGameState() != GameState.LOGGED_IN || !started) {
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }
        if (taskManager.hasTasks()) {
            for (AbstractTask t : taskManager.getTasks()) {
                if (t.validate()) {
                    t.execute();
                    return;
                }
            }
        }

    }

    public boolean hasBarsButNotEnough() {
        return InventoryUtil.hasItem(config.bar().getName()) && !hasEnoughBars();
    }

    public boolean hasEnoughBars() {
        return (Inventory.getItemAmount(config.bar().getName()) >= config.item().getBarsRequired());
    }

    private void checkRunEnergy() {
        if (runIsOff() && client.getEnergy() >= 30 * 100) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 10485787, -1, -1);
        }
    }

    private boolean runIsOff() {
        return EthanApiPlugin.getClient().getVarpValue(173) == 0;
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void toggle() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        started = !started;
        if (started) {
            taskManager.addTask(new OpenBank(this, config));
            taskManager.addTask(new Banking(this, config));
        } else {
            taskManager.clearTasks();
        }
    }
}