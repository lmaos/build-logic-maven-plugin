package com.clmcat.maven.plugins.action.compare.match;



import java.util.HashMap;
import java.util.Map;

public class MatchConst {

    public static final char[] MATCH_WORDS = {'|', '&', '!', '>', '<', '=', '!', '(', ')'};
    public static final String[] MATCH_SYMBOLS = {"!=", ">", "<", "==", ">=", "<="};


    public static final Map<Character, Integer> MATCH_WORDS_INDEX =  new HashMap<>();
    static {
        for (int i = 0; i < MATCH_WORDS.length; i++) {
            MATCH_WORDS_INDEX.put(MATCH_WORDS[i], i);
        }
    }
}

