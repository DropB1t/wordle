package wordle.server;

public class User {
    private final String username;
    private final String psw;

    private String currentSecretWord;
    private boolean playing;
    private int gamesPlayed;
    private int gamesWon;
    private int lastStreak;
    private int bestStreak;

    public User(String username, String psw, int gamesPlayed, int gamesWon, int lastStreak, int bestStreak) {
        this.username = username;
        this.psw = psw;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.lastStreak = lastStreak;
        this.bestStreak = bestStreak;

        this.playing = false;
        this.currentSecretWord = "";
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", currentSecretWord=" + currentSecretWord + ", playing=" + playing
                + ", gamesPlayed=" + gamesPlayed + ", gamesWon=" + gamesWon + ", lastStreak=" + lastStreak
                + ", bestStreak=" + bestStreak + "]";
    }

    public String getUsername() {
        return username;
    }

    public String getPsw() {
        return psw;
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

}
