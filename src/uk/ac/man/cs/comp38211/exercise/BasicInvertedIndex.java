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
import uk.ac.man.cs.comp38211.ir.Tokeniser;
import uk.ac.man.cs.comp38211.util.XParser;

public class BasicInvertedIndex extends Configured implements Tool {
    private static final Logger LOG = Logger
            .getLogger(BasicInvertedIndex.class);

    public static class Map extends 
            Mapper<Object, Text, Text, Text> {

        // INPUTFILE holds the name of the current file
        private final static Text INPUT_FILE = new Text();
        // TOKEN should be set to the current token rather than creating a 
        // new Text object for each one
        private final static Text TOKEN = new Text();

        private ArrayList<String> tokenise(String line){
            //use naive tokenisation for now
            return Tokeniser.tokenise(line);
        }
        
        // This method gets the name of the file the current Mapper is working
        // on
        @Override
        public void setup(Context context) {
            String inputFilePath = ((FileSplit) context.getInputSplit()).getPath().toString();
            String[] pathComponents = inputFilePath.split("/");
            INPUT_FILE.set(pathComponents[pathComponents.length - 1]);
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            TOKEN.clear();
            // This Mapper should read in a line, convert it to a set of tokens
            // and output each token with the name of the file it was found in
            // i.e. emit a term as a key and a doc_id as a value (emit (Modified term, doc_id) pairs
            // here, note that "Object key" is a byte offset of the value.
            // get a doc_id, for which we shall use the input file name.
            // get the line
            String line = value.toString();
            // tokenise the line into terms
            for (String term: tokenise(line)) {
                // O(n), where n is the length of each line. -> could we do better than this?
                // emit (modified_term, doc_id) pair
                // both of them are characters
                TOKEN.set(term);
                context.write(TOKEN, INPUT_FILE);
            } // for loop
        } // map
    } // mapper

    public static class Reduce extends Reducer<Text, Text, Text, ArrayListWritable<Text>> {
        // TERM_FREQ should be set to the current term freq for a given doc
        private final static HashMap<String, Integer> TERM_FREQ = new HashMap<>();
        // POSTINGS_LIST should be set to the current posting list for a given term
        private final static ArrayListWritable<Text> POSTINGS_LIST = new ArrayListWritable<>();
        // DOC_ID_SET should be set to the current set of unique doc ids for a given term
        // this is needed to compute DOC_FREQ
        private final static HashSet<String> DOC_ID_SET = new HashSet<>();
        //TOKEN_WITH_DOC_FREQ
        private final static Text TOKEN_WITH_DOC_FREQ = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // code adapted from: https://acadgild.com/blog/building-inverted-index-mapreduce
            // This Reduce Job should take in a key and an iterable of file names
            // It should convert this iterable to a writable array list and output
            // it along with the key
            // here, the key = a term
            // values = an iterable of doc_id (file name)
            // ideally, you would want to compute..
            //compute term_freq & doc_freq, so that you can later use them to compute tf * idf
            // clear all the static variables before start.
            TERM_FREQ.clear();
            POSTINGS_LIST.clear();
            DOC_ID_SET.clear();
            TOKEN_WITH_DOC_FREQ.clear();

            // iterate over file names
            //DOC_ID - just an alias for the current file name. to be used when iterating over values
            String DOC_ID;
            for (Text t: values) {
                DOC_ID = t.toString();
                DOC_ID_SET.add(DOC_ID);
                // add doc_ids to this first (term_freq will be added later)
                POSTINGS_LIST.add(new Text(DOC_ID));
                // update term_freq
                TERM_FREQ.put(DOC_ID, TERM_FREQ.getOrDefault(DOC_ID, 0) + 1);
                // update doc_freq
            } // for values - O(N)
            // sort the postings
            Collections.sort(POSTINGS_LIST);
            // update each doc_id with term_freq. (later to be used for tfidf)
            for (int idx = 0; idx < POSTINGS_LIST.size(); idx++) {
                DOC_ID = POSTINGS_LIST.get(idx).toString();
                POSTINGS_LIST.set(idx, new Text(DOC_ID + "|"
                                                + TERM_FREQ.get(DOC_ID)));
            }
            // doc_freq is the size of the set
            // DOC_FREQ should be set to the current document freq for a given term
            int DOC_FREQ = DOC_ID_SET.size();
            // now, array list of what..? array list of (doc_id, term_freq)?
            TOKEN_WITH_DOC_FREQ.set(key.toString() + "|" + DOC_FREQ);
            // sort the postings
            context.write(TOKEN_WITH_DOC_FREQ, POSTINGS_LIST);
        } // reduce
    } // Reduce

    // Lets create an object! :)
    public BasicInvertedIndex() { }

    // Variables to hold cmd line args
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String NUM_REDUCERS = "numReducers";

    @SuppressWarnings({ "static-access" })
    public int run(String[] args) throws Exception {
        
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

        try {
            cmdline = parser.parse(options, args);
        }
        catch (ParseException exp) {
            System.err.println("Error parsing command line: "
                    + exp.getMessage());
            System.err.println(cmdline);
            return -1;
        }

        // If we are missing the input or output flag, let the user know
        if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
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

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new BasicInvertedIndex(), args);
    } //  main
} // BasicInvertedIndex
