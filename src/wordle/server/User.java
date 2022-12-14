package wordle.server;

import wordle.utils.Code;
import wordle.utils.Response;
import wordle.utils.Util;

public class User {
    private final String username;
    private final String psw;

    private GameSession game;
    private boolean playing;
    private boolean lastGuessedWord;

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
        this.playing = false;
        this.lastGuessedWord = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPsw() {
        return psw;
    }

    public void newGame(String currWord, int secretWordNum) {
        setPlaying(true);
        setLastGuessedWord(true);
        this.game.newGame(currWord, secretWordNum);
        return;
    }

    public void resetGame() {
        if(isPlaying()){
            setPlaying(false);
            this.gamesPlayed++;
            this.lastStreak = 0;
        }
        //this.game = new GameSession();
        return;
    }

    public Response takeGuess(String guessWord) {
        game.incGuess();
        String payload = Util.ConsoleColors.WHITE + "\nSecret Word N." + game.getSecretWordNum() + " Guess N. " + game.getGuess() + "/12\n" + Util.ConsoleColors.RESET;
        payload += game.updateTable(guessWord);
        if (guessWord.equals(game.getCurrentSecretWord())){
            setPlaying(false);
            this.gamesPlayed++;
            this.gamesWon++;
            this.lastStreak++;
            if(this.lastStreak > this.bestStreak)
                this.bestStreak = this.lastStreak;
            payload += Util.ConsoleColors.GREEN + "\nYou Won!" + Util.ConsoleColors.RESET + "\n";
            return new Response(Code.Win, payload);
        }
        if(game.getGuess() == 12){
            setPlaying(false);
            this.gamesPlayed++;
            this.lastStreak = 0;
            payload += Util.ConsoleColors.YELLOW + "\nSorry you lost :C" + Util.ConsoleColors.RESET + "\n";
            return new Response(Code.Lose, payload);
        }
        return new Response(Code.Success, payload);
    }

    public String getGuessTable(){
        String payload = Util.ConsoleColors.WHITE + "\nSecret Word N." + game.getSecretWordNum() + " Guess N." + game.getGuess() + "/12\n" + Util.ConsoleColors.RESET;
        payload += game.getGuessTable();
        return payload;
    }

    public String getShare(){
        String payload = Util.ConsoleColors.PURPLE + "\nUser: " +  this.getUsername() + Util.ConsoleColors.RESET;
        payload += Util.ConsoleColors.WHITE + "\nSecret Word N." + game.getSecretWordNum() + " Guesses " + game.getGuess() + "/12\n" + Util.ConsoleColors.RESET;
        payload += game.getShare();
        return payload;
    }

    public String currentSecretWord(){
        return this.game.getCurrentSecretWord();
    }

    public boolean isPlaying() {
        return playing;
    }

    private void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isLastGuessedWord() {
        return lastGuessedWord;
    }

    public void setLastGuessedWord(boolean lastGuessedWord) {
        this.lastGuessedWord = lastGuessedWord;
    }

    public String stats(){
        String stats = Util.ConsoleColors.PURPLE;
        stats += "\n" + this.getUsername() + " Stats: \n";
        stats += "Games Played: " + this.gamesPlayed + "\n";
        stats += "Games Won: " + this.gamesWon + "\n";
        stats += "Last Streak: " + this.lastStreak + "\n";
        stats += "Best Streak: " + this.bestStreak + "\n";
        stats += Util.ConsoleColors.RESET;
        return stats;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", psw=" + psw + ", gamesPlayed=" + gamesPlayed + ", gamesWon=" + gamesWon
                + ", lastStreak=" + lastStreak + ", bestStreak=" + bestStreak + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((psw == null) ? 0 : psw.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (psw == null) {
            if (other.psw != null)
                return false;
        } else if (!psw.equals(other.psw))
            return false;
        return true;
    }

}
