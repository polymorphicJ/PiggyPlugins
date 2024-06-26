package com.piggyplugins.polyhunter;


import com.example.EthanApiPlugin.Collections.TileObjects;
import com.google.common.base.Strings;
import com.piggyplugins.polyhunter.data.HunterMode;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.util.Optional;

public class PolyHunterOverlay extends Overlay {
    private final PanelComponent panelComponent = new PanelComponent();
    private final Client client;
    private final PolyHunterPlugin plugin;

    @Inject
    private PolyHunterOverlay(Client client, PolyHunterPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);

    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();

        LineComponent started = buildLine("Poly Hunter: ", plugin.started ? "Running" : "Paused");
        LineComponent timeout = buildLine("Timeout: ", String.valueOf(plugin.timeout));

        panelComponent.getChildren().add(started);
        panelComponent.getChildren().add(timeout);

        if (plugin.config.hunterMode() == HunterMode.SALAMANDER) {

            LineComponent maxTraps = buildLine("Max Traps: ", String.valueOf(plugin.maxTraps));
            LineComponent traps = buildLine("Caught traps: ", String.valueOf(plugin.helper.getCaughtTraps()));
            LineComponent trapSupplies = buildLine("Set traps: ", String.valueOf(plugin.helper.getSetTraps()));
            LineComponent tickRegion = buildLine("Ticks out of Region: ", String.valueOf(plugin.ticksNotInRegion));

            panelComponent.getChildren().add(maxTraps);
            panelComponent.getChildren().add(traps);
            panelComponent.getChildren().add(trapSupplies);
            panelComponent.getChildren().add(tickRegion);
        }

        return panelComponent.render(graphics);
    }

    /**
     * Builds a line component with the given left and right text
     *
     * @param left
     * @param right
     * @return Returns a built line component with White left text and Yellow right text
     */
    private LineComponent buildLine(String left, String right) {
        return LineComponent.builder()
                .left(left)
                .right(right)
                .leftColor(Color.WHITE)
                .rightColor(Color.YELLOW)
                .build();
    }

    private void renderTile(Graphics2D graphics, LocalPoint dest, Color color, double borderWidth, Color fillColor) {
        if (dest != null) {
            Polygon poly = Perspective.getCanvasTilePoly(this.client, dest);
            if (poly != null) {
                OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
            }
        }
    }

}
