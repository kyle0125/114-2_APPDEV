package com.example.myapplication.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class ChaseShark {
    public static final int STATE_IDLE = 0;
    public static final int STATE_WARNING = 1;
    public static final int STATE_ATTACK = 2;
    public static final int STATE_RETREAT = 3;

    private int state;
    private float x, y;
    private float targetX;
    private final float playerBaseY;
    private float speed;
    private float warningTimer;
    private static final float WARNING_DURATION = 1.2f;

    private final Paint bodyPaint, bellyPaint, finPaint, eyePaint, pupilPaint;
    private final Paint mouthPaint, toothPaint, warningPaint, ripplePaint;

    private float time;
    private float attackSpeed;
    private boolean hitPlayer;

    public ChaseShark(float playerBaseY) {
        this.playerBaseY = playerBaseY;
        this.state = STATE_IDLE;
        this.hitPlayer = false;
        this.time = 0;

        bodyPaint = new Paint();
        bodyPaint.setColor(Color.rgb(45, 50, 60));

        bellyPaint = new Paint();
        bellyPaint.setColor(Color.argb(120, 200, 200, 200));

        finPaint = new Paint();
        finPaint.setColor(Color.rgb(35, 40, 50));

        eyePaint = new Paint();
        eyePaint.setColor(Color.rgb(255, 30, 30));

        pupilPaint = new Paint();
        pupilPaint.setColor(Color.BLACK);

        mouthPaint = new Paint();
        mouthPaint.setColor(Color.rgb(30, 20, 20));

        toothPaint = new Paint();
        toothPaint.setColor(Color.WHITE);

        warningPaint = new Paint();
        warningPaint.setStyle(Paint.Style.FILL);
        warningPaint.setColor(Color.argb(200, 255, 50, 50));

        ripplePaint = new Paint();
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(Color.argb(100, 255, 100, 100));
    }

    public void startAttack(float startX, int playerLane, float[] lanePositions) {
        state = STATE_WARNING;
        x = startX;
        y = -150;
        targetX = lanePositions[playerLane];
        warningTimer = WARNING_DURATION;
        hitPlayer = false;
        time = 0;
        attackSpeed = 0;
    }

    public void update(float deltaTime, float gameSpeed, int playerLane, float[] lanePositions, float gameTime) {
        time += deltaTime;

        switch (state) {
            case STATE_WARNING:
                warningTimer -= deltaTime;
                x = lanePositions[playerLane];
                if (warningTimer <= 0) {
                    state = STATE_ATTACK;
                    x = lanePositions[playerLane];
                    y = -200;
                    attackSpeed = gameSpeed * 1.5f + gameTime * 3.0f;
                    if (attackSpeed < 350) attackSpeed = 350;
                }
                break;

            case STATE_ATTACK:
                attackSpeed += deltaTime * 30;
                y += attackSpeed * deltaTime;

                targetX = lanePositions[playerLane];
                float diff = targetX - x;
                x += diff * deltaTime * 3;

                if (y > playerBaseY + 300) {
                    state = STATE_RETREAT;
                }
                break;

            case STATE_RETREAT:
                y += attackSpeed * 0.5f * deltaTime;
                if (y > playerBaseY + 600 || y > 2000) {
                    state = STATE_IDLE;
                }
                break;
        }
    }

    public void draw(Canvas canvas, float screenWidth, float screenHeight) {
        switch (state) {
            case STATE_WARNING:
                drawWarning(canvas, screenWidth, screenHeight);
                break;
            case STATE_ATTACK:
            case STATE_RETREAT:
                drawShark(canvas);
                break;
        }
    }

    private void drawWarning(Canvas canvas, float screenWidth, float screenHeight) {
        float pulse = (float) Math.sin(warningTimer * 8) * 0.3f + 0.7f;
        float warnSize = 80 + pulse * 20;

        // Ripple effect
        float rippleRadius = 30 + (1 - warningTimer / WARNING_DURATION) * 200;
        ripplePaint.setAlpha((int) (100 * (1 - rippleRadius / 230)));
        ripplePaint.setStrokeWidth(4);
        canvas.drawCircle(x, screenHeight * 0.3f, rippleRadius, ripplePaint);

        // Exclamation mark
        warningPaint.setAlpha((int) (200 * pulse));
        canvas.drawCircle(x, screenHeight * 0.3f, warnSize * 0.5f, warningPaint);

        Paint exclaimPaint = new Paint();
        exclaimPaint.setColor(Color.WHITE);
        exclaimPaint.setTextSize(warnSize * 0.8f);
        exclaimPaint.setTextAlign(Paint.Align.CENTER);
        exclaimPaint.setFakeBoldText(true);
        canvas.drawText("!", x, screenHeight * 0.3f + warnSize * 0.3f, exclaimPaint);

        // Warning text
        Paint warnText = new Paint();
        warnText.setColor(Color.argb(200, 255, 100, 100));
        warnText.setTextSize(28);
        warnText.setTextAlign(Paint.Align.CENTER);
        warnText.setFakeBoldText(true);
        canvas.drawText("⚠ 紅眼鯊魚來襲！", x, screenHeight * 0.3f - warnSize * 0.7f, warnText);
    }

    private void drawShark(Canvas canvas) {
        canvas.save();

        float sharkX = x;
        float sharkY = y;
        float s = Math.min(1f, 0.6f + (y + 200) / 800f);
        float bodyW = 140 * s;
        float bodyH = 55 * s;

        canvas.translate(sharkX, sharkY);

        // Body
        Path bodyPath = new Path();
        bodyPath.moveTo(-bodyW * 0.5f, 0);
        bodyPath.cubicTo(-bodyW * 0.3f, -bodyH * 0.5f, bodyW * 0.1f, -bodyH * 0.7f,
                bodyW * 0.5f, -bodyH * 0.3f);
        bodyPath.cubicTo(bodyW * 0.6f, -bodyH * 0.1f, bodyW * 0.55f, bodyH * 0.1f,
                bodyW * 0.5f, bodyH * 0.3f);
        bodyPath.cubicTo(bodyW * 0.1f, bodyH * 0.7f, -bodyW * 0.3f, bodyH * 0.5f,
                -bodyW * 0.5f, 0);
        bodyPath.close();
        canvas.drawPath(bodyPath, bodyPaint);

        // Belly
        Paint belly = new Paint();
        belly.setColor(Color.argb(80, 200, 200, 210));
        canvas.drawOval(new RectF(-bodyW * 0.1f, -bodyH * 0.2f, bodyW * 0.4f, bodyH * 0.2f), belly);

        // Dorsal fin
        Path dorsalFin = new Path();
        dorsalFin.moveTo(bodyW * 0.1f, -bodyH * 0.5f);
        dorsalFin.lineTo(bodyW * 0.25f, -bodyH * 1.2f);
        dorsalFin.lineTo(bodyW * 0.35f, -bodyH * 0.5f);
        dorsalFin.close();
        canvas.drawPath(dorsalFin, finPaint);

        // Tail fin
        Path tailFin = new Path();
        tailFin.moveTo(-bodyW * 0.5f, 0);
        tailFin.lineTo(-bodyW * 0.8f, -bodyH * 0.7f);
        tailFin.lineTo(-bodyW * 0.7f, 0);
        tailFin.lineTo(-bodyW * 0.8f, bodyH * 0.7f);
        tailFin.close();
        canvas.drawPath(tailFin, finPaint);

        // Eyes (glowing red)
        float eyeY = -bodyH * 0.15f;
        float eyeR = 10 * s;
        float pulse = (float) Math.sin(time * 4) * 0.2f + 0.8f;

        // Red glow
        Paint glowPaint = new Paint();
        glowPaint.setColor(Color.argb((int)(60 * pulse), 255, 0, 0));
        canvas.drawCircle(bodyW * 0.35f, eyeY - 5, eyeR * 2, glowPaint);
        canvas.drawCircle(bodyW * 0.45f, eyeY - 3, eyeR * 2, glowPaint);

        // Red eye
        eyePaint.setColor(Color.rgb((int)(255 * pulse), 30, 30));
        canvas.drawCircle(bodyW * 0.35f, eyeY - 5, eyeR, eyePaint);
        canvas.drawCircle(bodyW * 0.45f, eyeY - 3, eyeR, eyePaint);

        // Pupil
        canvas.drawCircle(bodyW * 0.35f, eyeY - 5, eyeR * 0.45f, pupilPaint);
        canvas.drawCircle(bodyW * 0.45f, eyeY - 3, eyeR * 0.45f, pupilPaint);

        // Eyebrows (angry)
        Paint brow = new Paint();
        brow.setColor(Color.rgb(20, 20, 30));
        brow.setStrokeWidth(3 * s);
        canvas.drawLine(bodyW * 0.28f, eyeY - 15, bodyW * 0.42f, eyeY - 12, brow);
        canvas.drawLine(bodyW * 0.38f, eyeY - 13, bodyW * 0.52f, eyeY - 10, brow);

        // Open mouth
        float mouthY = bodyH * 0.1f;
        float mouthW = bodyW * 0.25f;
        float mouthH2 = bodyH * 0.15f;
        RectF mouthRect = new RectF(bodyW * 0.2f, mouthY - mouthH2, bodyW * 0.45f, mouthY + mouthH2);
        canvas.drawOval(mouthRect, mouthPaint);

        // Teeth (upper)
        for (int i = 0; i < 5; i++) {
            float tx = bodyW * 0.22f + i * mouthW / 5;
            Path tooth = new Path();
            tooth.moveTo(tx, mouthY - mouthH2);
            tooth.lineTo(tx + 4, mouthY - mouthH2 + 10);
            tooth.lineTo(tx + 8, mouthY - mouthH2);
            canvas.drawPath(tooth, toothPaint);
        }

        // Teeth (lower)
        for (int i = 0; i < 4; i++) {
            float tx = bodyW * 0.24f + i * mouthW / 4;
            Path tooth = new Path();
            tooth.moveTo(tx, mouthY + mouthH2);
            tooth.lineTo(tx + 4, mouthY + mouthH2 - 10);
            tooth.lineTo(tx + 8, mouthY + mouthH2);
            canvas.drawPath(tooth, toothPaint);
        }

        // Gill slits
        Paint gill = new Paint();
        gill.setColor(Color.argb(60, 0, 0, 0));
        gill.setStrokeWidth(2 * s);
        for (int i = 0; i < 3; i++) {
            float gx = bodyW * 0.15f;
            float gy = -bodyH * 0.2f + i * bodyH * 0.12f;
            canvas.drawLine(gx, gy, gx - 8, gy + 5, gill);
        }

        // Pectoral fin
        Path pecFin = new Path();
        pecFin.moveTo(bodyW * 0.1f, bodyH * 0.3f);
        pecFin.lineTo(bodyW * 0.05f, bodyH * 0.8f);
        pecFin.lineTo(bodyW * 0.2f, bodyH * 0.5f);
        pecFin.close();
        finPaint.setColor(Color.rgb(40, 45, 55));
        canvas.drawPath(pecFin, finPaint);

        // Swim animation ripple
        if (state == STATE_ATTACK) {
            float wave = (float) Math.sin(time * 15) * 3;
            canvas.translate(0, wave);
        }

        canvas.restore();
    }

    public RectF getCollisionRect() {
        if (state != STATE_ATTACK) return new RectF(0, 0, 0, 0);
        float s = Math.min(1f, 0.6f + (y + 200) / 800f);
        float w = 140 * s * 0.6f;
        float h = 55 * s * 0.6f;
        return new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
    }

    public int getState() { return state; }
    public boolean isActive() { return state != STATE_IDLE; }
    public boolean hasHitPlayer() { return hitPlayer; }
    public void setHitPlayer(boolean hit) { this.hitPlayer = hit; }

    public void reset() {
        state = STATE_IDLE;
        hitPlayer = false;
    }
}
