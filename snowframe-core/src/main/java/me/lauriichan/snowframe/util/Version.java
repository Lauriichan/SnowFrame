package me.lauriichan.snowframe.util;

public final class Version implements Comparable<Version> {

    public static Version parse(String string) {
        if (string.startsWith("v")) {
            string = string.substring(1);
        }
        int[] components = new int[] {
            -1,
            -1,
            -1,
            -1
        };
        StringBuilder builder = new StringBuilder();
        char ch;
        int component = 0;
        boolean minusPreviously = false;
        try {
            for (int i = 0; i < string.length(); i++) {
                ch = string.charAt(i);
                if (Character.isDigit(ch)) {
                    builder.append(ch);
                    minusPreviously = false;
                    continue;
                }
                if (ch != '.' && ch != '-') {
                    if (minusPreviously && ch == 'R') {
                        minusPreviously = false;
                        continue;
                    }
                    throw new IllegalArgumentException("Unsupported character '%s' found at index %s".formatted(ch, i));
                }
                if (component == 4) {
                    throw new IllegalArgumentException("Too many components");
                }
                components[component] = Integer.parseInt(builder.toString());
                builder = new StringBuilder();
                if (ch == '.') {
                    component++;
                } else if (ch == '-') {
                    component = 3;
                    minusPreviously = true;
                }
            }
            if (!builder.isEmpty()) {
                components[component] = Integer.parseInt(builder.toString());
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                "Failed to parse number component at index %s: '%s'".formatted(component, builder.toString()));
        }
        return new Version(components[0], components[1], components[2], components[3]);
    }

    public final int major, minor, patch, revision;

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, -1);
    }

    public Version(int major, int minor, int patch, int revision) {
        if (major < 0) {
            throw new IllegalArgumentException("Major is not allowed to be below 0");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor is not allowed to be below 0");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.revision = revision;
    }

    public Version add(int major, int minor, int patch) {
        return add(major, minor, patch, 0);
    }

    public Version add(int major, int minor, int patch, int revision) {
        return new Version(this.major + major, this.minor + minor, this.patch + patch, this.revision + revision);
    }

    public boolean isHigher(Version version) {
        return compareTo(version) > 0;
    }

    public boolean isSame(Version version) {
        return compareTo(version) == 0;
    }

    public boolean isLower(Version version) {
        return compareTo(version) < 0;
    }

    @Override
    public int compareTo(Version o) {
        int tmp = Integer.compare(major, o.major);
        if (tmp != 0) {
            return tmp;
        }
        tmp = Integer.compare(minor, o.minor);
        if (tmp != 0) {
            return tmp;
        }
        tmp = Integer.compare(patch, o.patch);
        if (tmp != 0) {
            return tmp;
        }
        return Integer.compare(revision, o.revision);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
            || ((obj instanceof Version ver) && ver.major == major && ver.minor == minor && ver.patch == patch && ver.revision == revision);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(major).append('.').append(minor);
        if (patch >= -1) {
            builder.append('.').append(patch);
        }
        if (revision > -1) {
            builder.append("-R").append(revision);
        }
        return builder.toString();
    }

}
