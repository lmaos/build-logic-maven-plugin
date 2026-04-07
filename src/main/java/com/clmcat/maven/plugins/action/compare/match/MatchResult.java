package com.clmcat.maven.plugins.action.compare.match;

public class MatchResult {
    private String match;
    private int start;
    private int length;

    public MatchResult(String match, int start, int length) {
        this.match = match;
        this.start = start;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getStart() {
        return start;
    }
    public String getMatch() {
        return match;
    }

    public int getEnd() {
        return start + length;
    }
}
