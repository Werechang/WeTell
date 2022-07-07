package com.gebb.wetell.server;

public class RunArgumentsManager {
    private final String[] args;

    public RunArgumentsManager(String[] args) {
        this.args = args;
    }

    public boolean getOption(String arg) {
        for (String a : args) {
            if (arg.equalsIgnoreCase(a)) {
                return true;
            }
        }
        return false;
    }
}
