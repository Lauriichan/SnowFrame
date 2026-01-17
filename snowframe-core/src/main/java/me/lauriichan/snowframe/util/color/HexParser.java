package me.lauriichan.snowframe.util.color;

public final class HexParser {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private HexParser() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Turns an integer to a hex string
     * 
     * @param  value the integer to turn to a hex string
     * 
     * @return       the hex string
     */
    public static String asHex(int value) {
        return asHex(value, 0);
    }

    /**
     * Turns an integer to a hex string
     * 
     * @param  value    the integer to turn to a hex string
     * @param  minDigit the amount of minimum digits to represent in the string
     * 
     * @return          the hex string
     */
    public static String asHex(int value, int minDigit) {
        StringBuilder builder = new StringBuilder();
        appendHex(builder, value, minDigit);
        return builder.toString();
    }

    /**
     * Turns a long to a hex string
     * 
     * @param  value the long to turn to a hex string
     * 
     * @return       the hex string
     */
    public static String asHex(long value) {
        return asHex(value, 0);
    }

    /**
     * Turns a long to a hex string
     * 
     * @param  value    the long to turn to a hex string
     * @param  minDigit the amount of minimum digits to represent in the string
     * 
     * @return          the hex string
     */
    public static String asHex(long value, int minDigit) {
        StringBuilder builder = new StringBuilder();
        appendHex(builder, value, minDigit);
        return builder.toString();
    }

    /**
     * Appends an integer in hex representation to the provided
     * {@link StringBuilder}
     * 
     * @param value the integer to append in hex representation
     */
    public static void appendHex(StringBuilder builder, int value) {
        appendHex(builder, value, 0);
    }

    /**
     * Appends an integer in hex representation to the provided
     * {@link StringBuilder}
     * 
     * @param value    the integer to append in hex representation
     * @param minDigit the amount of minimum digits to represent in the string
     */
    public static void appendHex(StringBuilder builder, int value, int minDigits) {
        minDigits = Math.clamp(minDigits, 0, 12);
        int tmp, count = 0;
        StringBuilder hex = new StringBuilder();
        while (value != 0 || count < minDigits) {
            tmp = value & 0xF;
            value = value >> 4;
            hex.insert(0, HEX_DIGITS[tmp]);
            count++;
        }
        builder.append(hex);
    }

    /**
     * Appends a long in hex representation to the provided {@link StringBuilder}
     * 
     * @param value the long to append in hex representation
     */
    public static void appendHex(StringBuilder builder, long value) {
        appendHex(builder, value, 0);
    }

    /**
     * Appends a long in hex representation to the provided {@link StringBuilder}
     * 
     * @param value    the long to append in hex representation
     * @param minDigit the amount of minimum digits to represent in the string
     */
    public static void appendHex(StringBuilder builder, long value, int minDigits) {
        minDigits = Math.clamp(minDigits, 0, 24);
        int tmp, count = 0;
        StringBuilder hex = new StringBuilder();
        while (value != 0 || count++ < minDigits) {
            tmp = (int) (value & 0xF);
            value = value >> 4;
            hex.insert(0, HEX_DIGITS[tmp]);
        }
        builder.append(hex);
    }

