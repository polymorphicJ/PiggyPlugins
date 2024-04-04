package com.piggyplugins.polyhunter;

import com.example.EthanApiPlugin.Collections.TileObjects;
import com.google.inject.Inject;
import com.piggyplugins.PiggyUtils.API.PlayerUtil;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;


public class PolyHunterHelper {

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private PlayerUtil playerUtil;

    public boolean inRegion(int regionId) {
        return client.getLocalPlayer().getWorldLocation().getRegionID() == regionId;
    }

    public int getCaughtTraps() {
        int size = TileObjects.search().withName("Net trap").withAction("Check").withinDistance(10).result().size();
        if (size > 0) size = size / 2;
        return size;
    }

    public int getSetTraps() {
        return TileObjects.search().withName("Young tree").withAction("Dismantle").withinDistance(10).result().size();
    }

    public boolean hasTrapSupplies() {
        return playerUtil.hasItem("Small fishing net") && playerUtil.hasItem("Rope");
    }

    public int getMaxTraps() {
        int lvl = client.getBoostedSkillLevel(Skill.HUNTER);
        if (lvl >= 80) {
            return 5;
        } else if (lvl >= 60) {
            return 4;
        } else if (lvl >= 40) {
            return 3;
        } else if (lvl >= 20) {
            return 2;
        }
        return 1;
    }


}