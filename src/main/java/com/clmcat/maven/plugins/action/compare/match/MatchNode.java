package com.clmcat.maven.plugins.action.compare.match;

public class MatchNode {
    private char word;
    private String text;
    private MatchNode nexts[] = new MatchNode[MatchConst.MATCH_WORDS.length];

    public MatchNode(char word) {
        this.word = word;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }

    public MatchNode getNext(char word) {
        Integer i = MatchConst.MATCH_WORDS_INDEX.get(word);
        if (i == null) return null;
        return nexts[i];
    }

    public MatchNode getOrCreateNext(char word) {
        MatchNode next = getNext(word);
        if (next == null) {
            next = new MatchNode(word);
            nexts[MatchConst.MATCH_WORDS_INDEX.get(word)] = next;
        }
        return next;
    }
}
