package com.comp90015;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ReceiverParser {
    private ChatPeerService chatPeerService;

    public ReceiverParser(ChatPeerService chatPeerService) {this.chatPeerService = chatPeerService;}

    public boolean parse(JSONObject msgObj) {
        String type = (String) msgObj.get("type");
        Guest user = chatPeerService.getChatConn2Guest().get(null);
        if(type.equals("message")) {
            System.out.println(msgObj.get("identity") + ": " + msgObj.get("content"));
            System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
        }else if(type.equals("roomchange")) {
            String identity = (String) msgObj.get("identity");
            String former = (String) msgObj.get("former");
            String roomId = (String) msgObj.get("roomid");

//            if(roomId.equals("")) { // leave room
//                if(identity.equals(clientState.getIdentity())) {
//                    System.out.println(identity + " has quit!");
//                    return false;
//                }else {
//                    System.out.println(identity + " has quit!");
//                    System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
//                }
//            }else if(former.equals(roomId)) {
//                System.out.println(roomId + " is invalid or non existent");
//                System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
//            }else {
//                if(former.equals("")) {
//                    clientState.setRoomId(roomId);
//                    System.out.println(identity + " moves to " + roomId);
//                    System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
//                }else {
//                    if(identity.equals(clientState.getIdentity())) {clientState.setRoomId(roomId);}
//                    System.out.printf("%s move from %s to %s\n",identity,former,roomId);
//                    System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
//                }
//            }
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
            System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
        }else if(type.equals("roomlist")) {
            JSONArray rooms = (JSONArray) msgObj.get("rooms");
                for(int i = 0; i < rooms.size(); i++) {
                    JSONObject room = (JSONObject) rooms.get(i);
                    String roomid = (String) room.get("roomid");
                    Long count = (Long) room.get("count");
                    System.out.println( roomid + " : " + count + ((count>1)?(" guests"):(" guest")));
                    System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + ":" + user.getpPort() + "> ");
                }
                //System.out.print("[" + clientState.getRoomId() + "] " + clientState.getIdentity() + "> ");
        }
        return true;
    }
}
