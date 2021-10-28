package com.comp90015;

import org.json.simple.JSONObject;
import java.util.regex.Pattern;

public class SenderParser {

    public JSONObject parse(String msg) {
        JSONObject msgObj = new JSONObject();
        String[] msgArr = msg.split(" ");
        String roomIdPattern = "^[a-zA-Z]{1}[a-zA-Z0-9]{2,32}";
        if(!msgArr[0].startsWith("#")) {
            return msgObj = ClientMessages.message(msg);
        }else if(msgArr.length == 1) {
            if(msgArr[0].startsWith("#join")) {
                return msgObj = ClientMessages.join("");
            }else if(msgArr[0].startsWith("#list")) {
                return msgObj = ClientMessages.list();
            }else if(msgArr[0].startsWith("#quit")) {
                return msgObj = ClientMessages.quit();
            }else if(msgArr[0].startsWith("#help")) {
                return msgObj = ClientMessages.help();
            }else if(msgArr[0].startsWith("#listneighbors")) {
                return msgObj = ClientMessages.listNeighbors();
            }else if(msgArr[0].startsWith("#searchnetwork")){
                return msgObj = ClientMessages.searchNetwork();
            }else {
                System.out.println("Invalid command!");
            }
        }else if(msgArr.length >= 2) {
            if(msgArr[0].startsWith("#join")) {
                if(!Pattern.matches(roomIdPattern, msgArr[1]) && !msgArr[1].equals("")) {
                    System.out.println("Invalid roomId!");
                    return null;
                }
                return msgObj = ClientMessages.join(msgArr[1]);
            }else if(msgArr[0].startsWith("#who")) {
                if(!Pattern.matches(roomIdPattern, msgArr[1])) {
                    System.out.println("Invalid roomId!");
                    return null;
                }
                return msgObj = ClientMessages.who(msgArr[1]);
            }else if(msgArr[0].startsWith("#createroom")) {
                if(!Pattern.matches(roomIdPattern, msgArr[1])) {
                    System.out.println("Invalid roomId!");
                    return null;
                }
                return msgObj = ClientMessages.createRoom(msgArr[1]);
            }else if(msgArr[0].startsWith("#delete")) {
                if(!Pattern.matches(roomIdPattern, msgArr[1])) {
                    System.out.println("Invalid roomId!");
                    return null;
                }
                return msgObj = ClientMessages.delete(msgArr[1]);
            }else if(msgArr[0].startsWith("#kick")) {
                return msgObj = ClientMessages.kick(msgArr[1]);
            }else if(msgArr[0].startsWith("#connect")) {
                if(msgArr.length == 2) {
                    return msgObj = ClientMessages.connect(msgArr[1], "");
                }else if(msgArr.length == 3) {
                    return msgObj = ClientMessages.connect(msgArr[1], msgArr[2]);
                }
            }else {
                System.out.println("Invalid command!");
            }
        }else {
            System.out.println("Invalid command!");
        }
        return null;
    }
}
