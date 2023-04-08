package de.goldendeveloper.guildmanager.server;

import de.goldendeveloper.guildmanager.Main;
import io.sentry.Sentry;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class ServerListener {

    private String responseHostname = "";
    private int responsePort = 55;


    public ServerListener() {
        try  {
            System.out.println("Server wird gestartet ...");
            ServerSocket server = new ServerSocket(5555);
            while (true) {
                Socket socket = null;
                try {
                    socket = server.accept();
                    BufferedReader rein = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintStream raus = new PrintStream(socket.getOutputStream());
                    Stream<String> ts = rein.lines();
                    for (Object st : ts.toArray()) {
                        JSONObject object = new JSONObject(st.toString());
                        if (object.has("type")) {
                            String type = object.getString("type");
                            switch (type) {
                                case "restart" -> restart();
                                case "shutdown" -> shutdown();
                                case "status" -> status();
                                case "information" -> information();
                            }
                        } else {
                            System.out.println(" [Type] konnte nicht gefunden werden!");
                        }
                    }
                    raus.close();
                } catch (Exception e) {
                    Sentry.captureException(e);
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            Sentry.captureException(e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Sentry.captureException(e);
            e.printStackTrace();
        }

    }

    private  void restart() {
        System.out.println("Restart");
    }

    private  void shutdown() {
        System.out.println("Shutdown");
    }

    private  void status() {
        JSONObject msg = new JSONObject();
        msg.put("name", Main.getConfig().getProjektName());
        msg.put("type", "responseStatus");
        msg.put("status", "200 - Online");
        response(msg);
    }

    private void information() {
        JSONObject msg = new JSONObject();
        msg.put("name",  Main.getConfig().getProjektName());
        msg.put("type", "responseInformation");
        msg.put("guilds", Main.getDiscord().getBot().getGuilds());
        msg.put("commands", Main.getDiscord().getBot().retrieveCommands().complete());
        response(msg);
    }

    private void response(JSONObject responseJsonObject) {
        String serverHost = "localhost";
        int serverPort = 5554;
        Socket socket = null;
        try {
            socket = new Socket(serverHost, serverPort);
            OutputStream output = socket.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            outputWriter.write(responseJsonObject.toString());
            outputWriter.flush();
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    System.out.println("Socket geschlossen...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
