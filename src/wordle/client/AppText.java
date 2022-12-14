package wordle.client;

import wordle.utils.Util;

public class AppText {

    public static void welcomeText() {
        System.out.print( Util.ConsoleColors.PURPLE + "\n| Welcome to Wordle 2.0 |\n\n" + Util.ConsoleColors.RESET );
    }
    
    public static void accessText() {
        System.out.print( "\n" + Util.ConsoleColors.WHITE );
        System.out.println(" Access Menu ");
        System.out.println(" 1. Sign in  ");
        System.out.println(" 2. Log in   ");
        System.out.println(" 3. Exit     ");
        System.out.print( Util.ConsoleColors.RESET + "\n");
    }

    public static void menuText() {
        System.out.print( "\n" + Util.ConsoleColors.WHITE);
        System.out.println("  Main Menu   ");
        System.out.println(" 1. Play      ");
        System.out.println(" 2. Guess     ");
        System.out.println(" 3. Stats     ");
        System.out.println(" 4. ShowPosts ");
        System.out.println(" 5. Logout    ");
        System.out.print( Util.ConsoleColors.RESET + "\n" );
    }

    public static void shareText() {
        System.out.print( "\n" + Util.ConsoleColors.WHITE );
        System.out.println("Do you want to share your last game ? [yes/no]");
        System.out.print( Util.ConsoleColors.RESET + "\n" );
    }

}
