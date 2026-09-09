package com.gomoku.persist;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class AppStore {
    private final Path root;
    private final Path records;
    private final Path cloud;
    private final Path ratings;
    private final Path board;
    private final Path look;

    public AppStore() {
        this(Path.of(System.getProperty("user.home"), ".gomoku-idea"));
    }

    public AppStore(Path root) {
        this.root = root;
        this.records = root.resolve("records");
        this.cloud = root.resolve("cloud");
        this.ratings = root.resolve("ratings.tsv");
        this.board = root.resolve("leaderboard.tsv");
        this.look = root.resolve("look.txt");
        try {
            Files.createDirectories(records);
            Files.createDirectories(cloud);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Path saveRecord(GameRecord record) throws IOException {
        Path file = records.resolve(record.id + ".gmk");
        Files.writeString(file, record.toText(), StandardCharsets.UTF_8);
        return file;
    }

    public Path cloudSave(GameRecord record) throws IOException {
        Path file = cloud.resolve(record.id + ".gmk");
        Files.writeString(file, record.toText(), StandardCharsets.UTF_8);
        return file;
    }

    public List<GameRecord> listRecords() throws IOException {
        List<GameRecord> list = new ArrayList<>();
        try (Stream<Path> stream = Files.list(records)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".gmk"))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .forEach(p -> {
                        try {
                            list.add(GameRecord.parse(Files.readString(p, StandardCharsets.UTF_8)));
                        } catch (IOException ignored) {
                        }
                    });
        }
        return list;
    }

    public void recordResult(String name, boolean win, boolean draw) throws IOException {
        List<Rating> all = loadRatings();
        Rating r = all.stream().filter(x -> x.name.equals(name)).findFirst().orElseGet(() -> {
            Rating n = new Rating(name, 1200, 0, 0, 0);
            all.add(n);
            return n;
        });
        r.games++;
        if (draw) {
            r.draws++;
            r.elo += 2;
        } else if (win) {
            r.wins++;
            r.elo += 16;
        } else {
            r.losses++;
            r.elo -= 12;
        }
        writeRatings(all);
    }

    public List<Rating> loadRatings() throws IOException {
        List<Rating> list = new ArrayList<>();
        if (!Files.exists(ratings)) {
            return list;
        }
        for (String line : Files.readAllLines(ratings, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] p = line.split("\t");
            list.add(new Rating(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Integer.parseInt(p[4])));
        }
        list.sort(Comparator.comparingInt((Rating r) -> r.elo).reversed());
        return list;
    }

    private void writeRatings(List<Rating> all) throws IOException {
        all.sort(Comparator.comparingInt((Rating r) -> r.elo).reversed());
        StringBuilder sb = new StringBuilder();
        for (Rating r : all) {
            sb.append(r.name).append('\t').append(r.elo).append('\t')
                    .append(r.wins).append('\t').append(r.losses).append('\t').append(r.draws).append('\n');
        }
        Files.writeString(ratings, sb.toString(), StandardCharsets.UTF_8);
        Files.writeString(board, "# updated " + Instant.now() + "\n" + sb, StandardCharsets.UTF_8);
    }

    public Path root() {
        return root;
    }

    public void saveLook(String themeId, String skinId) throws IOException {
        Files.writeString(look, themeId + "\n" + skinId + "\n", StandardCharsets.UTF_8);
    }

    public String[] loadLook() throws IOException {
        if (!Files.exists(look)) {
            return null;
        }
        var lines = Files.readAllLines(look, StandardCharsets.UTF_8);
        if (lines.size() < 2) {
            return null;
        }
        String themeId = lines.get(0).trim();
        String skinId = lines.get(1).trim();
        if (themeId.isEmpty() || skinId.isEmpty()) {
            return null;
        }
        return new String[]{themeId, skinId};
    }

    public static final class Rating {
        public final String name;
        public int elo;
        public int wins;
        public int losses;
        public int draws;
        public int games;

        public Rating(String name, int elo, int wins, int losses, int draws) {
            this.name = name;
            this.elo = elo;
            this.wins = wins;
            this.losses = losses;
            this.draws = draws;
            this.games = wins + losses + draws;
        }
    }
}
