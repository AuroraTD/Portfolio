/*************************************************************************************************************
 * FILE:            TFIDF.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Calculate TFIDF for words in many documents using Hadoop.
 *                  This program uses more variables than are strictly necessary.
 *                      These are for intermediate calculation step debugging, and explicitness.
 *                  This program uses more comment blocks than are sane for a piece of production code.
 *                      These are for minimizing first-time Hadoop developer confusion.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw4/
 *
 * TO RUN:          As described in link above.
 *************************************************************************************************************/

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/*
 * Main class of the TFIDF MapReduce implementation.
 * Author: Tyler Stocksdale
 * Date:   10/18/2017
 */
@SuppressWarnings("unused")
public class TFIDF {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        
        // Check for correct usage
        if (args.length != 1) {
            System.err.println("Usage: TFIDF <input dir>");
            System.exit(1);
        }
		
		// Create configuration
		Configuration conf = new Configuration();
		
		// Input and output paths for each job
		Path inputPath = new Path(args[0]);
		Path wcInputPath = inputPath;
		Path wcOutputPath = new Path("output/WordCount");
		Path dsInputPath = wcOutputPath;
		Path dsOutputPath = new Path("output/DocSize");
		Path tfidfInputPath = dsOutputPath;
		Path tfidfOutputPath = new Path("output/TFIDF");
		
		// Get/set the number of documents (to be used in the TFIDF MapReduce job)
        FileSystem fs = inputPath.getFileSystem(conf);
        FileStatus[] stat = fs.listStatus(inputPath);
		String numDocs = String.valueOf(stat.length);
		conf.set("numDocs", numDocs);
		
		// Delete output paths if they exist
		FileSystem hdfs = FileSystem.get(conf);
		if (hdfs.exists(wcOutputPath))
			hdfs.delete(wcOutputPath, true);
		if (hdfs.exists(dsOutputPath))
			hdfs.delete(dsOutputPath, true);
		if (hdfs.exists(tfidfOutputPath))
			hdfs.delete(tfidfOutputPath, true);
		
		/* Summary of what gets passed around:
		 *    Word Count Map IN
		 *        Object          byte offset
		 *        Text            contents of one line
		 *    Word Count Map OUT / Word Count Reduce IN
		 *        Text            word@document 
		 *        IntWritable     1
         *    Word Count Reduce OUT
         *        Text            word@document
         *        IntWritable     wordCount
         *    Doc Size Map IN
         *        Object          byte offset
         *        Text            contents of one line
         *    Doc Size Map OUT / Doc Size Reduce IN
         *        Text            document
         *        Text            word=wordCount
         *    Doc Size Reduce OUT
         *        Text            word@document
         *        Text            wordCount/docSize
         *    TFID Map IN
         *        Object          byte offset
         *        Text            contents of one line
         *    TFID Map OUT / TFID Reduce IN
         *        Text            word
         *        Text            document=wordCount/docSize
         *    TFID Reduce OUT
         *        Text            document@word
         *        Text            TFIDF
		 */
		
		// Create and execute Word Count job
		
    		// Create a new job with no particular cluster and a given job name
    	    Job jobWC = Job.getInstance(conf, "WordCount");
    	    
    	    // Tell Hadoop which jar it should send to nodes to perform Map and Reduce tasks
    	    jobWC.setJarByClass(TFIDF.class);
    	    
    	    // Set mapper and reducer
    	    jobWC.setMapperClass(WCMapper.class);
    	    jobWC.setReducerClass(WCReducer.class);
    	    
    	    // Set result key and value classes
    	    jobWC.setOutputKeyClass(Text.class);
    	    jobWC.setOutputValueClass(IntWritable.class);
    	    
    	    // Set input and output paths
    	    FileInputFormat.addInputPath(jobWC, wcInputPath);
    	    FileOutputFormat.setOutputPath(jobWC, wcOutputPath);
    	    
    	    // Execute job, waiting for completion
    	    jobWC.waitForCompletion(true);
			
		// Create and execute Document Size job
		
    	    // Create a new job with no particular cluster and a given job name
            Job jobDS = Job.getInstance(conf, "DocSize");
            
