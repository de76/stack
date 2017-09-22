package com.sufnom.sys;

import netscape.javascript.JSObject;
import org.json.JSONObject;

public class Config {
    public static final String FILE_NAME = "stack-config.json";

    public static final String KEY_STACK_PATH = "stack_path";
    public static final String KEY_STACK_PORT = "stack_port";

    private static final Config session = new Config();
    public static Config getSession() { return session; }

    private JSONObject config;
    private Config(){
        config = new JSONObject(Common.readFile(FILE_NAME));
    }

    public String getValue(String key){
        return config.getString(key);
    }
}
