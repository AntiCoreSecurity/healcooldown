package org.enc.healcooldown.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.enc.healcooldown.client.HealCooldownClient;
import org.enc.healcooldown.config.ModConfig;

import java.util.UUID;

import static org.enc.healcooldown.util.HealCooldownState.getRemainingCooldown;

@Mixin(EntityRenderer.class)
public class HealCooldownNametagsMixin {

    @Redirect(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private int injected(TextRenderer instance, Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light) {
        ModConfig config = HealCooldownClient.CONFIG;

        TextRenderer.TextLayerType appliedLayerType = config.enableSeeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

        return instance.draw(text, x, y, color, shadow, matrix, vertexConsumers, appliedLayerType, backgroundColor, light);
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void modifyLabelRenderArgs(Args args) {
        if (!HealCooldownClient.isEnabled()) {
            return;
        }

        if (!(args.get(0) instanceof AbstractClientPlayerEntity playerEntity)) {
            return;
        }

        UUID uuid = playerEntity.getUuid();
        long remainingSeconds = getRemainingCooldown(uuid);

        int color = getCooldownColor(remainingSeconds);

        Text baseText = args.get(1);

        String baseName = baseText.getString();

        // 칭호 제거
        baseName = baseName.replaceAll("(?i)^[^a-zA-Z0-9_§]+", "");

        // 힐 쿨다운 텍스트 추가
        MutableText cooldownText = Text.literal(remainingSeconds > 0 ? " " + remainingSeconds : " 가능")
                .setStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(color))
                        .withBold(true)
                );

        Text newText = Text.literal(baseName)
                .append(cooldownText);

        args.set(1, newText);
    }


    private int getCooldownColor(long remainingSeconds) {
        ModConfig config = HealCooldownClient.CONFIG;
        int startColor = config.startColor;
        int endColor = config.endColor;

        if (remainingSeconds <= 0) {
            return endColor;
        }

        // 색상 비율 계산
        float ratio = (float) remainingSeconds / config.healCooldown;
        int redStart = (startColor >> 16) & 0xFF;
        int greenStart = (startColor >> 8) & 0xFF;
        int blueStart = startColor & 0xFF;

        int redEnd = (endColor >> 16) & 0xFF;
        int greenEnd = (endColor >> 8) & 0xFF;
        int blueEnd = endColor & 0xFF;

        int red = (int) (redStart + (redEnd - redStart) * (1.0f - ratio));
        int green = (int) (greenStart + (greenEnd - greenStart) * (1.0f - ratio));
        int blue = (int) (blueStart + (blueEnd - blueStart) * (1.0f - ratio));

        return (red << 16) | (green << 8) | blue;
    }
}
