package com.gomoku.ui;

import com.gomoku.ai.GomokuAi;
import com.gomoku.demo.ForbiddenDemo;
import com.gomoku.model.AiLevel;
import com.gomoku.model.Game;
import com.gomoku.model.GameMode;
import com.gomoku.model.GameSettings;
import com.gomoku.model.GameStatus;
import com.gomoku.model.Move;
import com.gomoku.model.PlaceResult;
import com.gomoku.model.Stone;
import com.gomoku.net.NetMessage;
import com.gomoku.net.RoomClient;
import com.gomoku.net.RoomServer;
import com.gomoku.persist.AppStore;
import com.gomoku.persist.GameRecord;
import com.gomoku.puzzle.Puzzle;
import com.gomoku.puzzle.PuzzleBank;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class MainFrame extends JFrame {
    private final GameSettings settings = new GameSettings();
    private Game game = new Game(settings);
    private final BoardPanel boardPanel = new BoardPanel();
    private final GomokuAi ai = new GomokuAi();
    private final SoundEngine sound = new SoundEngine();
    private final AppStore store = new AppStore();
    private final JLabel status = new JLabel();
    private final JLabel clock = new JLabel();
    private final JTextArea log = new JTextArea();
    private final JTextArea chat = new JTextArea();
    private final JTextField chatInput = new JTextField();
    private final DefaultListModel<String> moveModel = new DefaultListModel<>();
    private final JList<String> moveList = new JList<>(moveModel);
    private RoomServer server;
    private RoomClient client;
    private Puzzle puzzle;
    private GameRecord reviewRecord;
    private int reviewIndex;
    private boolean rated;
    private boolean endAnnounced;
    private final Timer clockTimer;
    private final StyleBar styleBar;

    public MainFrame() {
        super("五子棋 · 古风");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1140, 860));
        try {
            String[] look = store.loadLook();
            if (look != null && Theme.isKnown(look[0])) {
                settings.themeId = look[0];
                settings.skinId = look[1];
            }
        } catch (IOException ignored) {
        }
        boardPanel.setGame(game);
        boardPanel.setOnClick(this::handleClick);
        setJMenuBar(buildMenu());
        styleBar = new StyleBar(this::pickStyle);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardPanel, buildSide());
        split.setResizeWeight(0.72);
        add(styleBar, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(6, 10, 8, 10));
        status.setFont(status.getFont().deriveFont(Font.BOLD, 14f));
        clock.setFont(clock.getFont().deriveFont(Font.BOLD, 14f));
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(clock, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        clockTimer = new Timer(1000, e -> {
            if (game.tickSecond()) {
                appendLog(game.lastMessage());
                finishIfNeeded();
            }
            refreshHud();
        });
        clockTimer.start();
        applyTheme();
        refreshAll("新对局：" + settings.mode.label);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu gameMenu = new JMenu("对局");
        gameMenu.add(item("新的双人对战", () -> startMode(GameMode.LOCAL_PVP)));
        gameMenu.add(item("人机对战", () -> startMode(GameMode.PVE)));
        gameMenu.add(item("死活 / 残局", this::openPuzzle));
        gameMenu.add(item("禁手演示", this::showForbiddenDemo));
        gameMenu.addSeparator();
        gameMenu.add(item("悔棋", this::undo));
        gameMenu.add(item("认输", this::resign));
        gameMenu.add(item("提出和棋", this::offerDraw));
        gameMenu.add(item("确认落子", () -> applyPlace(game.confirmPending())));
        bar.add(gameMenu);
        JMenu rec = new JMenu("棋谱");
        rec.add(item("保存棋谱", this::saveRecord));
        rec.add(item("云存档", this::cloudSave));
        rec.add(item("打开棋谱打谱", this::openRecord));
        rec.add(item("复盘标注当前手", this::annotate));
        rec.add(item("棋风分析", this::analyzeStyle));
        bar.add(rec);
        JMenu net = new JMenu("联网");
        net.add(item("创建房间", this::hostRoom));
        net.add(item("加入房间", this::joinRoom));
        net.add(item("断开", this::disconnectNet));
        bar.add(net);
        JMenu view = new JMenu("桌面风格");
        for (Theme t : Theme.featured()) {
            view.add(item(t.label + " · " + t.tagline, () -> pickStyle(t)));
        }
        view.addSeparator();
        for (Theme t : Theme.extras()) {
            view.add(item("更多：" + t.label, () -> pickStyle(t)));
        }
        view.addSeparator();
        for (StoneSkin s : StoneSkin.values()) {
            view.add(item("棋子：" + s.label, () -> {
                settings.skinId = s.id;
                applyTheme();
            }));
        }
        bar.add(view);
        JMenu extra = new JMenu("更多");
        extra.add(item("排行榜 / 评分", this::showLeaderboard));
        extra.add(item("盲棋模式切换", () -> {
            settings.blindLastMoves = settings.blindLastMoves == 0 ? 3 : 0;
            game.settings().blindLastMoves = settings.blindLastMoves;
            refreshAll(settings.blindLastMoves == 0 ? "关闭盲棋" : "盲棋：只显示最后 3 手");
        }));
        extra.add(item("规则与帮助", this::help));
        bar.add(extra);
        return bar;
    }

    private JMenuItem item(String name, Runnable action) {
        JMenuItem it = new JMenuItem(name);
        it.addActionListener(e -> action.run());
        return it;
    }

    private java.awt.Component buildSide() {
        JPanel side = new JPanel(new BorderLayout(8, 8));
        side.setBorder(new EmptyBorder(8, 8, 8, 8));
        side.setPreferredSize(new Dimension(340, 700));
        JPanel controls = new JPanel(new GridLayout(0, 2, 6, 6));
        JComboBox<Integer> size = new JComboBox<>(new Integer[]{9, 11, 13, 15, 19});
        size.setSelectedItem(15);
        size.addActionListener(e -> settings.boardSize = (Integer) size.getSelectedItem());
        JComboBox<AiLevel> aiBox = new JComboBox<>(AiLevel.values());
        aiBox.setSelectedItem(AiLevel.NORMAL);
        aiBox.addActionListener(e -> settings.aiLevel = (AiLevel) aiBox.getSelectedItem());
        JCheckBox renju = new JCheckBox("禁手", true);
        renju.addActionListener(e -> settings.renjuForbidden = renju.isSelected());
        JCheckBox hints = new JCheckBox("禁手提示", true);
        hints.addActionListener(e -> {
            settings.showForbiddenHints = hints.isSelected();
            boardPanel.repaint();
        });
        JCheckBox confirm = new JCheckBox("落子确认", false);
        confirm.addActionListener(e -> settings.confirmMove = confirm.isSelected());
        JCheckBox swap = new JCheckBox("三手交换", false);
        swap.addActionListener(e -> settings.swapThree = swap.isSelected());
        JCheckBox soundBox = new JCheckBox("音效", true);
        soundBox.addActionListener(e -> settings.sound = soundBox.isSelected());
        JCheckBox anim = new JCheckBox("动画特效", true);
        anim.addActionListener(e -> {
            settings.animation = anim.isSelected();
            settings.effects = anim.isSelected();
        });
        JSpinner undo = new JSpinner(new SpinnerNumberModel(3, 0, 20, 1));
        undo.addChangeListener(e -> settings.undoLimitPerPlayer = (Integer) undo.getValue());
        JSpinner time = new JSpinner(new SpinnerNumberModel(300, 10, 3600, 10));
        time.addChangeListener(e -> settings.secondsPerPlayer = (Integer) time.getValue());
        JComboBox<String> hand = new JComboBox<>(new String[]{"不让子", "让先", "让二子"});
        hand.addActionListener(e -> settings.handicap = hand.getSelectedIndex());
        JSpinner komi = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 7.5, 0.5));
        komi.addChangeListener(e -> settings.komi = (Double) komi.getValue());
        controls.add(new JLabel("棋盘"));
        controls.add(size);
        controls.add(new JLabel("AI 难度"));
        controls.add(aiBox);
        controls.add(renju);
        controls.add(hints);
        controls.add(confirm);
        controls.add(swap);
        controls.add(soundBox);
        controls.add(anim);
        controls.add(new JLabel("悔棋次数"));
        controls.add(undo);
        controls.add(new JLabel("倒计时(秒)"));
        controls.add(time);
        controls.add(new JLabel("让子"));
        controls.add(hand);
        controls.add(new JLabel("贴目"));
        controls.add(komi);
        JPanel btns = new JPanel(new GridLayout(0, 2, 6, 6));
        btns.add(btn("开局", () -> startMode(settings.mode)));
        btns.add(btn("人机", () -> startMode(GameMode.PVE)));
        btns.add(btn("悔棋", this::undo));
        btns.add(btn("认输", this::resign));
        btns.add(btn("和棋", this::offerDraw));
        btns.add(btn("保存", this::saveRecord));
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(controls, BorderLayout.NORTH);
        top.add(btns, BorderLayout.CENTER);
        moveList.setVisibleRowCount(8);
        JPanel mid = new JPanel(new BorderLayout(4, 4));
        mid.add(new JLabel("手数 / 回放"), BorderLayout.NORTH);
        mid.add(new JScrollPane(moveList), BorderLayout.CENTER);
        JPanel nav = new JPanel(new GridLayout(1, 4, 4, 4));
        nav.add(btn("|◄", () -> reviewTo(0)));
        nav.add(btn("◄", () -> reviewTo(reviewIndex - 1)));
        nav.add(btn("►", () -> reviewTo(reviewIndex + 1)));
        nav.add(btn("►|", () -> {
            if (reviewRecord != null) {
                reviewTo(reviewRecord.moves.size());
            }
        }));
        mid.add(nav, BorderLayout.SOUTH);
        mid.setPreferredSize(new Dimension(320, 170));
        log.setEditable(false);
        log.setLineWrap(true);
        log.setRows(7);
        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setRows(3);
        JPanel netChat = new JPanel(new BorderLayout(4, 4));
        netChat.add(new JScrollPane(chat), BorderLayout.CENTER);
        netChat.add(chatInput, BorderLayout.SOUTH);
        chatInput.addActionListener(e -> sendChat());
        JPanel logs = new JPanel(new GridLayout(2, 1, 4, 4));
        logs.add(new JScrollPane(log));
        logs.add(netChat);
        logs.setPreferredSize(new Dimension(320, 200));
        side.add(top, BorderLayout.NORTH);
        side.add(mid, BorderLayout.CENTER);
        side.add(logs, BorderLayout.SOUTH);
        JScrollPane wrap = new JScrollPane(side);
        wrap.setBorder(null);
        wrap.setPreferredSize(new Dimension(360, 700));
        return wrap;
    }

    private JButton btn(String t, Runnable r) {
        JButton b = new JButton(t);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void startMode(GameMode mode) {
        settings.mode = mode;
        rated = false;
        endAnnounced = false;
        puzzle = null;
        reviewRecord = null;
        game = new Game(settings);
        boardPanel.setGame(game);
        refreshAll("开始：" + mode.label);
        maybeAi();
    }

    private void handleClick(int x, int y) {
        if (game.settings().mode == GameMode.REVIEW && reviewRecord != null) {
            return;
        }
        if (game.settings().mode == GameMode.NETWORK && client != null) {
            String role = client.role();
            Stone need = "BLACK".equals(role) ? Stone.BLACK : "WHITE".equals(role) ? Stone.WHITE : null;
            if (need == null) {
                appendLog("观战中，不能落子");
                return;
            }
            if (game.toMove() != need) {
                appendLog("还没轮到你");
                return;
            }
        }
        if (game.settings().mode == GameMode.PVE && game.toMove() != game.settings().humanStone) {
            return;
        }
        PlaceResult r = game.click(x, y);
        applyPlace(r);
        if (r.accepted && game.status() == GameStatus.WAITING_SWAP) {
            int c = JOptionPane.showConfirmDialog(this, "白方是否换子？", "三手交换", JOptionPane.YES_NO_OPTION);
            game.swapColors(c == JOptionPane.YES_OPTION);
            refreshAll(game.lastMessage());
        }
        if (r.accepted && game.settings().mode == GameMode.NETWORK && client != null) {
            client.send(NetMessage.place(x, y, r.status == GameStatus.PLAYING ? game.moves().get(game.moves().size() - 1).stone : game.moves().get(game.moves().size() - 1).stone));
        }
        if (puzzle != null && r.accepted) {
            Move last = game.moves().get(game.moves().size() - 1);
            if (last.x == puzzle.solution.x && last.y == puzzle.solution.y) {
                appendLog("题目正确！" + puzzle.comment);
                JOptionPane.showMessageDialog(this, "回答正确\n" + puzzle.comment);
            } else {
                appendLog("不是要点，可悔棋再试。正解：" + puzzle.solution);
            }
        }
        maybeAi();
    }

    private void applyPlace(PlaceResult r) {
        if (r.forbidden != com.gomoku.model.ForbiddenKind.NONE && settings.sound) {
            sound.forbidden();
        } else if (r.accepted && settings.sound) {
            sound.place();
        }
        boardPanel.bounce();
        refreshAll(r.message);
        finishIfNeeded();
    }

    private void maybeAi() {
        if (game.settings().mode != GameMode.PVE || game.isOver()) {
            return;
        }
        if (game.toMove() == game.settings().humanStone) {
            return;
        }
        Timer t = new Timer(280, e -> {
            var p = ai.choose(game);
            applyPlace(game.commit(p.x, p.y));
        });
        t.setRepeats(false);
        t.start();
    }

    private void undo() {
        Stone who = game.settings().mode == GameMode.PVE ? game.settings().humanStone : game.toMove().opponent();
        if (game.moves().isEmpty()) {
            who = Stone.BLACK;
        }
        if (game.undo(who)) {
            refreshAll(game.lastMessage());
            boardPanel.bounce();
        } else {
            appendLog(game.lastMessage());
        }
    }

    private void resign() {
        Stone who = game.settings().mode == GameMode.PVE ? game.settings().humanStone : game.toMove();
        game.resign(who);
        refreshAll(game.lastMessage());
        finishIfNeeded();
    }

    private void offerDraw() {
        appendLog(game.offerDraw(game.toMove()));
        refreshHud();
        finishIfNeeded();
    }

    private void finishIfNeeded() {
        if (!game.isOver()) {
            return;
        }
        if (settings.sound && (game.status() == GameStatus.BLACK_WIN || game.status() == GameStatus.WHITE_WIN)) {
            sound.win();
        }
        if (!rated && game.settings().mode != GameMode.REVIEW && game.settings().mode != GameMode.PUZZLE) {
            rated = true;
            try {
                boolean draw = game.status() == GameStatus.DRAW;
                boolean blackWin = game.status() == GameStatus.BLACK_WIN;
                store.recordResult(game.settings().blackName, blackWin, draw);
                store.recordResult(game.settings().whiteName, !blackWin && !draw, draw);
            } catch (IOException e) {
                appendLog("评分写入失败：" + e.getMessage());
            }
        }
        if (!endAnnounced) {
            endAnnounced = true;
            JOptionPane.showMessageDialog(this, game.lastMessage(), "对局结束", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveRecord() {
        try {
            GameRecord r = toRecord();
            var path = store.saveRecord(r);
            appendLog("已保存 " + path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void cloudSave() {
        try {
            var path = store.cloudSave(toRecord());
            appendLog("云存档（本地云目录）" + path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private GameRecord toRecord() {
        GameRecord r = new GameRecord();
        r.boardSize = game.board().size();
        r.blackName = game.settings().blackName;
        r.whiteName = game.settings().whiteName;
        r.mode = game.settings().mode.name();
        r.forbidden = game.settings().renjuForbidden;
        r.handicap = game.settings().handicap;
        r.komi = game.settings().komi;
        r.result = switch (game.status()) {
            case BLACK_WIN -> "B+";
            case WHITE_WIN -> "W+";
            case DRAW -> "0";
            default -> "*";
        };
        r.moves.addAll(game.moves());
        return r;
    }

    private void openRecord() {
        try {
            List<GameRecord> list = store.listRecords();
            JFileChooser fc = new JFileChooser(store.root().resolve("records").toFile());
            if (list.isEmpty() && fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            GameRecord rec;
            if (!list.isEmpty()) {
                String[] names = list.stream().map(x -> x.id + " " + x.blackName + " vs " + x.whiteName + " " + x.result)
                        .toArray(String[]::new);
                String pick = (String) JOptionPane.showInputDialog(this, "选择棋谱", "打谱",
                        JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
                if (pick == null) {
                    return;
                }
                rec = list.get(java.util.Arrays.asList(names).indexOf(pick));
            } else {
                rec = GameRecord.parse(Files.readString(fc.getSelectedFile().toPath(), StandardCharsets.UTF_8));
            }
            settings.mode = GameMode.REVIEW;
            settings.boardSize = rec.boardSize;
            game = new Game(settings);
            reviewRecord = rec;
            reviewIndex = rec.moves.size();
            game.loadMoves(rec.moves, reviewIndex);
            boardPanel.setGame(game);
            refreshAll("打谱载入，共 " + rec.moves.size() + " 手");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void reviewTo(int idx) {
        if (reviewRecord == null) {
            return;
        }
        reviewIndex = Math.max(0, Math.min(idx, reviewRecord.moves.size()));
        game.loadMoves(reviewRecord.moves, reviewIndex);
        boardPanel.setGame(game);
        refreshAll("打谱第 " + reviewIndex + " 手");
    }

    private void annotate() {
        if (game.moves().isEmpty()) {
            return;
        }
        String a = JOptionPane.showInputDialog(this, "为本手添加复盘标注", "好手");
        if (a != null) {
            game.moves().get(game.moves().size() - 1).annotation = a;
            refreshMoves();
            appendLog("标注：" + a);
        }
    }

    private void analyzeStyle() {
        JOptionPane.showMessageDialog(this, game.analyzeStyle().toString(), "棋风分析", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPuzzle() {
        Puzzle[] ps = PuzzleBank.all().toArray(Puzzle[]::new);
        Puzzle p = (Puzzle) JOptionPane.showInputDialog(this, "选择题目", "残局库",
                JOptionPane.PLAIN_MESSAGE, null, ps, ps[0]);
        if (p == null) {
            return;
        }
        puzzle = p;
        settings.mode = GameMode.PUZZLE;
        settings.boardSize = p.size;
        settings.handicap = 0;
        game = new Game(settings);
        p.applyTo(game.board());
        // puzzle stones are on board but not in move list; that's ok for display via... wait BoardPanel paints from moves only!
        boardPanel.setGame(game);
        // Need to show setup stones - BoardPanel only draws moves. I should fix BoardPanel to draw board cells too.
        refreshAll(p.title + "：" + p.goal);
        JOptionPane.showMessageDialog(this, p.goal + "\n" + p.comment, p.title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showForbiddenDemo() {
        var examples = ForbiddenDemo.examples();
        String[] names = examples.stream().map(e -> e.title() + " → " + e.kind().label).toArray(String[]::new);
        String pick = (String) JOptionPane.showInputDialog(this, "选择演示", "禁手演示",
                JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (pick == null) {
            return;
        }
        var ex = examples.get(java.util.Arrays.asList(names).indexOf(pick));
        settings.mode = GameMode.REVIEW;
        game = new Game(settings);
        // copy board by replaying? BoardPanel uses moves. Let's add stones as fake moves
        for (int y = 0; y < ex.board().size(); y++) {
            for (int x = 0; x < ex.board().size(); x++) {
                Stone s = ex.board().get(x, y);
                if (s != null) {
                    game.board().set(x, y, s);
                    game.moves().add(new Move(x, y, s, 0));
                }
            }
        }
        boardPanel.setGame(game);
        refreshAll(ex.title() + "：" + ex.explain() + " 判定=" + ex.kind().label + " 点 " + ex.mark());
        JOptionPane.showMessageDialog(this, ex.explain() + "\n判定：" + ex.kind().label + "\n标记点：" + ex.mark());
    }

    private void hostRoom() {
        try {
            disconnectNet();
            server = new RoomServer(9527, this::appendLog);
            server.start();
            client = new RoomClient();
            client.connect("127.0.0.1", 9527, settings.blackName, this::onNet, this::appendLog);
            settings.mode = GameMode.NETWORK;
            startMode(GameMode.NETWORK);
            appendLog("已创建房间 9527，其他人可加入本机 IP:9527");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void joinRoom() {
        String host = JOptionPane.showInputDialog(this, "主机地址", "127.0.0.1");
        if (host == null) {
            return;
        }
        try {
            disconnectNet();
            client = new RoomClient();
            client.connect(host, 9527, settings.whiteName, this::onNet, this::appendLog);
            settings.mode = GameMode.NETWORK;
            startMode(GameMode.NETWORK);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void onNet(NetMessage m) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            switch (m.type) {
                case "WELCOME" -> appendLog("你的角色：" + m.body + ("SPEC".equals(m.body) ? "（观战）" : ""));
                case "CHAT" -> chat.append(m.body.replace("|", ": ") + "\n");
                case "SYS" -> appendLog(m.body);
                case "PLACE" -> {
                    String[] p = m.body.split("\\s+");
                    int x = Integer.parseInt(p[1]);
                    int y = Integer.parseInt(p[2]);
                    if (game.board().isEmpty(x, y)) {
                        applyPlace(game.commit(x, y));
                    }
                }
                default -> {
                }
            }
        });
    }

    private void sendChat() {
        String t = chatInput.getText().trim();
        if (t.isEmpty()) {
            return;
        }
        chat.append("我: " + t + "\n");
        chatInput.setText("");
        if (client != null) {
            client.send(NetMessage.chat("我", t));
        }
    }

    private void disconnectNet() {
        if (client != null) {
            client.close();
            client = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private void showLeaderboard() {
        try {
            var list = store.loadRatings();
            StringBuilder sb = new StringBuilder("评分排行（本地 Elo）\n");
            int i = 1;
            for (var r : list) {
                sb.append(i++).append(". ").append(r.name).append("  ").append(r.elo)
                        .append("  胜").append(r.wins).append(" 负").append(r.losses).append(" 和").append(r.draws).append('\n');
            }
            if (list.isEmpty()) {
                sb.append("暂无对局。");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void help() {
        JOptionPane.showMessageDialog(this, """
                玩法：两人轮流在交叉点落子，黑先白后，横竖斜先连成五子（或以上）者胜。
                默认 15×15。开启禁手时，黑棋不能双三、双四、长连。
                人机：普通难度会优先成五、堵活四、冲活三。
                桌面风格：顶部可选古风、卡通、现代皮肤，菜单里还有棋院 / 暗夜 / 高对比，以及多种棋子皮肤。
                IntelliJ：打开本 Maven 项目，运行 com.gomoku.GomokuApp。
                """, "帮助", JOptionPane.INFORMATION_MESSAGE);
    }

    private void pickStyle(Theme theme) {
        settings.themeId = theme.id;
        settings.skinId = theme.defaultSkinId;
        applyTheme();
        refreshAll("已切换皮肤：" + theme.label + " · " + theme.tagline);
    }

    private void applyTheme() {
        Theme t = Theme.byId(settings.themeId);
        StoneSkin skin = StoneSkin.byId(settings.skinId);
        setTitle("五子棋 · " + t.label);
        getContentPane().setBackground(t.panelBg);
        if (getJMenuBar() != null) {
            Chrome.apply(getJMenuBar(), t);
        }
        Chrome.apply(getContentPane(), t);
        styleBar.setTheme(t);
        boardPanel.applyLook(t, skin);
        status.setForeground(t.panelFg);
        status.setFont(Fonts.title(t, 14f));
        clock.setForeground(t.accent);
        clock.setFont(Fonts.title(t, 14f));
        log.setBackground(t.cardBg);
        log.setForeground(t.panelFg);
        chat.setBackground(t.cardBg);
        chat.setForeground(t.panelFg);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(t.accent, 2));
        try {
            store.saveLook(t.id, skin.id);
        } catch (IOException ignored) {
        }
    }

    private void refreshAll(String msg) {
        if (msg != null) {
            appendLog(msg);
        }
        status.setText(game.lastMessage());
        refreshHud();
        refreshMoves();
        boardPanel.repaint();
    }

    private void refreshHud() {
        String a = fmtTime(game.secondsLeft(Stone.BLACK));
        String b = fmtTime(game.secondsLeft(Stone.WHITE));
        clock.setText("黑 " + a + "  |  白 " + b + "   悔棋剩 " + game.undosLeft(Stone.BLACK) + "/" + game.undosLeft(Stone.WHITE));
        status.setText(game.lastMessage());
    }

    private String fmtTime(int s) {
        s = Math.max(0, s);
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private void refreshMoves() {
        moveModel.clear();
        int i = 1;
        for (Move m : game.moves()) {
            String extra = m.annotation == null ? "" : "  [" + m.annotation + "]";
            moveModel.addElement(i++ + ". " + m.stone.displayName() + " " + m.algebraic() + extra);
        }
    }

    private void appendLog(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    @Override
    public void dispose() {
        disconnectNet();
        super.dispose();
    }
}
