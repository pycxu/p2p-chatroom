package com.comp90015;

import java.util.ArrayList;

public class Guest {
    private String identity;
    private int pPort;
    private ChatRoom currentChatRoom;
    private ChatPeer.ChatConnection chatConnection;

    public Guest(String identity, int pPort, ChatRoom currentChatRoom, ChatPeer.ChatConnection chatConnection) {
        this.identity = identity;
        this.pPort = pPort;
        this.currentChatRoom = currentChatRoom;
        this.chatConnection = chatConnection;
    }

    public String getIdentity() {return identity;}

    public int getpPort() {return pPort;}

    public ChatRoom getCurrentChatRoom() {return currentChatRoom;}

    public ChatPeer.ChatConnection getChatConnection() {return chatConnection;}

    public void setIdentity(String identity) {this.identity = identity;}

    public void setpPort(int pPort) {this.pPort = pPort;}

    public void setCurrentChatRoom(ChatRoom currentChatRoom) {this.currentChatRoom = currentChatRoom;}

    public void setChatConnection(ChatPeer.ChatConnection chatConnection) {this.chatConnection = chatConnection;}

}