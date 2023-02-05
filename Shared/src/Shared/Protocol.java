package Shared;

public class Protocol {
    public static final String BROADCAST = "BCST";
    public static final String PRIVATE_MSG = "PM";
    public static final String RECEIVE_PRIVATE_MSG = "NPM";
    public static final String INITIALISE = "INIT";
    public static final String QUIT = "QUIT";
    public static final String JOINED = "JOINED";
    public static final String USERS = "USERS";
    public static final String DISCONNECT = "DSCN";
    public static final String IDENTIFY = "IDENT";
    public static final String OK = "OK";
    public static final String GOODBYE = "Goodbye";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String START_SURVEY = "SV";
    public static final String START_SURVEY_OPEN = "SVO";
    public static final String SURVEY_QUESTION = "SVQ";
    public static final String SURVEY_REQUEST = "SVR";
    public static final String SURVEY_ANSWER = "SVA";

    public static final String FAIL00 = "FAIL00 Unknown command";
    public static final String FAIL01 = "FAIL01 User already logged in";
    public static final String FAIL02 = "FAIL02 Username has an invalid format or length";
    public static final String FAIL03 = "FAIL03 Please log in first";
    public static final String FAIL04 = "FAIL04 User cannot login twice";
    public static final String FAIL05 = "FAIL05 Pong without ping";
    public static final String FAIL06 = "FAIL06 YOU ARE THE ONLY CONNECTED USER";
    public static final String FAIL07 = "FAIL07 USER DOES NOT EXIST";
    public static final String FAIL08 = "FAIL08 CANNOT START SURVEY, TO FEW USERS";
    public static final String FAIL09 = "FAIL09 SURVEY ALREADY STARTED";
    public static final String FAIL10 = "FAIL10 NO SURVEY RUNNING";
}

