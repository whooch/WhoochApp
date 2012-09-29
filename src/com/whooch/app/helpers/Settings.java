package com.whooch.app.helpers;

public class Settings {

    public static final String cdnUrl                = "https://c705603.ssl.cf2.rackcdn.com/";
    public static final String host                    = "www.whooch.com";
    public static final String testHost                = "108.166.96.201";
    //public static final String apiUrl                 = "http://" + host + "/api";
    public static final String apiUrl                = "http://" + testHost + "/api";
    
    public static final String defaultWhoochImageUriSmall = cdnUrl + "s_defaultWhooch.png";
    public static final String defaultWhoochImageUriMedium = cdnUrl + "m_defaultWhooch.png";
    public static final String defaultWhoochImageUriLarge = cdnUrl + "l_defaultWhooch.png";
    
    // MAX_POST_LENGTH is also defined in strings.xml
    public static final int MAX_POST_LENGTH            = 151;
}