            // Tell Hadoop which jar it should send to nodes to perform Map and Reduce tasks
            jobDS.setJarByClass(TFIDF.class);
            
            // Set mapper and reducer
            jobDS.setMapperClass(DSMapper.class);
            jobDS.setReducerClass(DSReducer.class);
            
            // Set result key and value classes
            jobDS.setOutputKeyClass(Text.class);
            jobDS.setOutputValueClass(Text.class);
            
            // Set input and output paths
            FileInputFormat.addInputPath(jobDS, dsInputPath);
            FileOutputFormat.setOutputPath(jobDS, dsOutputPath);
            
            // Execute job, waiting for completion
            jobDS.waitForCompletion(true);
		
		// Create and execute TFIDF job
		
            // Create a new job with no particular cluster and a given job name
            Job jobTFIDF = Job.getInstance(conf, "DocSize");
            
            // Tell Hadoop which jar it should send to nodes to perform Map and Reduce tasks
            jobTFIDF.setJarByClass(TFIDF.class);
            
            // Set mapper and reducer
            jobTFIDF.setMapperClass(TFIDFMapper.class);
            jobTFIDF.setReducerClass(TFIDFReducer.class);
            
            // Set result key and value classes
            jobTFIDF.setOutputKeyClass(Text.class);
            jobTFIDF.setOutputValueClass(Text.class);
            
            // Set input and output paths
            FileInputFormat.addInputPath(jobTFIDF, tfidfInputPath);
            FileOutputFormat.setOutputPath(jobTFIDF, tfidfOutputPath);
            
            // Execute job, waiting for completion
            jobTFIDF.waitForCompletion(true);
    	    
    	// Clean up
    	    
