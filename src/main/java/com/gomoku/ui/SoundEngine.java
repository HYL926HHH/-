package com.gomoku.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SoundEngine {
    public void place() {
        tone(660, 70, 0.25);
    }

    public void forbidden() {
        tone(180, 180, 0.3);
    }

    public void win() {
        tone(523, 120, 0.3);
        tone(659, 140, 0.3);
        tone(784, 220, 0.35);
    }

    private void tone(int hz, int ms, double vol) {
        Thread t = new Thread(() -> {
            try {
                float sampleRate = 16000f;
                byte[] buf = new byte[(int) (sampleRate * ms / 1000.0)];
                for (int i = 0; i < buf.length; i++) {
                    double angle = 2 * Math.PI * i * hz / sampleRate;
                    buf[i] = (byte) (Math.sin(angle) * 127 * vol);
                }
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    line.write(buf, 0, buf.length);
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "gomoku-sfx");
        t.setDaemon(true);
        t.start();
    }
}
