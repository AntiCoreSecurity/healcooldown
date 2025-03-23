package org.enc.healcooldown.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "healcooldown")
public class ModConfig implements ConfigData {

    @ConfigEntry.ColorPicker
    public int startColor = 0x00FF00; // 초록 (가능)

    @ConfigEntry.ColorPicker
    public int endColor = 0xFA002D; // 빨강 (0s)

    public boolean enableSeeThrough = true; // 벽 관통할지

    public int healCooldown = 60; // 기본 힐 재사용 쿨

    public boolean enableTimerSound = false;

    public boolean enableSelfTimer = true;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public DisplayPosition displayPosition = DisplayPosition.TOP_LEFT;

    public enum DisplayPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
