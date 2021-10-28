package com.comp90015;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ReceiverParser {
    private ChatPeerService chatPeerService;

    public ReceiverParser(ChatPeerService chatPeerService) {this.chatPeerService = chatPeerService;}

    public synchronized boolean parse(JSONObject msgObj) {
        String type = (String) msgObj.get("type");
        Guest user = chatPeerService.getChatConn2Guest().get(null);
        if(type.equals("message")) {
            System.out.println(msgObj.get("identity") + ": " + msgObj.get("content"));
        }else if(type.equals("roomchange")) {
            String identity = (String) msgObj.get("identity");
            String former = (String) msgObj.get("former");
            String roomId = (String) msgObj.get("roomid");
            if(former.equals(roomId)) {
                System.out.println("The requested room is invalid or non existent");
            }else {
                if(former.equals("-") && !roomId.equals("*")) {
                    System.out.printf("%s joined the server.\n",identity);
                    chatPeerService.setPeerIdentity(identity);
                }else if(roomId.equals("-")) {
                    System.out.printf("%s left the server.\n", identity);
                }else if(former.equals("") && !roomId.equals("*")) {
                    System.out.printf("%s moved to %s.\n", identity, roomId);
                }else if(roomId.equals("")) {
                    System.out.printf("%s moved out from %s.\n", identity, former);
                }else if(roomId.equals("*")) {
                    System.out.println("You are kicked and blocked from reconnecting");
                    chatPeerService.init();
                    return false;
                }else {
                    System.out.printf("%s moved from %s to %s.\n", identity, former, roomId);
                }
                if(identity.equals(chatPeerService.getPeerIdentity())) {
                    if(!roomId.equals("-") && !roomId.equals("*")) {
                        chatPeerService.setPeerCurrentRoom(roomId);
                    }else {
                        chatPeerService.init();
                        return false;
                    }
                }
            }
        }else if(type.equals("roomcontents")) {
            JSONArray identities = (JSONArray) msgObj.get("identities");
            if(identities.size() == 0) {
                System.out.println((String) msgObj.get("roomid") + " is empty");
            }else {
                System.out.print((String) msgObj.get("roomid") + " contains");
                for(int i = 0; i < identities.size(); i++) {
                    System.out.print(" " + identities.get(i));
                }
                System.out.println();
            }
        }else if(type.equals("roomlist")) {
            JSONArray rooms = (JSONArray) msgObj.get("rooms");
            if(rooms.size() == 0) {
                System.out.println("There are no rooms.");
            }else{
                for(int i = 0; i < rooms.size(); i++) {
                    JSONObject room = (JSONObject) rooms.get(i);
                    String roomid = (String) room.get("roomid");
                    Long count = (Long) room.get("count");
                    System.out.println( roomid + " : " + count + ((count>1)?(" guests"):(" guest")));
                }
            }
        }
        return true;
    }
}
