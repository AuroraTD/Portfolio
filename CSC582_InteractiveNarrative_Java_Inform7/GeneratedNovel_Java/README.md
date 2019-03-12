# CSC582_HW04

## Run program
Option 1 (used during development)
- Open "NaturalLanguageGeneration" folder in your IDE of choice (we used VSCode)
- Open NaturalLanguageGeneration\src\app\App.java
- Run the "main" method
Option 2
- Open a terminal
- Navigate to NaturalLanguageGeneration\src\app
- Run the command "javac App.java"
- Run the command "java App"
*novel.txt* should be generated in the program folder or updated if it already exists. 
That wile will contain your newly generated 50k+ word novel!

## Existing Systems Used
The program utilizes the [Simple NLG Java API for Natural Language Generation](https://github.com/simplenlg/simplenlg). This API allows you to specify various parts of a sentence (subject, verb, object, conjunction, tense, interrogative, etc). The API takes each of these parts and forms them together into a fairly grammatically correct English sentence. 

## Language Structure & Generation
The basic structure of a sentence (main clause) is *subject* *verb* *object* with optional subordinate clause(s).
* *subject* or *object*: consists of a noun and may be accompanied by an adjective
* *verb*: may be accompanied by an adverb
* *tense*: past, present, future
* *negation*: "Alice did not..." instead of "Alice did..."
* *subordinate clause*: generated recursively the same way a main clause is generated

Words for each parts of a sentence are selected pseudorandomly. The program utilizes *java.util.Random* along with the percentage completion of the novel and a character map to determine how different parts defined above are generated to contribute to a particular sentence or clause.  

As a result, the sentence structure and content for each character can vary in accordance with their respective intellect scores (stored in `CHARACTER_MAP` described in **Data Structures** below). For example, a character with a higher intellect score will have a strong tendency to stick to the same topics (i.e. use more recently used words, have more complex sentences with subordinate clauses and adjectives/adverbs), while a less intelligent character will have a stronger tendency to skip from topic to topic with random word selections and less complex sentences.

## Inputs
* *NaturalLanguageGeneration\lib\default-lexicon.xml*, which is stored with our source code. This is a lexicon packaged with the Simple NLG Java API for Natural Language Generation. It contains thousands of words of various types (nouns, verbs, conjunctions, etc). The program reads in this xml file at the start and stores the lexicon into the *wordsByCategory* 'HashMap' (see **Data Structures** below).

## Outputs
* *novel.txt*, which contains the 50k+ word "novel," separated into chapters with paragraphs.

## Data Structures
* `static final String[] NAMES`: 1d array of strings holding character names
* `static final int[][] CHARACTER_MAP`: 2d array holding values for the initial recent word queue size and probability of complex clause for each character. These values correspond to the character's intellect score.
* `static HashMap<String, ArrayList<String>> wordsByCategory`: keys of this hashmap include "NOUN", "VERB", and other word types. The values are an array list of all words in its respective key's category (i.e. list of nouns). The words are parsed from the input lexicon *.xml file.
* `static HashMap<String, ArrayList<String>> mostRecentWordsByCategory`: same structure as 'wordsByCategory' above. However, the length of the array lists for each value are a length 'n', which is defined in the program. Changing 'n' changes the pool of recently used words a character can draw from. The smaller the value of 'n', the more likely the novel will "stay on topic."
