package org.enc.healcooldown.util;

import org.enc.healcooldown.config.ModConfig;
import org.enc.healcooldown.client.HealCooldownClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealCooldownState {
    private static final Map<UUID, Float> lastHealthMap = new HashMap<>();
    private static final Map<UUID, Long> lastPlayTimeMap = new HashMap<>();
    private static final Map<UUID, Long> lastMessageTimeMap = new HashMap<>();
    private static final Map<UUID, Long> lastCooldownMessageTimeMap = new HashMap<>();
    private static final Map<UUID, Long> healTimestamps = new HashMap<>();

    public static ModConfig getConfig() {
        if (HealCooldownClient.CONFIG == null) {
            System.out.println("Warning : HealCooldownClient 설정값이 비어있습니다. 기본 설정값을 변환합니다.");
            return new ModConfig();
        }
        return HealCooldownClient.CONFIG;
    }

    // 마지막 체력 호출
    public static float getLastHealth(UUID playerId) {
        return lastHealthMap.getOrDefault(playerId, -1.0f);
    }

    public static void setLastHealth(UUID playerId, float health) {
        lastHealthMap.put(playerId, health);
    }

    public static long getLastPlayTime(UUID playerId) {
        return lastPlayTimeMap.getOrDefault(playerId, 0L);
    }

    public static void setLastPlayTime(UUID playerId, long time) {
        lastPlayTimeMap.put(playerId, time);
    }

    public static long getLastMessageTime(UUID playerId) {
        return lastMessageTimeMap.getOrDefault(playerId, 0L);
    }

    public static void setLastMessageTime(UUID playerId, long time) {
        lastMessageTimeMap.put(playerId, time);
    }

    public static long getLastCooldownMessageTime(UUID playerId) {
        return lastCooldownMessageTimeMap.getOrDefault(playerId, 0L);
    }

    public static void setLastCooldownMessageTime(UUID playerId, long time) {
        lastCooldownMessageTimeMap.put(playerId, time);
    }

    // 힐 가능한 상태인지
    public static boolean isCooldownActive(UUID uuid) {
        Long healTime = healTimestamps.get(uuid);
        if (healTime == null) return false;

        long elapsedSeconds = (System.currentTimeMillis() - healTime) / 1000;
        return elapsedSeconds < getConfig().healCooldown;
    }

    public static long getRemainingCooldown(UUID uuid) {
        Long healTime = healTimestamps.get(uuid);
        if (healTime == null) return 0;

        long elapsedSeconds = (System.currentTimeMillis() - healTime) / 1000;
        return Math.max(getConfig().healCooldown - elapsedSeconds, 0);
    }

    // 쿨타임 설정
    public static void setCooldown(UUID uuid, long cooldownTime) {
        healTimestamps.put(uuid, System.currentTimeMillis());
    }
}
