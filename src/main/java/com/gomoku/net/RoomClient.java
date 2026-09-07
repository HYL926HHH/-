package com.gomoku.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class RoomClient {
    private Socket socket;
    private PrintWriter out;
    private volatile boolean running;
    private String role = "SPEC";

    public void connect(String host, int port, String nick, Consumer<NetMessage> onMessage, Consumer<String> onError)
            throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        running = true;
        send(new NetMessage("NICK", nick));
        Thread t = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    NetMessage m = NetMessage.parse(line);
                    if ("WELCOME".equals(m.type)) {
                        role = m.body;
                    }
                    onMessage.accept(m);
                }
            } catch (IOException e) {
                if (running) {
                    onError.accept(e.getMessage());
                }
            }
        }, "gomoku-net-in");
        t.setDaemon(true);
        t.start();
    }

    public void send(NetMessage msg) {
        if (out != null) {
            out.print(msg.encode());
            out.flush();
        }
    }

    public void close() {
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public String role() {
        return role;
    }
}
