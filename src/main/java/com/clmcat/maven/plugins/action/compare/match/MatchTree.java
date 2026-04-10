package com.clmcat.maven.plugins.action.compare.match;

public class MatchTree {


    private MatchNode root = new MatchNode('\0');



    public static MatchTree getLogicMatchTree() {
        MatchTree matchTree = new MatchTree();
        matchTree.add("&&");
        matchTree.add("||");
        return  matchTree;
    }

    public static MatchTree getSymboMatchTree() {
        MatchTree matchTree = new MatchTree();
        for (String matchSymbol : MatchConst.MATCH_SYMBOLS) {
            matchTree.add(matchSymbol);
        }
        return  matchTree;
    }

    // Add a match text to the trie
    private void add(String text) {
        MatchNode node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            node = node.getOrCreateNext(c);
        }
        node.setText(text);
    }

    public String match(String test, int offset, int end) {
        MatchNode next = root;
        MatchNode result = null;
        for (int i = offset; i < test.length() && i < end; i++) {
            char c = test.charAt(i);
            MatchNode matchNode = next.getNext(c);
            if (matchNode == null) {
                break;
            } else if (matchNode.getText() != null) {
                result = matchNode;
            } else {
                next = matchNode;
            }
        }
        return result != null ? result.getText() : null;
    }

    public MatchResult matchFristResult(String test, int offset, int end) {
        for (int i = offset; i < test.length() && i < end; i++) {
            String match = match(test, i, end);
            if (match != null) {
                return new MatchResult(match, i, match.length());
            }
        }
        return null;
    }

}
