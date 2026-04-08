package com.aarocket.golemforgetmenot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GolemForgetmeNotConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/golem_forget_me_not.json");

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveConfig();
            return;
        }
        boolean markRegenerate = false;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json.has("visitsUntilCooldown")) {
                GolemForgetMeNotConfig.setVisitsUntilCooldown(json.get("visitsUntilCooldown").getAsInt());
            } else markRegenerate = true;
            if (json.has("completeStacks")) {
                GolemForgetMeNotConfig.setCompleteStacks(json.get("completeStacks").getAsBoolean());
            } else markRegenerate = true;
            if (json.has("heightReach")) {
                GolemForgetMeNotConfig.setHeightReach(json.get("heightReach").getAsInt());
            } else markRegenerate = true;
            GolemForgetMeNot.LOGGER.info("Golem memory loaded!");
        } catch (IOException e) {
            GolemForgetMeNot.LOGGER.error("Failed to load golem_forget_me_not.json... using defaults", e);
        }
        // if updating from previous version, update old JSON
        if(markRegenerate) saveConfig();
    }

    private static void saveConfig() {
        try {
            // ensure config folder exists
            CONFIG_FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            json.addProperty("visitsUntilCooldown", GolemForgetMeNotConfig.getVisitsUntilCooldown());
            json.addProperty("completeStacks", GolemForgetMeNotConfig.getCompleteStacks());
            json.addProperty("heightReach", GolemForgetMeNotConfig.getHeightReach());

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(json, writer);
                GolemForgetMeNot.LOGGER.info("Golem memory increased!");
            }
        } catch (IOException e) {
            GolemForgetMeNot.LOGGER.error("Failed to save golem_forget_me_not.json", e);
        }
    }
}
