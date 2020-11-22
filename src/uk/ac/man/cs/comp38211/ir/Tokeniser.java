package uk.ac.man.cs.comp38211.ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokeniser {
    // tokeniser uses stop analyzer and stemmer to tokenise lines
    // but note that this is just for initialising the list
    // The StopAnalyser class helps remove stop words
    private final static StopAnalyser stopAnalyser = new StopAnalyser();

    private static String stem(String word) {
        Stemmer s = new Stemmer();
        // A char[] word is added to the stemmer with its length,
        // then stemmed
        s.add(word.toCharArray(), word.length());
        s.stem();

        // return the stemmed char[] word as a string
        return s.toString();
    } // stem

    public static ArrayList<String> tokenise(String line){
        line = line.toLowerCase();
        // for now, just use white-space tokenisation.
        List<String> tokens =  Arrays.asList(line.split(" "));
        return new ArrayList<>(tokens);
    } // tokenise
} // Tokeniser
