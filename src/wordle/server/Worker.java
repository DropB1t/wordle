package wordle.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import wordle.utils.Code;
import wordle.utils.Request;
import wordle.utils.Response;
import wordle.utils.Util;

public class Worker implements Callable<Response> {

    private final ConcurrentHashMap<Integer, User> loggedUsers;
    private final ShareController shareSocket;
    private final Integer clientID;
    private final Request req;
    private final WordManager wordSession;
    private final ResourceController rsc;

    public Worker(Request req, Integer clientID, ConcurrentHashMap<Integer, User> loggedUsers, WordManager wordSession, ShareController shareSocket) {
        this.req = req;
        this.clientID = clientID;
        this.loggedUsers = loggedUsers;
        this.shareSocket = shareSocket;
        this.wordSession = wordSession;
        rsc = new ResourceController();
    }

    @Override
    public Response call() throws Exception {
        Response res;
        switch (req.getType()) {
            case Register:
                res = register(req);
                break;
            case Login:
                res = login(req);
                break;
            case Logout:
                res = logout(true);
                break;
            case Play:
                res = play(req);
                break;
            case SendStats:
                res = stats();
                break;
            case SendWord:
                res = guess(req);
                break;
            case Share:
                res = share();
                break;
            default:
                res = new Response(Code.Logout, "");
                break;
        }
        //System.out.println(loggedUsers.toString());
        return res;
    }
    
    private Response register(Request req){
        Response res;
        String[] user_psw = req.getPayload().split(" ");

        if(rsc.checkIfUserExist(user_psw[0])){
            res = new Response(Code.ErrReg, Util.ConsoleColors.RED + "The username is already taken"+ Util.ConsoleColors.RESET);
        }else{
            User user = new User(user_psw[0], user_psw[1], 0, 0, 0, 0);
            rsc.saveUser(user);
            res = new Response(Code.SuccReg, Util.ConsoleColors.GREEN +"You sign in successfully"+ Util.ConsoleColors.RESET);
        }

        return res;
    }

    private Response login(Request req){
        String[] user_psw = req.getPayload().split(" ");

        if(!rsc.checkIfUserExist(user_psw[0])){
            return new Response(Code.ErrLog, Util.ConsoleColors.RED + "The username is incorrect"+ Util.ConsoleColors.RESET);
        }else{
            User user = rsc.getUser(user_psw[0]);
            if(loggedUsers.containsValue(user))
                return new Response(Code.ErrLog, Util.ConsoleColors.RED + "This account is already logged in"+ Util.ConsoleColors.RESET);
            if(!user.getPsw().equals(user_psw[1]))
                return new Response(Code.ErrPsw, Util.ConsoleColors.RED + "Password incorrect, please retry"+ Util.ConsoleColors.RESET);
            loggedUsers.putIfAbsent(clientID, user);
            if(!wordSession.getSecretWorld().equals(user.currentSecretWord()) && user.isLastGuessedWord())
                user.setLastGuessedWord(false);
        }
        return new Response(Code.SuccLog, Util.ConsoleColors.GREEN +"Logged in successfully"+ Util.ConsoleColors.RESET);
    }

    private Response logout(boolean resetGame){
        if(resetGame){
            User user = loggedUsers.get(clientID);
            user.resetGame();
            rsc.saveUser(user);
        }
        loggedUsers.remove(clientID);
        return new Response(Code.Logout, "");
    }

    private Response play(Request req){
        User user = loggedUsers.get(clientID);
        if (user.isPlaying()) {
            String payload = Util.ConsoleColors.RED + "\nYou are already in a game\n" + Util.ConsoleColors.RESET;
            payload += user.getGuessTable();
            return new Response(Code.ErrPlay, payload);
        }
        if (user.isLastGuessedWord() && wordSession.getSecretWorld().equals(user.currentSecretWord()))
            return new Response(Code.ErrPlay, Util.ConsoleColors.RED + "\nYou have already guessed current secret word. Please wait for the new secret word\n" + Util.ConsoleColors.RESET);
        
        user.newGame(wordSession.getSecretWorld(),wordSession.getSecretWorldNum());
        rsc.saveUser(user);
        return new Response(Code.Success, Util.ConsoleColors.GREEN + "You enter the game. Take your guess :)" + Util.ConsoleColors.RESET);
    }

    private Response guess(Request req){
        User user = loggedUsers.get(clientID);
        if (!user.isPlaying())
            return new Response(Code.ErrPlay, Util.ConsoleColors.RED + "\nYou are not playing. Please enter in a game\n" + Util.ConsoleColors.RESET);
        String guessWord = req.getPayload().trim();
        if (!wordSession.checkGuessWord(guessWord))
            return new Response(Code.Success, Util.ConsoleColors.YELLOW + "\nThe word does not appear in dictionary\n" + Util.ConsoleColors.RESET);

        Response res = user.takeGuess(guessWord);
        if(res.getCode() == Code.Win || res.getCode() == Code.Lose && wordSession.getSecretWorld().equals(user.currentSecretWord()))
            user.setLastGuessedWord(true);
            
        rsc.saveUser(user);
        return res;
    }

    private Response stats(){
        User user = loggedUsers.get(clientID);
        return new Response(Code.Success, user.stats());
    }

    private Response share(){
        User user = loggedUsers.get(clientID);
        shareSocket.send(user.getShare());
        return new Response(Code.Success, Util.ConsoleColors.GREEN + "Your game has been shared successfully" + Util.ConsoleColors.RESET);
    }

}
