package me.lauriichan.snowframe.util.color;

import static me.lauriichan.snowframe.util.color.HexParser.parseHexIntLenient;
import static me.lauriichan.snowframe.util.color.HexParser.appendHex;

import java.awt.Color;

public final class ColorParser {

    private ColorParser() {
        throw new UnsupportedOperationException("Static class");
    }

    public static int doubleParseHexIntLenient(String hex) {
        int value = parseHexIntLenient(hex) & 0xF;
        return value << 4 | value;
    }

    public static String asString(final Color color) {
        return asString(color, false);
    }

    public static String asString(final Color color, final boolean withAlpha) {
        StringBuilder builder = new StringBuilder("#");
        appendHex(builder, color.getRed());
        appendHex(builder, color.getGreen());
        appendHex(builder, color.getBlue());
        if (withAlpha) {
            appendHex(builder, color.getAlpha());
        }
        return builder.toString();
    }

    public static Color parse(final String hex, final Color fallback) {
        Color output = parse(hex);
        if (output == null) {
            return fallback;
        }
        return output;
    }

    public static Color parse(String hex) {
        while ((hex = hex.trim()).startsWith("#")) {
            hex = hex.substring(1);
        }
        int length = hex == null ? 0 : hex.length();
        int red = 0, green = 0, blue = 0, alpha = 0;
        switch (length) {
        case 1:
            red = green = blue = doubleParseHexIntLenient(hex);
            alpha = 255;
            break;
        case 2:
            red = green = blue = parseHexIntLenient(hex);
            alpha = 255;
            break;
        case 3:
            red = doubleParseHexIntLenient(hex.substring(0, 1));
            green = doubleParseHexIntLenient(hex.substring(1, 2));
            blue = doubleParseHexIntLenient(hex.substring(2, 3));
            alpha = 255;
            break;
        case 4:
            red = doubleParseHexIntLenient(hex.substring(0, 1));
            green = doubleParseHexIntLenient(hex.substring(1, 2));
            blue = doubleParseHexIntLenient(hex.substring(2, 3));
            alpha = doubleParseHexIntLenient(hex.substring(3, 4));
            break;
        case 5:
            red = doubleParseHexIntLenient(hex.substring(0, 1));
            green = doubleParseHexIntLenient(hex.substring(1, 2));
            blue = doubleParseHexIntLenient(hex.substring(2, 3));
            alpha = parseHexIntLenient(hex.substring(3, 5));
            break;
        case 6:
            red = parseHexIntLenient(hex.substring(0, 2));
            green = parseHexIntLenient(hex.substring(2, 4));
            blue = parseHexIntLenient(hex.substring(4, 6));
            alpha = 255;
            break;
        case 7:
            red = parseHexIntLenient(hex.substring(0, 2));
            green = parseHexIntLenient(hex.substring(2, 4));
            blue = parseHexIntLenient(hex.substring(4, 6));
            alpha = doubleParseHexIntLenient(hex.substring(6, 7));
            break;
        default:
            if (length >= 8) {
                red = parseHexIntLenient(hex.substring(0, 2));
                green = parseHexIntLenient(hex.substring(2, 4));
                blue = parseHexIntLenient(hex.substring(4, 6));
                alpha = parseHexIntLenient(hex.substring(6, 8));
            }
            break;
        }
        return new Color(red, green, blue, alpha);
    }

}