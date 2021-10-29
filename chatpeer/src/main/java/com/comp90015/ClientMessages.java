package com.comp90015;

import org.json.simple.JSONObject;

/**
 * This class constructs C2S protocol messages in the form af JSON object.
 * @author Yongchao XU
 * @author Guhan ZHANG
 */
public class ClientMessages {

    // local
    public static JSONObject createRoom(String roomId) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "createroom");
        msgObj.put("roomid", roomId);
        return msgObj;
    }

    public static JSONObject kick(String identity) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "kick");
        msgObj.put("identity", identity);
        return msgObj;
    }

    public static JSONObject help() {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "help");
        return msgObj;
    }

    public static JSONObject delete(String roomId) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "delete");
        msgObj.put("roomid", roomId);
        return msgObj;
    }

    // remote
    public static JSONObject connect(String ip, String port) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "connect");
        msgObj.put("ip", ip);
        msgObj.put("port", port);
        return msgObj;
    }

    public static JSONObject join(String roomId) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "join");
        msgObj.put("roomid", roomId);
        return msgObj;
    }

    public static JSONObject who(String roomId) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "who");
        msgObj.put("roomid", roomId);
        return msgObj;
    }

    public static JSONObject list() {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "list");
        return msgObj;
    }

    public static JSONObject quit() {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "quit");
        return msgObj;
    }

    public static JSONObject message(String content) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "message");
        msgObj.put("content", content);
        return msgObj;
    }

    public static JSONObject hostChange(String host) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "hostchange");
        msgObj.put("host", host);
        return msgObj;
    }

    public static JSONObject listNeighbors() {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "listneighbors");
        return msgObj;
    }

    public static JSONObject searchNetwork() {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "searchnetwork");
        return msgObj;
    }

    public static JSONObject shout(String content) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "shout");
        msgObj.put("content", content);
        return msgObj;
    }
    public static JSONObject shoutMessage(String identity, String content) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "shoutmessage");
        msgObj.put("identity", identity);
        msgObj.put("content", content);
        return msgObj;
    }
}