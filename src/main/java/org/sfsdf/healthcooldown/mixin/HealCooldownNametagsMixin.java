package org.sfsdf.healthcooldown.mixin;

import org.sfsdf.healthcooldown.client.HealCooldownClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.UUID;

@Mixin(EntityRenderer.class)
public class HealCooldownNametagsMixin {
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void modifyLabelRenderArgs(Args args) {
        if (!HealCooldownClient.isEnabled()) {
            return;
        }
        
        if (!(args.get(0) instanceof AbstractClientPlayerEntity)) {
            return;
        }
        
        AbstractClientPlayerEntity playerEntity = (AbstractClientPlayerEntity) args.get(0);
        UUID uuid = playerEntity.getUuid();
        
        long remainingSeconds = HealCooldownClient.getRemainingCooldown(uuid);
        
        // 시간 색상표시함
        float ratio = (float) remainingSeconds / 60.0f;
        int red = (int)(255 * ratio);
        int green = (int)(255 * (1.0f - ratio));
        int color = (red << 16) | (green << 8);
        

        // 렌더링하노 이기야
        Text baseText = (Text) args.get(1);
        MutableText cooldownText;
        if (remainingSeconds <= 0) {
            cooldownText = Text.literal(" (힐 가능)")
                .setStyle(Style.EMPTY.withColor(color));
        } else {
            cooldownText = Text.literal(" (" + remainingSeconds + "초)")
                .setStyle(Style.EMPTY.withColor(color));
        }
        
        Text newText = Text.literal("")
                .append(baseText)
                .append(cooldownText);
        
        args.set(1, newText);
    }
}
