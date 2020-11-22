package uk.ac.man.cs.comp38211.ir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopAnalyser
{

    private static List<String> stopWords = new ArrayList<String>();

    public StopAnalyser()
    {
        init();
    }

    public static List<String> getStopWords()
    {
        return stopWords;
    }

    public static Boolean isStopWord(String word)
    {
        List<String> words = Collections.synchronizedList(stopWords);
        return words.contains(word);
    }

    private void init()
    {
        String stopwords = "./lib/stopwords.txt";
        BufferedReader in = null;
        String stopWord;

        // List<String> words = Collections.synchronizedList(new ArrayList<String>());
        
        try
        {
            in = new BufferedReader(new FileReader(stopwords));

            while ((stopWord = in.readLine()) != null)
            {
                stopWords.add(stopWord);
            }
        }
        catch (IOException e)
        {
            System.err.println("Sentiment analysis term file ./lib/stopwords.txt lost");
        }
        /*
        synchronized(words)
        {
            Iterator<String> i = words.iterator();
            while(i.hasNext())
                stopWords.add(i.next());
        }
        */

    }

}
