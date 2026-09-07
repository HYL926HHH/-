# 五子棋（IntelliJ IDEA / Maven）

两人轮流在棋盘交叉点落子，黑先白后，谁先在横、竖、斜任意方向连成五个（或更多）同色子谁赢。默认 **15×15**。可开启连珠 **禁手**：黑棋不能双三、双四、长连。

## 用 IntelliJ IDEA 打开

1. `File` → `Open`，选择本仓库根目录（识别为 Maven 项目 `gomoku-idea`）。
2. 等待依赖索引完成。
3. 打开 `src/main/java/com/gomoku/GomokuApp.java`，点击行号旁绿色运行按钮，或 `Run` → `Run 'GomokuApp'`。
4. 也可在终端：`mvn -q test` 然后 `mvn -q exec:java`。

主类：`com.gomoku.GomokuApp`（Java 17+）。

## 已实现功能

- 双人对战、人机对战（堵活四 / 冲活三，难度分级，开局库，浅层搜索）
- 悔棋（可限制次数）、认输、和棋、倒计时、落子确认
- 禁手 + 禁手提示 + 禁手演示
- 棋盘 9/11/13/15/19、让先 / 让二子、三手交换、贴目（满盘贴目判白胜）
- 落子动画、音效、涟漪特效
- 棋谱保存 / 本地云存档 / 打谱回放 / 复盘标注 / 棋风分析
- 残局库与死活题、盲棋（只显示最后几手）
- 联网房间（创建/加入）、观战、聊天
- 评分 Elo 与本地排行榜
- 对比度主题、暗色/木质、棋子皮肤自选
- 自对弈训练统计（`SelfPlayTrainer`）

## 目录

```
src/main/java/com/gomoku/     主程序与界面
src/test/java/com/gomoku/     规则与 AI 单测
```
