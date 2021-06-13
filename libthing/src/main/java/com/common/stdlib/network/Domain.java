package com.common.stdlib.network;


/**
 * Created by xzw on 2016/6/9.
 */
public class Domain {
    private static String NAME;
    private static String BaseUrl;

    public static String baseUrl() {
        return BaseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        BaseUrl = baseUrl;
    }

    //switch server for debug or test ...
    public static void switchServer(Server server) {
        setBaseUrl(server.server);
        NAME = server.name();
    }

    public static String name() {
        return NAME;
    }

    public static void setName(String name) {
        NAME = name;
    }

    public enum Server {
        //local server
        //http:// or https://
        LOCAL(""),
        //online server
        ON_LINE(""),
        //test server
        TEST(""),
        //simulation server
        SIM(""),
        //custom
        CUSTOM("");
        private String server;

        Server(String server) {
            this.server = server;
        }

        public String value() {
            return server;
        }
    }
}
