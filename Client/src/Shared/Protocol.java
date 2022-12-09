package Shared;

public class Protocol {
    public static final String BROADCAST = "BCST";
    public static final String INITIALISE = "INIT";
    public static final String QUIT = "QUIT";
    public static final String IDENTIFY = "IDENT";
    public static final String OK = "OK";
    public static final String GOODBYE = "Goodbye";
    public static final String PING = "PING";
    public static final String PONG = "PONG";

    public static final String FAIL00 = "FAIL00 Unknown command";
    public static final String FAIL01 = "FAIL01 User already logged in";
    public static final String FAIL02 = "FAIL02 Username has an invalid format or length";
    public static final String FAIL03 = "FAIL03 Please log in first";
    public static final String FAIL04 = "FAIL04 User cannot login twice";
    public static final String FAIL05 = "FAIL05 Pong without ping";
}
