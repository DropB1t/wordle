package wordle.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wordle.utils.Util;

public class WordManager {
    private final String wordsFilename = "./resources/words.txt";
    
    private final Iterator<Integer> wordDrawer;
    private final List<String> wordsCollection;
    private final HashSet<String> wordsDict;
    private final int wordsNum;

    private final long sessionDuration;
    private long sessionStart;

    private String secretWorld;
    private int secretWorldNum = 0;

    /**
     * 
     * The dictionary will be loaded into a List of String from which Secret Word will be drawn,
     * and into a HashSet to perfom searching of existing words in O(1)
     * <p>
     * wordDrawer is an iterator on integers that simulate effectively unlimited stream of pseudorandom int values
     * 
     * @param seed of random number generator for sequence of words
     * @param sessionDuration duration, in minutes, that one Secret Word will persist
     */
    public WordManager(long seed, long sessionDuration) {
        this.sessionDuration = sessionDuration * 60 * 1000; // In ms
        Stream<String> stream = null;

        try {
            stream = Files.lines(Paths.get(wordsFilename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Util.printException(e);
            System.exit(1);
        }

        Random random = new Random();
        random.setSeed(seed);

        wordsCollection = stream.collect(Collectors.toList());
        Collections.shuffle(wordsCollection,random);
        this.wordsNum = wordsCollection.size();

        wordDrawer = random.ints(0,this.wordsNum).iterator();
        wordsDict = new HashSet<String>(wordsCollection);

        this.setSecretWord();
    }

    /**
     * Check if the session was expired and update the current Secret Word
     * @return true if the current Secret Word was updated
     */
    synchronized public boolean updateSession() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - this.sessionStart) > this.sessionDuration) {
            this.setSecretWord();
            return true;
        }
        return false;
    }

    synchronized public boolean checkGuessWord(String guess){
        return this.wordsDict.contains(guess);
    }

    synchronized public String getSecretWorld() {
        return this.secretWorld;
    }

    synchronized public int getSecretWorldNum() {
        return this.secretWorldNum;
    }

    private void setSecretWord() {
        this.secretWorld = wordsCollection.get(wordDrawer.next());
        increaseSecretWorldNum();
        System.out.println(Util.ConsoleColors.CYAN + "Setting Secret Word n." + this.getSecretWorldNum() + " to " + this.getSecretWorld() + Util.ConsoleColors.RESET);      
        this.sessionStart = System.currentTimeMillis();
    }

    private void increaseSecretWorldNum() {
        this.secretWorldNum++;
    }

}
