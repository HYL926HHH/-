package com.gomoku.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 单房间服务器：两位对局 + 观战，转发落子与聊天。
 */
public final class RoomServer {
    private final int port;
    private final Consumer<String> log;
    private volatile boolean running;
    private ServerSocket server;
    private final List<Client> clients = new CopyOnWriteArrayList<>();

    public RoomServer(int port, Consumer<String> log) {
        this.port = port;
        this.log = log;
    }

    public void start() throws IOException {
        server = new ServerSocket(port);
        running = true;
        Thread t = new Thread(this::acceptLoop, "gomoku-room");
        t.setDaemon(true);
        t.start();
        log.accept("房间已开，端口 " + port);
    }

    public void stop() {
        running = false;
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException ignored) {
        }
        for (Client c : clients) {
            c.close();
        }
        clients.clear();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket s = server.accept();
                Client c = new Client(s);
                clients.add(c);
                c.role = assignRole();
                c.send(new NetMessage("WELCOME", c.role));
                log.accept("加入：" + s.getRemoteSocketAddress() + " 角色 " + c.role);
                broadcast(new NetMessage("SYS", c.role + " 进入房间，当前人数 " + clients.size()), null);
                Thread t = new Thread(c::listen, "gomoku-client");
                t.setDaemon(true);
                t.start();
            } catch (IOException e) {
                if (running) {
                    log.accept("接受连接失败：" + e.getMessage());
                }
            }
        }
    }

    private String assignRole() {
        boolean hasBlack = clients.stream().anyMatch(c -> "BLACK".equals(c.role));
        boolean hasWhite = clients.stream().anyMatch(c -> "WHITE".equals(c.role));
        if (!hasBlack) {
            return "BLACK";
        }
        if (!hasWhite) {
            return "WHITE";
        }
        return "SPEC";
    }

    void broadcast(NetMessage msg, Client except) {
        for (Client c : clients) {
            if (c != except) {
                c.send(msg);
            }
        }
    }

    final class Client {
        final Socket socket;
        final PrintWriter out;
        final BufferedReader in;
        String role = "SPEC";
        String nick = "访客";

        Client(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        void send(NetMessage msg) {
            out.print(msg.encode());
            out.flush();
        }

        void listen() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    NetMessage m = NetMessage.parse(line);
                    if ("NICK".equals(m.type)) {
                        nick = m.body;
                    }
                    broadcast(m, this);
                }
            } catch (IOException ignored) {
            } finally {
                close();
                clients.remove(this);
                broadcast(new NetMessage("SYS", role + " 离开房间"), null);
            }
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
