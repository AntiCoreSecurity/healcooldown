package org.anticore.healcooldown.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.anticore.healcooldown.client.HealCooldownClient;
import org.anticore.healcooldown.config.ModConfig;

import java.util.UUID;

import static org.anticore.healcooldown.util.HealCooldownState.getRemainingCooldown;

@Mixin(PlayerEntityRenderer.class)
public abstract class HealCooldownNametagsMixin {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!HealCooldownClient.isEnabled()) {
            return;
        }

        // 웅크리고 있는지 확인
        if (player.isSneaking()) {
            return;
        }

        UUID uuid = player.getUuid();
        long remainingSeconds = getRemainingCooldown(uuid);

        String rawName = player.getDisplayName().getString();

        String filteredName = rawName.replaceAll("[^\\p{L}\\p{N}\\p{IsHangul} \\-\\_\\(\\)\\[\\]§&]", "");

        MutableText nameText = Text.literal(filteredName)
                .setStyle(player.getDisplayName().getStyle());

        // 힐 쿨타임 추가
        MutableText cooldownText;
        if (remainingSeconds <= 0) {
            cooldownText = Text.literal(" (가능)")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)).withBold(true));
        } else {
            int color = getCooldownColor(remainingSeconds);
            cooldownText = Text.literal(" (" + remainingSeconds + "초)")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(true));
        }



        MutableText displayText = nameText.append(cooldownText);

        // 체력 추가 (하트 이모지, 소숫점 없이 표시)
        String healthText = String.format("%d", (int) player.getHealth()) + " §c❤";  // 체력 소숫점 없이 표시
        MutableText healthDisplayText = Text.literal(healthText).setStyle(Style.EMPTY.withColor(0xFFFFFF));

        ci.cancel(); // 기존 이름표 렌더링 취소

        matrices.push();
        matrices.translate(0, player.getHeight() + 0.85, 0);  // 체력 텍스트를 머리 위로 올림 (조정된 y 값)
        matrices.multiply(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
        matrices.scale(-0.03F, -0.03F, 0.03F);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(displayText);
        int healthTextWidth = textRenderer.getWidth(healthDisplayText);

        // 이름표 위치 설정
        textRenderer.draw(displayText, -textWidth / 2.0F, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x000000, light);

        // 체력 이름표 위치 설정 (힐 쿨타임 텍스트 바로 아래)
        textRenderer.draw(healthDisplayText, -healthTextWidth / 2.0F, 12, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x000000, light);

        matrices.pop();
    }

    @Unique
    private int getCooldownColor(long remainingSeconds) {
        ModConfig config = HealCooldownClient.CONFIG;
        int startColor = config.startColor;
        int endColor = config.endColor;

        if (remainingSeconds <= 0) {
            return endColor;
        }

        float ratio = (float) remainingSeconds / config.healCooldown;
        int red = (int) (((startColor >> 16) & 0xFF) * (1.0f - ratio) + ((endColor >> 16) & 0xFF) * ratio);
        int green = (int) (((startColor >> 8) & 0xFF) * (1.0f - ratio) + ((endColor >> 8) & 0xFF) * ratio);
        int blue = (int) (((startColor) & 0xFF) * (1.0f - ratio) + ((endColor) & 0xFF) * ratio);

        return (red << 16) | (green << 8) | blue;
    }
}
