/**
 * Basic Inverted Index
 * 
 * This Map Reduce program should build an Inverted Index from a set of files.
 * Each token (the key) in a given file should reference the file it was found 
 * in. 
 * 
 * The output of the program should look like this:
 * sometoken [file001, file002, ... ]
 * 
 * @author Kristian Epps
 */
package uk.ac.man.cs.comp38211.exercise;

import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import uk.ac.man.cs.comp38211.io.array.ArrayListWritable;
import uk.ac.man.cs.comp38211.ir.Stemmer;
import uk.ac.man.cs.comp38211.ir.StopAnalyser;
import uk.ac.man.cs.comp38211.util.XParser;

public class BasicInvertedIndex extends Configured implements Tool
{
    private static final Logger LOG = Logger
            .getLogger(BasicInvertedIndex.class);

    public static class Map extends 
            Mapper<Object, Text, Text, Text>
    {

        // INPUTFILE holds the name of the current file
        private final static Text INPUTFILE = new Text();
        
        // TOKEN should be set to the current token rather than creating a 
        // new Text object for each one
        @SuppressWarnings("unused")
        private final static Text TOKEN = new Text();

        // The StopAnalyser class helps remove stop words
        @SuppressWarnings("unused")
        private final StopAnalyser stopAnalyser = new StopAnalyser();

        // The tokeniser class helps split line into tokens.
        // The stem method wraps the functionality of the Stemmer
        // class, which trims extra characters from English words
        // Please refer to the Stemmer class for more comments
        @SuppressWarnings("unused")
        private String stem(String word)
        {
            Stemmer s = new Stemmer();

            // A char[] word is added to the stemmer with its length,
            // then stemmed
            s.add(word.toCharArray(), word.length());
            s.stem();

            // return the stemmed char[] word as a string
            return s.toString();
        }

        private String[] tokenise(String line){
            //use naive tokenisation for now
            return line.split(" ");
        }
        
        // This method gets the name of the file the current Mapper is working
        // on
        @Override
        public void setup(Context context)
        {
            String inputFilePath = ((FileSplit) context.getInputSplit()).getPath().toString();
            String[] pathComponents = inputFilePath.split("/");
            INPUTFILE.set(pathComponents[pathComponents.length - 1]);
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException
        {
            // This Mapper should read in a line, convert it to a set of tokens
            // and output each token with the name of the file it was found in
            // i.e. emit a term as a key and a doc_id as a value (emit (Modified term, doc_id) pairs
            // here, note that "Object key" is a byte offset of the value.
            // get a doc_id, for which we shall use the input file name.
            String doc_id = ((FileSplit) context.getInputSplit()).getPath().getName();
            // get the line
            String line = value.toString();
            // tokenise the line into terms
            for (String term: tokenise(line)) {
                // O(n), where n is the length of each line. -> could we do better than this?
                // emit (modified_term, doc_id) pair
                // both of them are characters
                context.write(new Text(term), new Text(doc_id));
            } // for loop
        } // map
    } // mapper

    public static class Reduce extends Reducer<Text, Text, Text, ArrayListWritable<Text>>
    {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // code adapted from: https://acadgild.com/blog/building-inverted-index-mapreduce
            // This Reduce Job should take in a key and an iterable of file names
            // It should convert this iterable to a writable array list and output
            // it along with the key
            // here, the key = a term
            // values = an iterable of doc_id (file name)
            // ideally, you would want to compute..
            //compute term_freq & doc_freq, so that you can later use them to compute tf * idf
            HashMap<String, Integer> term_freq = new HashMap<>();  // (doc_id -> term_freq)
            String doc_id; // to be used in the loops
            HashSet<String> doc_id_set = new HashSet<>(); // for getting unique set of docs for a specific term
            ArrayListWritable<Text> postings_list =  new ArrayListWritable<>();
            for (Text t: values) {
                doc_id = t.toString();
                doc_id_set.add(doc_id);
                // add doc_ids to this first (term_freq will be added later)
                postings_list.add(new Text(doc_id));
                // update term_freq
                int to_put = term_freq.getOrDefault(doc_id, 0) + 1;
                term_freq.put(doc_id, to_put);
                // update doc_freq
            } // for values - O(N)
            // sort the postings
            Collections.sort(postings_list);
            // update each doc_id with term_freq. (later to be used for tfidf)
            for (int idx = 0; idx < postings_list.size(); idx++) {
                doc_id = postings_list.get(idx).toString();
                postings_list.set(idx, new Text(doc_id + "|" + term_freq.get(doc_id)));
            }
            // doc_freq is the size of the set
            int doc_freq = doc_id_set.size();
            // now, array list of what..? array list of (doc_id, term_freq)?
            Text key_to_write = new Text(key.toString() + "|" + doc_freq);
            // sort the postings
            context.write(key_to_write, postings_list);
        } // reduce
    } // Reduce

    // Lets create an object! :)
    public BasicInvertedIndex()
    {
    }

    // Variables to hold cmd line args
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String NUM_REDUCERS = "numReducers";

    @SuppressWarnings({ "static-access" })
    public int run(String[] args) throws Exception
    {
        
        // Handle command line args
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("input path").create(INPUT));
        options.addOption(OptionBuilder.withArgName("path").hasArg()
                .withDescription("output path").create(OUTPUT));
        options.addOption(OptionBuilder.withArgName("num").hasArg()
                .withDescription("number of reducers").create(NUM_REDUCERS));

        CommandLine cmdline = null;
        CommandLineParser parser = new XParser(true);

        try
        {
            cmdline = parser.parse(options, args);
        }
        catch (ParseException exp)
        {
            System.err.println("Error parsing command line: "
                    + exp.getMessage());
            System.err.println(cmdline);
            return -1;
        }

        // If we are missing the input or output flag, let the user know
        if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT))
        {
            System.out.println("args: " + Arrays.toString(args));
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(120);
            formatter.printHelp(this.getClass().getName(), options);
            ToolRunner.printGenericCommandUsage(System.out);
            return -1;
        }

        // Create a new Map Reduce Job
        Configuration conf = new Configuration();
        Job job = new Job(conf);
        String inputPath = cmdline.getOptionValue(INPUT);
        String outputPath = cmdline.getOptionValue(OUTPUT);
        int reduceTasks = cmdline.hasOption(NUM_REDUCERS) ? Integer
                .parseInt(cmdline.getOptionValue(NUM_REDUCERS)) : 1;

        // Set the name of the Job and the class it is in
        job.setJobName("Basic Inverted Index");
        job.setJarByClass(BasicInvertedIndex.class);
        job.setNumReduceTasks(reduceTasks);
        
        // Set the Mapper and Reducer class (no need for combiner here)
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        
        // Set the Output Classes
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ArrayListWritable.class);

        // Set the input and output file paths
        FileInputFormat.setInputPaths(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        // Time the job whilst it is running
        long startTime = System.currentTimeMillis();
        job.waitForCompletion(true);
        LOG.info("Job Finished in " + (System.currentTimeMillis() - startTime)
                / 1000.0 + " seconds");

        // Returning 0 lets everyone know the job was successful
        return 0;
    }

    public static void main(String[] args) throws Exception
    {
        ToolRunner.run(new BasicInvertedIndex(), args);
    }
}
