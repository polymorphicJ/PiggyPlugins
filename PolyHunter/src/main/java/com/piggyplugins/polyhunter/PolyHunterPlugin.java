package com.piggyplugins.polyhunter;


import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.*;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.piggyplugins.PiggyUtils.API.MathUtil;
import com.piggyplugins.PiggyUtils.API.PlayerUtil;
import com.piggyplugins.polyhunter.data.HunterMode;
import com.piggyplugins.polyhunter.data.Salamander;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Deque;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import java.util.*;

@PluginDescriptor(
        name = "<html><font color=\"#7ecbf2\">[PJ]</font> PolyHunter</html>",
        description = "Does various hunting related activities",
        enabledByDefault = false,
        tags = {"poly", "plugin"}
)
@Slf4j
public class PolyHunterPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    public PolyHunterConfig config;
    @Inject
    private PolyHunterOverlay overlay;
    @Inject
    private TrapTileOverlay trapTileOverlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    @Inject
    PlayerUtil playerUtil;
    @Inject
    PolyHunterHelper helper;
    public boolean started = false;
    public int timeout = 0;
    public int idleTicks = 0;
    public static final List<String> FISH_NAMES = List.of("Bluegill", "Common tench", "Mottled eel", "Greater siren");


    public int maxTraps = 1;
    public int ticksNotInRegion = 0;

    public WorldPoint startTile = null;
    Queue<WorldPoint> droppedSupplies = new LinkedList<>();

    public HunterMode hunterMode = HunterMode.AERIAL;

    @Provides
    private PolyHunterConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PolyHunterConfig.class);
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
        overlayManager.remove(trapTileOverlay);
        timeout = 0;
        started = false;
        startTile = null;
        ticksNotInRegion = 0;
        droppedSupplies.clear();
        maxTraps = 1;
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        if (client.getGameState() != GameState.LOGGED_IN || !started || config.hunterMode() != HunterMode.SALAMANDER) {
            return;
        }
        droppedSupplies.add(event.getTile().getWorldLocation());
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (client.getGameState() != GameState.LOGGED_IN || !started) {
            return;
        }
        hunterMode = config.hunterMode();
        if (ticksNotInRegion >= 20 && config.hunterMode() == HunterMode.SALAMANDER) {
            EthanApiPlugin.sendClientMessage("Not in correct region, stopping plugin");
            EthanApiPlugin.stopPlugin(this);
        }

        if (timeout > 0) {
            timeout--;
            return;
        }
        checkRunEnergy();
        idleTicks = client.getLocalPlayer().getAnimation() == -1 ? idleTicks + 1 : 0;
        switch (config.hunterMode()) {
            case AERIAL:
                handleAerialFishing();
                break;
            case BUTTERFLY:
                handleButterfly();
                break;
            case SALAMANDER:
                handleSalamander();
                break;
        }


    }

    private void handleSalamander() {
        if (startTile == null)
            startTile = client.getLocalPlayer().getWorldLocation();
        ticksNotInRegion = helper.inRegion(config.salamander().getRegionId()) ? 0 : ticksNotInRegion + 1;
        maxTraps = helper.getMaxTraps();
        if (config.salamander() == Salamander.BLACK_SALAMANDER) maxTraps++; //+1 for wildy
        dropSalamanders();

        TileObjects.search().filter(t -> startTile.distanceTo(t.getWorldLocation()) <= config.maxDist())
                .withName("Net trap").withAction("Check").nearestToPlayer().ifPresent(trap -> {
                    TileObjectInteraction.interact(trap, "Check");
                    timeout = 1 + config.tickDelay();
                });

        //this will eventually cause lost ropes and nets if another player is setting traps
        //maybe do something ab this eventually
        if (helper.getSetTraps() + helper.getCaughtTraps() >= maxTraps) {
            if (EthanApiPlugin.playerPosition().distanceTo(startTile) > 0 && !EthanApiPlugin.isMoving()) {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(startTile);
            }
        }

        TileObjects.search().filter(t -> startTile.distanceTo(t.getWorldLocation()) <= config.maxDist())
                .withName("Young tree").withAction("Set-trap").nearestToPlayer().ifPresent(tree -> {
                    if (helper.hasTrapSupplies()) {
                        TileObjectInteraction.interact(tree, "Set-trap");
                        timeout = 1 + config.tickDelay();
                    }
                });

        if (!droppedSupplies.isEmpty()) {
            WorldPoint point = droppedSupplies.peek();
            TileItems.search().itemsMatchingWildcardsNoCase("small fishing net", "rope")
                    .withinDistanceToPoint(1, point).first().ifPresentOrElse(item -> {
                        item.interact(false);
                        timeout = 1;
                    }, () -> {
                        droppedSupplies.remove();
                    });
        }
    }

    public void dropSalamanders() {
        Inventory.search().withName(config.salamander().getName()).withAction("Release").first().ifPresent(salamander -> {
            InventoryInteraction.useItem(salamander, "Release");
        });
    }

    private void handleButterfly() {
        Optional<NPC> butterfly = NPCs.search().withName(config.butterfly().getName()).withAction("Catch").nearestToPlayer();
        List<Widget> filledJars = Inventory.search().withAction("Release").withName(config.butterfly().getName()).result();
        Optional<Widget> emptyJar = Inventory.search().withName("Butterfly jar").first();


        if (!filledJars.isEmpty()) {
            filledJars.forEach(jar -> {
//                log.info("RELEASING BUTTERFLY");
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(jar, "Release");
            });
        }

        if (EthanApiPlugin.isMoving()) return;

        if (client.getLocalPlayer().getInteracting() == null && emptyJar.isPresent()) {
            if (butterfly.isPresent()) {
//                log.info("CATCHING BUTTERFLY");
                MousePackets.queueClickPacket();
                NPCPackets.queueNPCAction(butterfly.get(), "Catch");
            }
        }
        //1 tick delay after sipping stamina
        if (timeout == 0)
            timeout = config.tickDelay();
    }


    private void handleAerialFishing() {
        Deque<Projectile> projectiles = client.getProjectiles();
        ArrayList<Projectile> projectileList = new ArrayList<>();
        projectiles.forEach(projectileList::add);

        Optional<Widget> fish = Inventory.search().onlyUnnoted().nameInList(FISH_NAMES).first();
        Optional<Widget> knife = Inventory.search().withName("Knife").first();

        Optional<NPC> validFishingSpots = NPCs.search().withName("Fishing spot").nearestToPlayer().filter(npc -> {
            boolean isSpotInteractedWith = !Players.search()
                    .filter(p -> p.getInteracting() != null && p.getInteracting().equals(npc)).isEmpty();

            boolean isSpotTargetedByProjectile = projectileList.stream().anyMatch(projectile ->
                    projectile.getTarget() != null &&
                            projectile.getTarget().equals(npc.getLocalLocation()));

            if (isSpotInteractedWith || isSpotTargetedByProjectile) {
                return false;
            }
            return true;
        });
        Optional<NPC> arrowFishSpot = validFishingSpots.filter(npc -> client.getHintArrowNpc() == npc);

        if (knife.isPresent() && fish.isPresent()) {
            log.info("cutting fish");
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(knife.get(), fish.get());
            timeout = 2;
        }

        if (arrowFishSpot.isPresent()) {
//            log.info("arrow fishing");
            MousePackets.queueClickPacket();
            NPCPackets.queueNPCAction(arrowFishSpot.get(), "Catch");
        }

        if (validFishingSpots.isPresent()) {
//            log.info("fishing");
            MousePackets.queueClickPacket();
            NPCPackets.queueNPCAction(validFishingSpots.get(), "Catch");
        }
    }

    private void checkRunEnergy() {
        if (!playerUtil.isRunning() && playerUtil.runEnergy() >= MathUtil.random(25, 35)) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 10485787, -1, -1);
        }
        checkStamina();
    }

    private void checkStamina() {
        if (!playerUtil.isStaminaActive() && playerUtil.runEnergy() <= 70) {
            log.info("Stamina");
            Inventory.search().nameContains("Stamina pot").withAction("Drink").first().ifPresent(stamina -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(stamina, "Drink");
                timeout = 1;
            });
        }
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
    }
}