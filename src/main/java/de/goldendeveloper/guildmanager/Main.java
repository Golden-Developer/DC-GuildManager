package de.goldendeveloper.guildmanager;

import de.goldendeveloper.guildmanager.discord.Discord;
import de.goldendeveloper.guildmanager.server.ServerCommunicator;
import de.goldendeveloper.guildmanager.server.ServerListener;
import io.sentry.Sentry;

import java.io.IOException;

public class Main {

    private static Discord discord;
    private static Config config;
    private static MysqlConnection mysqlConnection;
    private static ServerCommunicator serverCommunicator;

    private static Boolean restart = false;
    private static Boolean deployment = true;

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("restart")) {
            restart = true;
        }
        String device = System.getProperty("os.name").split(" ")[0];
        if (device.equalsIgnoreCase("windows") || device.equalsIgnoreCase("Mac")) {
            deployment = false;
        }
        config = new Config();
        Sentry(config.getSentryDNS());
        try {
            serverCommunicator = new ServerCommunicator(config.getServerHostname(), config.getServerPort());
            new Thread(ServerListener::new).start();
            mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
            discord = new Discord(config.getDiscordToken());
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    public static void Sentry(String dns) {
        Sentry.init(options -> {
            options.setDsn(dns);
            options.setTracesSampleRate(1.0);
        });
    }

    public static Discord getDiscord() {
        return discord;
    }

    public static Config getConfig() {
        return config;
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static Boolean getRestart() {
        return restart;
    }

    public static Boolean getDeployment() {
        return deployment;
    }

    public static ServerCommunicator getServerCommunicator() {
        return serverCommunicator;
    }
}