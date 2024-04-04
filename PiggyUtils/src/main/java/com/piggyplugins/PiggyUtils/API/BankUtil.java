package com.piggyplugins.PiggyUtils.API;

import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.Collections.BankInventory;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import java.util.Collection;

public class BankUtil {

    /**
     * Turns on bank notes
     *
     * @return True if bank notes are enabled, false if the bank is open or otherwise
     */
    private boolean enableBankNotes() {
        int withdrawingNoted = EthanApiPlugin.getClient().getVarbitValue(3958);
        if (withdrawingNoted == 1 && Bank.isOpen()) {
            return true;
        }
        if (Bank.isOpen()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 786456, -1, -1);

        }
        return false;
    }

    public static void closeAmountInterface() {
        Widgets.search().withTextContains("Enter amount:").first().ifPresent(w -> {
            if (!Bank.isOpen()) EthanApiPlugin.getClient().runScript(299, 1, 0, 0);
        });
    }

    public static void depositWornItems() {
        Widget depositInventory = EthanApiPlugin.getClient().getWidget(WidgetInfoExtended.BANK_DEPOSIT_EQUIPMENT.getPackedId());
        if (depositInventory != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(depositInventory, "Deposit worn items");
        }
    }

    public static void depositInventory() {
        Widget depositInventory = EthanApiPlugin.getClient().getWidget(WidgetInfoExtended.BANK_DEPOSIT_INVENTORY.getPackedId());
        if (depositInventory != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
        }
    }

    @Deprecated
    public static void depositAll() {
        Widget depositInventory = EthanApiPlugin.getClient().getWidget(WidgetInfoExtended.BANK_DEPOSIT_INVENTORY.getPackedId());
        if (depositInventory != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
        }
    }

    public static ItemQuery nameContainsNoCase(String name) {
        return Bank.search().filter(widget -> widget.getName().toLowerCase().contains(name.toLowerCase()));
    }

    public static int getItemAmount(int itemId) {
        return getItemAmount(itemId, false);
    }

    public static int getItemAmount(int itemId, boolean stacked) {
        return stacked ?
                Bank.search().withId(itemId).first().map(Widget::getItemQuantity).orElse(0) :
                Bank.search().withId(itemId).result().size();
    }

    public static int getItemAmount(String itemName) {
        return nameContainsNoCase(itemName).result().size();
    }


    public static boolean hasItem(int id) {
        return hasItem(id, 1, false);
    }

    public static boolean hasItem(int id, int amount) {
        return getItemAmount(id, false) >= amount;
    }

    public static boolean hasItem(int id, int amount, boolean stacked) {
        return getItemAmount(id, stacked) >= amount;
    }

    public static boolean hasAny(int... ids) {
        for (int id : ids) {
            if (getItemAmount(id) > 0) {
                return true;
            }
        }
        return false;
    }

    //good idea, credit to marcojacobsNL
    public static boolean containsExcept(Collection<Integer> ids) {
        if (!Bank.isOpen()) {
            return false;
        }
        Collection<Widget> inventoryItems = BankInventory.search().result();

        for (Widget item : inventoryItems) {
            if (!ids.contains(item.getItemId())) {
                return true;
            }
        }
        return false;
    }

}
