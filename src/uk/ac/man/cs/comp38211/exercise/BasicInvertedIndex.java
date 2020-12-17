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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
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

public class BasicInvertedIndex extends Configured implements Tool {
    private static final Logger LOG = Logger
            .getLogger(BasicInvertedIndex.class);
    // to init stopwords.txt
    private final static StopAnalyser stopAnalyser = new StopAnalyser();

    public static class Tokeniser {
        // tokeniser uses stop analyzer and stemmer to tokenise lines
        // but note that this is just for initialising the list
        // The StopAnalyser class helps remove stop words
        // the string to be used as the separator for tokenisation.
        // separator should include ? as well..?
        private final static String SEPARATOR = " ";

        private final static Pattern datePattern = Pattern.compile(
                "[0-9]{4}-[0-9]{2}-[0-9]{2}",
                Pattern.CASE_INSENSITIVE
        );
        private final static Pattern urlPattern = Pattern.compile(
                "http:|https:|\\.com",
                Pattern.CASE_INSENSITIVE
        );

        private final static Pattern alphabetPattern = Pattern.compile(
                "[a-zA-Z]+",
                Pattern.CASE_INSENSITIVE
        );

        private final static Pattern toDeletePattern = Pattern.compile(
                // TODO: but you're deleting "'s". Don't forget to fix this after you implement positional indexing.
                "[^a-zA-Z\\-]",
                Pattern.CASE_INSENSITIVE
        );

        public static ArrayList<String> tokenise(String line){
            // adopt a functional paradigm to process list of tokens in an elegant, easy-to-read way.
            return Arrays.stream(line.split(SEPARATOR)) // first split the line into tokens
                    .filter(Tokeniser::isNotDate) // domain-specific filter 1
                    .filter(Tokeniser::isNotUrl) // domain-specific filter 2
                    .filter(Tokeniser::isNotISBN) // domain-specific filter 3
                    .filter(Tokeniser::isNotPageNum) // domain-specific filter 4
                    .filter(Tokeniser::containsAlphabet) // domain-specific filter
                    .map(Tokeniser::cleanse) // clean up left & right side of the token
                    .map(Tokeniser::normaliseCase)  // normalise tokens to lowercase
                    .filter(Tokeniser::isNotStopWord) // filter out stop words
                    .map(Tokeniser::stem) // apply stemming after stopwords are filtered
                    .collect(Collectors.toCollection(ArrayList::new)); // collect as array list and return
        } // tokenise

        private static String stem(String word) {
            Stemmer s = new Stemmer();
            // A char[] word is added to the stemmer with its length,
            // then stemmed
            s.add(word.toCharArray(), word.length());
            s.stem();

            // return the stemmed char[] word as a string
            return s.toString();
        } //

        private static String normaliseCase(String token) {
            // for now, lower the case of all terms
            // could work on conditional folding?
            return token.toLowerCase();
        }

        // domain-specific filters
        private static boolean isNotDate(String token) {
            // e.g.:
            // 2009-06-13.?
            // (1992-02-20).|1	[Bart_the_Lover.txt.gz|1]
            // (1997),|1	[Bart_the_General.txt.gz|1]
            // any four-digit numbers as well. (might be important (e.g.
            // bart mentionining a novel 1984, or 1940s), but
            // but make a distinction between
            // making a compromise here.
            return ! datePattern.matcher(token).find();
        }
        private static boolean isNotUrl(String token) {
            // e.g.:
            // http://www.npr.org/templates/story/story.php?storyid=4249835.|1	[Bart_the_Mother.txt.gz|1]
            // and also:
            // tvshowsondvd.com.|2	[Bart_the_General.txt.gz|1, Bart_the_Genius.txt.gz|1]
            // tv.com|4	[Bart_the_Fink.txt.gz|1, Bart_the_Genius.txt.gz|1, Bart_the_Mother.txt.gz|1, Bart_the_Murderer.txt.gz|1]
            // ugo.com.|1	[Bart_the_Genius.txt.gz|1]
            return ! urlPattern.matcher(token).find();
        }


