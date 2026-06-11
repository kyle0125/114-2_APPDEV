package com.example.myapplication.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private static final int STATE_READY = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_GAME_OVER = 2;

    private Thread gameThread;
    private volatile boolean running;
    private SurfaceHolder holder;
    private Paint textPaint, scorePaint, titlePaint, infoPaint, overlayPaint;
    private Paint hpPaint, hpBgPaint, hpBarPaint;

    private Background background;
    private Player player;
    private ChaseShark chaseShark;
    private ArrayList<Obstacle> obstacles;
    private Random random;

    private int gameState;
    private int score;
    private int highScore;
    private float gameTime;
    private float speed;
    private float spawnTimer;
    private float baseSpawnInterval;
    private float chaseSharkTimer;
    private float chaseSharkInterval;

    private float screenWidth, screenHeight;
    private float[] lanePositions;
    private int currentLane;

    private float touchStartX, touchStartY;
    private long touchStartTime;
    private static final float SWIPE_THRESHOLD = 50;
    private static final long SWIPE_TIME_THRESHOLD = 300;

    private float hp;
    private static final float MAX_HP = 3;
    private float invincibleTimer;

    private ArrayList<Starfish> starfishes;

    private static class Starfish {
        float x, y, rotation;
        int lane;
        boolean collected;

        Starfish(int lane, float laneX, float baseY) {
            this.lane = lane;
            this.x = laneX;
            this.y = -50;
            this.rotation = 0;
            this.collected = false;
        }
    }

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        random = new Random();
        obstacles = new ArrayList<>();
        starfishes = new ArrayList<>();
        gameState = STATE_READY;
        score = 0;
        highScore = 0;
        gameTime = 0;
        speed = 300;
        baseSpawnInterval = 2.0f;
        spawnTimer = 0;
        chaseSharkTimer = 10.0f;
        chaseSharkInterval = 12.0f;
        currentLane = 1;
        hp = MAX_HP;
        invincibleTimer = 0;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(50);
        scorePaint.setAntiAlias(true);
        scorePaint.setFakeBoldText(true);

        titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(70);
        titlePaint.setAntiAlias(true);
        titlePaint.setFakeBoldText(true);

        infoPaint = new Paint();
        infoPaint.setColor(Color.argb(200, 255, 255, 255));
        infoPaint.setTextSize(30);
        infoPaint.setAntiAlias(true);

        overlayPaint = new Paint();
        overlayPaint.setColor(Color.argb(150, 0, 0, 0));

        hpPaint = new Paint();
        hpPaint.setColor(Color.rgb(255, 50, 50));

        hpBgPaint = new Paint();
        hpBgPaint.setColor(Color.argb(80, 255, 255, 255));

        hpBarPaint = new Paint();
        hpBarPaint.setColor(Color.rgb(50, 200, 255));
    }

    @Override
    public void run() {
        while (running) {
            if (!holder.getSurface().isValid()) continue;

            Canvas canvas = holder.lockCanvas();
            if (canvas == null) continue;

            float deltaTime = 1.0f / 60.0f;

            update(deltaTime);
            draw(canvas);

            holder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void update(float deltaTime) {
        if (gameState != STATE_PLAYING) return;
        if (player.isDead()) {
            gameState = STATE_GAME_OVER;
            if (score > highScore) highScore = score;
            return;
        }

        gameTime += deltaTime;

        baseSpawnInterval = 2.0f - gameTime * 0.02f;
        if (baseSpawnInterval < 0.8f) baseSpawnInterval = 0.8f;

        background.update(deltaTime);
        player.update(deltaTime);

        if (invincibleTimer > 0) invincibleTimer -= deltaTime;

        spawnTimer -= deltaTime;
        if (spawnTimer <= 0) {
            spawnObstacle();
            spawnTimer = baseSpawnInterval + random.nextFloat() * 0.5f;
        }

        if (random.nextFloat() < 0.005f) {
            spawnStarfish();
        }

        chaseSharkTimer -= deltaTime;
        if (chaseSharkTimer <= 0 && !chaseShark.isActive()) {
            chaseShark.startAttack(lanePositions[currentLane], currentLane, lanePositions);
            chaseSharkInterval = 8.0f + random.nextFloat() * 6.0f - Math.min(gameTime * 0.04f, 4.0f);
            if (chaseSharkInterval < 4.0f) chaseSharkInterval = 4.0f;
            chaseSharkTimer = chaseSharkInterval;
        }

        if (chaseShark.isActive()) {
            chaseShark.update(deltaTime, speed, currentLane, lanePositions, gameTime);

            if (gameState == STATE_PLAYING && invincibleTimer <= 0) {
                RectF sharkRect = chaseShark.getCollisionRect();
                RectF pRect = player.getCollisionRect();
                if (RectF.intersects(pRect, sharkRect) && !chaseShark.hasHitPlayer()) {
                    if (!player.isJumping()) {
                        chaseShark.setHitPlayer(true);
                        hitObstacle();
                    }
                }
            }
        }

        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle obs = it.next();
            obs.update(deltaTime, speed);

            if (obs.isOffScreen(screenHeight)) {
                it.remove();
                continue;
            }

            if (gameState == STATE_PLAYING && invincibleTimer <= 0 && obs.lane == player.getCurrentLane()) {
                RectF playerRect = player.getCollisionRect();
                RectF obsRect = obs.getCollisionRect();

                if (RectF.intersects(playerRect, obsRect)) {
                    boolean canAvoid = (obs.canJumpOver() && player.isJumping())
                            || (obs.canDuckUnder() && player.isDucking());
                    if (!canAvoid) {
                        hitObstacle();
                        it.remove();
                    }
                }
            }
        }

        Iterator<Starfish> sfIt = starfishes.iterator();
        while (sfIt.hasNext()) {
            Starfish sf = sfIt.next();
            sf.y += speed * deltaTime;
            sf.rotation += deltaTime * 180;

            if (sf.y > screenHeight + 50) {
                sfIt.remove();
                continue;
            }

            if (gameState == STATE_PLAYING) {
                RectF sfRect = new RectF(sf.x - 20, sf.y - 20, sf.x + 20, sf.y + 20);
                RectF pRect = player.getCollisionRect();
                if (RectF.intersects(pRect, sfRect) && !sf.collected) {
                    sf.collected = true;
                    score += 10;
                    speed *= 1.05f;
                    if (speed > 800) speed = 800;
                    sfIt.remove();
                }
            }
        }
    }

    private void hitObstacle() {
        hp--;
        invincibleTimer = 1.0f;

        if (hp <= 0) {
            player.die();
            gameState = STATE_GAME_OVER;
            if (score > highScore) highScore = score;
        }
    }

    private void spawnObstacle() {
        int lane = random.nextInt(3);
        float laneX = lanePositions[lane];
        float baseY = screenHeight;

        Obstacle obs = Obstacle.createRandom(laneX, baseY);
        obs.lane = lane;
        obs.x = laneX;
        obstacles.add(obs);
    }

    private void spawnStarfish() {
        int lane = random.nextInt(3);
        starfishes.add(new Starfish(lane, lanePositions[lane], screenHeight));
    }

    public void draw(Canvas canvas) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        if (lanePositions == null) {
            lanePositions = new float[]{
                    screenWidth * 0.2f,
                    screenWidth * 0.5f,
                    screenWidth * 0.8f
            };
        }

        if (background == null) {
            background = new Background(screenWidth, screenHeight);
        }

        if (player == null) {
            player = new Player(screenWidth, screenHeight, lanePositions);
        }

        if (chaseShark == null) {
            chaseShark = new ChaseShark(screenHeight * 0.72f);
        }

        background.draw(canvas);

        drawLaneLines(canvas);

        for (Starfish sf : starfishes) {
            drawStarfish(canvas, sf);
        }

        if (chaseShark != null && chaseShark.isActive()) {
            chaseShark.draw(canvas, screenWidth, screenHeight);
        }

        for (Obstacle obs : obstacles) {
            obs.draw(canvas);
        }

        player.draw(canvas);

        drawHUD(canvas);

        if (gameState == STATE_READY) {
            drawReadyScreen(canvas);
        } else if (gameState == STATE_GAME_OVER) {
            drawGameOverScreen(canvas);
        }
    }

    private void drawLaneLines(Canvas canvas) {
        Paint lanePaint = new Paint();
        lanePaint.setColor(Color.argb(60, 255, 255, 255));
        lanePaint.setStrokeWidth(3);

        float vanishingY = screenHeight * 0.15f;

        for (int i = 0; i <= 3; i++) {
            float bottomX;
            if (i == 0) bottomX = 0;
            else if (i == 1) bottomX = screenWidth * 0.2f;
            else if (i == 2) bottomX = screenWidth * 0.8f;
            else bottomX = screenWidth;

            float topX = screenWidth * i / 3;
            float topY = vanishingY;

            lanePaint.setAlpha(40);
            canvas.drawLine(bottomX, screenHeight, topX, topY, lanePaint);
        }

        // Dashed lane markers (actual swimming lanes)
        lanePaint.setColor(Color.argb(80, 200, 230, 255));
        lanePaint.setStrokeWidth(2);
        float dashLen = 20, gapLen = 20;
        float lane1X = screenWidth * 0.2f;
        float lane2X = screenWidth * 0.8f;

        for (float y = 0; y < screenHeight; y += dashLen + gapLen) {
            canvas.drawLine(lane1X, y, lane1X, y + dashLen, lanePaint);
            canvas.drawLine(lane2X, y, lane2X, y + dashLen, lanePaint);
        }
    }

    private void drawStarfish(Canvas canvas, Starfish sf) {
        Paint sfPaint = new Paint();
        sfPaint.setColor(Color.rgb(255, 200, 50));

        Paint sfOutline = new Paint();
        sfOutline.setColor(Color.argb(100, 255, 150, 0));
        sfOutline.setStrokeWidth(3);
        sfOutline.setStyle(Paint.Style.STROKE);

        canvas.save();
        canvas.translate(sf.x, sf.y);
        canvas.rotate(sf.rotation);

        float r = 18;
        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * 2 * Math.PI / 5 - Math.PI / 2);
            float px = (float) Math.cos(angle) * r;
            float py = (float) Math.sin(angle) * r;
            float px2 = (float) Math.cos(angle) * (r * 0.4f);
            float py2 = (float) Math.sin(angle) * (r * 0.4f);
            canvas.drawLine(0, 0, px, py, sfOutline);
            canvas.drawCircle(px, py, 5, sfPaint);
        }

        canvas.drawCircle(0, 0, r * 0.35f, sfPaint);

        sfPaint.setColor(Color.rgb(255, 230, 100));
        canvas.drawCircle(0, 0, r * 0.2f, sfPaint);

        canvas.restore();
    }

    private void drawHUD(Canvas canvas) {
        // Score
        String scoreStr = String.valueOf(score);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(scoreStr, screenWidth / 2, 80, scorePaint);

        // HP bar
        float hpBarWidth = 150;
        float hpBarHeight = 20;
        float hpX = 20;
        float hpY = 20;

        canvas.drawRoundRect(hpX, hpY, hpX + hpBarWidth, hpY + hpBarHeight, 10, 10, hpBgPaint);

        float hpRatio = hp / MAX_HP;
        hpPaint.setColor(hpRatio > 0.5f ? Color.rgb(50, 200, 80) :
                hpRatio > 0.25f ? Color.rgb(255, 200, 50) : Color.rgb(255, 50, 50));
        canvas.drawRoundRect(hpX + 2, hpY + 2,
                hpX + (hpBarWidth - 4) * hpRatio, hpY + hpBarHeight - 2, 8, 8, hpPaint);

        // HP icons (oxygen tanks)
        for (int i = 0; i < (int) hp; i++) {
            float iconX = hpX + hpBarWidth + 12 + i * 28;
            hpBarPaint.setColor(Color.argb(180, 100, 200, 255));
            canvas.drawRoundRect(iconX, hpY + 2, iconX + 22, hpY + hpBarHeight - 2, 5, 5, hpBarPaint);
            hpBarPaint.setColor(Color.argb(120, 200, 230, 255));
            canvas.drawCircle(iconX + 11, hpY + 2, 5, hpBarPaint);
        }

        // Invincibility flash
        if (invincibleTimer > 0 && (int) (invincibleTimer * 10) % 2 == 0) {
            Paint flash = new Paint();
            flash.setColor(Color.argb(40, 255, 100, 100));
            canvas.drawRect(0, 0, screenWidth, screenHeight, flash);
        }
    }

    private void drawReadyScreen(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.rgb(100, 200, 255));
        canvas.drawText("深 海 跑 酷", screenWidth / 2, screenHeight / 2 - 120, titlePaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.argb(200, 200, 230, 255));
        textPaint.setTextSize(30);
        canvas.drawText("◀ ▶ 左右滑動  切換跑道", screenWidth / 2, screenHeight / 2 - 30, textPaint);
        canvas.drawText("▲ 向上滑動  跳躍", screenWidth / 2, screenHeight / 2 + 10, textPaint);
        canvas.drawText("▼ 向下滑動  蹲下", screenWidth / 2, screenHeight / 2 + 50, textPaint);

        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setColor(Color.argb(180, 255, 100, 100));
        canvas.drawText("⚠ 小心紅眼鯊魚！快閃避！", screenWidth / 2, screenHeight / 2 + 100, infoPaint);

        if (highScore > 0) {
            infoPaint.setTextAlign(Paint.Align.CENTER);
            infoPaint.setColor(Color.argb(150, 255, 200, 50));
            canvas.drawText("最高分: " + highScore, screenWidth / 2, screenHeight / 2 + 150, infoPaint);
        }

        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setColor(Color.argb(180, 150, 200, 255));
        canvas.drawText("點擊開始", screenWidth / 2, screenHeight / 2 + 210, infoPaint);
    }

    private void drawGameOverScreen(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.rgb(255, 80, 80));
        canvas.drawText("遊 戲 結 束", screenWidth / 2, screenHeight / 2 - 100, titlePaint);

        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setColor(Color.WHITE);
        canvas.drawText("得分: " + score, screenWidth / 2, screenHeight / 2, scorePaint);

        if (score == highScore && score > 0) {
            infoPaint.setTextAlign(Paint.Align.CENTER);
            infoPaint.setColor(Color.rgb(255, 200, 50));
            canvas.drawText("★ 新紀錄! ★", screenWidth / 2, screenHeight / 2 + 50, infoPaint);
        }

        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setColor(Color.argb(180, 150, 200, 255));
        canvas.drawText("最高分: " + highScore, screenWidth / 2, screenHeight / 2 + 100, infoPaint);
        canvas.drawText("點擊重新開始", screenWidth / 2, screenHeight / 2 + 160, infoPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                touchStartTime = System.currentTimeMillis();

                if (gameState == STATE_READY) {
                    startGame();
                    return true;
                }
                if (gameState == STATE_GAME_OVER) {
                    resetGame();
                    return true;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (gameState != STATE_PLAYING) return true;

                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;
                long dt = System.currentTimeMillis() - touchStartTime;

                if (dt > SWIPE_TIME_THRESHOLD) return true;

                float absDx = Math.abs(dx);
                float absDy = Math.abs(dy);

                if (absDx > absDy && absDx > SWIPE_THRESHOLD) {
                    if (dx > 0 && currentLane < 2) {
                        currentLane++;
                        player.setLane(currentLane);
                    } else if (dx < 0 && currentLane > 0) {
                        currentLane--;
                        player.setLane(currentLane);
                    }
                } else if (absDy > absDx && absDy > SWIPE_THRESHOLD) {
                    if (dy < 0) {
                        player.jump();
                    } else {
                        player.duck();
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void startGame() {
        gameState = STATE_PLAYING;
    }

    private void resetGame() {
        gameState = STATE_PLAYING;
        score = 0;
        gameTime = 0;
        speed = 300;
        spawnTimer = 0;
        hp = MAX_HP;
        invincibleTimer = 0;
        currentLane = 1;
        obstacles.clear();
        starfishes.clear();
        player.reset();
        if (chaseShark != null) chaseShark.reset();
        chaseSharkTimer = 10.0f;
    }

    public void pause() {
        running = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
