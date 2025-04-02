package org.anticore.healcooldown.render;

import org.anticore.healcooldown.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class HealCooldownRenderer implements HudRenderCallback {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if (!config.enableSelfTimer) return;

        ClientPlayerEntity player = client.player;
        if (player == null || !config.enableSelfTimer) return;

        // 힐 쿨타임
        int cooldown = getHealCooldown(player);
        if (cooldown <= 0) return;

        float seconds = cooldown / 20.0f;
        String cooldownText = String.format("%d", (int) seconds) + "초";

        // 체력
        float health = player.getHealth();
        String healthText = String.format("%.1f", health) + " §c❤";

        int cooldownColor = getCooldownColor(seconds);

        int x = 10, y = 10;
        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();
        int cooldownTextWidth = client.textRenderer.getWidth(cooldownText);
        int healthTextWidth = client.textRenderer.getWidth(healthText);

        switch (config.displayPosition) {
            case TOP_RIGHT -> x = screenWidth - Math.max(cooldownTextWidth, healthTextWidth) - 10;
            case BOTTOM_LEFT -> y = screenHeight - 15;
            case BOTTOM_RIGHT -> {
                x = screenWidth - Math.max(cooldownTextWidth, healthTextWidth) - 10;
                y = screenHeight - 15;
            }
        }

        drawContext.drawText(client.textRenderer, cooldownText, x, y, cooldownColor, true);
        drawContext.drawText(client.textRenderer, healthText, x, y + 15, 0xFFFFFF, true); // 흰색으로 표시
    }

    private int getHealCooldown(ClientPlayerEntity player) {
        float cooldownProgress = player.getItemCooldownManager().getCooldownProgress(player.getMainHandStack().getItem(), 0);
        int cooldown = (int) (cooldownProgress * config.healCooldown);

        return cooldown;
    }

    private int getCooldownColor(float seconds) {
        float progress = seconds / config.healCooldown;

        int startColor = config.startColor;
        int endColor = config.endColor;

        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int r = (int) (startR * progress + endR * (1 - progress));
        int g = (int) (startG * progress + endG * (1 - progress));
        int b = (int) (startB * progress + endB * (1 - progress));

        return (r << 16) | (g << 8) | b;
    }
}
