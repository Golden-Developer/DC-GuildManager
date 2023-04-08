package de.goldendeveloper.guildmanager.discord.events;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.goldendeveloper.guildmanager.MysqlConnection;
import de.goldendeveloper.guildmanager.Main;
import de.goldendeveloper.mysql.entities.SearchResult;
import de.goldendeveloper.mysql.entities.Table;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;

public class Events extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent e) {
        try {
            if (Main.getDeployment()) {
                WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
                embed.setAuthor(new WebhookEmbed.EmbedAuthor(Main.getDiscord().getBot().getSelfUser().getName(), Main.getDiscord().getBot().getSelfUser().getAvatarUrl(), "https://Golden-Developer.de"));
                embed.addField(new WebhookEmbed.EmbedField(false, "[Status]", "Offline"));
                embed.addField(new WebhookEmbed.EmbedField(false, "Gestoppt als", Main.getDiscord().getBot().getSelfUser().getName()));
                embed.addField(new WebhookEmbed.EmbedField(false, "Server", Integer.toString(Main.getDiscord().getBot().getGuilds().size())));
                embed.addField(new WebhookEmbed.EmbedField(false, "Status", "\uD83D\uDD34 Offline"));
                embed.addField(new WebhookEmbed.EmbedField(false, "Version", Main.getConfig().getProjektVersion()));
                embed.setFooter(new WebhookEmbed.EmbedFooter("@Golden-Developer", Main.getDiscord().getBot().getSelfUser().getAvatarUrl()));
                embed.setTimestamp(new Date().toInstant());
                embed.setColor(0xFF0000);
                new WebhookClientBuilder(Main.getConfig().getDiscordWebhook()).build().send(embed.build()).thenRun(() -> System.exit(0));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        try {
            Main.getServerCommunicator().addServer(e.getGuild().getId());
            e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent e) {
        try {
            Main.getServerCommunicator().removeServer(e.getGuild().getId());
            e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
        try {
            if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
                if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.settingsTName)) {
                    Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingsTName);
                    if (table.existsColumn(MysqlConnection.colmGuild)) {
                        if (table.getColumn(MysqlConnection.colmGuild).getAll().getAsString().contains(e.getGuild().getId())) {
                            HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.colmGuild), e.getGuild().getId()).getData();
                            if (!row.get(MysqlConnection.colmWChannel).getAsString().isEmpty() || !row.get(MysqlConnection.colmWChannel).getAsString().isBlank()) {
                                TextChannel channel = e.getGuild().getTextChannelById(row.get(MysqlConnection.colmWChannel).getAsString());
                                if (channel != null) {
                                    User user = e.getMember().getUser();
                                    String ServerName = e.getGuild().getName();
                                    EmbedBuilder emb = new EmbedBuilder();
                                    emb.setColor(3447003);
                                    emb.setTitle("**" + ServerName + "**");
                                    emb.setDescription("Willkommen **" + user.getName() + "**,\n" + " auf dem **" + ServerName + "** Discord Server!");
                                    emb.setTimestamp(new Date().toInstant());
                                    emb.setThumbnail(user.getAvatarUrl());
                                    emb.setFooter("@Golden-Developer");
                                    channel.sendMessageEmbeds(emb.build()).queue();
                                } else {
                                    Member Owner = e.getGuild().getOwner();
                                    if (Owner != null) {
                                        sendErrorMessage(Owner, "Der angegebene Willkommens Channel konnte auf **" + e.getGuild().getName() + "** nicht gefunden werden!");
                                    }
                                }
                            }
                            if (!row.get(MysqlConnection.colmJRole).getAsString().isEmpty() || !row.get(MysqlConnection.colmJRole).getAsString().isBlank()) {
                                Role role = e.getGuild().getRoleById(row.get(MysqlConnection.colmJRole).getAsString());
                                Member bot = e.getGuild().getMember(e.getJDA().getSelfUser());
                                Member Owner = e.getGuild().getOwner();
                                if (role != null && bot != null ) {
                                    if (bot.canInteract(role))  {
                                        e.getGuild().addRoleToMember(e.getMember(), role).queue();
                                    } else {
                                        if (Owner != null) {
                                            sendErrorMessage(Owner, "Mit der angegebene Join Rolle konnte auf **" + e.getGuild().getName() + "** nicht Interagiert werden!");
                                        }
                                    }
                                } else {
                                    if (Owner != null) {
                                        sendErrorMessage(Owner, "Die angegebene Join Rolle konnte auf **" + e.getGuild().getName() + "** nicht gefunden oder nicht mit ihr Interagiert werden!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        try {
            if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
                if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.settingsTName)) {
                    Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.settingsTName);
                    if (table.existsColumn(MysqlConnection.colmGuild)) {
                        HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.colmGuild), event.getGuild().getId()).getData();
                        for (SelectOption option : event.getSelectedOptions()) {
                            if (option.getValue().equalsIgnoreCase(RegisterCommands.settingsSupJoinRole)) {
                                if (!row.get(MysqlConnection.colmJRole).getAsString().isEmpty()) {
                                    table.getRow(table.getColumn(MysqlConnection.colmGuild), event.getGuild().getId()).set(table.getColumn(MysqlConnection.colmJRole), "");
                                    event.getInteraction().reply("Die Einstellung für die Join Rolle wurde entfernt").queue();
                                } else {
                                    event.getInteraction().reply("Es ist keine Einstellung mit dieser Option vorhanden!").queue();
                                }
                            } else if (option.getValue().equalsIgnoreCase(RegisterCommands.settingsSupWMessage)) {
                                if (!row.get(MysqlConnection.colmWChannel).getAsString().isEmpty()) {
                                    table.getRow(table.getColumn(MysqlConnection.colmGuild), event.getGuild().getId()).set(table.getColumn(MysqlConnection.colmWChannel), "");
                                    event.getInteraction().reply("Die Einstellung für die Willkommens Nachricht wurde entfernt").queue();
                                } else {
                                    event.getInteraction().reply("Es ist keine Einstellung mit dieser Option vorhanden!").queue();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        try {
            String cmd = e.getName();
            if (cmd.equalsIgnoreCase(RegisterCommands.Ban)) {
                if (e.getFocusedOption().getName().equalsIgnoreCase(RegisterCommands.BanOptionTime)) {
                    e.getInteraction().replyChoices(
                            new Command.Choice("Permanent", 9999),
                            new Command.Choice("14 Tag", 14),
                            new Command.Choice("7 Tag", 7),
                            new Command.Choice("5 Tag", 5),
                            new Command.Choice("3 Tag", 3),
                            new Command.Choice("2 Tag", 2),
                            new Command.Choice("1 Tag", 1)
                    ).queue();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Sentry.captureException(exception);
        }
    }

    public void sendErrorMessage(Member member, String message) {
        if (member != null) {
            member.getUser().openPrivateChannel().queue(channel -> {
                EmbedBuilder em = new EmbedBuilder();
                em.setColor(Color.RED);
                em.setTitle("**ERROR**");
                em.setDescription(message);
                em.setTimestamp(new Date().toInstant());
                em.setFooter("@Golden-Developer");
                channel.sendMessageEmbeds(em.build()).queue();
            });
        }
    }
}
