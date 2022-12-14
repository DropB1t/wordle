package wordle.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class of utilities functions
 */
public class Util {
    public static final long KILOBYTE = 1024L;
    public static final long MEGABYTE = 1024L * 1024L;

    public static void printException(Exception e) {
        System.out.print(ConsoleColors.RED);
        System.out.println("String: " + e.toString());
        System.out.println("Message: " + e.getMessage());
        System.out.println("StackTrace: ");
        System.out.print(ConsoleColors.RESET);
        e.printStackTrace();
        System.out.print("\n");
    }

    public static long bytesToKilobytes(long bytes) {
        return bytes / Util.KILOBYTE;
    }

    public static long bytesToMegabytes(long bytes) {
        return bytes / Util.MEGABYTE;
    }

    public static void memoryStats(){
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();

        System.out.print("\n" + ConsoleColors.YELLOW);
        //System.out.println("Used memory in bytes: " + memory);
        System.out.println("Used memory in kilobytes: " + bytesToKilobytes(memory));
        System.out.println("Used memory in megabytes: " + bytesToMegabytes(memory));
        System.out.print(ConsoleColors.RESET);
    }

    public static String hash(String str){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Util.printException(e);
        }
        return bytesToHex(digest.digest(str.getBytes()));
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public class ConsoleColors {
        // Reset
        public static final String RESET = "\033[0m"; // Text Reset

        // Colors
        public static final String BLACK = "\033[0;30m"; // BLACK
        public static final String RED = "\033[0;31m"; // RED
        public static final String GREEN = "\033[0;32m"; // GREEN
        public static final String YELLOW = "\033[0;33m"; // YELLOW
        public static final String BLUE = "\033[0;34m"; // BLUE
        public static final String PURPLE = "\033[0;35m"; // PURPLE
        public static final String CYAN = "\033[0;36m"; // CYAN
        public static final String WHITE = "\033[0;37m"; // WHITE

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m"; // BLACK
        public static final String RED_BACKGROUND = "\033[41m"; // RED
        public static final String GREEN_BACKGROUND = "\033[42m"; // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m"; // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m"; // CYAN
        public static final String WHITE_BACKGROUND = "\033[47m"; // WHITE
        
    }
}
