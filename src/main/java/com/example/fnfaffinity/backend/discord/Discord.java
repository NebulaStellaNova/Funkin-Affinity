package com.example.fnfaffinity.backend.discord;

import net.arikia.dev.drpc.*;

public class Discord {
    public static void initialize() {
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
            System.out.println("Welcome " + user.username + "#" + user.discriminator + "!");
        }).build();
        DiscordRPC.discordInitialize("1366635302331875398", handlers, true);
        DiscordRichPresence rich = new DiscordRichPresence.Builder("This is the current state.").setDetails("These are some details.").build();
        DiscordRPC.discordUpdatePresence(rich);
    }
    public static void setDescription(String line1) {
        DiscordRichPresence rich = new DiscordRichPresence.Builder(line1).build();
        DiscordRPC.discordUpdatePresence(rich);
    }
    public static void setDescription(String line1, String line2) {
        DiscordRichPresence rich = new DiscordRichPresence.Builder(line2).setDetails(line1).build();
        DiscordRPC.discordUpdatePresence(rich);
    }
}
