package me.lauriichan.snowframe.util.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.lauriichan.laylib.logger.AbstractSimpleLogger;
import me.lauriichan.laylib.logger.util.StringUtil;

public final class FileLogger extends AbstractSimpleLogger {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("u-MM-d", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:m:s", Locale.ENGLISH);
    private static final String ZIP_FORMAT = "%s_%s.zip";

    private static final String INFO_FORMAT = "[%s][INFO/%s] %s";
    private static final String WARN_FORMAT = "[%s][WARN/%s] %s";
    private static final String ERROR_FORMAT = "[%s][ERROR/%s] %s";
    private static final String DEBUG_FORMAT = "[%s][DEBUG/%s] %s";
    private static final String TRACK_FORMAT = "[%s][TRACK/%s] %s";

    private final PrintStream fileOut;
    private final File file;

    public FileLogger(final File directory) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            this.file = new File(directory, "latest.log");
            if (file.exists()) {
                file.delete();
            }
            this.fileOut = new PrintStream(new FileOutputStream(file), true, StandardCharsets.UTF_8);
        } catch (IOException exp) {
            throw new IllegalStateException("Couldn't create file output", exp);
        }
    }

    public void close() {
        fileOut.close();
        File parent = file.getParentFile();
        if (!file.exists() || !parent.exists()) {
            return;
        }
        File zipFile = null;
        int index = 0;
        String date = DATE_FORMATTER.format(LocalDateTime.now());
        while (zipFile == null || zipFile.exists()) {
            zipFile = new File(parent, ZIP_FORMAT.formatted(date, index++));
        }
        try (ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(zipFile.getName().substring(0, zipFile.getName().length() - 4));
            entry.setSize(file.length());
            zipOutput.putNextEntry(entry);
            byte[] buf = new byte[32768];
            try (FileInputStream fileInput = new FileInputStream(file)) {
                int length;
                while ((length = fileInput.read(buf)) != -1) {
                    zipOutput.write(buf, 0, length);
                }
            }
            zipOutput.closeEntry();
        } catch (IOException e) {
            System.err.println("Failed to zip latest log");
            System.err.println(StringUtil.stackTraceToString(e));
        }
    }

    @Override
    protected void info(String message) {
        message = INFO_FORMAT.formatted(TIME_FORMATTER.format(LocalTime.now()), Thread.currentThread().getName(), message);
        System.out.println(message);
        fileOut.println(message);
    }

    @Override
    protected void warning(String message) {
        message = WARN_FORMAT.formatted(TIME_FORMATTER.format(LocalTime.now()), Thread.currentThread().getName(), message);
        System.err.println(message);
        fileOut.println(message);
    }

    @Override
    protected void error(String message) {
        message = ERROR_FORMAT.formatted(TIME_FORMATTER.format(LocalTime.now()), Thread.currentThread().getName(), message);
        System.err.println(message);
        fileOut.println(message);
    }

    @Override
    protected void track(String message) {
        message = TRACK_FORMAT.formatted(TIME_FORMATTER.format(LocalTime.now()), Thread.currentThread().getName(), message);
        System.out.println(message);
        fileOut.println(message);
    }

    @Override
    protected void debug(String message) {
        message = DEBUG_FORMAT.formatted(TIME_FORMATTER.format(LocalTime.now()), Thread.currentThread().getName(), message);
        System.out.println(message);
        fileOut.println(message);
    }

}
