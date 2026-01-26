package me.lauriichan.snowframe.util.color;

import static me.lauriichan.snowframe.util.color.HexParser.appendHex;
import static me.lauriichan.snowframe.util.color.HexParser.parseHexIntLenient;
import static me.lauriichan.snowframe.util.color.ColorParser.doubleParseHexIntLenient;

import java.awt.Color;
import java.util.Objects;

public final class SimpleColor {

    public static SimpleColor sRGB(String hex) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.set(hex);
        return color;
    }

    public static SimpleColor sRGB(Color awtColor) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.set(awtColor);
        return color;
    }

    public static SimpleColor sRGB(double red, double green, double blue, double alpha) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.alpha = alpha;
        color.components[0] = red;
        color.components[1] = green;
        color.components[2] = blue;
        return color;
    }

    public static SimpleColor lRGB(double red, double green, double blue, double alpha) {
        SimpleColor color = new SimpleColor(ColorType.LRGB);
        color.alpha = alpha;
        color.components[0] = red;
        color.components[1] = green;
        color.components[2] = blue;
        return color;
    }

    public static SimpleColor okLab(double l, double a, double b, double alpha) {
        SimpleColor color = new SimpleColor(ColorType.OKLAB);
        color.alpha = alpha;
        color.components[0] = l;
        color.components[1] = a;
        color.components[2] = b;
        return color;
    }

    public static SimpleColor sRGB(double red, double green, double blue) {
        SimpleColor color = new SimpleColor(ColorType.SRGB);
        color.components[0] = red;
        color.components[1] = green;
        color.components[2] = blue;
        return color;
    }

    public static SimpleColor lRGB(double red, double green, double blue) {
        SimpleColor color = new SimpleColor(ColorType.LRGB);
        color.components[0] = red;
        color.components[1] = green;
        color.components[2] = blue;
        return color;
    }

    public static SimpleColor okLab(double l, double a, double b) {
        SimpleColor color = new SimpleColor(ColorType.OKLAB);
        color.components[0] = l;
        color.components[1] = a;
        color.components[2] = b;
        return color;
    }

    public static enum ColorType {
        SRGB,
        LRGB,
        OKLAB;
    }

    private final ColorType type;
    // 0 = red / l
    // 1 = green / m
    // 2 = blue / s
    private final double[] components;
    private volatile double alpha = 1d;

    public SimpleColor(final ColorType type) {
        this.type = Objects.requireNonNull(type, "ColorType can't be null");
        this.components = new double[3];
    }

    public int alphaInt() {
        return toIntRGB(alpha);
    }

    public double alpha() {
        return alpha;
    }

    public SimpleColor alpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public int redInt() {
        return toIntRGB(components[0]);
    }

    public double red() {
        return components[0];
    }

    public SimpleColor red(double red) {
        components[0] = red;
        return this;
    }

    public int greenInt() {
        return toIntRGB(components[1]);
    }

    public double green() {
        return components[1];
    }

    public SimpleColor green(double green) {
        components[1] = green;
        return this;
    }

    public int blueInt() {
        return toIntRGB(components[2]);
    }

    public double blue() {
        return components[2];
    }

    public SimpleColor blue(double blue) {
        components[2] = blue;
        return this;
    }

    public int asABGR() {
        double[] components = this.components;
        if (type != ColorType.SRGB) {
            convertToSRGB(components = copy(components), type);
        }
        return abgr(alpha, components);
    }

    public int asRGBA() {
        double[] components = this.components;
        if (type != ColorType.SRGB) {
            convertToSRGB(components = copy(components), type);
        }
        return rgba(alpha, components);
    }

    public String asHex() {
        return asHex(false);
    }

    public String asHex(boolean withAlpha) {
        StringBuilder builder = new StringBuilder("#");
        double[] components = this.components;
        if (type != ColorType.SRGB) {
            convertToSRGB(components = copy(components), type);
        }
        for (double component : components) {
            appendHex(builder, toIntRGB(component), 2);
        }
        if (withAlpha) {
            appendHex(builder, toIntRGB(alpha), 2);
        }
        return builder.toString();
    }

    public SimpleColor set(String hex) {
        while ((hex = hex.trim()).startsWith("#")) {
            hex = hex.substring(1);
        }
        int length = hex == null ? 0 : hex.length();
        int red = 0, green = 0, blue = 0, alpha = 0;
        switch (length) {
        case 0:
            break;
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
            red = parseHexIntLenient(hex.substring(0, 2));
            green = parseHexIntLenient(hex.substring(2, 4));
            blue = parseHexIntLenient(hex.substring(4, 6));
            alpha = parseHexIntLenient(hex.substring(6, 8));
            break;
        }
        return setInt(red, green, blue, alpha);
    }

    public SimpleColor set(Color color) {
        return setInt(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private SimpleColor setInt(int red, int green, int blue, int alpha) {
        this.alpha = alpha / 255d;
        components[0] = red / 255d;
        components[1] = green / 255d;
        components[2] = blue / 255d;
        if (type != ColorType.SRGB) {
            convert(components, ColorType.SRGB, type);
        }
        return this;
    }

    public SimpleColor set(SimpleColor color) {
        this.alpha = color.alpha;
        System.arraycopy(color.components, 0, components, 0, 3);
        convert(components, color.type, type);
        return this;
    }

    public SimpleColor subtract(double value) {
        this.alpha -= value;
        components[0] -= value;
        components[1] -= value;
        components[2] -= value;
        return this;
    }

    public SimpleColor subtract(SimpleColor color) {
        double[] values = color.components;
        if (type != color.type) {
            convert(values = copy(values), color.type, type);
        }
        this.alpha -= color.alpha;
        components[0] -= values[0];
        components[1] -= values[1];
        components[2] -= values[2];
        return this;
    }

    public SimpleColor subtractTo(SimpleColor color) {
        double[] values = components;
        if (type != color.type) {
            convert(values = copy(values), type, color.type);
        }
        color.alpha -= alpha;
        color.components[0] -= values[0];
        color.components[1] -= values[1];
        color.components[2] -= values[2];
        return this;
    }

    public SimpleColor add(double value) {
        this.alpha += value;
        components[0] += value;
        components[1] += value;
        components[2] += value;
        return this;
    }

    public SimpleColor add(SimpleColor color) {
        double[] values = color.components;
        if (type != color.type) {
            convert(values = copy(values), color.type, type);
        }
        this.alpha += color.alpha;
        components[0] += values[0];
        components[1] += values[1];
        components[2] += values[2];
        return this;
    }

    public SimpleColor addTo(SimpleColor color) {
        double[] values = components;
        if (type != color.type) {
            convert(values = copy(values), type, color.type);
        }
        color.alpha += alpha;
        color.components[0] += values[0];
        color.components[1] += values[1];
        color.components[2] += values[2];
        return this;
    }

    public SimpleColor multiply(double value) {
        this.alpha *= value;
        components[0] *= value;
        components[1] *= value;
        components[2] *= value;
        return this;
    }

    public SimpleColor multiply(SimpleColor color) {
        double[] values = color.components;
        if (type != color.type) {
            convert(values = copy(values), color.type, type);
        }
        this.alpha *= color.alpha;
        components[0] *= values[0];
        components[1] *= values[1];
        components[2] *= values[2];
        return this;
    }

    public SimpleColor multiplyTo(SimpleColor color) {
        double[] values = components;
        if (type != color.type) {
            convert(values = copy(values), type, color.type);
        }
        color.alpha *= alpha;
        color.components[0] *= values[0];
        color.components[1] *= values[1];
        color.components[2] *= values[2];
        return this;
    }

    public SimpleColor interpolate(SimpleColor start, SimpleColor end, double progress) {
        double[] tmp = copy(start.components);
        convertToOkLab(tmp, start.type);
        System.arraycopy(end.components, 0, components, 0, 3);
        convertToOkLab(components, end.type);
        components[0] = components[0] * progress + tmp[0] * (1 - progress);
        components[1] = components[1] * progress + tmp[1] * (1 - progress);
        components[2] = components[2] * progress + tmp[2] * (1 - progress);
        this.alpha = end.alpha * progress + start.alpha * (1d - progress);
        convert(components, ColorType.OKLAB, type);
        return this;
    }

    public SimpleColor as(ColorType type) {
        SimpleColor color = new SimpleColor(type);
        ;
        color.alpha = alpha;
        System.arraycopy(components, 0, color.components, 0, 3);
        convert(color.components, this.type, color.type);
        return color;
    }

    public SimpleColor duplicate() {
        SimpleColor color = new SimpleColor(type);
        color.alpha = alpha;
        System.arraycopy(components, 0, color.components, 0, 3);
        return color;
    }

    public Color asAwtColor() {
        return new Color(asRGBA());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleColor color)) {
            return false;
        }
        double[] values = copy(color.components);
        if (type != color.type) {
            convert(values, color.type, type);
        }
        return alpha == color.alpha && components[0] == values[0] && components[1] == values[1] && components[2] == values[2];
    }

    /*
     * Converters
     */

    private static void convert(double[] values, ColorType from, ColorType to) {
        switch (to) {
        case LRGB -> convertToLRGB(values, from);
        case OKLAB -> convertToOkLab(values, from);
        case SRGB -> convertToSRGB(values, from);
        }
    }

    private static void convertToLRGB(double[] values, ColorType from) {
        switch (from) {
        case SRGB -> {
            srgb2lrgb(values);
        }
        case OKLAB -> {
            oklab2lrgb(values);
        }
        default -> {
        }
        }
    }

    private static void convertToSRGB(double[] values, ColorType from) {
        switch (from) {
        case LRGB -> {
            lrgb2srgb(values);
        }
        case OKLAB -> {
            oklab2lrgb(values);
            lrgb2srgb(values);
        }
        default -> {
        }
        }
    }

    private static void convertToOkLab(double[] values, ColorType from) {
        switch (from) {
        case LRGB -> {
            lrgb2oklab(values);
        }
        case SRGB -> {
            srgb2lrgb(values);
            lrgb2oklab(values);
        }
        default -> {
        }
        }
    }

    /*
     * Conversion helper
     */

    private static final double CONST_1_OVER_2_4 = 1d / 2.4d;

    private static double[] copy(double[] input) {
        double[] output = new double[3];
        System.arraycopy(input, 0, output, 0, 3);
        return output;
    }

    private static int abgr(double alpha, double[] values) {
        return toIntRGB(alpha) << 24 | toIntRGB(values[2]) << 16 | toIntRGB(values[1]) << 8 | toIntRGB(values[0]);
    }

    private static int rgba(double alpha, double[] values) {
        return toIntRGB(values[0]) << 24 | toIntRGB(values[1]) << 16 | toIntRGB(values[2]) << 8 | toIntRGB(alpha);
    }

    private static void lrgb2srgb(double[] values) {
        values[0] = lrgb2srgb(values[0]);
        values[1] = lrgb2srgb(values[1]);
        values[2] = lrgb2srgb(values[2]);
    }

    private static double lrgb2srgb(double value) {
        return value < 0.0031308d ? value * 12.92d : 1.055d * Math.pow(value, CONST_1_OVER_2_4) - 0.055d;
    }

    private static void srgb2lrgb(double[] values) {
        values[0] = srgb2lrgb(values[0]);
        values[1] = srgb2lrgb(values[1]);
        values[2] = srgb2lrgb(values[2]);
    }

    private static double srgb2lrgb(double value) {
        return value < 0.04045d ? value / 12.92d : Math.pow((value + 0.055d) / 1.055d, 2.4d);
    }

    private static void lrgb2oklab(double[] values) {
        double l = Math.cbrt(values[0] * 0.4122214708d + values[1] * 0.5363325363d + values[2] * 0.0514459929d);
        double m = Math.cbrt(values[0] * 0.2119034982d + values[1] * 0.6806995451d + values[2] * 0.1073969566d);
        double s = Math.cbrt(values[0] * 0.0883024619d + values[1] * 0.2817188376d + values[2] * 0.6299787005d);
        values[0] = l * 0.2104542553d + m * 0.7936177850d - s * 0.0040720468d;
        values[1] = l * 1.9779984951d - m * 2.4285922050d + s * 0.4505937099d;
        values[2] = l * 0.0259040371d + m * 0.7827717662d - s * 0.8086757660d;
    }

    private static void oklab2lrgb(double[] values) {
        double l = cube(values[0] + values[1] * 0.3963377774d + values[2] * 0.2158037573d);
        double m = cube(values[0] - values[1] * 0.1055613458d - values[2] * 0.0638541728d);
        double s = cube(values[0] - values[1] * 0.0894841775d - values[2] * 1.2914855480d);
        values[0] = l * 4.0767416621d - m * 3.3077115913d + s * 0.2309699292d;
        values[1] = l * -1.2684380046d + m * 2.6097574011d - s * 0.3413193965d;
        values[2] = l * -0.0041960863d - m * 0.7034186147d + s * 1.7076147010d;
    }

    private static double cube(double value) {
        return value * value * value;
    }

    public static int toIntRGB(double value) {
        return Math.min(Math.max((int) Math.round(value * 255), 0), 255);
    }

}
