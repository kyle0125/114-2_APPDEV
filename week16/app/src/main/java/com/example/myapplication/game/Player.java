package com.example.myapplication.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Player {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_JUMPING = 1;
    public static final int STATE_DUCKING = 2;
    public static final int STATE_DEAD = 3;

    private static final float JUMP_HEIGHT = 180f;
    private static final float JUMP_DURATION = 0.75f;
    private static final float DUCK_HEIGHT = 80f;
    private static final float DUCK_DURATION = 0.75f;
    private static final float LANE_SWITCH_SPEED = 0.18f;

    private final Paint bodyPaint, headPaint, finPaint, maskPaint, glassPaint, outlinePaint;
    private final Paint airTankPaint, stripePaint;

    private int currentLane;
    private float targetX, currentX, currentY;
    private float baseY;
    private int state;
    private float stateTime;
    private final float laneWidth;
    private final float[] lanePositions;
    private final float width, height;
    private float playerWidth = 50, playerHeight = 100;
    private RectF collisionRect;

    private float swimCycle;

    public Player(float width, float height, float[] lanePositions) {
        this.width = width;
        this.height = height;
        this.lanePositions = lanePositions;
        this.laneWidth = width / 3;
        this.currentLane = 1;
        this.targetX = lanePositions[1];
        this.currentX = targetX;
        this.baseY = height * 0.72f;
        this.currentY = baseY;
        this.state = STATE_NORMAL;
        this.stateTime = 0;
        this.swimCycle = 0;

        bodyPaint = new Paint();
        bodyPaint.setColor(Color.rgb(30, 60, 90));

        headPaint = new Paint();
        headPaint.setColor(Color.rgb(255, 220, 180));

        finPaint = new Paint();
        finPaint.setColor(Color.rgb(50, 50, 60));

        maskPaint = new Paint();
        maskPaint.setColor(Color.rgb(40, 40, 50));

        glassPaint = new Paint();
        glassPaint.setColor(Color.argb(100, 150, 200, 255));

        airTankPaint = new Paint();
        airTankPaint.setColor(Color.rgb(180, 180, 180));

        stripePaint = new Paint();
        stripePaint.setColor(Color.rgb(255, 100, 50));

        outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        outlinePaint.setColor(Color.argb(80, 0, 0, 0));

        collisionRect = new RectF();
    }

    public void setLane(int lane) {
        if (lane < 0 || lane > 2 || state == STATE_DEAD) return;
        currentLane = lane;
        targetX = lanePositions[lane];
    }

    public int getCurrentLane() { return currentLane; }
    public boolean isJumping() { return state == STATE_JUMPING; }
    public boolean isDucking() { return state == STATE_DUCKING; }
    public RectF getCollisionRect() {
        float x = currentX - playerWidth * 0.4f;
        float y = currentY - playerHeight * 0.3f;
        if (state == STATE_DUCKING) {
            y += DUCK_HEIGHT * 0.6f;
        }
        return new RectF(x, y, x + playerWidth * 0.8f, y + playerHeight * 0.6f);
    }
    public boolean isDead() { return state == STATE_DEAD; }
    public void die() { state = STATE_DEAD; }

    public void jump() {
        if (state == STATE_NORMAL) {
            state = STATE_JUMPING;
            stateTime = 0;
        }
    }

    public void duck() {
        if (state == STATE_NORMAL) {
            state = STATE_DUCKING;
            stateTime = 0;
        }
    }

    public void reset() {
        currentLane = 1;
        targetX = lanePositions[1];
        currentX = targetX;
        currentY = baseY;
        state = STATE_NORMAL;
        stateTime = 0;
    }

    public void update(float deltaTime) {
        if (state == STATE_DEAD) return;

        swimCycle += deltaTime * 4;

        currentX += (targetX - currentX) * LANE_SWITCH_SPEED;

        switch (state) {
            case STATE_JUMPING:
                stateTime += deltaTime;
                if (stateTime >= JUMP_DURATION) {
                    state = STATE_NORMAL;
                    currentY = baseY;
                } else {
                    float progress = stateTime / JUMP_DURATION;
                    currentY = baseY - (float) Math.sin(progress * Math.PI) * JUMP_HEIGHT;
                }
                break;
            case STATE_DUCKING:
                stateTime += deltaTime;
                if (stateTime >= DUCK_DURATION) {
                    state = STATE_NORMAL;
                    currentY = baseY;
                } else {
                    float progress = stateTime / DUCK_DURATION;
                    currentY = baseY + DUCK_HEIGHT * (progress < 0.5f ? progress * 2 : (1 - progress) * 2);
                }
                break;
            default:
                currentY += (baseY - currentY) * 0.15f;
                break;
        }
    }

    public void draw(Canvas canvas) {
        if (state == STATE_DEAD) return;

        canvas.save();

        float cx = currentX;
        float cy = currentY;

        float s = (state == STATE_DUCKING) ? 0.8f : 1.0f;
        float bodyW = playerWidth * s;
        float bodyH = playerHeight * s;

        float legSwing = (float) Math.sin(swimCycle) * 15;

        // Air tank (behind body)
        float tankX = cx + bodyW * 0.15f;
        float tankY = cy - bodyH * 0.1f;
        canvas.drawRoundRect(tankX - bodyW * 0.12f, tankY - bodyH * 0.35f,
                tankX + bodyW * 0.12f, tankY + bodyH * 0.15f, 8, 8, airTankPaint);

        // Body (wetsuit)
        bodyPaint.setColor(Color.rgb(30, 60, 90));
        canvas.drawRoundRect(cx - bodyW * 0.35f, cy - bodyH * 0.4f,
                cx + bodyW * 0.35f, cy + bodyH * 0.15f, 15, 15, bodyPaint);
        canvas.drawRoundRect(cx - bodyW * 0.35f, cy - bodyH * 0.4f,
                cx + bodyW * 0.35f, cy + bodyH * 0.15f, 15, 15, outlinePaint);

        // Stripe on wetsuit
        canvas.drawLine(cx - bodyW * 0.2f, cy - bodyH * 0.3f,
                cx - bodyW * 0.2f, cy + bodyH * 0.1f, stripePaint);
        canvas.drawLine(cx + bodyW * 0.2f, cy - bodyH * 0.3f,
                cx + bodyW * 0.2f, cy + bodyH * 0.1f, stripePaint);

        // Legs / Flippers
        finPaint.setColor(Color.rgb(40, 50, 60));
        float finY = cy + bodyH * 0.15f;
        Path leftFin = new Path();
        leftFin.moveTo(cx - bodyW * 0.2f, finY);
        leftFin.lineTo(cx - bodyW * 0.35f + legSwing * 0.3f, finY + bodyH * 0.25f);
        leftFin.lineTo(cx - bodyW * 0.15f + legSwing * 0.3f, finY + bodyH * 0.2f);
        leftFin.close();
        canvas.drawPath(leftFin, finPaint);

        Path rightFin = new Path();
        rightFin.moveTo(cx + bodyW * 0.2f, finY);
        rightFin.lineTo(cx + bodyW * 0.35f - legSwing * 0.3f, finY + bodyH * 0.25f);
        rightFin.lineTo(cx + bodyW * 0.15f - legSwing * 0.3f, finY + bodyH * 0.2f);
        rightFin.close();
        canvas.drawPath(rightFin, finPaint);

        // Arms
        float armSwing = (float) Math.sin(swimCycle + Math.PI) * 10;
        canvas.drawLine(cx - bodyW * 0.4f, cy - bodyH * 0.2f,
                cx - bodyW * 0.5f + armSwing * 0.5f, cy + bodyH * 0.05f, bodyPaint);
        canvas.drawLine(cx + bodyW * 0.4f, cy - bodyH * 0.2f,
                cx + bodyW * 0.5f - armSwing * 0.5f, cy + bodyH * 0.05f, bodyPaint);

        // Head
        float headR = bodyW * 0.3f;
        float headY = cy - bodyH * 0.5f;
        canvas.drawCircle(cx, headY, headR, headPaint);
        canvas.drawCircle(cx, headY, headR, outlinePaint);

        // Diving mask
        canvas.drawRoundRect(cx - headR * 0.7f, headY - headR * 0.5f,
                cx + headR * 0.7f, headY + headR * 0.1f, 5, 5, maskPaint);
        canvas.drawRoundRect(cx - headR * 0.6f, headY - headR * 0.35f,
                cx + headR * 0.6f, headY, 3, 3, glassPaint);

        // Snorkel
        canvas.drawLine(cx, headY - headR, cx + headR * 0.3f, headY - headR * 1.3f, maskPaint);
        canvas.drawCircle(cx + headR * 0.3f, headY - headR * 1.3f, 4, maskPaint);

        // Bubbles from snorkel
        if (state != STATE_DUCKING) {
            Paint bubbleP = new Paint();
            bubbleP.setStyle(Paint.Style.STROKE);
            bubbleP.setStrokeWidth(1.5f);
            bubbleP.setColor(Color.argb(100, 200, 230, 255));
            float bx = cx + headR * 0.3f;
            float by = headY - headR * 1.3f;
            canvas.drawCircle(bx + 5, by - 10, 5, bubbleP);
            canvas.drawCircle(bx + 12, by - 22, 7, bubbleP);
            canvas.drawCircle(bx + 8, by - 35, 4, bubbleP);
        }

        canvas.restore();
    }
}
