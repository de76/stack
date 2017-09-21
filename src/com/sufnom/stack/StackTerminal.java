package com.sufnom.stack;

import java.util.HashMap;

public class StackTerminal {
    private static final StackTerminal session = new StackTerminal();
    public static StackTerminal getSession() { return session; }

    private HashMap<String, Object> connectedStacks;
}
