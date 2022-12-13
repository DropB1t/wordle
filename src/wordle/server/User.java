package wordle.server;

public class User {
    private final String username;
    private final String psw;

    private GameSession game;

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

        this.game = new GameSession();
    }

    public String getUsername() {
        return username;
    }

    public String getPsw() {
        return psw;
    }

    public boolean isPlaying() {
        return this.game.isPlaying();
    }

    public void setPlaying(boolean playing) {
        this.game.setPlaying(playing);
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", psw=" + psw + ", gamesPlayed=" + gamesPlayed + ", gamesWon=" + gamesWon
                + ", lastStreak=" + lastStreak + ", bestStreak=" + bestStreak + "]";
    }

}
