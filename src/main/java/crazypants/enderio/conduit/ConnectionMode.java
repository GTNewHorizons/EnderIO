package crazypants.enderio.conduit;

import crazypants.enderio.EnderIO;

public enum ConnectionMode {

    IN_OUT("gui.conduit.ioMode.inOut"),
    INPUT("gui.conduit.ioMode.input"),
    OUTPUT("gui.conduit.ioMode.output"),
    DISABLED("gui.conduit.ioMode.disabled"),
    NOT_SET("gui.conduit.ioMode.notSet");

    /**
     * Cached values() array for frequent read-only operations, the array should NOT be mutated.
     */
    public static final ConnectionMode[] VALUES = values();
    private final String unlocalisedName;

    ConnectionMode(String unlocalisedName) {
        this.unlocalisedName = unlocalisedName;
    }

    public String getUnlocalisedName() {
        return unlocalisedName;
    }

    public static ConnectionMode getNext(ConnectionMode mode) {
        int ord = mode.ordinal() + 1;
        if (ord >= ConnectionMode.VALUES.length) {
            ord = 0;
        }
        return ConnectionMode.VALUES[ord];
    }

    public static ConnectionMode getPrevious(ConnectionMode mode) {

        int ord = mode.ordinal() - 1;
        if (ord < 0) {
            ord = ConnectionMode.VALUES.length - 1;
        }
        return ConnectionMode.VALUES[ord];
    }

    public boolean acceptsInput() {
        return this == IN_OUT || this == INPUT;
    }

    public boolean acceptsOutput() {
        return this == IN_OUT || this == OUTPUT;
    }

    public String getLocalisedName() {
        return EnderIO.lang.localize(unlocalisedName);
    }
}
