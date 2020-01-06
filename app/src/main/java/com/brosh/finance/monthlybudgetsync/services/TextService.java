package com.brosh.finance.monthlybudgetsync.services;

public final class TextService {
    public static String getSentenceCapitalLetter(String sentence,char separator) {
        if(sentence.indexOf(separator) == -1)
            return getWordCapitalLetter(sentence);
        return new String(getWordCapitalLetter(sentence.substring(0,sentence.indexOf(separator)+1)) + getSentenceCapitalLetter(sentence.substring(sentence.indexOf(separator)+1),separator));
    }

    public static String getWordCapitalLetter(String word) {
        char firstLetter = word.charAt(0);
        if(firstLetter >= 97 && firstLetter <= 122)
            firstLetter -= 32;
        return new String(firstLetter + word.substring(1));
    }

    public static String getSeperator() {
        return "->";
    }
}
