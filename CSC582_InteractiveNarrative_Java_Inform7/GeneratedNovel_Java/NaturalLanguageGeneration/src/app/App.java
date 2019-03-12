package app;

/*  CSC 582 HW04 - NaNoGenMo Solution

    Description -   This solution makes heavy use of the open source project
                    "SimpleNLG", a Java API for Natural Language Generation
                    (https://github.com/simplenlg/simplenlg).

    Authors -       ATTIFFAN -  Aurora Tiffany-Davis
                    THLE -      Tammy Le
*/

// IMPORTS
import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/* MAIN CLASS
    Description -   Load and parse lexicon XML
                    Generate a novel from lexicon words, using Simple NLG API
                    Save novel to a text file
    Input -         None
    Output -        Text file
*/
public class App {

    // Static constants
    static final String[] NAMES = {
        "Aaron",
        "Ameya",
        "Aurora",
        "Tammy",
        "Vibhav"
    };
    
    // For each row, {,} --> {initial wordQueueSize, initial complexClauseProbability (10-x)/10)}
    static final int[][] CHARACTER_MAP = {
    		{200, 2},	// Aaron's intellect score = 5 --> always selects words from mostRecentWordsByCategory
    		{500, 3},	// Ameya's intellect score = 4 
    		{900, 5},	// Aurora's intellect score = 3 
    		{1400, 7},	// Tammy's intellect score = 2 
    		{2000, 9}	// Vibhev's intellect score = 1 --> always selects word from wordsByCategory
    }; 
    
    static final int NUM_WORDS_TO_PRODUCE = 50000;
    static final int NUM_WORDS_PER_PARAGRAPH = 150;
    static final int NUM_WORDS_PER_CHAPTER = 3000;
    static final double AVG_CHARS_PER_ENGLISH_WORD = (double) 7.5;
    static final int RECENT_WORD_QUEUE_SIZE = 75;

    // Static variables
    static HashMap<String, ArrayList<String>> wordsByCategory = new HashMap<String, ArrayList<String>>();
    static HashMap<String, ArrayList<String>> mostRecentWordsByCategory = new HashMap<String, ArrayList<String>>();
    static Random randomizer = new Random();
    static NLGFactory nlgFactory;
    static Realiser nlgRealiser;
    static int charCountLastBreakParagraph = 0;
    static int charCountLastBreakChapter = 0;
    static int charCountLatest = 0;
    static int currentChapter = 1;
    static String mainCharacters;
    static CoordinatedPhraseElement mostRecentNamePhrase;
    static String currentMainCharacter = null;

    /* MAIN
        See class description
    */
    public static void main(String[] args) throws Exception {

        try {

            // Declare variables
            Lexicon nlgLexicon;
            String novelOutput;
            PrintWriter novelFileWriter;

            // Load words from XML
            loadWordsFromXML();

            // Set up NLG objects
            // https://github.com/simplenlg/simplenlg
            nlgLexicon = Lexicon.getDefaultLexicon();
            nlgFactory = new NLGFactory(nlgLexicon);
            nlgRealiser = new Realiser(nlgLexicon);

            // Start the novel
            mainCharacters = nlgRealiser.realiseSentence(produceNamePhrase()).replace(".","");
            novelOutput = 
                "The Adventures of " + 
                mainCharacters + 
                "\n\n" +
                novelPremise() +
                "\n\n" +
                "CHAPTER " + 
                currentChapter + 
                "\n\n";
            currentChapter++;

            // Add novel content
            while (novelOutput.length() < NUM_WORDS_TO_PRODUCE * AVG_CHARS_PER_ENGLISH_WORD) {
                // Add 1 sentence
                novelOutput += produceSentence() + " ";
                charCountLatest = novelOutput.length();
                // Insert paragraph break?
                if (
                    charCountLatest - charCountLastBreakParagraph > NUM_WORDS_PER_PARAGRAPH * AVG_CHARS_PER_ENGLISH_WORD &&
                    randomizer.nextBoolean()
                ) {
                    novelOutput += "\n\n";
                    charCountLastBreakParagraph = charCountLatest;
                }
                // Insert chapter break?
                if (
                    charCountLatest - charCountLastBreakChapter > NUM_WORDS_PER_CHAPTER * AVG_CHARS_PER_ENGLISH_WORD &&
                    randomizer.nextBoolean()
                ) {
                    novelOutput += "\n\nCHAPTER " + currentChapter + "\n\n";
                    currentChapter++;
                    charCountLastBreakChapter = charCountLatest;
                }
            }

            // End the novel
            novelOutput += 
                "\n\n" + 
                mainCharacters + 
                " lived happily ever after." + 
                "\n\nTHE END";

            // Save the novel off to a file
            novelFileWriter = new PrintWriter("novel.txt");
            novelFileWriter.print(novelOutput);
            novelFileWriter.close ();
            
        }
        catch (Throwable error) {
            handleError(error);
        }

    }

