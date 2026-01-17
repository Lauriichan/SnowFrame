package me.lauriichan.snowframe.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.snowframe.util.color.HexParser;
import me.lauriichan.snowframe.util.color.SimpleColor;

public class ColorTest {

    @TestFactory
    public Stream<DynamicTest> testHexColor() {
        String[] colors = new String[] {
            "#F96EA3",
            "#C5FF00"
        };
        return Arrays.stream(colors).map(hexColor -> DynamicTest.dynamicTest("Test Hex Color: " + hexColor, () -> {
            SimpleColor color = SimpleColor.sRGB(hexColor);
            assertEquals(hexColor, color.asHex().toUpperCase());
        }));
    }

    @TestFactory
    public Iterable<DynamicTest> testHexParser() {
        String[] hexValue = new String[] {
            "F96EA3",
            "C5FF00"
        };
        int[] hexExpected = new int[] {
            0xF96EA3,
            0xC5FF00
        };
        ObjectArrayList<DynamicTest> tests = new ObjectArrayList<>(hexValue.length);
        for (int i = 0; i < hexValue.length; i++) {
            int fI = i;
            tests.add(DynamicTest.dynamicTest("Test Hex Value: " + hexValue[fI], () -> {
                int value = HexParser.parseHexInt(hexValue[fI]);
                int javaValue = Integer.parseInt(hexValue[fI], 16);
                assertEquals(javaValue, value);
                assertEquals(hexExpected[fI], value);
            }));
        }
        return tests;
    }

}
