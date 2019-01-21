/*************************************************************************************************************
 * FILE:            TFIDF.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Calculate TFIDF for words in many documents using Spark.
 *
 * MODIFIED FROM:   https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw5/
 *
 * TO RUN:          As described in link above.
 *************************************************************************************************************/

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;
import java.util.*;

/*
 * Main class of the TFIDF Spark implementation.
 * Author: Tyler Stocksdale
 * Date:   10/31/2017
 */
public class TFIDF {

	static boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        // Check for correct usage
        if (args.length != 1) {
            System.err.println("Usage: TFIDF <input dir>");
            System.exit(1);
        }
		
		// Create a Java Spark Context
		SparkConf conf = new SparkConf().setAppName("TFIDF");
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Load our input data
		// Output is: ( filePath , fileContents ) for each file in inputPath
		String inputPath = args[0];
		JavaPairRDD<String,String> filesRDD = sc.wholeTextFiles(inputPath);
		
		// Get/set the number of documents (to be used in the IDF job)
		long numDocs = filesRDD.count();
		
		//Print filesRDD contents
		if (DEBUG) {
			List<Tuple2<String, String>> list = filesRDD.collect();
			System.out.println("------Contents of filesRDD------");
			for (Tuple2<String, String> tuple : list) {
				System.out.println("(" + tuple._1 + ") , (" + tuple._2.trim() + ")");
			}
			System.out.println("--------------------------------");
		}
		
