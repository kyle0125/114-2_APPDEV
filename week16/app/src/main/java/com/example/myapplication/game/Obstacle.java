package com.example.myapplication.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.Random;

public class Obstacle {
    public static final int TYPE_ROCK = 0;
    public static final int TYPE_JELLYFISH = 1;
    public static final int TYPE_SHARK = 2;
    public static final int TYPE_NET = 3;

    private static final Random random = new Random();
    private final Paint rockPaint, jellyPaint, jellyBodyPaint, sharkPaint, netPaint;
    private final Paint coralPaint, eyePaint;

    public int type;
    public int lane;
    public float x, y;
    public float width_, height_;
    public boolean active;
    public float scale;

    private float baseY;
    private float time;

    public Obstacle(int type, int lane, float laneX, float baseY) {
        this.type = type;
        this.lane = lane;
        this.x = laneX;
        this.baseY = baseY;
        this.y = -200;
        this.active = true;
        this.scale = 0.3f;
        this.time = 0;

        rockPaint = new Paint();
        rockPaint.setColor(Color.rgb(100, 95, 85));

        coralPaint = new Paint();
        coralPaint.setColor(Color.rgb(180, 100, 80));

        jellyPaint = new Paint();
        jellyPaint.setColor(Color.argb(120, 180, 50, 200));

        jellyBodyPaint = new Paint();
        jellyBodyPaint.setColor(Color.argb(200, 200, 80, 220));

        sharkPaint = new Paint();
        sharkPaint.setColor(Color.rgb(80, 85, 95));

        netPaint = new Paint();
        netPaint.setStyle(Paint.Style.STROKE);
        netPaint.setStrokeWidth(3);
        netPaint.setColor(Color.argb(180, 180, 180, 180));

        eyePaint = new Paint();
        eyePaint.setColor(Color.WHITE);

        switch (type) {
            case TYPE_ROCK:
                width_ = 80;
                height_ = 70;
                break;
            case TYPE_JELLYFISH:
                width_ = 70;
                height_ = 100;
                break;
            case TYPE_SHARK:
                width_ = 120;
                height_ = 50;
                break;
            case TYPE_NET:
                width_ = 120;
                height_ = 60;
                break;
        }
    }

    public static Obstacle createRandom(float laneX, float baseY) {
        int type = random.nextInt(4);
        return new Obstacle(type, 0, laneX, baseY);
    }

    public void update(float deltaTime, float speed) {
        time += deltaTime;
        scale = Math.min(1.0f, scale + deltaTime * 0.8f);
        y += speed * deltaTime * scale;
    }

    public boolean isOffScreen(float height) {
        return y > height + 100;
    }

    public RectF getCollisionRect() {
        float w = width_ * scale * 0.7f;
        float h = height_ * scale * 0.7f;
        return new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
    }

    public boolean canJumpOver() {
        return type == TYPE_SHARK || type == TYPE_NET;
    }

    public boolean canDuckUnder() {
        return type == TYPE_JELLYFISH || type == TYPE_SHARK || type == TYPE_NET;
    }

    public void draw(Canvas canvas) {
        if (!active) return;

        canvas.save();
        canvas.translate(x, y);
        canvas.scale(scale, scale);

        switch (type) {
            case TYPE_ROCK:
                drawRock(canvas);
                break;
            case TYPE_JELLYFISH:
                drawJellyfish(canvas);
                break;
            case TYPE_SHARK:
                drawShark(canvas);
                break;
            case TYPE_NET:
                drawNet(canvas);
                break;
        }

        canvas.restore();
    }

    private void drawRock(Canvas canvas) {
        Path path = new Path();
        path.moveTo(-width_ / 2, height_ / 2);
        path.lineTo(-width_ / 2 + 10, -height_ / 2 + 5);
        path.lineTo(-width_ / 4, -height_ / 2 - 5);
        path.lineTo(0, -height_ / 2 + 8);
        path.lineTo(width_ / 4, -height_ / 2);
        path.lineTo(width_ / 2 - 10, -height_ / 2 + 10);
        path.lineTo(width_ / 2, height_ / 2);
        path.close();

        rockPaint.setColor(Color.rgb(100 + random.nextInt(20), 90 + random.nextInt(15), 80));
        canvas.drawPath(path, rockPaint);

        // Coral on rock
        coralPaint.setColor(Color.rgb(200, 120, 80));
        canvas.drawCircle(-width_ / 4, -height_ / 4, 12, coralPaint);
        canvas.drawCircle(width_ / 4, -height_ / 4 - 5, 10, coralPaint);
        coralPaint.setColor(Color.rgb(220, 160, 100));
        canvas.drawCircle(0, -height_ / 4 - 8, 8, coralPaint);

        // Highlight
        Paint hl = new Paint();
        hl.setColor(Color.argb(30, 255, 255, 255));
        canvas.drawRect(-width_ / 2 + 5, -height_ / 2 + 5, 0, 0, hl);
    }

