package com.comp90015;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatPeerService {
    private volatile int pPort;
    private volatile int iPort;
    private volatile ChatRoom emptyRoom;
    private volatile ArrayList<ChatRoom> chatRooms;
    private volatile ArrayList<String> identities;
    private volatile HashMap<String, ChatRoom> roomId2ChatRoom;
    private volatile HashMap<ChatPeer.ChatConnection, Guest> chatConn2Guest;


    public ChatPeerService(int pPort, int iPort) {
        this.pPort = pPort;
        this.iPort = iPort;
        this.emptyRoom = new ChatRoom("", null);
        this.chatRooms = new ArrayList<>();
        this.identities = new ArrayList<>();
        this.roomId2ChatRoom = new HashMap<>();
        this.chatConn2Guest = new HashMap<>();

        this.chatRooms.add(emptyRoom);
        this.roomId2ChatRoom.put(emptyRoom.getRoomId(), emptyRoom);
    }

    public void init() {
        Guest user = new Guest("127.0.0.1", pPort, emptyRoom, null);
        identities.add(user.getIdentity());
        chatConn2Guest.put(user.getChatConnection(), user);
        emptyRoom.addGuest(user);
    }

    public void serve() {

    }

    public HashMap<ChatPeer.ChatConnection, Guest> getChatConn2Guest() {return this.chatConn2Guest;}
}
