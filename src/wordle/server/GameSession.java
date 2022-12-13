package wordle.server;

public class GameSession {

    private String currentSecretWord;
    private boolean playing;
    private int guess;

    public GameSession() {
        this.currentSecretWord = "";
        this.playing = false;
        this.guess = 0;
    }

    public String getCurrentSecretWord() {
        return currentSecretWord;
    }

    public void setCurrentSecretWord(String currentSecretWord) {
        this.currentSecretWord = currentSecretWord;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public int getGuess() {
        return guess;
    }

    private void incGuess() {
        this.guess++;
    }

}
