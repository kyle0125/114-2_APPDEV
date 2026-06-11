package com.example.myapplication.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Background {
    private final Paint bgPaint, rayPaint, bubblePaint, sandPaint, seaweedPaint;
    private final Random random;
    private final ArrayList<Bubble> bubbles;
    private final ArrayList<LightRay> lightRays;
    private final float width, height;
    private float time;

    private static class Bubble {
        float x, y, radius, speed;
        Bubble(float x, float y, float radius, float speed) {
            this.x = x; this.y = y; this.radius = radius; this.speed = speed;
        }
    }

    private static class LightRay {
        float x, width_; int alpha;
        float sway, swaySpeed;
        LightRay(float x, float width_, int alpha, float swayOffset) {
            this.x = x; this.width_ = width_; this.alpha = alpha;
            this.sway = swayOffset; this.swaySpeed = 0.3f + new Random().nextFloat() * 0.2f;
        }
    }

    public Background(float width, float height) {
        this.width = width;
        this.height = height;
        this.random = new Random();
        this.bubbles = new ArrayList<>();
        this.lightRays = new ArrayList<>();
        this.time = 0;

        bgPaint = new Paint();
        rayPaint = new Paint();
        bubblePaint = new Paint();
        sandPaint = new Paint();
        seaweedPaint = new Paint();

        rayPaint.setStyle(Paint.Style.FILL);

        bubblePaint.setStyle(Paint.Style.STROKE);
        bubblePaint.setStrokeWidth(2);
        bubblePaint.setColor(Color.argb(120, 255, 255, 255));

        sandPaint.setColor(Color.argb(80, 194, 178, 128));

        seaweedPaint.setStyle(Paint.Style.FILL);
        seaweedPaint.setStrokeWidth(8);
        seaweedPaint.setStrokeCap(Paint.Cap.ROUND);

        for (int i = 0; i < 5; i++) {
            lightRays.add(new LightRay(
                    random.nextFloat() * width,
                    30 + random.nextFloat() * 60,
                    10 + random.nextInt(20),
                    random.nextFloat() * 100
            ));
        }

        for (int i = 0; i < 15; i++) {
            addBubble();
        }
    }

    private void addBubble() {
        bubbles.add(new Bubble(
                random.nextFloat() * width,
                height + random.nextFloat() * 200,
                3 + random.nextFloat() * 8,
                20 + random.nextFloat() * 40
        ));
    }

    public void update(float deltaTime) {
        time += deltaTime;

        Iterator<Bubble> it = bubbles.iterator();
        while (it.hasNext()) {
            Bubble b = it.next();
            b.y -= b.speed * deltaTime;
            b.x += Math.sin(time * 2 + b.radius) * 10 * deltaTime;
            if (b.y < -20) it.remove();
        }

        while (bubbles.size() < 15) {
            addBubble();
        }
    }

    public void draw(Canvas canvas) {
        drawOceanGradient(canvas);
        drawLightRays(canvas);
        drawSand(canvas);
        drawSeaweed(canvas);
        drawBubbles(canvas);
    }

    private void drawOceanGradient(Canvas canvas) {
        for (int y = 0; y < height; y++) {
            float ratio = y / height;
            int r = (int) (20 + 30 * (1 - ratio));
            int g = (int) (100 + 60 * (1 - ratio));
            int b = (int) (180 - 40 * ratio);
            if (r < 0) r = 0; if (g < 0) g = 0; if (b < 0) b = 0;
            if (r > 255) r = 255; if (g > 255) g = 255; if (b > 255) b = 255;
            bgPaint.setColor(Color.rgb(r, g, b));
            canvas.drawLine(0, y, width, y, bgPaint);
        }
    }

    private void drawLightRays(Canvas canvas) {
        for (LightRay ray : lightRays) {
            ray.sway += ray.swaySpeed * 0.02f;
            float swayX = (float) Math.sin(ray.sway) * 10;
            int alpha = 15 + (int) (Math.sin(time * 0.5f + ray.x) * 8);
            rayPaint.setColor(Color.argb(alpha, 255, 255, 255));

            Path path = new Path();
            float topX = ray.x + swayX;
            path.moveTo(topX - ray.width_ / 2, 0);
            path.lineTo(topX + ray.width_ / 2, 0);
            path.lineTo(topX + ray.width_ * 2, height);
            path.lineTo(topX - ray.width_ * 2, height);
            path.close();
            canvas.drawPath(path, rayPaint);
        }
    }

    private void drawSand(Canvas canvas) {
        Path sandPath = new Path();
        sandPath.moveTo(0, height - 80);
        for (float x = 0; x <= width; x += 20) {
            float y = height - 80 + (float) Math.sin(x * 0.03 + time) * 5;
            sandPath.lineTo(x, y);
        }
        sandPath.lineTo(width, height);
        sandPath.lineTo(0, height);
        sandPath.close();
        canvas.drawPath(sandPath, sandPaint);
    }

    private void drawSeaweed(Canvas canvas) {
        int[] seaweedX = {(int)(width * 0.08f), (int)(width * 0.15f),
                (int)(width * 0.85f), (int)(width * 0.92f)};
        int[] seaweedColors = {
                Color.argb(180, 34, 139, 34),
                Color.argb(160, 0, 100, 0),
                Color.argb(170, 46, 139, 87),
                Color.argb(150, 60, 120, 60)
        };

        for (int i = 0; i < seaweedX.length; i++) {
            seaweedPaint.setColor(seaweedColors[i]);
            float baseY = height - 80;
            Path path = new Path();
            path.moveTo(seaweedX[i], baseY);
            for (int j = 0; j < 5; j++) {
                float t = j / 4.0f;
                float sx = seaweedX[i] + (float) Math.sin(t * 3 + time * 1.5f + i) * 15;
                float sy = baseY - t * 120;
                path.lineTo(sx, sy);
            }
            path.lineTo(seaweedX[i] + 8, baseY - 120);
            for (int j = 4; j >= 0; j--) {
                float t = j / 4.0f;
                float sx = seaweedX[i] + 8 + (float) Math.sin(t * 3 + time * 1.5f + i + 0.5f) * 12;
                float sy = baseY - t * 120;
                path.lineTo(sx, sy);
            }
            path.close();
            canvas.drawPath(path, seaweedPaint);
        }
    }

    private void drawBubbles(Canvas canvas) {
        for (Bubble b : bubbles) {
            int alpha = (int) (80 + 60 * (1 - b.y / height));
            bubblePaint.setAlpha(Math.min(alpha, 180));
            bubblePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(b.x, b.y, b.radius, bubblePaint);
            bubblePaint.setStyle(Paint.Style.FILL);
            bubblePaint.setAlpha(alpha / 3);
            canvas.drawCircle(b.x - b.radius * 0.2f, b.y - b.radius * 0.2f, b.radius * 0.3f, bubblePaint);
        }
    }
}