    /* LOAD WORDS FROM XML
        Description -   Load words from the Simple NLG lexicon XML file,
                        into a hash of arrays, where the key is category,
                        and the value is an array of Strings, where each String is a word in the category.
        Input -         None
        Output -        Words loaded into node lists
    */
    static private void loadWordsFromXML () {

        try {

            // Declare variables
            File fXmlFile;
            DocumentBuilderFactory dbFactory;
            DocumentBuilder dBuilder;
            Document xmlDoc;
            NodeList xmlWordNodes;
            Node xmlNode;
            Element xmlElement;
            Node nodeBase;
            Node nodeCategory;
            String nodeBaseString;
            String nodeCategoryString;
            PrintWriter wordFileWriter;
            int i;

            // Read lexicon XML in directly
            // https://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
            fXmlFile = new File("lib/default-lexicon.xml");
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.parse(fXmlFile);
            xmlDoc.getDocumentElement().normalize();

            // Iterate through NLG lexicon, adding words to the correct list in the hash
            xmlWordNodes = xmlDoc.getElementsByTagName("word");
            for (i = 0; i < xmlWordNodes.getLength(); i++) {
                xmlNode = xmlWordNodes.item(i);
                if(xmlNode.getNodeType() == Node.ELEMENT_NODE)
                {

                    // Get a single word element
                    xmlElement = (Element) xmlNode;

                    // Get base, category (not guaranteed to be present!)
                    nodeBase = xmlElement.getElementsByTagName("base").item(0);
                    nodeCategory = xmlElement.getElementsByTagName("category").item(0);

                    // Get strings (or nulls if not present)
                    nodeBaseString = nodeBase == null ? null : nodeBase.getTextContent();
                    nodeCategoryString = nodeCategory == null ? null : nodeCategory.getTextContent().toUpperCase();

                    // Add word to the correct list in the hash
                    if (!wordsByCategory.containsKey(nodeCategoryString)) {
                        wordsByCategory.put(nodeCategoryString, new ArrayList<String>());
                    }
                    wordsByCategory.get(nodeCategoryString).add(nodeBaseString);

                }
            }

            // Save the hash map off to a file for easy debugging
            wordFileWriter = new PrintWriter("word_hash.txt");
            for (Map.Entry<String, ArrayList<String>> entry : wordsByCategory.entrySet()) {
                wordFileWriter.println("Category: " + entry.getKey());
                for (i = 0; i < entry.getValue().size(); i++) {
                    wordFileWriter.println("\t" + entry.getValue().get(i));
                }
            }
            wordFileWriter.close ();     

        }
        catch (Throwable error) {
            handleError(error);
        }

    }

