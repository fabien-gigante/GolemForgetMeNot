package com.aarocket.golemforgetmenot;

public class GolemForgetMeNotConfig {
    private static int visitsUntilCooldown = 24;
    private static boolean completeStacks = true;
    private static int heightReach = 3;

    public static int getVisitsUntilCooldown() {
        return visitsUntilCooldown;
    }

    public static void setVisitsUntilCooldown(int value) {
        visitsUntilCooldown = value;
    }

    public static boolean getCompleteStacks() {
        return completeStacks;
    }

    public static void setCompleteStacks(boolean value) {
        completeStacks = value;
    }

    public static int getHeightReach() {
        return heightReach;
    }

    public static void setHeightReach(int value) {
        if (value < 2) value = 2;
        if (value > 4) {
            value = 4;
            GolemForgetMeNot.LOGGER.error("A copper golem reach height greater than 4 is likely to break! clamped to 4, create a github issue if you want more");
        }
        heightReach = value;
    }
}
