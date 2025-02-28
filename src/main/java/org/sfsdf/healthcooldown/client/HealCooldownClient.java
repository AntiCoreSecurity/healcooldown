package org.sfsdf.healthcooldown.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealCooldownClient implements ClientModInitializer {
    // 기본으로 모드꺼져있음
    public static boolean enabled = true;
    private static KeyBinding toggleKey;
    private static final Map<UUID, Long> healTimestamps = new HashMap<>();
    private static final Map<UUID, Float> lastHealthMap = new HashMap<>();

    public static boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "HealCooldown 토글",
            GLFW.GLFW_KEY_H,
            "HealCooldown 카테고리"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 토글입력감지
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(enabled ? "[HealCooldown - 활성화]" : "[HealCooldown - 비활성화]"), false);
                }
            }
            
            if (client.world == null) return;
            
            for (PlayerEntity player : client.world.getPlayers()) {
                if (!(player instanceof AbstractClientPlayerEntity) || player == client.player) continue;
                UUID uuid = player.getUuid();
                float currentHealth = player.getHealth();
                float previousHealth = lastHealthMap.getOrDefault(uuid, currentHealth);
                
                if (currentHealth - previousHealth >= 20.0f) {
                    healTimestamps.put(uuid, System.currentTimeMillis());
                }
                
                lastHealthMap.put(uuid, currentHealth);
            }
        });
    }

    // 힐쿨 계산
    public static long getRemainingCooldown(UUID uuid) {
        Long healTime = healTimestamps.get(uuid);
        if (healTime == null) return 0;
        long elapsedSeconds = (System.currentTimeMillis() - healTime) / 1000;
        long remaining = 60 - elapsedSeconds;
        return Math.max(remaining, 0);
    }
}