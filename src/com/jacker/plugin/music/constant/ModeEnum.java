package com.jacker.plugin.music.constant;

public enum ModeEnum {
    LIST(0),
    SINGLE(1),
    RANDOM(2),
    ;

    int mode;

    ModeEnum(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static ModeEnum getNexMode(ModeEnum currentMode) {
        switch (currentMode) {
            case LIST:
                return SINGLE;
            case SINGLE:
                return RANDOM;
            case RANDOM:
                return LIST;
        }
        return LIST;
    }
}
