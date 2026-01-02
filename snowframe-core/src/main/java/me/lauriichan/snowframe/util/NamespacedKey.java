package me.lauriichan.snowframe.util;

public final class NamespacedKey {

    private static boolean isValidNamespaceChar(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
    }

    private static boolean isValidKeyChar(int c) {
        return isValidNamespaceChar(c) || c == '/';
    }

    public static boolean isValidNamespace(String namespace) {
        return namespace != null && !namespace.isEmpty() && namespace.chars().allMatch(NamespacedKey::isValidNamespaceChar);
    }

    public static String expectValidNamespace(String namespace) {
        if (!isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace, has to be [a-zA-Z0-9._-]: %s".formatted(namespace));
        }
        return namespace;
    }

    public static String expectValidKey(String key) {
        if (!isValidKey(key)) {
            throw new IllegalArgumentException("Invalid key, has to be [a-zA-Z0-9/._-]: %s".formatted(key));
        }
        return key;
    }

    public static boolean isValidKey(String key) {
        return key != null && !key.isEmpty() && key.chars().allMatch(NamespacedKey::isValidKeyChar);
    }

    public static NamespacedKey of(String namespacedKey) {
        try {
            return parse(namespacedKey);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public static NamespacedKey of(String namespace, String key) {
        try {
            return new NamespacedKey(namespace, key);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public static NamespacedKey parse(String namespacedKey) {
        int index = namespacedKey.indexOf(':');
        if (index == -1) {
            throw new IllegalArgumentException("No key provided: %s".formatted(namespacedKey));
        }
        return new NamespacedKey(namespacedKey.substring(0, index), namespacedKey.substring(index + 1));
    }

    private final String namespace, key;
    private final String string;

    public NamespacedKey(String namespace, String key) {
        this.namespace = expectValidNamespace(namespace).toLowerCase();
        this.key = expectValidKey(key).toLowerCase();
        this.string = "%s:%s".formatted(this.namespace, this.key);
    }

    public final String namespace() {
        return namespace;
    }

    public final String key() {
        return key;
    }

    @Override
    public final int hashCode() {
        return string.hashCode();
    }

    @Override
    public final String toString() {
        return string;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NamespacedKey keyObj) {
            return keyObj.namespace.equals(namespace) && keyObj.key.equals(key);
        }
        if (obj instanceof String str) {
            return str.equals(string);
        }
        return false;
    }

}
