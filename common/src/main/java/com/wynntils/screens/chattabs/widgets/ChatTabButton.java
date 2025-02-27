/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.chattabs.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.chattabs.ChatTabEditingScreen;
import com.wynntils.services.chat.ChatTab;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ChatTabButton extends WynntilsButton {
    private final ChatTab tab;

    public ChatTabButton(int x, int y, int width, int height, ChatTab tab) {
        super(x, y, width, height, Component.literal("Chat Tab Button"));
        this.tab = tab;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (tab == null) return;

        RenderUtils.drawRect(
                poseStack, CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), 0, width, height);

        CustomColor color = getTabColor();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(tab.getName()),
                        getX() + 1,
                        getX() + width,
                        getY() + 1,
                        getY() + height,
                        0,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private CustomColor getTabColor() {
        if (Services.ChatTab.getFocusedTab() == tab) return CommonColors.GREEN;

        return Services.ChatTab.hasUnreadMessages(tab) ? CommonColors.YELLOW : CommonColors.WHITE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Services.ChatTab.setFocusedTab(tab);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            McUtils.mc().setScreen(ChatTabEditingScreen.create(tab));
        }

        // Hack: We should return true here,
        //       but we don't want this button to be focused
        return false;
    }

    // unused
    @Override
    public void onPress() {}
}