    private void drawJellyfish(Canvas canvas) {
        // Dome (bell)
        float domeR = width_ * 0.45f;
        jellyBodyPaint.setColor(Color.argb(200, 180 + random.nextInt(40), 60 + random.nextInt(40), 200));
        canvas.drawOval(new RectF(-domeR, -height_ * 0.35f, domeR, height_ * 0.1f), jellyBodyPaint);

        // Inner glow
        Paint innerGlow = new Paint();
        innerGlow.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawOval(new RectF(-domeR * 0.5f, -height_ * 0.25f, domeR * 0.5f, 0), innerGlow);

        // Tentacles
        int tentacleCount = 6 + random.nextInt(3);
        jellyPaint.setColor(Color.argb(150, 200, 100, 220));
        for (int i = 0; i < tentacleCount; i++) {
            float tx = -domeR * 0.7f + (domeR * 1.4f * i / (tentacleCount - 1));
            float ty = height_ * 0.05f;
            float tentacleLen = height_ * 0.5f + random.nextFloat() * height_ * 0.2f;
            Path tentacle = new Path();
            tentacle.moveTo(tx, ty);
            float waveOffset = time * 3 + i * 0.8f;
            for (int j = 1; j <= 8; j++) {
                float t = j / 8.0f;
                float wx = tx + (float) Math.sin(t * 4 + waveOffset) * 10;
                float wy = ty + t * tentacleLen;
                tentacle.lineTo(wx, wy);
            }
            canvas.drawPath(tentacle, jellyPaint);
        }
    }

    private void drawShark(Canvas canvas) {
        Path body = new Path();
        body.moveTo(-width_ / 2, 0);
        body.lineTo(-width_ / 2 + 20, -height_ / 2);
        body.lineTo(width_ / 2 - 30, -height_ / 2 + 5);
        body.lineTo(width_ / 2, -height_ / 4);
        body.lineTo(width_ / 2 + 15, 0);
        body.lineTo(width_ / 2, height_ / 4);
        body.lineTo(width_ / 2 - 30, height_ / 2 - 5);
        body.lineTo(-width_ / 2 + 20, height_ / 2);
        body.close();
        canvas.drawPath(body, sharkPaint);

        // Dorsal fin
        Path fin = new Path();
        fin.moveTo(-10, -height_ / 2);
        fin.lineTo(10, -height_ * 0.9f);
        fin.lineTo(30, -height_ / 2);
        fin.close();
        canvas.drawPath(fin, sharkPaint);

        // Tail
        Path tail = new Path();
        tail.moveTo(-width_ / 2, 0);
        tail.lineTo(-width_ / 2 - 20, -height_ / 3);
        tail.lineTo(-width_ / 2 - 15, 0);
        tail.lineTo(-width_ / 2 - 20, height_ / 3);
        tail.close();
        canvas.drawPath(tail, sharkPaint);

        // Eye
        canvas.drawCircle(width_ / 2 - 30, -height_ / 6, 8, eyePaint);
        Paint pupil = new Paint();
        pupil.setColor(Color.BLACK);
        canvas.drawCircle(width_ / 2 - 28, -height_ / 6, 4, pupil);

        // Teeth
        Paint teeth = new Paint();
        teeth.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            float tx = width_ / 2 - 50 + i * 12;
            Path tooth = new Path();
            tooth.moveTo(tx, 2);
            tooth.lineTo(tx + 3, 12);
            tooth.lineTo(tx + 6, 2);
            canvas.drawPath(tooth, teeth);
        }

        // Mouth line
        Paint mouth = new Paint();
        mouth.setColor(Color.argb(80, 0, 0, 0));
        mouth.setStrokeWidth(2);
        canvas.drawLine(width_ / 2 - 60, 3, width_ / 2 - 15, 3, mouth);

        // Belly
        Paint belly = new Paint();
        belly.setColor(Color.argb(50, 200, 200, 200));
        canvas.drawOval(new RectF(-width_ / 4, -height_ / 6, width_ / 4, height_ / 6), belly);
    }

    private void drawNet(Canvas canvas) {
        float halfW = width_ / 2;
        float halfH = height_ / 2;

        netPaint.setColor(Color.argb(160, 160, 160, 160));
        netPaint.setStyle(Paint.Style.STROKE);
        netPaint.setStrokeWidth(2);

        // Border
        canvas.drawRoundRect(-halfW, -halfH, halfW, halfH, 10, 10, netPaint);

        // Rope border
        Paint rope = new Paint();
        rope.setStyle(Paint.Style.STROKE);
        rope.setStrokeWidth(4);
        rope.setColor(Color.argb(180, 139, 90, 43));
        canvas.drawRoundRect(-halfW, -halfH, halfW, halfH, 10, 10, rope);

        // Net grid
        float cellSize = 20 + (float) Math.sin(time * 2) * 2;
        for (float x = -halfW; x <= halfW; x += cellSize) {
            canvas.drawLine(x, -halfH, x, halfH, netPaint);
        }
        for (float y = -halfH; y <= halfH; y += cellSize) {
            canvas.drawLine(-halfW, y, halfW, y, netPaint);
        }

        // Buoy
        Paint buoy = new Paint();
        buoy.setColor(Color.rgb(255, 100, 50));
        canvas.drawCircle(halfW + 8, -halfH - 8, 8, buoy);
        buoy.setColor(Color.rgb(255, 200, 50));
        canvas.drawCircle(halfW + 8, -halfH - 8, 5, buoy);
    }
}
