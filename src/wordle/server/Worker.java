package wordle.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import wordle.utils.Code;
import wordle.utils.Request;
import wordle.utils.Response;

public class Worker implements Callable<Response> {

    private final ConcurrentHashMap<Integer, User> loggedUsers;
    private final ShareController shareSocket;
    private final Integer clientID;
    private final Request req;
    private final WordManager wordSession;

    public Worker(Request req, Integer clientID, ConcurrentHashMap<Integer, User> loggedUsers, WordManager game, ShareController shareSocket) {
        this.req = req;
        this.clientID = clientID;
        this.loggedUsers = loggedUsers;
        this.shareSocket = shareSocket;
        this.wordSession = game;
    }

    @Override
    public Response call() throws Exception {
        Response res;
        switch (req.getType()) {
            case Register:
                res = register(req);
                break;
            case Login:
            case Logout:
            case Play:
            case SendStats:
            case SendWord:
            case Share:
            default:
                res = null;
                break;
        }
        return res;
    }
    
    private Response register(Request req){
        Response res;
        ResourceController rsc = new ResourceController();
        String[] user_psw = req.getPayload().split(" ");

        if(rsc.checkIfUserExist(user_psw[0])){
            res = new Response(Code.ErrReg, "The username is already taken");
        }else{
            User user = new User(user_psw[0], user_psw[1], 0, 0, 0, 0);
            rsc.saveUser(user);
            res = new Response(Code.Success, "Hello " + user_psw[0] + " your password is " + user_psw[1]);
        }

        return res;
    }

}
