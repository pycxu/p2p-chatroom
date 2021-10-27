package com.comp90015;

import org.json.simple.JSONObject;

import java.util.regex.Pattern;

public class ParseSend {
    private ChatPeerService chatPeerService;

    public ParseSend(ChatPeerService chatPeerService) {this.chatPeerService = chatPeerService;}

    public JSONObject parse(String msg) {
        JSONObject msgObj = new JSONObject();
        String[] msgArr = msg.split(" ");
        String roomIdPattern = "^[a-zA-Z]{1}[a-zA-Z0-9]{2,32}";
        Guest user = chatPeerService.getChatConn2Guest().get(null);
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
                System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
            }
        }else if(msgArr.length == 2) {
            if(msgArr[0].startsWith("#join")) {
                return msgObj = ClientMessages.join("");
            }
        }


        return null;
    }
}
