package org.anticore.healcooldown.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import org.anticore.healcooldown.config.ModConfig;
import org.anticore.healcooldown.render.HealCooldownRenderer;
import org.anticore.healcooldown.util.HealCooldownState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealCooldownClient implements ClientModInitializer {
    public static ModConfig CONFIG;
    public static boolean enabled = true;
    private static KeyBinding toggleKey;
    private static final Map<UUID, Float> lastHealthMap = new HashMap<>();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "HealCooldown 토글 키",
                GLFW.GLFW_KEY_H,
                "HealCooldown"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    Text message = Text.literal(enabled ? "§a§l[힐 쿨타임 측정 - 활성화]" : "§c§l[힐 쿨타임 측정 - 비활성화]");
                    client.player.sendMessage(message, false);
                }
            }

            // 힐 감지 로직
            for (PlayerEntity player : client.world.getPlayers()) {
                UUID uuid = player.getUuid();
                float currentHealth = player.getHealth();
                float previousHealth = lastHealthMap.getOrDefault(uuid, currentHealth);

                if (currentHealth - previousHealth >= 10.0f) {
                    long cooldownTime = HealCooldownState.getConfig().healCooldown;
                    HealCooldownState.setCooldown(uuid, cooldownTime);
                }

                lastHealthMap.put(uuid, currentHealth);
            }
        });

        if (enabled) {
            HealCooldownRenderer renderer = new HealCooldownRenderer();
            HudRenderCallback.EVENT.register(renderer);
        }
    }
}