package uk.ac.man.cs.comp38211.ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokeniser {
    // constructor is empty for now
    public Tokeniser() { }

    public ArrayList<String> tokenise(String line){
        // for now, just use white-space tokenisation.
        List<String> tokens =  Arrays.asList(line.split(" "));
        return new ArrayList<>(tokens);
    } // tokenise
} // Tokeniser