        private static boolean isNotISBN(String token) {
            // e.g.
            // isbn?0-00-638898-1.?|4	[Bart_the_Fink.txt.gz|1, Bart_the_General.txt.gz|1, Bart_the_Genius.txt.gz|1, Bart_the_Murderer.txt.gz|1]
            // isbn?0-679-31318-4.?|1	[Bart_the_Genius.txt.gz|1]
            // isbn?978-0-306-81341-2.?|1	[Bart_the_Fink.txt.gz|1]
            // isbn?978-0-415-96917-8.?|1	[Bart_the_Fink.txt.gz|1]
            // isbn?978-0-8126-9433-8.?|1
            // there is no way "my boss" would search for isbn
            return !token.startsWith("isbn");
        }
        private static boolean isNotPageNum(String token) {
            // e.g.
            //p.|2	[Bart_the_Genius.txt.gz|1, Bart_the_Lover.txt.gz|1]
            //p.?136.|1	[Bart_the_Fink.txt.gz|1]
            //p.?18.|1	[Bart_the_Genius.txt.gz|1]
            //p.?195.|1	[Bart_the_Fink.txt.gz|1]
            //p.?21.|1	[Bart_the_General.txt.gz|1]
            //p.?36d.?|1	[Bart_the_Fink.txt.gz|1]
            return !token.startsWith("p.");
        }

        private static boolean containsAlphabet(String token) {
            // e.g.
            // just pure numbers.
            return alphabetPattern.matcher(token).find();
        }

        private static boolean isNotStopWord(String token) {
            // get use of StopAnalyzer
            // e.g. don't, ain't, etc.
            return ! StopAnalyser.isStopWord(token);
        }

        private static String cleanse(String token) {
            // replace all but alphabets (and hyphen) with empty strings
            return token.replaceAll(toDeletePattern.toString(), "");
        } // cleanse
    } // Tokeniser

    public static class Map extends
            Mapper<Object, Text, Text, Text> {
        // INPUTFILE holds the name of the current file
        private final static Text INPUT_FILE = new Text();
        // TOKEN should be set to the current token rather than creating a
        private final static Text TOKEN = new Text();
        // VALUE should be set to the current value
        private final static Text VALUE = new Text();
        // to be used for in-mapper aggregation (in-memory local cache)
        private final static HashMap<String, Integer> LINE_TERM_FREQ = new HashMap<>(); // for agg. term freq
        private final static HashMap<String, ArrayList<Long>> LINE_TERM_POSITIONS = new HashMap<>(); // for agg. pos
        // This method gets the name of the file the current Mapper is working on
        @Override
        public void setup(Context context) {
            String inputFilePath = ((FileSplit) context.getInputSplit()).getPath().toString();
            String[] pathComponents = inputFilePath.split("/");
            INPUT_FILE.set(pathComponents[pathComponents.length - 1]);
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            // clear all the local caches to be used from the start
            TOKEN.clear();
            VALUE.clear();
            LINE_TERM_FREQ.clear();
            LINE_TERM_POSITIONS.clear();
            // TODO here, the poisitional index is incorrect. Because you are tokenizing them first.
            // TODO: This is the way you should have done it!
            // TODO: make sure you don't tokenize them before you calculate positional index.
            long start_idx = ((FileSplit) context.getInputSplit()).getStart();
            // get a counter with doc_id as both the group name and counter name
            // this will be globally shared among mapper operations with the same doc id
            Counter counter = context.getCounter(INPUT_FILE.toString(), INPUT_FILE.toString());
            ArrayList<String> tokens = Tokeniser.tokenise(value.toString()); // tokenise the line
            inMapperAggregation(tokens, counter); // execute in-mapper aggregation
            emitAggregations(context); // emit the result of aggregations
        } // map

        public static void inMapperAggregation(ArrayList<String> tokens, Counter counter) {
            ArrayList<Long> positions;
            // tokenise the line using the predefined Singleton class
            for (String term: tokens) {
                // aggregate line term freq
                LINE_TERM_FREQ.put(term, LINE_TERM_FREQ.getOrDefault(term, 0) + 1);
                // aggregate line term positions
                if (LINE_TERM_POSITIONS.containsKey(term)) {
                    LINE_TERM_POSITIONS.get(term).add(counter.getValue());
                }
                else {
                    positions = new ArrayList<>();
                    positions.add(counter.getValue());
                    LINE_TERM_POSITIONS.put(term, positions);
                }counter.increment(1);
            } // for each tokenized term
        } // inMapperAggregation

        public static void emitAggregations(Context context)
                throws IOException, InterruptedException{
            String term;
            int lineTermFreq;
            for (java.util.Map.Entry<String, Integer> entry : LINE_TERM_FREQ.entrySet()){
                term = entry.getKey();
                lineTermFreq = entry.getValue();
                TOKEN.set(term);
                VALUE.set(INPUT_FILE.toString() // encode doc id
                        + "|" + lineTermFreq  // encode a summary of term freq
                        + "|" + LINE_TERM_POSITIONS.get(term).toString()); // encode a summary of term pos
                context.write(TOKEN, VALUE);
            } // for each line term freq
        }
    } // mapper