    	    fs.close();
    	    hdfs.close();
		
    }
	
    /* Summary of what gets passed around:
     *    *** Word Count Map IN ***
     *        Object          byte offset
     *        Text            contents of one line
     *    *** Word Count Map OUT / Word Count Reduce IN ***
     *        Text            word@document 
     *        IntWritable     1
     *    Word Count Reduce OUT
     *        Text            word@document
     *        IntWritable     wordCount
     *    Doc Size Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Doc Size Map OUT / Doc Size Reduce IN
     *        Text            document
     *        Text            word=wordCount
     *    Doc Size Reduce OUT
     *        Text            word@document
     *        Text            wordCount/docSize
     *    TFID Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    TFID Map OUT / TFID Reduce IN
     *        Text            word
     *        Text            document=wordCount/docSize
     *    TFID Reduce OUT
     *        Text            document@word
     *        Text            TFIDF
     */
	public static class WCMapper extends Mapper<Object, Text, Text, IntWritable> {
	    
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        
        /*************************************************************************************************************
         * FUNCTION:        map
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Map function for word count mapper class
         * 
         * MODIFIED FROM:   https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
         *
         * REFERENCED:      https://stackoverflow.com/questions/31663500/the-key-parameter-of-hadoop-map-function-is-not-used
         *                  https://stackoverflow.com/questions/19012482/how-to-get-the-input-file-name-in-the-mapper-in-a-hadoop-program
         *
         * ARGUMENTS:       key -       Offset of the text file
         *                  value -     A line from the text file file
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
        public void map (Object key, Text value, Context context) throws IOException, InterruptedException {
            
            // Declare variables
            StringTokenizer oIterator;
            String sDoc;
            String sKeyOut;
            
            // Get the filename
            sDoc = ((FileSplit) context.getInputSplit()).getPath().getName();
            
            // Get an iterator to go over this line from the text file
            oIterator = new StringTokenizer(value.toString());
            
            // While there are tokens
            while (oIterator.hasMoreTokens()) {
                // Build output key
                sKeyOut = oIterator.nextToken() + "@" + sDoc;
                // Write one key value pair to output context (word@doc, count of 1)
                word.set(sKeyOut);
                context.write(word, one);
            }
            
        }
		
    }

    /* Summary of what gets passed around:
     *    Word Count Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    *** Word Count Map OUT / Word Count Reduce IN ***
     *        Text            word@document 
     *        IntWritable     1
     *    *** Word Count Reduce OUT ***
     *        Text            word@document
     *        IntWritable     wordCount
     *    Doc Size Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Doc Size Map OUT / Doc Size Reduce IN
     *        Text            document
     *        Text            word=wordCount
     *    Doc Size Reduce OUT
     *        Text            word@document
     *        Text            wordCount/docSize
     *    TFID Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    TFID Map OUT / TFID Reduce IN
     *        Text            word
     *        Text            document=wordCount/docSize
     *    TFID Reduce OUT
     *        Text            document@word
     *        Text            TFIDF
     */
	public static class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	    
        private IntWritable result = new IntWritable();
        
        /*************************************************************************************************************
         * FUNCTION:        reduce
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Reduce function for word count reducer class
         * 
         * MODIFIED FROM:   https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
         *
         * ARGUMENTS:       key -       A word@doc key from the map function
         *                  values -    An iterable collection of count values for the word@doc key
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
        public void reduce (Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            
            // Declare variables
            int nSum;
            
            // Sum up the counts for the word@doc key
            nSum = 0;
            for (IntWritable val : values) {
                nSum += val.get();
            }
            
            // Write to the output context
            result.set(nSum);
            context.write(key, result);
            
        }
		
    }
	
    /* Summary of what gets passed around:
     *    Word Count Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Word Count Map OUT / Word Count Reduce IN
     *        Text            word@document 
     *        IntWritable     1
     *    Word Count Reduce OUT
     *        Text            word@document
     *        IntWritable     wordCount
     *    *** Doc Size Map IN ***
     *        Object          byte offset
     *        Text            contents of one line
     *    *** Doc Size Map OUT / Doc Size Reduce IN ***
     *        Text            document
     *        Text            word=wordCount
     *    Doc Size Reduce OUT
     *        Text            word@document
     *        Text            wordCount/docSize
     *    TFID Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    TFID Map OUT / TFID Reduce IN
     *        Text            word
     *        Text            document=wordCount/docSize
     *    TFID Reduce OUT
     *        Text            document@word
     *        Text            TFIDF
     */
	public static class DSMapper extends Mapper<Object, Text, Text, Text> {
	    
        private Text oKeyOut = new Text();
        private Text oValueOut = new Text();
        
        /*************************************************************************************************************
         * FUNCTION:        map
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Map function for document size mapper class
         *
         * ARGUMENTS:       key -       Offset of the text file
         *                  value -     A line from the text file file
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
        public void map (Object key, Text value, Context context) throws IOException, InterruptedException {
            
            // Declare variables
            String sLineContents;
            String sLineKey;
            String sLineValue;
            String sWord;
            String sDoc;
            String sValueOut;
            
            // Get line contents
            sLineContents = value.toString();
            sLineKey = sLineContents.split("\t")[0];
            sLineValue = sLineContents.split("\t")[1];
            
            // Split up key (word@doc)
            sWord = sLineKey.split("@")[0];
            sDoc = sLineKey.split("@")[1];
            
            // Build output value string
            sValueOut = sWord + "=" + sLineValue;
            
            // Write one key value pair to output context (doc, word=count)
            oKeyOut.set(sDoc);
            oValueOut.set(sValueOut);
            context.write(oKeyOut, oValueOut);
            
        }
		
    }

    /* Summary of what gets passed around:
     *    Word Count Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Word Count Map OUT / Word Count Reduce IN
     *        Text            word@document 
     *        IntWritable     1
     *    Word Count Reduce OUT
     *        Text            word@document
     *        IntWritable     wordCount
     *    Doc Size Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    *** Doc Size Map OUT / Doc Size Reduce IN ***
     *        Text            document
     *        Text            word=wordCount
     *    *** Doc Size Reduce OUT ***
     *        Text            word@document
     *        Text            wordCount/docSize
     *    TFID Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    TFID Map OUT / TFID Reduce IN
     *        Text            word
     *        Text            document=wordCount/docSize
     *    TFID Reduce OUT
     *        Text            document@word
     *        Text            TFIDF
     */
	public static class DSReducer extends Reducer<Text, Text, Text, Text> {
	    
        private Text oKeyOut = new Text();
        private Text oValueOut = new Text();
        
        /*************************************************************************************************************
         * FUNCTION:        reduce
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Reduce function for document size reducer class
         *
         * ARGUMENTS:       key -       A doc key from the map function
         *                  values -    An iterable collection of word=count values for the doc key
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
        public void reduce (Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            
            // Declare variables
            String sDoc;
            String sWord;
            String sWordCount;
            String sKeyOut;
            String sValueOut;
            String sSingleValue;
            int nWordCount;
            int nDocSize;
            int nNumValuesIn;
            int i;
            
            // Pick out useful pieces from the input
            sDoc = key.toString();
            
            /* Set up a cache
             * https://www.roman10.net/2012/08/13/hadoop-reducer-iterable-how-to-iterate-twice/
             */
            ArrayList<String> aValuesIn = new ArrayList<String>();
            nNumValuesIn = 0;
            
            // Iterate once to get doc size
            nDocSize = 0;
            for (Text val : values) {
                
                // Save value to cache
                aValuesIn.add(val.toString());
                nNumValuesIn++;
                
                // Pick out useful pieces from the input
                sWordCount = val.toString().split("=")[1];
                nWordCount = Integer.parseInt(sWordCount);
                
                // Add to sum
                nDocSize += nWordCount;
                
            }
            
            // Iterate again to write to output
            for (i = 0; i < nNumValuesIn; i++) {
                
                // Pick out useful pieces from the input
                sSingleValue = aValuesIn.get(i);
                sWord = sSingleValue.split("=")[0];
                sWordCount = sSingleValue.split("=")[1];
                
                // Write one key value pair to output context (word@doc, wordCount / docSize)
                sKeyOut = sWord + "@" + sDoc;
                sValueOut = sWordCount + "/" + nDocSize;
                
                oKeyOut.set(sKeyOut);
                oValueOut.set(sValueOut);
                context.write(oKeyOut, oValueOut);
                
            }
            
        }
		
    }
	
    /* Summary of what gets passed around:
     *    Word Count Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Word Count Map OUT / Word Count Reduce IN
     *        Text            word@document 
     *        IntWritable     1
     *    Word Count Reduce OUT
     *        Text            word@document
     *        IntWritable     wordCount
     *    Doc Size Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Doc Size Map OUT / Doc Size Reduce IN
     *        Text            document
     *        Text            word=wordCount
     *    Doc Size Reduce OUT
     *        Text            word@document
     *        Text            wordCount/docSize
     *    *** TFID Map IN ***
     *        Object          byte offset
     *        Text            contents of one line
     *    *** TFID Map OUT / TFID Reduce IN ***
     *        Text            word
     *        Text            document=wordCount/docSize
     *    TFID Reduce OUT
     *        Text            document@word
     *        Text            TFIDF
     */
	public static class TFIDFMapper extends Mapper<Object, Text, Text, Text> {
	    
        private Text oKeyOut = new Text();
        private Text oValueOut = new Text();
        
        /*************************************************************************************************************
         * FUNCTION:        map
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Map function for TFIDF mapper class
         *
         * ARGUMENTS:       key -       Offset of the text file
         *                  value -     A line from the text file file
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
        public void map (Object key, Text value, Context context) throws IOException, InterruptedException {
            
            // Declare variables
            String sLineContents;
            String sLineKey;
            String sLineValue;
            String sWord;
            String sDoc;
            String sValueOut;
            
            // Get line contents
            sLineContents = value.toString();
            sLineKey = sLineContents.split("\t")[0];
            sLineValue = sLineContents.split("\t")[1];
            
            // Split up key (word@doc)
            sWord = sLineKey.split("@")[0];
            sDoc = sLineKey.split("@")[1];
            
            // Build output value string
            sValueOut = sDoc + "=" + sLineValue;
            
            // Write one key value pair to output context (word, document=wordCount/docSize)
            oKeyOut.set(sWord);
            oValueOut.set(sValueOut);
            context.write(oKeyOut, oValueOut);
            
        }
		
    }

    /* Summary of what gets passed around:
     *    Word Count Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Word Count Map OUT / Word Count Reduce IN
     *        Text            word@document 
     *        IntWritable     1
     *    Word Count Reduce OUT
     *        Text            word@document
     *        IntWritable     wordCount
     *    Doc Size Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    Doc Size Map OUT / Doc Size Reduce IN
     *        Text            document
     *        Text            word=wordCount
     *    Doc Size Reduce OUT
     *        Text            word@document
     *        Text            wordCount/docSize
     *    TFID Map IN
     *        Object          byte offset
     *        Text            contents of one line
     *    *** TFID Map OUT / TFID Reduce IN ***
     *        Text            word
     *        Text            document=wordCount/docSize
     *    *** TFID Reduce OUT ***
     *        Text            document@word
     *        Text            TFIDF
     *
     * More explanation:
     * 
	 * For each identical key (word), reduces the values (document=wordCount/docSize) into a 
	 * the final TFIDF value (TFIDF). 
	 * Along the way, calculates the total number of documents and 
	 * the number of documents that contain the word.
	 * 
	 * TFIDF = (wordCount/docSize) * ln(numDocs/numDocsWithWord)
	 *
	 * Note: The output (key,value) pairs are sorted using TreeMap ONLY for grading purposes. For
	 *       extremely large datasets, having a for loop iterate through all the (key,value) pairs 
	 *       is highly inefficient!
	 */
	public static class TFIDFReducer extends Reducer<Text, Text, Text, Text> {
		
		private static int numDocs;
		private Map<Text, Text> tfidfMap = new HashMap<>();
        private Text oKeyOut;
        private Text oValueOut;
		
		// gets the numDocs value and stores it
		protected void setup(Context context) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			numDocs = Integer.parseInt(conf.get("numDocs"));
		}
		
        /*************************************************************************************************************
         * FUNCTION:        reduce
         *
         * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
         *
         * DESCRIPTION:     Reduce function for TFIDF reducer class
         *
         * ARGUMENTS:       key -       A word key from the map function
         *                  values -    An iterable collection of document=wordCount/docSize values for the doc key
         *                  context -   Output context (where result will be pushed)
         *************************************************************************************************************/
		public void reduce (Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		    
		    // Declare variables
		    String sWord;
		    String sDoc;
		    String sWordCount;
		    String sDocSize;
		    String sKeyOut;
		    String sValueOut;
		    String sSingleValue;
		    int nNumDocsWithWord;
		    int i;
		    double nTFIDF;
		    
		    // Get word
		    sWord = key.toString();
		    
            /* Set up a cache
             * https://www.roman10.net/2012/08/13/hadoop-reducer-iterable-how-to-iterate-twice/
             */
            ArrayList<String> aValuesIn = new ArrayList<String>();
		    
		    // Iterate once to count # of documents with this word
		    nNumDocsWithWord = 0;
            for (Text val : values) {
                
                // Save value to cache
                aValuesIn.add(val.toString());
                
                // Increment
                nNumDocsWithWord++;
                
            }
            
            // Iterate again to calculate TFIDF
            for (i = 0; i < nNumDocsWithWord; i++) {
                
                // Pick out useful pieces from the input
                sSingleValue = aValuesIn.get(i);
                sDoc = sSingleValue.split("=")[0];
                sWordCount = sSingleValue.split("=")[1].split("/")[0];
                sDocSize = sSingleValue.split("=")[1].split("/")[1];
                
                // Calculate TFIDF: (wordCount/docSize) * ln(numDocs/numDocsWithWord)
                nTFIDF = 
                    (Double.parseDouble(sWordCount) / Double.parseDouble(sDocSize)) * 
                    Math.log((double) numDocs / (double) nNumDocsWithWord);
                
                // Build output key and value
                sKeyOut = sDoc + "@" + sWord;
                sValueOut = Double.toString(nTFIDF);
         
                /* Put the output (key,value) pair into the tfidfMap instead of doing a context.write
                 * Create new text values here so that we actually populate the map with many unique key-value pair
                 */
                oKeyOut = new Text();
                oValueOut = new Text();
                oKeyOut.set(sKeyOut);
                oValueOut.set(sValueOut);
                tfidfMap.put(oKeyOut, oValueOut);
                
            }

		}
		
		// Sort the output (key,value) pairs that are contained in the tfidfMap
		protected void cleanup(Context context) throws IOException, InterruptedException {
            Map<Text, Text> sortedMap = new TreeMap<Text, Text>(tfidfMap);
			for (Text key : sortedMap.keySet()) {
                context.write(key, sortedMap.get(key));
            }
        }
		
    }
}
