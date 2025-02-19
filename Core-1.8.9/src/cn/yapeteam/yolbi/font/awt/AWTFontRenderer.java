package cn.yapeteam.yolbi.font.awt;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.font.FontManager;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;


/**
 * @author ChengFeng
 * @since 2024/7/29
 **/
public class AWTFontRenderer implements AbstractFontRenderer {
    private static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
    private static final int[] COLOR_CODES = new int[32];

    static {
        calculateColorCodes();
    }

    private final java.awt.Font font;
    @Getter
    private final float fontHeight;
    private final Map<Character, FontCharacter> defaultCharacters = new ConcurrentHashMap<>();
    private final Map<Character, FontCharacter> boldCharacters = new ConcurrentHashMap<>();
    private final boolean chinese;

    public AWTFontRenderer(java.awt.Font font, boolean chinese) {
        this.font = font;
        this.fontHeight = (float) (font.getStringBounds("ABCDEFGHOKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", new FontRenderContext(new AffineTransform(), true, true)).getHeight() / 4.0D);
        this.chinese = chinese;

        new Thread(() -> {
            this.fillCharacters(this.defaultCharacters, 0, chinese);
            this.fillCharacters(this.boldCharacters, 1, chinese);
        }).start();
        FontManager.renderers.add(this);
    }

    public float getMiddleOfBox(float height) {
        return height / 2f - this.getFontHeight() / 2f;
    }

