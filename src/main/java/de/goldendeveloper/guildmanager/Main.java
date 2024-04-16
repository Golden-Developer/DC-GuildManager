package de.goldendeveloper.guildmanager;

import de.goldendeveloper.dcbcore.DCBotBuilder;
import de.goldendeveloper.guildmanager.discord.commands.*;
import de.goldendeveloper.guildmanager.discord.events.CustomEvents;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;

public class Main {

    private static Mysql mysql;
    private static CustomConfig customConfig;

    public static void main(String[] args) {
        customConfig = new CustomConfig();
        DCBotBuilder dcBotBuilder = new DCBotBuilder(args, true);
        dcBotBuilder.registerCommands(new Ban(), new Birthday(), new Clear(), new JoinChannel(), new Kick(), new LeaveChannel(), new ServerOwner(), new ServerStats(), new Settings(), new TimeOut());
        dcBotBuilder.registerEvents(new CustomEvents());
        dcBotBuilder.build();
        mysql = new Mysql();
    }

    public static Mysql getMysql() {
        return mysql;
    }

    public static int getOnlineUsers(Guild guild) {
        return Long.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus() != OnlineStatus.OFFLINE && m.getOnlineStatus() != OnlineStatus.INVISIBLE).count()).intValue();
    }

    public static int getDoNotDisturbUsers(Guild guild) {
        return Long.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus() != OnlineStatus.DO_NOT_DISTURB).count()).intValue();
    }

    public static int getAfkUsers(Guild guild) {
        return Long.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus() != OnlineStatus.OFFLINE && m.getOnlineStatus() != OnlineStatus.INVISIBLE).count()).intValue();
    }

    public static CustomConfig getCustomConfig() {
        return customConfig;
    }

    public static int getOfflineUsers(Guild guild) {
        return Long.valueOf(guild.getMembers().stream().filter(m -> m.getOnlineStatus() == OnlineStatus.OFFLINE).count()).intValue();
    }
}