/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.emeralds.EmeraldModel;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketPriceMatchFeature extends Feature {
    private static final StyledText CLICK_TO_SET_PRICE = StyledText.fromString("§aClick to Set Price");
    private static final StyledText SELL_DIALOGUE_TITLE = StyledText.fromString("What would you like to sell?");
    private static final StyledText TYPE_SELL_PRICE =
            StyledText.fromString("§6Type the price in emeralds or type 'cancel' to cancel:");

    // Test suite: https://regexr.com/7h631
    private static final Pattern HIGHEST_BUY_PATTERN = Pattern.compile("§7Highest Buy Offer: §a(\\d+)²§8 \\(.+\\)");
    // Test suite: https://regexr.com/7h62u
    private static final Pattern LOWEST_SELL_PATTERN = Pattern.compile("§7Lowest Sell Offer: §a(\\d+)²§8 \\(.+\\)");

    private static final int PRICE_SET_ITEM_SLOT = 12;
    private static final int PRICE_INFO_ITEM_SLOT = 17;

    private boolean sendPriceMessage = false;
    private long priceToSend = 0;

    @SubscribeEvent
    public void onSellDialogueUpdated(ContainerSetContentEvent.Post e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen containerScreen)) return;

        StyledText title = StyledText.fromComponent(containerScreen.getTitle());
        if (!title.equals(SELL_DIALOGUE_TITLE)) return;

        StyledText amountItemName = StyledText.fromComponent(
                containerScreen.getMenu().getSlot(PRICE_SET_ITEM_SLOT).getItem().getHoverName());
        if (!amountItemName.equals(CLICK_TO_SET_PRICE)) return;

        removePriceButtons(containerScreen);

        addPriceButtons(containerScreen);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!sendPriceMessage) return;
        if (!e.getStyledText().equals(TYPE_SELL_PRICE)) return;

        WynntilsMod.info("Trying to set trade market price to " + priceToSend);

        McUtils.sendChat(String.valueOf(priceToSend));

        sendPriceMessage = false;
    }

    private Pair<Integer, Integer> getBuySellOffers(MenuAccess<ChestMenu> containerScreen) {
        ItemStack priceInfoItem =
                containerScreen.getMenu().getSlot(PRICE_INFO_ITEM_SLOT).getItem();
        Integer a, b;

        Matcher highestBuyMatcher = LoreUtils.matchLoreLine(priceInfoItem, 6, HIGHEST_BUY_PATTERN);
        a = highestBuyMatcher.matches() ? Integer.parseInt(highestBuyMatcher.group(1)) : null;
        Matcher lowestSellMatcher = LoreUtils.matchLoreLine(priceInfoItem, 6, LOWEST_SELL_PATTERN);
        b = lowestSellMatcher.matches() ? Integer.parseInt(lowestSellMatcher.group(1)) : null;

        return Pair.of(a, b);
    }

    private void addPriceButtons(ContainerScreen containerScreen) {
        Pair<Integer, Integer> buySellOffers = getBuySellOffers(containerScreen);

        int rightPos = containerScreen.leftPos + containerScreen.imageWidth;

        if (buySellOffers.a() != null) {
            int untaxedA = (int) Math.round(buySellOffers.a() / EmeraldModel.TAX_AMOUNT);
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos,
                    buySellOffers.a(),
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOffer"),
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOfferTooltip")
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedA, false))
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(buySellOffers.a(), false))
                                    .withStyle(ChatFormatting.GRAY))));
        }

        if (buySellOffers.b() != null) {
            int untaxedB = (int) Math.round(buySellOffers.b() / EmeraldModel.TAX_AMOUNT);
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos + PriceButton.BUTTON_HEIGHT + 2,
                    untaxedB,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOffer"),
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOfferTooltip")
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedB, false))
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(buySellOffers.b(), false))
                                    .withStyle(ChatFormatting.GRAY))));
        }
    }

    private void removePriceButtons(ContainerScreen containerScreen) {
        containerScreen.children.stream()
                .filter(child -> child instanceof PriceButton)
                .toList()
                .forEach(containerScreen::removeWidget);
    }

    private final class PriceButton extends WynntilsButton {
        private static final int BUTTON_WIDTH = 100;
        private static final int BUTTON_HEIGHT = 20;

        private final int price;

        private PriceButton(int x, int y, int priceNoTax, Component name, Component hoverText) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, name);

            this.price = priceNoTax;
            this.setTooltip(Tooltip.create(hoverText));
        }

        @Override
        public void onPress() {
            priceToSend = price;
            sendPriceMessage = true;

            ContainerUtils.clickOnSlot(
                    PRICE_SET_ITEM_SLOT,
                    McUtils.containerMenu().containerId,
                    0,
                    McUtils.containerMenu().getItems());
        }
    }
}