    private static void calculateColorCodes() {
        for (int i = 0; i < 32; ++i) {
            int amplifier = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + amplifier;
            int green = (i >> 1 & 1) * 170 + amplifier;
            int blue = (i & 1) * 170 + amplifier;
            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            COLOR_CODES[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    private void fillCharacters(Map<Character, FontCharacter> map, int style, boolean includeChinese) {
        java.awt.Font font = this.font.deriveFont(style);

        BufferedImage fontImage = new BufferedImage(1, 1, 2);
        Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();
        FontMetrics fontMetrics = fontGraphics.getFontMetrics(font);

        // 先把英文加进去
        for (char c : CharUtil.getEnglishCharArray()) {
            fillCharacter(c, map, fontGraphics, fontMetrics);
        }

        // 如果需要加中文
        if (includeChinese) {
            for (char c : CharUtil.getChineseCharArray()) {
                fillCharacter(c, map, fontGraphics, fontMetrics);
            }
        }
    }

    private final List<Runnable> tasks = new CopyOnWriteArrayList<>();

    private void fillCharacter(char character, Map<Character, FontCharacter> map, Graphics2D fontGraphics, FontMetrics fontMetrics) {
        Rectangle2D charRectangle = fontMetrics.getStringBounds(character + "", fontGraphics);

        BufferedImage charImage = new BufferedImage(MathHelper.ceiling_float_int((float) charRectangle.getWidth()) + 8, MathHelper.ceiling_float_int((float) charRectangle.getHeight()), 2);
        Graphics2D charGraphics = (Graphics2D) charImage.getGraphics();
        int width = charImage.getWidth();
        int height = charImage.getHeight();

        // 绘制透明背景
        charGraphics.setColor(TRANSPARENT_COLOR);
        charGraphics.fillRect(0, 0, width, height);
        charGraphics.setFont(font);

        this.preDraw(charGraphics);
        charGraphics.drawString(character + "", 4, font.getSize());

        tasks.add(() -> {
            int charTexture = GL11.glGenTextures();
            this.uploadTexture(charTexture, charImage, width, height);

            map.put(character, new FontCharacter(charTexture, (float) width, (float) height));
        });
    }

    @Override
    public void update() {
        if (!tasks.isEmpty()) {
            tasks.get(0).run();
            tasks.remove(0);
        }
    }

    private void preDraw(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private float getCharWidthFloat(char c) {
        if (c == 167) {
            return -1.0F;
        } else if (c == ' ') {
            return 2.0F;
        } else {
            int var2 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c);
            if (c > 0 && var2 != -1) {
                return this.defaultCharacters.get((char) var2).width() / 2.0F - 4.0F;
            } else if (this.defaultCharacters.containsKey(c) && this.defaultCharacters.get(c).width() / 2.0F - 4.0F != 0.0F) {
                int var3 = (int) (this.defaultCharacters.get(c).width() / 2.0F - 4.0F) >>> 4;
                int var4 = (int) (this.defaultCharacters.get(c).width() / 2.0F - 4.0F) & 15;
                var3 &= 15;
                ++var4;
                return (float) ((var4 - var3) / 2 + 1);
            } else {
                return 0.0F;
            }
        }
    }

    public String trimStringToWidth(String text, float width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, float width, boolean reverse) {
        if (!chinese && isChinese(text))
            return AWTFontLoader.d1fault(font.getSize() - 1).trimStringToWidth(text, width, reverse);
        if (text == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            int startIndex = reverse ? text.length() - 1 : 0;
            int step = reverse ? -1 : 1;

            String nextChar = "";
            for (int i = startIndex; i <= text.length() - 1 && i >= 0 && getStringWidth(builder + nextChar) <= width; i += step) {
                builder.append(text.charAt(i));
                String s = String.valueOf(text.charAt(i + step));
                nextChar = reverse ? s : i == text.length() - 1 ? "" : s;
            }

            if (reverse) builder.reverse();
            return builder.toString();
        }
    }

    private void uploadTexture(int texture, BufferedImage image, int width, int height) {
        int[] pixels = image.getRGB(0, 0, width, height, new int[width * height], 0, width);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int pixel = pixels[x + y * width];
                byteBuffer.put((byte) (pixel >> 16 & 255));
                byteBuffer.put((byte) (pixel >> 8 & 255));
                byteBuffer.put((byte) (pixel & 255));
                byteBuffer.put((byte) (pixel >> 24 & 255));
            }
        }

        byteBuffer.flip();
        GlStateManager.bindTexture(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 6408, width, height, 0, 6408, 5121, byteBuffer);
        byteBuffer.clear();
    }

    public double drawString(String text, double x, double y, int color) {
        return this.renderString(text, x, y, color, false);
    }

    public double drawCenteredString(String text, double x, double y, int color, CenterType type) {
        return drawCenteredString(text, x, y, color, type, false);
    }

    public double drawCenteredString(String text, double x, double y, int color, CenterType type, boolean shadow) {
        if (!this.chinese && isChinese(text))
            return AWTFontLoader.d1fault(this.font.getSize() - 1).drawCenteredString(text, x, y, color, type, shadow);

        boolean xCenter = type == CenterType.Both || type == CenterType.Horizontal;
        boolean yCenter = type == CenterType.Both || type == CenterType.Vertical;

        double realX = x - (xCenter ? getStringWidth(text) / 2d : 0d);
        double realY = y - (yCenter ? fontHeight / 2d : 0d);

        if (shadow)
            this.renderString(text, realX + 0.25d, realY + 0.25d, color, true);
        return this.drawString(text, realX, realY, color);
    }

    public double drawString(String text, double x, double y, int color, boolean shadow) {
        if (shadow) {
            this.renderString(text, x + 0.5, y + 0.5, color, true);
        }
        return this.renderString(text, x, y, color, false);
    }

    public double renderString(String text, double x, double y, int color, boolean shadowMode) {
        if (!chinese && isChinese(text))
            return AWTFontLoader.d1fault(font.getSize() - 1).renderString(text, x, y, color, shadowMode);

        GL11.glPushMatrix();
        GL11.glPushAttrib(1048575);
        GL11.glEnable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        double startX = x;
        x -= 2.0D;
        y -= 2.0D;
        x *= 2.0D;
        y *= 2.0D;
        y -= this.fontHeight / 2.5F;

        // 处理颜色

        if ((color & -67108864) == 0) {
            color |= -16777216;
        }

        if (shadowMode) {
            color = (color & 16579836) >> 2 | color & -16777216;
            ;
        }

        float red = (float) (color >> 16 & 255) / 255.0F;
        float blue = (float) (color >> 8 & 255) / 255.0F;
        float green = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        // 先上一遍色，防止之前渲染的遗留问题
        GlStateManager.color(red, blue, green, alpha);

        boolean bold = false;
        boolean specialColor = false;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            switch (character) {
                case '\n': {
                    x = startX;
                    y += this.getFontHeight() * 2.0F;
                    specialColor = false;
                    break;
                }

                case '§': {
                    int colorIndex = 21;

                    try {
                        colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                    } catch (Exception var19) {
                        Logger.warn("Illegal color code: " + text.charAt(i + 1));
                    }

                    if (colorIndex < 0) {
                        colorIndex = 15;
                    }

                    if (colorIndex < 16) {
                        if (shadowMode) {
                            colorIndex += 16;
                        }

                        int mcColor = COLOR_CODES[colorIndex];

                        GlStateManager.color((float) (mcColor >> 16 & 255) / 255.0F, (float) (mcColor >> 8 & 255) / 255.0F, (float) (mcColor & 255) / 255.0F, alpha);
                        specialColor = true;
                    } else {
                        bold = colorIndex == 17;
                    }

                    i++;
                    break;
                }

                default: {
                    if (!specialColor) {
                        GlStateManager.color(red, blue, green, alpha);
                    }

                    Map<Character, FontCharacter> map = bold ? boldCharacters : defaultCharacters;
                    if (map.containsKey(character)) {
                        /* 英文字母 gyj 这类比较靠下导致 font height 较高，渲染高度一致的中文字符会显得偏上
                         * 所以渲染中文字符时将 y 下调
                         */
                        float realY = (character > 256) ? (float) y + 1 : (float) y;
                        map.get(character).render((float) x, realY);
                        x += map.get(character).width() - 8.0F;
                    } else {
                        fillSingleChar(character, map, bold);
                    }
                }
            }
        }

        GL11.glDisable(3042);
        GL11.glDisable(3553);
        GlStateManager.bindTexture(0);
        GlStateManager.resetColor();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
        return x - startX;
    }

