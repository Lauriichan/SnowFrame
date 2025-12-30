package me.lauriichan.snowframe;

import java.util.Objects;

import me.lauriichan.snowframe.signal.ISignal;

public record WindowConfiguration(String title, int width, int height, boolean fullscreen, boolean borderless, boolean transparent) {
    
    public static final class Signal implements ISignal {

        private String title = "Application";
        private int width = 1280, height = 720;
        private boolean fullscreen = false, borderless = false, transparent = false;

        public WindowConfiguration asConfiguration() {
            return new WindowConfiguration(title, width, height, fullscreen, borderless, transparent);
        }

        public String title() {
            return title;
        }

        public void title(String title) {
            Objects.requireNonNull(title, "Title can not be null");
            if (title.isBlank()) {
                throw new IllegalArgumentException("Title can not be blank");
            }
            this.title = title;
        }

        public int width() {
            return width;
        }

        public void width(int width) {
            if (width <= 0) {
                throw new IllegalArgumentException("Width has to be positive");
            }
            this.width = width;
        }

        public int height() {
            return height;
        }

        public void height(int height) {
            if (height <= 0) {
                throw new IllegalArgumentException("Height has to be positive");
            }
            this.height = height;
        }

        public boolean fullscreen() {
            return fullscreen;
        }

        public void fullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
        }

        public boolean borderless() {
            return borderless;
        }

        public void borderless(boolean borderless) {
            this.borderless = borderless;
        }

        public boolean transparent() {
            return transparent;
        }

        public void transparent(boolean transparent) {
            this.transparent = transparent;
        }
        
    }

}