    public static class Reduce extends Reducer<Text, Text, Text, ArrayListWritable<Text>> {
        // TERM_FREQ should be set to the current term freq for a given doc
        private final static HashMap<String, Integer> TERM_FREQ = new HashMap<>();
        // TERM_POSITIONS
        private final static HashMap<String, ArrayList<Integer>> TERM_POSITIONS = new HashMap<>();
        // POSTINGS_LIST should be set to the current posting list for a given term
        private final static ArrayListWritable<Text> POSTINGS_LIST = new ArrayListWritable<>();
        // DOC_ID_SET should be set to the current set of unique doc ids for a given term
        // this is needed to compute DOC_FREQ
        private final static HashSet<String> DOC_ID_SET = new HashSet<>();
        // TOKEN_WITH_DOC_FREQ
        private final static Text TOKEN_WITH_DOC_FREQ = new Text();


        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // This Reduce Job should take in a key and an iterable of file names
            // It should convert this iterable to a writable array list and output
            // clear all the local caches on start
            TERM_FREQ.clear();
            TERM_POSITIONS.clear();
            POSTINGS_LIST.clear();
            DOC_ID_SET.clear();
            TOKEN_WITH_DOC_FREQ.clear();
            parseInMapperAggregations(values); // first, parse the aggregated summary and put the results in the cache
            emitIndex(key.toString(), context); // from the cache, build inverted index and emit it
        } // reduce

        public static void parseInMapperAggregations(Iterable<Text> values){
            String docId;
            int lineTermFreq;
            ArrayList<Integer> lineTermPositions;
            String[] docIdWithSummary;
            for (Text t: values) {
                // escaping "|" in java.
                // reference: https://www.baeldung.com/java-regexp-escape-char
                docIdWithSummary = t.toString().split("\\Q|\\E");
                // get the summaries precomputed by the combiner pattern in mapper
                docId = docIdWithSummary[0];
                lineTermFreq = Integer.parseInt(docIdWithSummary[1]);
                lineTermPositions = parseLineTermPositionsStr(docIdWithSummary[2]);
                // update unique doc id set, term freq and term positions
                DOC_ID_SET.add(docId);
                TERM_FREQ.put(docId, TERM_FREQ.getOrDefault(docId, 0) + lineTermFreq);
                if (TERM_POSITIONS.containsKey(docId)) {
                    // if it already exists, just merge into the current
                    TERM_POSITIONS.get(docId).addAll(lineTermPositions);
                }
                else {
                    // if it does not exist, add in the positions that we have
                    TERM_POSITIONS.put(docId, lineTermPositions);
                }
                // update doc_freq
            } // for values
        } // parseInMapperAggregations

        private static ArrayList<Integer> parseLineTermPositionsStr(String lineTermPositionsStr) {
            // e.g.
            // [30, 39, 104, 248, 628, 664]
            // [30, 54, 347, 376, 817, 939]
            // get rid of the brackets
            ArrayList<Integer> LineTermPositions = new ArrayList<>();
            lineTermPositionsStr = lineTermPositionsStr.replace("[", "");
            lineTermPositionsStr = lineTermPositionsStr.replace("]", "");
            for(String numStr : lineTermPositionsStr.split(",")){
                LineTermPositions.add(Integer.parseInt(numStr.trim()));
            }
            return LineTermPositions;
        }

        public static void emitIndex(String term, Context context)
                throws IOException, InterruptedException {
            // TODO: should have done flagging for important terms.
            // by actually getting TF * IDF.
            int lineTermFreq;
            String docId;
            ArrayList<Integer> positionalIndex;
            for (java.util.Map.Entry<String, Integer> entry : TERM_FREQ.entrySet()) {
                docId = entry.getKey();
                lineTermFreq = entry.getValue();
                positionalIndex = TERM_POSITIONS.get(docId);
                POSTINGS_LIST.add(new Text(docId + "|" + lineTermFreq+ "|" + positionalIndex));
            } // for each term freq pair
            Collections.sort(POSTINGS_LIST); // postings list must be sorted after the index is sorted by terms
            int DOC_FREQ = DOC_ID_SET.size(); // unique set of all doc ids for this term is the doc freq
            TOKEN_WITH_DOC_FREQ.set(term + "|" + DOC_FREQ); // join the term with doc freq
            // sort the postings
            context.write(TOKEN_WITH_DOC_FREQ, POSTINGS_LIST);
        } // emitInvertedIndex ..
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