    /**
     * 填充单个字符，用于常用3000汉字之外的字
     *
     * @param character 字符
     * @param map       map
     * @param bold      粗体
     */
    private void fillSingleChar(char character, Map<Character, FontCharacter> map, boolean bold) {
        // 不是中文字体就算了，肯定画不出来
        if (!this.chinese) return;
        java.awt.Font font = this.font.deriveFont(bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN);

        BufferedImage fontImage = new BufferedImage(1, 1, 2);
        Graphics2D fontGraphics = (Graphics2D) fontImage.getGraphics();
        FontMetrics fontMetrics = fontGraphics.getFontMetrics(font);
        fillCharacter(character, map, fontGraphics, fontMetrics);
    }

    public static String removeColorCodes(String text) {
        return Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]").matcher(text).replaceAll("");
    }

    private int getStringWidth(String text, Map<Character, FontCharacter> map) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (map.containsKey(c)) {
                width += (int) (map.get(c).width() - 8f);
            } else if (chinese) {
                fillSingleChar(c, map, map == boldCharacters);
            }
        }
        return width;
    }

    @Override
    public float getStringWidth(String text) {
        if (!this.chinese && this.isChinese(text)) {
            return AWTFontLoader.d1fault(font.getSize() - 1).getStringWidth(text);
        } else {
            boolean hasCode = text.contains("§");

            int width = 0;

            if (hasCode) {
                for (String str : text.split("§")) {
                    if (str.isEmpty()) continue;
                    boolean isBold = str.startsWith("l");
                    Map<Character, FontCharacter> map = isBold ? boldCharacters : defaultCharacters;

                    String realText = str.substring(1);
                    width += getStringWidth(realText, map);
                }
            } else width += getStringWidth(text, defaultCharacters);

            return width / 2f;
        }
    }

    @Override
    public float getStringHeight(String text) {
        return getFontHeight();
    }

    @Override
    public float drawStringWithShadow(String text, float x, float y, int color) {
        return (float) drawString(text, (double) x, y, color, true);
    }

    @Override
    public void drawStringWithShadow(String text, float x, float y, Color color) {
        drawString(text, (double) x, y, color.getRGB(), true);
    }

    @Override
    public float drawString(String text, float x, float y, int color, boolean shadow) {
        return (float) drawString(text, (double) x, y, color, shadow);
    }

    @Override
    public float drawString(String text, float x, float y, Color color) {
        return (float) drawString(text, (double) x, y, color.getRGB());
    }

    @Override
    public float drawString(String text, float x, float y, int color) {
        return (float) drawString(text, (double) x, y, color);
    }

    @Override
    public float getStringHeight() {
        return getFontHeight();
    }

    @Override
    public java.awt.Font getFont() {
        return font;
    }

    private boolean isChinese(String text) {
        int highest = 0;

        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) > highest) {
                highest = text.charAt(i);
            }
        }

        return highest >= 256;
    }
}
