package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.Locale;

/**
 * A uniform dark channel banner for the apps on this TV: a graphite field tinted with the app's accent colour,
 * a soft glow, a thin-line glyph in a round chip, and the app's name with its category underneath. Drawn in code,
 * so it weighs nothing in the APK; the menu renders it once into the tile's cached bitmap.
 */
final class ThemedBanner extends Drawable {

    enum Glyph { BROADCAST, SPARKLE, SCREEN, PLAY, NOTE, DOWNLOAD, BAG }

    private final String title, subtitle;
    private final int accent;
    private final Glyph glyph;
    private final Typeface titleFace, subFace;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF box = new RectF();

    private ThemedBanner(String title, String subtitle, int accent, Glyph glyph, Typeface titleFace, Typeface subFace) {
        this.title = title;
        this.subtitle = subtitle;
        this.accent = accent;
        this.glyph = glyph;
        this.titleFace = titleFace;
        this.subFace = subFace;
    }

    /**
     * The themed banner for this app, or null to keep the app's own artwork. Apps are listed in config.xml
     * (themed_apps) and, optionally, in a local res/values/themed_private.xml (themed_apps_private) that is kept
     * out of the repository. Each entry: "package=Title|Category|AARRGGBB|GLYPH".
     */
    static ThemedBanner forApp(Context ctx, String pkg, String label) {
        String spec = find(ctx, pkg, ctx.getResources().getIdentifier("themed_apps_private", "array",
                ctx.getPackageName()));
        if (spec == null) spec = find(ctx, pkg, R.array.themed_apps);
        if (spec == null) return null;
        String[] f = spec.split("\\|");
        if (f.length < 4) return null;
        String title = f[0].trim(), sub = f[1].trim();
        int accent;
        Glyph g;
        try {
            accent = (int) Long.parseLong(f[2].trim().replace("#", ""), 16);
            g = Glyph.valueOf(f[3].trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
        Typeface medium, light;
        try {
            medium = ctx.getResources().getFont(R.font.outfit_medium);
            light = ctx.getResources().getFont(R.font.outfit_light);
        } catch (RuntimeException e) {
            medium = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            light = Typeface.SANS_SERIF;
        }
        return new ThemedBanner(title, sub, accent, g, medium, light);
    }

    /** The "Title|Category|colour|GLYPH" part of the entry for pkg in this string array, or null. */
    private static String find(Context ctx, String pkg, int arrayId) {
        if (arrayId == 0) return null;
        for (String entry : ctx.getResources().getStringArray(arrayId)) {
            int eq = entry.indexOf('=');
            if (eq > 0 && entry.substring(0, eq).trim().equals(pkg)) return entry.substring(eq + 1);
        }
        return null;
    }

    @Override
    public int getIntrinsicWidth() {
        return 640;
    }

    @Override
    public int getIntrinsicHeight() {
        return 360;
    }

    @Override
    public void draw(Canvas c) {
        Rect b = getBounds();
        float w = b.width(), h = b.height();
        int save = c.save();
        c.translate(b.left, b.top);

        // Graphite field, tinted toward the accent at the lower right.
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, w, h, 0xFF1E222A, mix(accent, 0xFF111318, 0.78f),
                Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        // Soft accent glow behind the glyph.
        float gx = w * 0.2f, gy = h * 0.5f;
        paint.setShader(new RadialGradient(gx, gy, h * 0.75f, withAlpha(accent, 0x55), 0x00000000,
                Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        // Thin accent line along the bottom edge.
        paint.setColor(withAlpha(accent, 0xCC));
        c.drawRect(0, h - h * 0.022f, w, h, paint);

        // Round chip with the glyph.
        float r = h * 0.21f;
        paint.setColor(withAlpha(accent, 0x2E));
        c.drawCircle(gx, gy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(h * 0.008f);
        paint.setColor(withAlpha(accent, 0x80));
        c.drawCircle(gx, gy, r, paint);
        drawGlyph(c, gx, gy, r * 0.58f, h);

        // Name and category.
        paint.setStyle(Paint.Style.FILL);
        float tx = w * 0.385f, maxW = w * 0.58f;
        paint.setTypeface(titleFace);
        paint.setColor(0xFFF3F5F8);
        float ts = h * 0.17f;
        paint.setTextSize(ts);
        float tw = paint.measureText(title);
        if (tw > maxW) paint.setTextSize(ts * maxW / tw);
        c.drawText(title, tx, h * 0.53f, paint);
        paint.setTypeface(subFace);
        paint.setTextSize(h * 0.095f);
        paint.setColor(withAlpha(mix(accent, 0xFFFFFFFF, 0.55f), 0xE6));
        c.drawText(subtitle.toUpperCase(Locale.ROOT), tx, h * 0.7f, paint);
        paint.setTypeface(null);
        c.restoreToCount(save);
    }

    /** The glyph centred on (cx, cy), about 2u across, in white thin line. */
    private void drawGlyph(Canvas c, float cx, float cy, float u, float h) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(h * 0.022f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(0xFFFFFFFF);
        path.reset();
        switch (glyph) {
            case BROADCAST: {
                paint.setStyle(Paint.Style.FILL);
                c.drawCircle(cx, cy, u * 0.18f, paint);
                paint.setStyle(Paint.Style.STROKE);
                for (int i = 1; i <= 2; i++) {
                    float rr = u * (0.45f + 0.42f * i);
                    box.set(cx - rr, cy - rr, cx + rr, cy + rr);
                    c.drawArc(box, -45f, 90f, false, paint);
                    c.drawArc(box, 135f, 90f, false, paint);
                }
                break;
            }
            case SPARKLE: {
                path.moveTo(cx, cy - u);
                path.quadTo(cx + u * 0.12f, cy - u * 0.12f, cx + u, cy);
                path.quadTo(cx + u * 0.12f, cy + u * 0.12f, cx, cy + u);
                path.quadTo(cx - u * 0.12f, cy + u * 0.12f, cx - u, cy);
                path.quadTo(cx - u * 0.12f, cy - u * 0.12f, cx, cy - u);
                path.close();
                c.drawPath(path, paint);
                break;
            }
            case SCREEN: {
                box.set(cx - u, cy - u * 0.62f, cx + u, cy + u * 0.5f);
                c.drawRoundRect(box, u * 0.18f, u * 0.18f, paint);
                c.drawLine(cx - u * 0.45f, cy + u * 0.82f, cx + u * 0.45f, cy + u * 0.82f, paint);
                break;
            }
            case PLAY: {
                paint.setStyle(Paint.Style.FILL);
                path.moveTo(cx - u * 0.5f, cy - u * 0.75f);
                path.lineTo(cx + u * 0.8f, cy);
                path.lineTo(cx - u * 0.5f, cy + u * 0.75f);
                path.close();
                c.drawPath(path, paint);
                break;
            }
            case NOTE: {
                float x1 = cx - u * 0.35f, x2 = cx + u * 0.65f;
                c.drawLine(x1, cy + u * 0.55f, x1, cy - u * 0.7f, paint);
                c.drawLine(x2, cy + u * 0.35f, x2, cy - u * 0.9f, paint);
                c.drawLine(x1, cy - u * 0.7f, x2, cy - u * 0.9f, paint);
                paint.setStyle(Paint.Style.FILL);
                c.drawCircle(x1 - u * 0.28f, cy + u * 0.6f, u * 0.3f, paint);
                c.drawCircle(x2 - u * 0.28f, cy + u * 0.4f, u * 0.3f, paint);
                break;
            }
            case DOWNLOAD: {
                c.drawLine(cx, cy - u * 0.9f, cx, cy + u * 0.35f, paint);
                c.drawLine(cx - u * 0.5f, cy - u * 0.15f, cx, cy + u * 0.35f, paint);
                c.drawLine(cx + u * 0.5f, cy - u * 0.15f, cx, cy + u * 0.35f, paint);
                c.drawLine(cx - u * 0.85f, cy + u * 0.85f, cx + u * 0.85f, cy + u * 0.85f, paint);
                break;
            }
            case BAG: {
                box.set(cx - u * 0.8f, cy - u * 0.35f, cx + u * 0.8f, cy + u * 0.9f);
                c.drawRoundRect(box, u * 0.15f, u * 0.15f, paint);
                box.set(cx - u * 0.4f, cy - u * 0.95f, cx + u * 0.4f, cy - u * 0.05f);
                c.drawArc(box, 180f, 180f, false, paint);
                break;
            }
            default:
                break;
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    /** t of the way from a to b. */
    private static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