    /**
     * Parses a hex string to an integer
     * 
     * @param  string                the string to parse
     * 
     * @return                       the parsed integer
     * 
     * @throws NumberFormatException if the string is too long or an invalid
     *                                   character is contained within the string
     */
    public static int parseHexInt(String string) {
        char[] chars = string.toCharArray();
        int value = 0;
        boolean leading = true;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (leading) {
                if (ch == '0') {
                    continue;
                }
                if (i > 11) {
                    throw new NumberFormatException("Hex string too long to parse");
                }
                leading = false;
            }
            value <<= 4;
            if (ch >= 65 && ch <= 70) {
                value |= (ch - 55);
                continue;
            }
            if (ch >= 48 && ch <= 57) {
                value |= (ch - 48);
                continue;
            }
            if (ch >= 97 && ch <= 102) {
                value |= (ch - 87);
                continue;
            }
            throw new NumberFormatException("Invalid character at index " + i + ": " + ch);
        }
        return value;
    }

    /**
     * Parses a hex string to an integer without throwing any exceptions
     * 
     * If there are more characters than allowed all leading characters will be
     * skipped until there are only 12 left
     * 
     * If these 12 characters contain any invalid characters the invalid characters
     * will be evaluated as 0
     * 
     * @param  string the string to parse
     * 
     * @return        the parsed integer
     */
    public static int parseHexIntLenient(String string) {
        return parseHexIntLenient(string, false);
    }

    /**
     * Parses a hex string to an integer without throwing any exceptions
     * 
     * If there are more characters than allowed all leading characters will be
     * skipped until there are only 12 left
     * 
     * If these 12 characters contain any invalid characters the invalid characters
     * will be evaluated as 0 or disregarded and therefore skipped if
     * {@link skipInvalid} is set to {@code true}
     * 
     * @param  string      the string to parse
     * @param  skipInvalid if invalid characters should be disregarded or be
     *                         evaluated as 0
     * 
     * @return             the parsed integer
     */
    public static int parseHexIntLenient(String string, boolean skipInvalid) {
        char[] chars = string.toCharArray();
        int value = 0;
        boolean leading = true;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (leading) {
                if (ch == '0' || i > 11) {
                    continue;
                }
                leading = false;
            }
            if (ch >= 65 && ch <= 70) {
                value <<= 4;
                value |= (ch - 55);
                continue;
            }
            if (ch >= 48 && ch <= 57) {
                value <<= 4;
                value |= (ch - 48);
                continue;
            }
            if (ch >= 97 && ch <= 102) {
                value <<= 4;
                value |= (ch - 87);
                continue;
            }
            if (!skipInvalid) {
                // If we don't skip invalid characters then we will consider them as 0
                value <<= 4;
            }
        }
        return value;
    }

    /**
     * Parses a hex string to an long
     * 
     * @param  string                the string to parse
     * 
     * @return                       the parsed long
     * 
     * @throws NumberFormatException if the string is too long or an invalid
     *                                   character is contained within the string
     */
    public static int parseHexLong(String string) {
        char[] chars = string.toCharArray();
        int value = 0;
        boolean leading = true;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (leading) {
                if (ch == '0') {
                    continue;
                }
                if (i > 23) {
                    throw new NumberFormatException("Hex string too long to parse");
                }
                leading = false;
            }
            value <<= 4;
            if (ch >= 65 && ch <= 70) {
                value |= (ch - 55);
                continue;
            }
            if (ch >= 48 && ch <= 57) {
                value |= (ch - 48);
                continue;
            }
            if (ch >= 97 && ch <= 102) {
                value |= (ch - 87);
                continue;
            }
            throw new NumberFormatException("Invalid character at index " + i + ": " + ch);
        }
        return value;
    }

    /**
     * Parses a hex string to an long without throwing any exceptions
     * 
     * If there are more characters than allowed all leading characters will be
     * skipped until there are only 24 left
     * 
     * If these 24 characters contain any invalid characters the invalid characters
     * will be evaluated as 0
     * 
     * @param  string the string to parse
     * 
     * @return        the parsed long
     */
    public static long parseHexLongLenient(String string) {
        return parseHexLongLenient(string, false);
    }

    /**
     * Parses a hex string to an long without throwing any exceptions
     * 
     * If there are more characters than allowed all leading characters will be
     * skipped until there are only 24 left
     * 
     * If these 24 characters contain any invalid characters the invalid characters
     * will be evaluated as 0 or disregarded and therefore skipped if
     * {@link skipInvalid} is set to {@code true}
     * 
     * @param  string      the string to parse
     * @param  skipInvalid if invalid characters should be disregarded or be
     *                         evaluated as 0
     * 
     * @return             the parsed long
     */
    public static long parseHexLongLenient(String string, boolean skipInvalid) {
        char[] chars = string.toCharArray();
        long value = 0;
        boolean leading = true;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (leading) {
                if (ch == '0' || i > 23) {
                    continue;
                }
                leading = false;
            }
            if (ch >= 65 && ch <= 70) {
                value <<= 4;
                value |= (ch - 55);
                continue;
            }
            if (ch >= 48 && ch <= 57) {
                value <<= 4;
                value |= (ch - 48);
                continue;
            }
            if (ch >= 97 && ch <= 102) {
                value <<= 4;
                value |= (ch - 87);
                continue;
            }
            if (!skipInvalid) {
                // If we don't skip invalid characters then we will consider them as 0
                value <<= 4;
            }
        }
        return value;
    }

}
