package Shared;

public class Printer {
    public enum ConsoleColour {
        BLUE,
        PURPLE,
        CYAN,
        YELLOW,
        GREEN,
        RED,
        WHITE,
        BLACK
    }

    //eew magic strings
    private final static String BLUE = "\033[0;34m";
    private final static String BOLD_BLUE = "\033[1;34m";
    private final static String UNDERLINED_BLUE = "\033[4;34m";
    private final static String PURPLE = "\033[0;35m";
    private final static String BOLD_PURPLE = "\033[1;35m";
    private final static String UNDERLINED_PURPLE = "\033[4;35m";
    private final static String CYAN = "\033[0;36m";
    private final static String BOLD_CYAN = "\033[1;36m";
    private final static String UNDERLINED_CYAN = "\033[4;36m";
    private final static String YELLOW = "\033[0;33m";
    private final static String BOLD_YELLOW = "\033[1;33m";
    private final static String UNDERLINED_YELLOW = "\033[4;33m";
    private final static String GREEN = "\033[0;32m";
    private final static String BOLD_GREEN = "\033[1;32m";
    private final static String UNDERLINED_GREEN = "\033[4;32m";
    private static final String RED = "\033[0;31m";
    private final static String BOLD_RED = "\033[1;31m";
    private final static String UNDERLINED_RED = "\0\033[4;31m";
    private final static String WHITE = "\033[0;37m";
    private final static String BOLD_WHITE = "\033[1;37m";
    private final static String UNDERLINED_WHITE = "\033[4;37m";
    private static final String BLACK = "\033[0;30m";
    private final static String BOLD_BLACK = "\033[1;30m";
    private final static String UNDERLINED_BLACK = "\033[4;30m";
    private final static String RESET = "\033[0m";

    public static void printLineColour(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.println(BLACK + message + RESET);
            case RED -> System.out.println(RED + message + RESET);
            case GREEN -> System.out.println(GREEN + message + RESET);
            case YELLOW -> System.out.println(YELLOW + message + RESET);
            case BLUE -> System.out.println(BLUE + message + RESET);
            case PURPLE -> System.out.println(PURPLE + message + RESET);
            case CYAN -> System.out.println(CYAN + message + RESET);
            case WHITE -> System.out.println(WHITE + message + RESET);
        }
    }

    public static void printColour(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.printf(BLACK + message + RESET);
            case RED -> System.out.printf(RED + message + RESET);
            case GREEN -> System.out.printf(GREEN + message + RESET);
            case YELLOW -> System.out.printf(YELLOW + message + RESET);
            case BLUE -> System.out.printf(BLUE + message + RESET);
            case PURPLE -> System.out.printf(PURPLE + message + RESET);
            case CYAN -> System.out.printf(CYAN + message + RESET);
            case WHITE -> System.out.printf(WHITE + message + RESET);
        }
    }

    public static void printLineColourBold(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.println(BOLD_BLACK + message + RESET);
            case RED -> System.out.println(BOLD_RED + message + RESET);
            case GREEN -> System.out.println(BOLD_GREEN + message + RESET);
            case YELLOW -> System.out.println(BOLD_YELLOW + message + RESET);
            case BLUE -> System.out.println(BOLD_BLUE + message + RESET);
            case PURPLE -> System.out.println(BOLD_PURPLE + message + RESET);
            case CYAN -> System.out.println(BOLD_CYAN + message + RESET);
            case WHITE -> System.out.println(BOLD_WHITE + message + RESET);
        }
    }

    public static void printColourBold(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.printf(BOLD_BLACK + message + RESET);
            case RED -> System.out.printf(BOLD_RED + message + RESET);
            case GREEN -> System.out.printf(BOLD_GREEN + message + RESET);
            case YELLOW -> System.out.printf(BOLD_YELLOW + message + RESET);
            case BLUE -> System.out.printf(BOLD_BLUE + message + RESET);
            case PURPLE -> System.out.printf(BOLD_PURPLE + message + RESET);
            case CYAN -> System.out.printf(BOLD_CYAN + message + RESET);
            case WHITE -> System.out.printf(BOLD_WHITE + message + RESET);
        }
    }

    public static void printLineColourUnderline(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.println(UNDERLINED_BLACK + message + RESET);
            case RED -> System.out.println(UNDERLINED_RED + message + RESET);
            case GREEN -> System.out.println(UNDERLINED_GREEN + message + RESET);
            case YELLOW -> System.out.println(UNDERLINED_YELLOW + message + RESET);
            case BLUE -> System.out.println(UNDERLINED_BLUE + message + RESET);
            case PURPLE -> System.out.println(UNDERLINED_PURPLE + message + RESET);
            case CYAN -> System.out.println(UNDERLINED_CYAN + message + RESET);
            case WHITE -> System.out.println(UNDERLINED_WHITE + message + RESET);
        }
    }

    public static void printColourUnderline(String message, ConsoleColour colour) {
        switch (colour) {
            case BLACK -> System.out.printf(UNDERLINED_BLACK + message + RESET);
            case RED -> System.out.printf(UNDERLINED_RED + message + RESET);
            case GREEN -> System.out.printf(UNDERLINED_GREEN + message + RESET);
            case YELLOW -> System.out.printf(UNDERLINED_YELLOW + message + RESET);
            case BLUE -> System.out.printf(UNDERLINED_BLUE + message + RESET);
            case PURPLE -> System.out.printf(UNDERLINED_PURPLE + message + RESET);
            case CYAN -> System.out.printf(UNDERLINED_CYAN + message + RESET);
            case WHITE -> System.out.printf(UNDERLINED_WHITE + message + RESET);
        }
    }
}
