package cn.yapeteam.yolbi.utils.render;

import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.shader.GaussianFilter;
import cn.yapeteam.yolbi.shader.impl.ShaderScissor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class RenderUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<Integer, Integer> shadowCache = new HashMap<>();


    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed) {
        return RenderUtil.colorSwitch(firstColor, secondColor, time, index, timePerIndex, speed, 255.0);
    }

    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed, double alpha) {
        long now = (long) (speed * (double) System.currentTimeMillis() + (double) ((long) index * timePerIndex));
        float redDiff = (float) (firstColor.getRed() - secondColor.getRed()) / time;
        float greenDiff = (float) (firstColor.getGreen() - secondColor.getGreen()) / time;
        float blueDiff = (float) (firstColor.getBlue() - secondColor.getBlue()) / time;
        int red = Math.round((float) secondColor.getRed() + redDiff * (float) (now % (long) time));
        int green = Math.round((float) secondColor.getGreen() + greenDiff * (float) (now % (long) time));
        int blue = Math.round((float) secondColor.getBlue() + blueDiff * (float) (now % (long) time));
        float redInverseDiff = (float) (secondColor.getRed() - firstColor.getRed()) / time;
        float greenInverseDiff = (float) (secondColor.getGreen() - firstColor.getGreen()) / time;
        float blueInverseDiff = (float) (secondColor.getBlue() - firstColor.getBlue()) / time;
        int inverseRed = Math.round((float) firstColor.getRed() + redInverseDiff * (float) (now % (long) time));
        int inverseGreen = Math.round((float) firstColor.getGreen() + greenInverseDiff * (float) (now % (long) time));
        int inverseBlue = Math.round((float) firstColor.getBlue() + blueInverseDiff * (float) (now % (long) time));
        if (now % ((long) time * 2L) < (long) time) {
            return getColor(inverseRed, inverseGreen, inverseBlue, (int) alpha);
        }
        return getColor(red, green, blue, (int) alpha);
    }

    public static int getColor(final int red, final int green, final int blue) {
        return getColor(red, green, blue, 255);
    }

    public static void drawGoodCircle(double x2, double y2, float radius, int color) {//徐锦良的奇妙命名
        RenderUtil.color(color);
        GLUtils.setup2DRendering(() -> {
            GL11.glEnable(2832);
            GL11.glHint(3153, 4354);
            GL11.glPointSize(radius * (float) (2 * Minecraft.getMinecraft().gameSettings.guiScale));
            GLUtils.render(0, () -> GL11.glVertex2d(x2, y2));
        });
    }

    public static void renderRoundedRect(float x2, float y2, float width, float height, float radius, int color) {
        RenderUtil.drawGoodCircle(x2 + radius, y2 + radius, radius, color);
        RenderUtil.drawGoodCircle(x2 + width - radius, y2 + radius, radius, color);
        RenderUtil.drawGoodCircle(x2 + radius, y2 + height - radius, radius, color);
        RenderUtil.drawGoodCircle(x2 + width - radius, y2 + height - radius, radius, color);
        drawRect3(x2 + radius, y2, width - radius * 2.0f, height, color);
        drawRect3(x2, y2 + radius, width, height - radius * 2.0f, color);
    }

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float) ((double) limit * 0.01));
    }

    public static void drawRect3(double x2, double y2, double width, double height, int color) {
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0.0f);
        GLUtils.setup2DRendering(true);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        color(worldrenderer.pos(x2, y2, 0.0), color).endVertex();
        color(worldrenderer.pos(x2, y2 + height, 0.0), color).endVertex();
        color(worldrenderer.pos(x2 + width, y2 + height, 0.0), color).endVertex();
        color(worldrenderer.pos(x2 + width, y2, 0.0), color).endVertex();
        tessellator.draw();
        GLUtils.end2DRendering();
    }

    public static WorldRenderer color(WorldRenderer wr, final int colorHex) {
        return wr.color(colorHex >> 16 & 0xFF, colorHex >> 8 & 0xFF, colorHex & 0xFF, colorHex >> 24 & 0xFF);
    }

    public static int getColor(final int red, final int green, final int blue, final int alpha) {
        int color = MathHelper.clamp_int(alpha, 0, 255) << 24;
        color |= MathHelper.clamp_int(red, 0, 255) << 16;
        color |= MathHelper.clamp_int(green, 0, 255) << 8;
        color |= MathHelper.clamp_int(blue, 0, 255);
        return color;
    }

    public static void drawBorderedRect(int x, int y, int x2, int y2, float lineWidth, int color1, final int color2) {
        Gui.drawRect(x, y, x2, y2, color2);
        final float f = (color1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (color1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (color1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (color1 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void scaleStart(float x2, float y2, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x2, y2, 0.0f);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-x2, -y2, 0.0f);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void startDrawing() {
        GL11.glEnable(GL_BLEND);
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glDisable(GL_DEPTH_TEST);
        ReflectionManager.EntityRenderer$setupCameraTransform(mc.entityRenderer, ReflectionManager.Minecraft$getTimer(mc).renderPartialTicks, 0);
    }

    public static void stopDrawing() {
        GL11.glDisable(GL_BLEND);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glDisable(GL_BLEND);
        GL11.glEnable(GL_DEPTH_TEST);
    }

    public static void enableGL2D() {
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_BLEND);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
    }

    public static void disableGL2D() {
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL_BLEND);
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        GL11.glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);
    }


    public static void color(int color) {
        float f = (color >> 24 & 0xFF) / 255.0F;
        float f1 = (color >> 16 & 0xFF) / 255.0F;
        float f2 = (color >> 8 & 0xFF) / 255.0F;
        float f3 = (color & 0xFF) / 255.0F;
        GL11.glColor4f(f1, f2, f3, f);
    }

    public static void color(Color color) {
        color(color.getRGB());
    }

    public static void drawCircle(double x, double y, double radius, int c) {
        float alpha = (float) (c >> 24 & 255) / 255.0f;
        float red = (float) (c >> 16 & 255) / 255.0f;
        float green = (float) (c >> 8 & 255) / 255.0f;
        float blue = (float) (c & 255) / 255.0f;
        boolean blend = GL11.glIsEnabled(3042);
        boolean line = GL11.glIsEnabled(2848);
        boolean texture = GL11.glIsEnabled(3553);
        if (!blend) {
            GL11.glEnable(3042);
        }
        if (!line) {
            GL11.glEnable(2848);
        }
        if (texture) {
            GL11.glDisable(3553);
        }
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(9);
        int i = 0;
        while (i <= 360) {
            GL11.glVertex2d(
                    x + Math.sin((double) i * 3.141526 / 180.0) * radius,
                    y + Math.cos((double) i * 3.141526 / 180.0) * radius);
            ++i;
        }

        GL11.glEnd();
        if (texture) {
            GL11.glEnable(3553);
        }
        if (!line) {
            GL11.glDisable(2848);
        }
        if (!blend) {
            GL11.glDisable(3042);
        }
    }

    public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
        enableGL2D();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        color(topColor);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        color(bottomColor);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(7424);
        disableGL2D();
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static double interpolate(double current, double old, double scale) {
        return (old + (current - old) * scale);
    }

    public static void drawFastRoundedRect(double left, double top, double right, double bottom, double radius, int color) {
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        glColor4f((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, (color >> 24 & 0xFF) / 255.0f);
        glBegin(5);
        glVertex2d(left + radius, top);
        glVertex2d(left + radius, bottom);
        glVertex2d(right - radius, top);
        glVertex2d(right - radius, bottom);
        glEnd();
        glBegin(5);
        glVertex2d(left, top + radius);
        glVertex2d(left + radius, top + radius);
        glVertex2d(left, bottom - radius);
        glVertex2d(left + radius, bottom - radius);
        glEnd();
        glBegin(5);
        glVertex2d(right, top + radius);
        glVertex2d(right - radius, top + radius);
        glVertex2d(right, bottom - radius);
        glVertex2d(right - radius, bottom - radius);
        glEnd();
        glBegin(6);
        double d1 = right - radius;
        double d2 = top + radius;
        glVertex2d(d1, d2);
        int j;
        for (j = 0; j <= 18; ++j)
            glVertex2d(d1 + radius * Math.cos(Math.toRadians(j * 5.0f)), d2 - radius * Math.sin(Math.toRadians(j * 5.0f)));
        glEnd();
        glBegin(6);
        d1 = left + radius;
        d2 = top + radius;
        glVertex2d(d1, d2);
        for (j = 0; j <= 18; ++j)
            glVertex2d(d1 - radius * Math.cos(Math.toRadians(j * 5.0f)), d2 - radius * Math.sin(Math.toRadians(j * 5.0f)));
        glEnd();
        glBegin(6);
        d1 = left + radius;
        d2 = bottom - radius;
        glVertex2d(d1, d2);
        for (j = 0; j <= 18; ++j)
            glVertex2d(d1 - radius * Math.cos(Math.toRadians(j * 5.0f)), d2 + radius * Math.sin(Math.toRadians(j * 5.0f)));
        glEnd();
        glBegin(6);
        d1 = right - radius;
        d2 = bottom - radius;
        glVertex2d(d1, d2);
        for (j = 0; j <= 18; ++j)
            glVertex2d(d1 + radius * Math.cos(Math.toRadians(j * 5.0f)), d2 + radius * Math.sin(Math.toRadians(j * 5.0f)));
        glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
    }

    public static void drawFastRoundedRect2(double x, double y, double width, double height, double radius, int color) {
        drawFastRoundedRect(x, y, x + width, y + height, radius, color);
    }

    public static void circle(final float x, final float y, final float radius, final int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float var11 = (color >> 24 & 0xFF) / 255.0f;
        final float var12 = (color >> 16 & 0xFF) / 255.0f;
        final float var13 = (color >> 8 & 0xFF) / 255.0f;
        final float var14 = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        if (var11 > 0.5f) {
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(GL_LINE_SMOOTH);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w;
            final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    protected static float zLevel;

    public static void drawGradientRectTB(double left, double top, double right, double bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectLR(double left, double top, double right, double bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, top, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawImage(int image, float x, float y, float width, float height, int color) {
        //enableGL2D();
        glPushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.enableBlend();
        GlStateManager.bindTexture(image);
        Color c = new Color(color, true);
        GlStateManager.resetColor();
        GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glPopMatrix();
        //GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        //glEnable(GL11.GL_TEXTURE_2D);
        //glDisable(GL_CULL_FACE);
        //glEnable(GL11.GL_ALPHA_TEST);
        //GlStateManager.enableBlend();
        //GlStateManager.bindTexture(image);
//
        //color(color);
//
        //GL11.glBegin(GL11.GL_QUADS);
        //GL11.glTexCoord2f(0, 0); // top left
        //GL11.glVertex2f(x, y);
//
        //GL11.glTexCoord2f(0, 1); // bottom left
        //GL11.glVertex2f(x, y + height);
//
        //GL11.glTexCoord2f(1, 1); // bottom right
        //GL11.glVertex2f(x + width, y + height);
//
        //GL11.glTexCoord2f(1, 0); // top right
        //GL11.glVertex2f(x + width, y);
        //GL11.glEnd();
//
        //GlStateManager.enableTexture2D();
        //GlStateManager.disableBlend();
        //GlStateManager.resetColor();
//
        //glEnable(GL_CULL_FACE);
        //glPopMatrix();
        //disableGL2D();
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0).tex(u * f, (v + height) * f1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0).tex((u + width) * f, (v + height) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0).tex((u + width) * f, v * f1).endVertex();
        bufferbuilder.pos(x, y, 0.0).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final Tessellator var9 = Tessellator.getInstance();
        final WorldRenderer var10 = var9.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f);
        if (color.getAlpha() > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w;
            final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawFilledCircleNoGL(final int x, final int y, final double r, final int c, final int quality) {
        final float f = ((c >> 24) & 0xff) / 255F;
        final float f1 = ((c >> 16) & 0xff) / 255F;
        final float f2 = ((c >> 8) & 0xff) / 255F;
        final float f3 = (c & 0xff) / 255F;

        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        for (int i = 0; i <= 360 / quality; i++) {
            final double x2 = Math.sin(((i * quality * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * quality * Math.PI) / 180)) * r;
            GL11.glVertex2d(x + x2, y + y2);
        }

        GL11.glEnd();
    }

    public static void start2D() {
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
    }

    public static void stop2D() {
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        glColor4f(1, 1, 1, 1);
    }

    public static void drawCornerBox(double x, double y, double x2, double y2, double lw, Color color) {
        double width = Math.abs(x2 - x);
        double height = Math.abs(y2 - y);
        double halfWidth = width / 4;
        double halfHeight = height / 4;
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth((float) lw);
        color(color);

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + halfWidth, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + halfHeight);
        GL11.glEnd();


        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y + height - halfHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + halfWidth, y + height);
        GL11.glEnd();

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + width - halfWidth, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y + height - halfHeight);
        GL11.glEnd();

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + width, y + halfHeight);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width - halfWidth, y);
        GL11.glEnd();

        GL11.glPopMatrix();
        stop2D();
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        double j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect2(double x, double y, double width, double height, int color) {
        drawRect(x, y, x + width, y + height, color);
    }

    public static final Field renderPosX, renderPosY, renderPosZ;

    static {
        try {
            renderPosX = RenderManager.class.getDeclaredField(Mapper.map("net.minecraft.client.renderer.entity.RenderManager", "renderPosX", null, Mapper.Type.Field));
            renderPosY = RenderManager.class.getDeclaredField(Mapper.map("net.minecraft.client.renderer.entity.RenderManager", "renderPosY", null, Mapper.Type.Field));
            renderPosZ = RenderManager.class.getDeclaredField(Mapper.map("net.minecraft.client.renderer.entity.RenderManager", "renderPosZ", null, Mapper.Type.Field));
            renderPosX.setAccessible(true);
            renderPosY.setAccessible(true);
            renderPosZ.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static double getRenderPos(Field field, Object obj) {
        try {
            return (double) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void drawEntityBox(final EntityLivingBase entity, final Color color, final boolean outline, final boolean box, final float outlineWidth, float partialTicks) {
        final RenderManager renderManager = mc.getRenderManager();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - getRenderPos(renderPosX, renderManager);
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - getRenderPos(renderPosY, renderManager);
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - getRenderPos(renderPosZ, renderManager);

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        if (outline) {
            glLineWidth(outlineWidth);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), box ? 170 : 255);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        if (box) {
            glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
            drawFilledBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    // public static void drawGradientRectTB(float x, float y, float x1, float y1, int topColor, int bottomColor) {
    //     enableGL2D();
    //     GL11.glShadeModel(7425);
    //     GL11.glBegin(7);
    //     color(topColor);
    //     GL11.glVertex2f(x1, y);
    //     GL11.glVertex2f(x, y);
    //     color(bottomColor);
    //     GL11.glVertex2f(x, y1);
    //     GL11.glVertex2f(x1, y1);
    //     GL11.glEnd();
    //     GL11.glShadeModel(7424);
    //     disableGL2D();
    // }

    // public static void drawGradientRectLR(float x, float y, float x1, float y1, int leftColor, int rightColor) {
    //     enableGL2D();
    //     GL11.glShadeModel(7425);
    //     GL11.glBegin(7);
    //     color(leftColor);
    //     GL11.glVertex2f(x, y);
    //     GL11.glVertex2f(x, y1);
    //     color(rightColor);
    //     GL11.glVertex2f(x1, y1);
    //     GL11.glVertex2f(x1, y);
    //     GL11.glEnd();
    //     GL11.glShadeModel(7424);
    //     disableGL2D();
    // }

    public static void drawEntityBox(AxisAlignedBB entityBox, double lastTickPosX, double lastTickPosY, double lastTickPosZ, double posX, double posY, double posZ, final Color color, final boolean outline, final boolean box, final float outlineWidth, float partialTicks) {
        final RenderManager renderManager = mc.getRenderManager();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = lastTickPosX + (posX - lastTickPosX) * partialTicks
                - getRenderPos(renderPosX, renderManager);
        final double y = lastTickPosY + (posY - lastTickPosY) * partialTicks
                - getRenderPos(renderPosY, renderManager);
        final double z = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks
                - getRenderPos(renderPosZ, renderManager);
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - posX + x - 0.05D,
                entityBox.minY - posY + y,
                entityBox.minZ - posZ + z - 0.05D,
                entityBox.maxX - posX + x + 0.05D,
                entityBox.maxY - posY + y + 0.15D,
                entityBox.maxZ - posZ + z + 0.05D
        );

        if (outline) {
            glLineWidth(outlineWidth);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), box ? 170 : 255);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        if (box) {
            glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
            drawFilledBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();

        tessellator.draw();
    }

    public static void resetCaps(final String scale) {
        if (!glCapMap.containsKey(scale)) {
            return;
        }
        Map<Integer, Boolean> map = glCapMap.get(scale);
        map.forEach(RenderUtil::setGlState);
        map.clear();
    }

    public static void resetCaps() {
        resetCaps("COMMON");
    }

    public static void enableGlCap(final int cap, final String scale) {
        setGlCap(cap, true, scale);
    }

    public static void enableGlCap(final int cap) {
        enableGlCap(cap, "COMMON");
    }

    public static void disableGlCap(final int cap, final String scale) {
        setGlCap(cap, false, scale);
    }

    public static void disableGlCap(final int cap) {
        disableGlCap(cap, "COMMON");
    }


    public static void enableGlCap(final int... caps) {
        for (int cap : caps) {
            setGlCap(cap, true, "COMMON");
        }
    }

    public static void disableGlCap(final int... caps) {
        for (int cap : caps) {
            setGlCap(cap, false, "COMMON");
        }
    }

    private static final Map<String, Map<Integer, Boolean>> glCapMap = new HashMap<>();

    public static void setGlCap(final int cap, final boolean state, final String scale) {
        if (!glCapMap.containsKey(scale)) {
            glCapMap.put(scale, new HashMap<>());
        }
        glCapMap.get(scale).put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlCap(final int cap, final boolean state) {
        setGlCap(cap, state, "COMMON");
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;
        final float alpha = color.getAlpha() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(final Color color, final int alpha) {
        glColor(color, alpha / 255F);
    }

    public static void glColor(final Color color, final float alpha) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(final int hex) {
        final float alpha = (hex >> 24 & 0xFF) / 255F;
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glColor(final int hex, final int alpha) {
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha / 255F);
    }

    public static void glColor(final int hex, final float alpha) {
        final float red = (hex >> 16 & 0xFF) / 255F;
        final float green = (hex >> 8 & 0xFF) / 255F;
        final float blue = (hex & 0xFF) / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    public static void drawBloomShadow(float x, float y, float width, float height, int blurRadius, Color color, boolean scissor) {
        drawBloomShadow(x, y, width, height, blurRadius, 0, color.getRGB(), scissor, false, false, false, false);
    }

    public static void drawBloomShadow(float x, float y, float width, float height, int blurRadius, int color, boolean scissor) {
        drawBloomShadow(x, y, width, height, blurRadius, 0, color, scissor, false, false, false, false);
    }

    public static void drawBloomShadow(float x, float y, float width, float height, int blurRadius, int roundRadius, int color, boolean scissor) {
        drawBloomShadow(x, y, width, height, blurRadius, roundRadius, color, scissor, false, false, false, false);
    }

    public static void drawBloomShadow(float x, float y, float width, float height, int blurRadius, int roundRadius, int color, boolean scissor, boolean cut_top, boolean cut_bottom, boolean cut_left, boolean cut_right) {
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x -= blurRadius - 0.5f;
        y -= blurRadius - 0.5f;

        int identifier = Arrays.deepHashCode(new Object[]{width, height, blurRadius, roundRadius});
        if (!shadowCache.containsKey(identifier)) {
            if (width <= 0) width = 1;
            if (height <= 0) height = 1;
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = original.getGraphics();
            g.setColor(ColorUtil.colorFromInt(-1));
            g.fillRoundRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2), roundRadius, roundRadius);
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            int cut_x = blurRadius, cut_y = blurRadius, cut_w = (int) (width - blurRadius * 2), cut_h = (int) (height - blurRadius * 2);
            if (cut_top) {
                cut_y = 0;
                cut_h = (int) (height - blurRadius);
            }

            if (cut_bottom) {
                cut_h = (int) (height - blurRadius);
            }

            if (cut_left) {
                cut_x = 0;
                cut_w = (int) (width - blurRadius);
            }

            if (cut_right) {
                cut_w = (int) (width - blurRadius);
            }
            if (scissor)
                blurred = new ShaderScissor(cut_x, cut_y, cut_w, cut_h, blurred, 1, false, false).generate();
            shadowCache.put(identifier, TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false));
        }
        drawImage(shadowCache.get(identifier), x, y, width, height, color);
    }

    private static void drawESPImage(int texture, double x, double y, double x2, double y2, Color c, Color c2, Color c3, Color c4, float alpha) {
        GlStateManager.bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x, y2, 1).tex(0.0, 1.0).color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)).endVertex();
        bufferbuilder.pos(x2, y2, 1).tex(1.0, 1.0).color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (alpha * 255)).endVertex();
        bufferbuilder.pos(x2, y, 1).tex(1.0, 0.0).color(c3.getRed(), c3.getGreen(), c3.getBlue(), (int) (alpha * 255)).endVertex();
        bufferbuilder.pos(x, y, 1).tex(0.0, 0.0).color(c4.getRed(), c4.getGreen(), c4.getBlue(), (int) (alpha * 255)).endVertex();
        GlStateManager.shadeModel(7425);
        GlStateManager.depthMask(false);
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.shadeModel(7424);
    }

    public static void renderESPImage(int texture, EntityLivingBase entity, float scale, float rotate, Color color, Color color2, Color color3, Color color4, float alpha, float partialTicks) {
        RenderManager renderManager = mc.getRenderManager();
        double x = interpolate(entity.posX, entity.prevPosX, partialTicks) - ReflectionManager.GetRenderManager$renderPosX(renderManager);
        double y = interpolate(entity.posY, entity.prevPosY, partialTicks) - ReflectionManager.GetRenderManager$renderPosY(renderManager);
        double z = interpolate(entity.posZ, entity.prevPosZ, partialTicks) - ReflectionManager.GetRenderManager$renderPosZ(renderManager);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.0F, (float) y + entity.height / 2f, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.scale(-f1, -f1, f1);
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST, GL_ALPHA_TEST);
        enableGlCap(GL_BLEND,GL_TEXTURE_2D);
        GlStateManager.shadeModel(7425);
        GL11.glRotated(rotate, 0.0, 0.0, 1.0);
        float w = 50 * scale, h = 50 * scale;
        drawESPImage(texture, -w / 2f, -h / 2f, w / 2f, h / 2f, color, color2, color3, color4, alpha * 0.7f);
        GlStateManager.shadeModel(7424);
        resetCaps();
        glColor4f(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }
}
