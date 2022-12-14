package wordle.server;

import wordle.utils.Util;

public class GameSession {

    private String currentSecretWord;
    private int secretWordNum;
    private int guess;
    private String guessTable;
    private String share;

    public GameSession() {
        this.currentSecretWord = "";
        this.secretWordNum = 0;
        this.guess = 0;
        this.guessTable = "";
        this.share = "";
    }

    public void newGame(String currentSecretWord, int secretWordNum){
        setCurrentSecretWord(currentSecretWord);
        setSecretWordNum(secretWordNum);
        resetGuessNum();
        resetGuessTable();
        resetShare();
    }

    public String updateTable(String guessWord){
        int len = currentSecretWord.length();
        for (int i = 0; i < len; i++) {
            if (guessWord.charAt(i) == currentSecretWord.charAt(i)) { 
                guessTable += Util.ConsoleColors.GREEN + guessWord.charAt(i);
                share += Util.ConsoleColors.GREEN + "█";
            } else if (currentSecretWord.indexOf(guessWord.charAt(i)) != -1){
                guessTable += Util.ConsoleColors.YELLOW + guessWord.charAt(i);
                share += Util.ConsoleColors.YELLOW + "█";
            } else {
                guessTable += Util.ConsoleColors.WHITE + guessWord.charAt(i);
                share += Util.ConsoleColors.WHITE + "█";
            }
            if (i<len-1){
                guessTable += " ";
                share += " ";
            }
        }
        guessTable += Util.ConsoleColors.RESET + "\n";
        share += Util.ConsoleColors.RESET + "\n\n";
        return guessTable;
    }

    public String getGuessTable() {
        return guessTable;
    }

    public String getShare() {
        return share;
    }
    
    public String getCurrentSecretWord() {
        return currentSecretWord;
    }

    public int getGuess() {
        return guess;
    }

    public void incGuess() {
        this.guess++;
    }

    public int getSecretWordNum() {
        return secretWordNum;
    }

    private void setCurrentSecretWord(String currentSecretWord) {
        this.currentSecretWord = currentSecretWord;
    }

    private void setSecretWordNum(int secretWordNum) {
        this.secretWordNum = secretWordNum;
    }

    private void resetGuessTable() {
        this.guessTable = "";
    }

    private void resetShare() {
        this.share = "";
    }

    private void resetGuessNum() {
        this.guess = 0;
    }

}