		/* 
		 * Initial Job
		 * Creates initial JavaPairRDD from filesRDD
		 * Contains each word@document from the corpus and also attaches the document size for 
		 * later use
		 * 
		 * Input:  ( filePath , fileContents )
		 * Map:    ( (word@document) , docSize )
		 */
		@SuppressWarnings("serial")
        JavaPairRDD<String,Integer> wordsRDD = filesRDD.flatMapToPair(
			new PairFlatMapFunction<Tuple2<String,String>,String,Integer>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
                public Iterable<Tuple2<String,Integer>> call(Tuple2<String,String> x) {
					// Collect data attributes
					String[] filePath = x._1.split("/");
					String document = filePath[filePath.length-1];
					String fileContents = x._2;
					String[] words = fileContents.split("\\s+");
					int docSize = words.length;
					
					// Output to Arraylist
					ArrayList ret = new ArrayList();
					for(String word : words) {
						ret.add(new Tuple2(word.trim() + "@" + document, docSize));
					}
					return ret;
				}
			}
		);
		
		//Print wordsRDD contents
		if (DEBUG) {
			List<Tuple2<String, Integer>> list = wordsRDD.collect();
			System.out.println("------Contents of wordsRDD------");
			for (Tuple2<String, Integer> tuple : list) {
				System.out.println("(" + tuple._1 + ") , (" + tuple._2 + ")");
			}
			System.out.println("--------------------------------");
		}		
		
		/* 
		 * TF Job (Word Count Job + Document Size Job)
		 * Gathers all data needed for TF calculation from wordsRDD
		 *
		 * Input:  ( (word@document) , docSize )
		 * Map:    ( (word@document) , (1/docSize) )
		 * Reduce: ( (word@document) , (wordCount/docSize) )
		 * 
		 * References:    https://www.programcreek.com/java-api-examples/?class=org.apache.spark.api.java.JavaPairRDD&method=mapToPair
		 *                https://www.programcreek.com/java-api-examples/?class=org.apache.spark.api.java.JavaPairRDD&method=reduceByKey
		 */
		JavaPairRDD<String,String> tfRDD = wordsRDD.mapToPair(
	        new PairFunction <Tuple2<String, Integer>, String, String> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, String> call(Tuple2<String, Integer> oInputTuple) throws Exception {
                
                return new Tuple2<String, String>(oInputTuple._1, ("1/" + oInputTuple._2));
                
            }
		    
		}).reduceByKey(new Function2 <String, String, String> () {

            private static final long serialVersionUID = 1L;

            @Override
            public String call(String sValue1, String sValue2) throws Exception {
                
                // Declare variables
                int nPartialWordCount;
                String sReducedValue;
                
                // Calculate
                nPartialWordCount = 
                    Integer.parseInt(sValue1.split("/")[0]) + 
                    Integer.parseInt(sValue2.split("/")[0]);
                sReducedValue = nPartialWordCount + "/" + sValue1.split("/")[1];
                
                // Return
                return sReducedValue;
                
            }
		    
		});
		
		//Print tfRDD contents
		if (DEBUG) {
			List<Tuple2<String, String>> list = tfRDD.collect();
			System.out.println("-------Contents of tfRDD--------");
			for (Tuple2<String, String> tuple : list) {
				System.out.println("(" + tuple._1 + ") , (" + tuple._2 + ")");
			}
			System.out.println("--------------------------------");
		}
		
		/*
		 * IDF Job
		 * Gathers all data needed for IDF calculation from tfRDD
		 *
		 * Input:  ( (word@document) , (wordCount/docSize) )
		 * Map:    ( word , (1/document) )
		 * Reduce: ( word , (numDocsWithWord/document1,document2...) )
		 * Map:    ( (word@document) , (numDocs/numDocsWithWord) )
		 * 
         * References:    https://www.programcreek.com/java-api-examples/?class=org.apache.spark.api.java.JavaPairRDD&method=mapToPair
         *                https://www.programcreek.com/java-api-examples/?class=org.apache.spark.api.java.JavaPairRDD&method=reduceByKey
         *                https://www.programcreek.com/java-api-examples/index.php?api=org.apache.spark.api.java.function.PairFlatMapFunction
         *                https://stackoverflow.com/questions/41092865/apache-spark-in-pairflatmapfunction-how-to-add-tuples-back-to-the-iterabletup
         */
		JavaPairRDD<String,String> idfRDD = tfRDD.mapToPair(
	        new PairFunction <Tuple2<String, String>, String, String> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, String> call(Tuple2<String, String> oInputTuple) throws Exception {
                
                return new Tuple2<String, String>(
                    oInputTuple._1.split("@")[0], 
                    ("1/" + oInputTuple._1.split("@")[1])
                );
                
            }
            
        }).reduceByKey(new Function2 <String, String, String> () {

            private static final long serialVersionUID = 1L;

            @Override
            public String call(String sValue1, String sValue2) throws Exception {
                
                // Declare variables
                int nPartialDocumentCount;
                String sPartialDocumentList;
                String sReducedValue;
                
                // Calculate
                nPartialDocumentCount = 
                    Integer.parseInt(sValue1.split("/")[0]) + 
                    Integer.parseInt(sValue2.split("/")[0]);
                sPartialDocumentList = 
                    sValue1.split("/")[1] +
                    "," +
                    sValue2.split("/")[1];
                sReducedValue = nPartialDocumentCount + "/" + sPartialDocumentList;
                
                // Return
                return sReducedValue;
                
            }
            
        }).flatMapToPair(new PairFlatMapFunction <Tuple2<String, String>, String, String> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Iterable<Tuple2<String, String>> call(Tuple2<String, String> oInputTuple) throws Exception {
                
                // Declare variables
                List<Tuple2<String, String>> aoOutputTuples;
                String sWord;
                String sNumDocsWithWord;
                String sKeyOut;
                String sValueOut;
                String [] asDocuments;
                int i;
                
                // Initialize
                aoOutputTuples = new ArrayList<Tuple2<String, String>>();
                
                // Populate
                sWord = oInputTuple._1;
                sNumDocsWithWord = oInputTuple._2.split("/")[0];
                asDocuments = oInputTuple._2.split("/")[1].split(",");
                for (i = 0; i < asDocuments.length; i++) {
                    sKeyOut = sWord + "@" + asDocuments[i];
                    sValueOut = numDocs + "/" + sNumDocsWithWord;
                    aoOutputTuples.add(new Tuple2<String, String>(sKeyOut, sValueOut));
                }
                
                // Return                
                return aoOutputTuples;
                
            }
            
        });
		
		//Print idfRDD contents
		if (DEBUG) {
			List<Tuple2<String, String>> list = idfRDD.collect();
			System.out.println("-------Contents of idfRDD-------");
			for (Tuple2<String, String> tuple : list) {
				System.out.println("(" + tuple._1 + ") , (" + tuple._2 + ")");
			}
			System.out.println("--------------------------------");
		}
	
		/*
		 * TF * IDF Job
		 * Calculates final TFIDF value from tfRDD and idfRDD
		 *
		 * Input:  ( (word@document) , (wordCount/docSize) )          [from tfRDD]
		 * Map:    ( (word@document) , TF )
		 * 
		 * Input:  ( (word@document) , (numDocs/numDocsWithWord) )    [from idfRDD]
		 * Map:    ( (word@document) , IDF )
		 * 
		 * Union:  ( (word@document) , TF )  U  ( (word@document) , IDF )
		 * Reduce: ( (word@document) , TFIDF )
		 * Map:    ( (document@word) , TFIDF )
		 *
		 * where TF    = wordCount/docSize
		 * where IDF   = ln(numDocs/numDocsWithWord)
		 * where TFIDF = TF * IDF
		 */
		@SuppressWarnings("serial")
        JavaPairRDD<String,Double> tfFinalRDD = tfRDD.mapToPair(
			new PairFunction<Tuple2<String,String>,String,Double>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
                public Tuple2<String,Double> call(Tuple2<String,String> x) {
					double wordCount = Double.parseDouble(x._2.split("/")[0]);
					double docSize = Double.parseDouble(x._2.split("/")[1]);
					double TF = wordCount/docSize;
					return new Tuple2(x._1, TF);
				}
			}
		);
		
		JavaPairRDD<String,Double> idfFinalRDD = idfRDD.mapToPair(
	        new PairFunction <Tuple2<String, String>, String, Double> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, Double> call(Tuple2<String, String> oInputTuple) throws Exception {
                
                // Declare variables
                double nIDF;
                
                // Calculate
                nIDF = Math.log(
                    Double.parseDouble(oInputTuple._2.split("/")[0]) / 
                    Double.parseDouble(oInputTuple._2.split("/")[1])
                );
                
                // Return
                return new Tuple2<String, Double>(oInputTuple._1, nIDF);
                
            }
            
        });
		
		JavaPairRDD<String,Double> tfidfRDD = tfFinalRDD.union(idfFinalRDD).reduceByKey(
	        new Function2 <Double, Double, Double> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Double call(Double nTF, Double nIDF) throws Exception {
                
                return nTF * nIDF;
                
            }
            
        }).mapToPair(new PairFunction <Tuple2<String, Double>, String, Double> () {

            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, Double> call(Tuple2<String, Double> oInputTuple) throws Exception {
                
                return new Tuple2<String, Double>(
                    oInputTuple._1.split("@")[1] + "@" + oInputTuple._1.split("@")[0], 
                    oInputTuple._2
                );
                
            }
            
        });
		
		//Print tfidfRDD contents in sorted order
		Map<String, Double> sortedMap = new TreeMap<>();
		List<Tuple2<String, Double>> list = tfidfRDD.collect();
		for (Tuple2<String, Double> tuple : list) {
			sortedMap.put(tuple._1, tuple._2);
		}
		if(DEBUG) System.out.println("-------Contents of tfidfRDD-------");
		for (String key : sortedMap.keySet()) {
			System.out.println(key + "\t" + sortedMap.get(key));
		}
		if(DEBUG) System.out.println("--------------------------------");	 
	}	
}