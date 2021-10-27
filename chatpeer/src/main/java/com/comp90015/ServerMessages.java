package com.comp90015;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * This class construct S2C protocol message in the form of JSON object.
 * @author Yongchao XU
 * @author Guhan ZHANG
 */
public class ServerMessages {

    public static JSONObject roomChange(String identity, String former, String roomId) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "roomchange");
        msgObj.put("identity", identity);
        msgObj.put("former", former);
        msgObj.put("roomid", roomId);
        return msgObj;
    }

    public static JSONObject roomContents(ChatRoom chatRoom) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "roomcontents");
        msgObj.put("roomid", chatRoom.getRoomId());
        JSONArray identities = new JSONArray();
        for(Guest g : chatRoom.getGuests()) {identities.add(g.getIdentity());}
        msgObj.put("identities", identities);
        //msgObj.put("owner", chatRoom.getOwner()!=null?chatRoom.getOwner().getIdentity():"");
        return msgObj;
    }

    public static JSONObject roomList(ArrayList<ChatRoom> chatRooms) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "roomlist");
        JSONArray rooms = new JSONArray();
        for(ChatRoom chatRoom : chatRooms) {
            JSONObject room = new JSONObject();
            room.put("roomid", chatRoom.getRoomId());
            room.put("count", chatRoom.getGuests().size());
            rooms.add(room);
        }
        msgObj.put("rooms", rooms);
        return msgObj;
    }

    public static JSONObject message(String identity, String content) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "message");
        msgObj.put("identity", identity);
        msgObj.put("content", content);
        return msgObj;
    }

    public static JSONObject neighbors(ArrayList<String> neighbors) {
        JSONObject msgObj = new JSONObject();
        msgObj.put("type", "neighbors");
        JSONArray neighborsArr = new JSONArray();
        for(String neighbor : neighbors) {neighborsArr.add(neighbor);}
        msgObj.put("neighbors", neighborsArr);
        return msgObj;
    }
}
