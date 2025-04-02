package org.anticore.healcooldown.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.anticore.healcooldown.config.ModConfig;
import org.anticore.healcooldown.util.HealCooldownState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractClientPlayerEntity.class)
public class SoundPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void modifyTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

        if (player != client.player) {
            return;
        }

        UUID playerId = player.getUuid();
        float lastHealth = HealCooldownState.getLastHealth(playerId);
        float currentHealth = player.getHealth();

        if (lastHealth < 0) {
            HealCooldownState.setLastHealth(playerId, currentHealth);
            return;
        }

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (currentHealth > lastHealth && (currentHealth - lastHealth) >= 5.0f && !HealCooldownState.isCooldownActive(playerId)) {
            HealCooldownState.setCooldown(playerId, config.healCooldown);
            if (config.enableTimerSound) {
                playHealSound();
            }
        }

        HealCooldownState.setLastHealth(playerId, currentHealth);
        long remainingSeconds = HealCooldownState.getRemainingCooldown(playerId);

        if (remainingSeconds > 0 && remainingSeconds <= 3 && HealCooldownState.getLastMessageTime(playerId) != remainingSeconds) {
            client.player.sendMessage(Text.literal("§a§l[힐 쿨타임] " + remainingSeconds + "초"), true);
            HealCooldownState.setLastMessageTime(playerId, remainingSeconds);
        }

        if (remainingSeconds > 0 && remainingSeconds <= 3 && HealCooldownState.getLastPlayTime(playerId) != remainingSeconds) {
            if (config.enableTimerSound) {
                playHealReadySound();
            }
            HealCooldownState.setLastPlayTime(playerId, remainingSeconds);
        }

        if (remainingSeconds == 0 && HealCooldownState.getLastCooldownMessageTime(playerId) != -1) {
            client.player.sendMessage(Text.literal("§a§l[힐 사용 가능]"), true);
            HealCooldownState.setLastCooldownMessageTime(playerId, -1);
        }
    }

    private void playHealSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.getSoundManager() == null) return;

        SoundManager soundManager = client.getSoundManager();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F));
    }

    private void playHealReadySound() {
        playHealSound();
    }
}