     /* PICK WORD BY CATEGORY
        Description -   Pick a random word from a given category
        Input -         Category (noun, verb, etc.)
                        OR "name" to pick from a pre-configured list of proper names
                        With some likelihood (defined in code), 
                        the most recently selected word from a given category will be selected again
        Output -        Randomly selected word belonging to this category
    */
    static private String pickWordByCategory (String category) {

        String selectedWord = "";

        try {

            // Declare variables
            ArrayList<String> allWordsInCategory;
            ArrayList<String> mostRecentWordsInCategory;
            String categoryNormalized;

            /* Pick a word queue size
                The word queue is specific to each word category (noun, verb, etc.)
                When we need a noun (for example) we might pick from recently used nouns
                If the word queue size is large, we will have a lot of variation in nouns we pick
                If the word queue size is small, we will focus on a few nouns
                The word queue size shrinks over the course of the novel
                so that we get more and more focused
            */
            
            // Normalize category
            categoryNormalized = category.toUpperCase();

            // Use one of the most recent words from this category? (if main character has high intellect score)
            if 
                (randomizer.nextInt(10) > 1 && 
                mostRecentWordsByCategory.containsKey(categoryNormalized) &&
                mostRecentWordsByCategory.get(categoryNormalized).size() >= RECENT_WORD_QUEUE_SIZE &&
                getWordQueueSize() <= RECENT_WORD_QUEUE_SIZE
            ) {
                mostRecentWordsInCategory = mostRecentWordsByCategory.get(categoryNormalized);
                selectedWord = mostRecentWordsInCategory.get(randomizer.nextInt(getWordQueueSize()));
            }

            // Pick random word from this category?
            else {
                if (categoryNormalized.equals("NAME")) {
                    selectedWord = NAMES[randomizer.nextInt(NAMES.length)];
                }
                else {
                    allWordsInCategory = wordsByCategory.get(categoryNormalized);
                    selectedWord = allWordsInCategory.get(randomizer.nextInt(allWordsInCategory.size()));
                }
            }

            // Remember the word that was selected
            if (!mostRecentWordsByCategory.containsKey(categoryNormalized)) {
                mostRecentWordsByCategory.put(categoryNormalized, new ArrayList<String>());
            }
            mostRecentWordsByCategory.get(categoryNormalized).add(selectedWord);
            if (mostRecentWordsByCategory.get(categoryNormalized).size() > RECENT_WORD_QUEUE_SIZE &&
            		getWordQueueSize() <= RECENT_WORD_QUEUE_SIZE
    		) {
                // Kick out oldest word in queue to account for new word added if word came from intellectual character
                while (mostRecentWordsByCategory.get(categoryNormalized).size() > RECENT_WORD_QUEUE_SIZE) {
                    mostRecentWordsByCategory.get(categoryNormalized).remove(0);
                }
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

        return selectedWord;

    }

    /* PRODUCE SENTENCE
        Description -   Produce (randomly) a valid English sentence
                        https://cdn.rawgit.com/simplenlg/simplenlg/master/docs/javadoc/simplenlg/framework/NLGFactory.html
        Input -         None
        Output -        A valid English sentence
    */
    static private String produceSentence () {

        String sentence = "";

        try {

            // Realize sentence from randomly produced (possibly recursive) clause
            sentence = nlgRealiser.realiseSentence(produceClause());

            /* Hack around possible Simple NLG API bug
                Observed sentences like this: "Tammy will will honestly justify Ameya"
                Simplified by commenting out, removed subordinate clauses, negation, interrogatives
                Very simple sentences still got stuff like this
                Possible Simple NLG API bug?
            */
            sentence = sentence.replaceAll("will will ","will ");
            sentence = sentence.replaceAll("will not will ","will ");
            sentence = sentence.replaceAll("did not did not ","did not ");
            sentence = sentence.replaceAll("does not does not ","does not ");

        }
        catch (Throwable error) {
            handleError(error);
        }

        return sentence;

    }

    /* PRODUCE CLAUSE
        Description -   Produce (randomly) a valid English clause
                        https://cdn.rawgit.com/simplenlg/simplenlg/master/docs/javadoc/simplenlg/framework/NLGFactory.html
                        This method may recurse (to get subordinate clauses)
        Input -         None
        Output -        A valid English clause
    */
    static private SPhraseSpec produceClause () {
    	
        SPhraseSpec nlgClauseToReturn = nlgFactory.createClause();

        try {

            // Declare variables
            SPhraseSpec nlgSubordinate;
            
            // Select subject, verb, object (some variation baked in here)
            nlgClauseToReturn.setSubject(produceNounPhrase("SUBJECT"));
            nlgClauseToReturn.setVerb(produceVerbPhrase());
            nlgClauseToReturn.setObject(produceNounPhrase("OBJECT"));

            // Add variation: select tense, negation, interrogative
            setTense(nlgClauseToReturn);
            setNegation(nlgClauseToReturn);
            setInterrogative(nlgClauseToReturn);

            // Add variation: select subordinate clause (RECURSE)?
            // Higher probability of selecting subordinate clause dependent on main character
            if (randomizer.nextInt(10) > getComplexClauseProbability()) {
                nlgSubordinate = produceClause();
                nlgSubordinate.setFeature(Feature.COMPLEMENTISER, pickWordByCategory("CONJUNCTION"));
                nlgClauseToReturn.addComplement(nlgSubordinate);
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

        return nlgClauseToReturn;

    }

    /* SET TENSE
        Description -   Set a tense (past, present future) on the given clause
        Input -         NLG clause
        Output -        None
    */
    static private void setTense (SPhraseSpec nlgClause) {

        try {

            // Declare variables
            int probabilityFuture;
            int probabilityPast;

            /* Pick tense probabilities
                The idea is to shift from future --> present --> past
                as the novel proceeds
            */
            probabilityFuture = Math.max(1, 100 - getPercentNovelCompletion());
            probabilityPast = Math.max(1, getPercentNovelCompletion());

            /* Pick and set a tense
                nextInt(n) returns a pseudo-random number between 0 (inclusive) and n (exclusive)
            */
            if (randomizer.nextInt(101) < probabilityFuture) {
                nlgClause.setFeature(Feature.TENSE, Tense.FUTURE);
            }
            else if (randomizer.nextInt(101) < probabilityPast) {
                nlgClause.setFeature(Feature.TENSE, Tense.PAST);
            }
            else {
                nlgClause.setFeature(Feature.TENSE, Tense.PRESENT);
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

    }

    /* SET NEGATION
        Description -   Set a negation, maybe, on the given clause
        Input -         NLG clause
        Output -        None
    */
    static private void setNegation (SPhraseSpec nlgClause) {

        try {

            if (randomizer.nextInt(10) > 7) {
                nlgClause.setFeature(Feature.NEGATED, true);
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

    }

    /* SET INTERROGATIVE
        Description -   Set an interrogative (who, where, why), maybe, on the given clause
        Input -         NLG clause
        Output -        None
    */
    static private void setInterrogative (SPhraseSpec nlgClause) {

        try {

            // Declare variables
            int probabilityInterrogative;

            /* Pick interrogative probability
                The idea is to shift from questions --> answers
                as the novel proceeds
            */
            probabilityInterrogative = Math.max(1, 100 - getPercentNovelCompletion());

            /* Pick and set an interrogative
                nextInt(n) returns a pseudo-random number between 0 (inclusive) and n (exclusive)
            */
            if (randomizer.nextInt(101) < probabilityInterrogative) {
                if (randomizer.nextInt(3) == 0) {
                    nlgClause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
                }
                else if (randomizer.nextInt(3) == 1) {
                    nlgClause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHY);
                }
                else {
                    nlgClause.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHERE);
                }
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

    }

    /* PRODUCE VERB PHRASE
        Description -   Produce (randomly) an NLG verb phrase
        Input -         None
        Output -        An NLG verb phrase
    */
    static private VPPhraseSpec produceVerbPhrase () {
        VPPhraseSpec phraseToReturn = nlgFactory.createVerbPhrase();

        try {

            phraseToReturn = nlgFactory.createVerbPhrase(pickWordByCategory("VERB"));
            if (randomizer.nextInt(10) > getComplexClauseProbability()) {
                phraseToReturn.addModifier(pickWordByCategory("ADVERB"));
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

        return phraseToReturn;

    }

    /* PRODUCE NOUN PHRASE
        Description -   Produce (randomly) an NLG subject / object phrase
                        https://cdn.rawgit.com/simplenlg/simplenlg/master/docs/javadoc/simplenlg/framework/NLGFactory.html
                        This method may recurse (to get subordinate clauses)
        Input -         "Subject" or "Object" (case insensitive)
        Output -        An NLG subject / object phrase
    */
    static private NLGElement produceNounPhrase (String subjectOrObject) {

        NLGElement nlgPhraseToReturn = nlgFactory.createNounPhrase();

        try {

            // Declare variables
            String determiner;
            boolean plural;

            // Subject is probably NAME, object is probably NOUN
            if (
                (subjectOrObject.toUpperCase().equals("SUBJECT") && randomizer.nextInt(10) > 9) ||
                (subjectOrObject.toUpperCase().equals("OBJECT") && randomizer.nextInt(10) > 1)
            ) {

                // NOUN (complex)
                nlgPhraseToReturn = nlgFactory.createNounPhrase(pickWordByCategory("NOUN"));

                // Select determinier (a, the, those, etc.)
                determiner = pickWordByCategory("DETERMINER");
                ((NPPhraseSpec) nlgPhraseToReturn).setDeterminer(determiner);

                // Select plurality
                switch (determiner) {
                    case "a":
                    case "an":
                    case "another":
                    case "each":
                    case "either":
                    case "every":
                    case "half":
                    case "neither":
                    case "that":
                    case "this":
                        plural = false;
                        break;
                    case "all":
                    case "both":
                    case "enough":
                    case "few":
                    case "fewer":
                    case "less":
                    case "many":
                    case "more":
                    case "most":
                    case "much":
                    case "no":
                    case "several":
                    case "some":
                    case "these":
                    case "those":
                        plural = true;
                        break;
                    default:
                        plural = randomizer.nextBoolean();
                        break;
                }
                nlgPhraseToReturn.setPlural(plural);

                // Select adjective (higher probability if more intellectual main character
                if (randomizer.nextInt(10) > getComplexClauseProbability()) {
                    ((NPPhraseSpec) nlgPhraseToReturn).addModifier(pickWordByCategory("ADJECTIVE"));
                }

            }
            else {
                // NAME (simple)
                nlgPhraseToReturn = produceNamePhrase();
                
                // If selecting a subject from the list of names, make it the current main character
                if (subjectOrObject.toUpperCase().equals("SUBJECT")) {
                	// Need to remove extra characters (',','and','.')
                    // If multiple names, take first name in namePhrase
                    String namePhrase = nlgRealiser.realiseSentence(nlgPhraseToReturn);
                    int periodIndex = namePhrase.indexOf('.');
                    int commaIndex = namePhrase.indexOf(',');
                    int andIndex = namePhrase.indexOf(" and");
                    
                    // Check ',' first because most complex name phrase contains ',' before 'and'
                    if (commaIndex > -1) {
                    	currentMainCharacter = namePhrase.substring(0, commaIndex);
                    }
                    // Check 'and' next because next most complex name phrase contains two names joined by 'and'
                    else if (andIndex > -1) {
                    	currentMainCharacter = namePhrase.substring(0, andIndex);
                    }
                    else if (periodIndex > -1) {
                    	currentMainCharacter = namePhrase.substring(0, periodIndex);
                    }
                    // Otherwise, don't change currentMainCharacter
                    
                }
                
            }

        }
        catch (Throwable error) {
            handleError(error);
        }

        return nlgPhraseToReturn;

    }

    /* PRODUCE NAME PHRASE
        Description -   Produce a list of one or two of the hard-coded character names
        Input -         (references hard-coded character names)
        Output -        Simple NLG Noun Phrase for the characters as a group
    */
    static private CoordinatedPhraseElement produceNamePhrase () {

        CoordinatedPhraseElement nlgPhraseToReturn = nlgFactory.createCoordinatedPhrase();

        try {

            // Declare variables
            ArrayList<Integer> nameIndices = new ArrayList<Integer>();
            int nameIndex;
            int i;

            /* Keep trying until we have something different than last time
                (to avoid things like "Aaron debated Aaron")
            */
            do {

                // Come up with list of name indices to use
                nameIndices.clear();
                do {
                    nameIndex = randomizer.nextInt(NAMES.length);
                    if (nameIndices.indexOf(nameIndex) < 0) {
                        nameIndices.add(nameIndex);
                    }
                } while (randomizer.nextInt(10) > 8);

                // Add names to coordinated phrase
                for (i = 0; i < nameIndices.size(); i++) {
                    nlgPhraseToReturn.addCoordinate(NAMES[nameIndices.get(i)]);
                }

            } while (nlgPhraseToReturn.equals(mostRecentNamePhrase));

           // Remember most recent name phrase
           mostRecentNamePhrase = nlgPhraseToReturn;

        }
        catch (Throwable error) {
            handleError(error);
        }

        return nlgPhraseToReturn;
    }

    /* GET PERCENTAGE COMPLETE
        Description -   Determine what % of the novel has been written
        Input -         None
        Output -        % of the novel that has already been written
    */
    static private int getPercentNovelCompletion () {

        int percentNovelCompletion = 0;

        try {

            // Declare variables
            double numCharsToProduce;

            // Compute
            numCharsToProduce = NUM_WORDS_TO_PRODUCE * AVG_CHARS_PER_ENGLISH_WORD;
            percentNovelCompletion = (int) (((double) charCountLatest / numCharsToProduce) * 100);

        }
        catch (Throwable error) {
            handleError(error);
        }

        return percentNovelCompletion;

    }
    
    /* GET CHARACTER'S WORD QUEUE SIZE
     * Description -	Retrieve the wordQueueSize from the map for the current main character
     * Input -			None
     * Output -			wordQueueSize
     */
    static private int getWordQueueSize () {
    	// Queue size decreases in proportion to the % novel completion --> queue size in map is the
    	// initial queue size
    	
    	double scaleFactor = Math.max(2, (100 - getPercentNovelCompletion())) / (100.0);
    	
    	if (currentMainCharacter == null) {
    		return RECENT_WORD_QUEUE_SIZE;
    	}
    	if (currentMainCharacter.equals("Aaron")) {
    		return (int) (CHARACTER_MAP[0][0] * scaleFactor);
    	} 
    	else if (currentMainCharacter.equals("Ameya")) {
    		return (int) (CHARACTER_MAP[1][0] * scaleFactor);
    	} 
    	else if (currentMainCharacter.equals("Aurora")) {
    		return (int) (CHARACTER_MAP[2][0] * scaleFactor);
    	} 
    	else if (currentMainCharacter.equals("Tammy")) {
    		return (int) (CHARACTER_MAP[3][0] * scaleFactor);
    	} 
    	else {
    		return Math.max(101, (int) (CHARACTER_MAP[4][0] * scaleFactor)); // essentially forces Vibhav to always select a new word
    	}
    	
    }
    
    /* GET CHARACTER'S PROBABILITY OF COMPLEX CLAUSE
     * Description - 	Retrieve the complexClauseProb from the map for the current main character
     * Input - 			None
     * Output -			complexClauseProb
     */
    static private int getComplexClauseProbability () {
    	// Probability increases in proportion to the % novel completion --> probability in map is the
    	// initial probability 
    	
    	double scaleFactor = Math.max(2, (100 - getPercentNovelCompletion())) / (100.0);
    	
    	if (currentMainCharacter == null) {
    		return 1;
    	}
    	if (currentMainCharacter.equals("Aaron")) {
    		return Math.max(1, (int) (CHARACTER_MAP[0][1] * scaleFactor));
    	} 
    	else if (currentMainCharacter.equals("Ameya")) {
    		return Math.max(2, (int) (CHARACTER_MAP[1][1] * scaleFactor));
    	} 
    	else if (currentMainCharacter.equals("Aurora")) {
    		return Math.max(4, (int) (CHARACTER_MAP[2][1] * scaleFactor));
    	} 
    	else if (currentMainCharacter.equals("Tammy")) {
    		return Math.max(7, (int) (CHARACTER_MAP[3][1] * scaleFactor));
    	} 
    	else {
    		return Math.max(9, (int) (CHARACTER_MAP[4][1] * scaleFactor)); // essentially Vibhav most likely never says complex sentences.
    	}
    }
    
    /* PRODUCE STRING FOR NOVEL PREMISE
     * Description - 	Simply returns the beginning of the novel where the premise is explained (to reduce the size of
     * 					the main function)
     * Input -			None
     * Output -			Beginning of the novel
     */
    static private String novelPremise () {
    	return "The world has been plagued by some mysterious infection which has caused all of Earth's inhabitants to lose their "
    			+ "cognitive, behavioral, and intellectual abilities. Words and ideas are still generated by their brains, but they've "
    			+ "lost the art of how to speak and convey them. We will follow the story of five computer science students from NC State "
    			+ "University, who desperately need the ability to think and do (the extraordinary) again to continue their studies. They "
    			+ "find an experimental drug to rid the infection from a mysterious Dr. Martens; however, Dr. Martens warns them that there "
    			+ "are exactly five experimental pills, each from different studies, so the effectiveness of each pill is unknown...\n"
    			+ "Dr. Martens explains that the medicine will take some time to kick in. Side affects include endless questioning about "
    			+ "the future and the lack of cohesive sentences. Eventually, the medicine should increase their intellectual abilities to "
    			+ "a certain intellect score. But what Dr. Martens is unsure of is how long these side affects will "
    			+ "last for each pill and each pill's intellect score."
    			+ "\n"
    			+ "So, each of our five computer science students takes a pick at the selection of available medicines..."
    			+ "\n"
    			+ "Now, we await and see the effects of their decisions."
    			+ "\n\n"
    			+ "CHARACTER MAP:"
    			+ "\n\t1. Aaron --> intellect score = 5"
    			+ "\n\t2. Ameya --> intellect score = 4"
    			+ "\n\t3. Aurora --> intellect score = 3"
    			+ "\n\t4. Tammy --> intellect score = 2"
    			+ "\n\t5. Vibhav --> intellect score = 1";
    }

    /* HANDLE ERROR
        Description -   Handle an error by spitting it to console and halting.
                        This is intended to catch errors quickly and halt, to aid in debugging.
        Input -         ERROR
        Output -        Information printed to console
    */
    static private void handleError (Throwable err) {
        System.out.println("ERROR: ");
        System.out.println(err.getMessage());
        System.out.println(err.getStackTrace());
    }

}