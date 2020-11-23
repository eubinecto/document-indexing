/*
  Basic Word Count

  This Map Reduce program performs the most simple word count possible.
  For each token it finds in a file it outputs that token and the amount of
  times it has been seen in all files.

  @author Kristian Epps

 */
package uk.ac.man.cs.comp38211.exercise;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import uk.ac.man.cs.comp38211.util.XParser;

public class WordCount extends Configured implements Tool
{
    // A logger is used to output text to system.out nicely
    private static final Logger LOG = Logger.getLogger(WordCount.class);

    private static class MyMapper extends
            Mapper<LongWritable, Text, Text, IntWritable> {

        // Use these objects instead of creating new ones every time
        private final static IntWritable ONE = new IntWritable(1);
        private final static Text WORD = new Text();

        // The map method takes in a line from a file, splits it into tokens
        // then outputs each token with a value of 1
        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // The line is broken up and turned into an iterator 
            String line = value.toString();
            StringTokenizer itr = new StringTokenizer(line);
            
            // While there are more tokens in the input, output with value 1
            while (itr.hasMoreTokens()) {
            	String token = itr.nextToken();
            	//clean up the token before setting
            	token = cleanUp(token);
                WORD.set(token);
                // context.write() is also known as 'output' or 'emit'
                // infrastructure will do the rest of the work for you.
                context.write(WORD, ONE);
            } // while
        } // map

        public String cleanUp(String token) {
            // trim the token
            token = token.trim();
            // normalise to lower case
            token = token.toLowerCase();
            // get rid of non-alphabetic characters
            // credit: https://stackoverflow.com/a/1805533
            token = token.replaceAll("[^a-z]", "");
            // return the token
            return token;
        }  // clean up
    }  // static class MyMapper

    private static class MyReducer extends
            Reducer<Text, IntWritable, Text, IntWritable>
    {

        // Reuse objects.
        private final static IntWritable SUM = new IntWritable();

        // The reducer outputs the key with the sum of all the values
        // provided to it
        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException
        {
            // For each of the values in the input, sum them up
            Iterator<IntWritable> iter = values.iterator();
            int sum = 0;
            while (iter.hasNext())
                sum += iter.next().get();

            // Output the key with the token
            SUM.set(sum);
            context.write(key, SUM);
        }
    }

    /**
     * Creates an instance of this tool.
     */
    public WordCount()
    {
    }
    
    // Variables to hold cmd line args
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String NUM_REDUCERS = "numReducers";

    /**
     * Runs this tool.
     */
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

        CommandLine cmdline;
        CommandLineParser parser = new XParser(true);

        try
        {
            cmdline = parser.parse(options, args);
        }
        catch (ParseException exp)
        {
            System.err.println("Error parsing command line: "
                    + exp.getMessage());
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
        job.setJobName(WordCount.class.getSimpleName());
        job.setJarByClass(WordCount.class);
        job.setNumReduceTasks(reduceTasks);

        // Set the mapper and reducer classes
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);
        
        // Set the output classes
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

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

    /**
     * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
     */
    public static void main(String[] args) throws Exception
    {
        ToolRunner.run(new WordCount(), args);
    }
}
