package com.comp90015;

import java.util.ArrayList;

public class Guest {
    private String identity;
    private int pPort;
    private int iPort;
    private ChatRoom currentChatRoom;
    private ChatPeer.ChatConnection chatConnection;

    public Guest() {}

    public Guest(String identity, int pPort, int iPort, ChatRoom currentChatRoom, ChatPeer.ChatConnection chatConnection) {
        this.identity = identity;
        this.pPort = pPort;
        this.iPort = iPort;
        this.currentChatRoom = currentChatRoom;
        this.chatConnection = chatConnection;
    }

    public String getIdentity() {
        return (identity.equals("localhost")?("127.0.0.1:" + pPort):(identity + ":" + iPort));
    }

    public int getpPort() {return pPort;}

    public int getiPort() {return iPort;}

    public ChatRoom getCurrentChatRoom() {return currentChatRoom;}

    public ChatPeer.ChatConnection getChatConnection() {return chatConnection;}

    public void setIdentity(String identity) {this.identity = identity;}

    public void setpPort(int pPort) {this.pPort = pPort;}

    public void setiPort(int pPort) {this.iPort = iPort;}

    public void setCurrentChatRoom(ChatRoom currentChatRoom) {this.currentChatRoom = currentChatRoom;}

    public void setChatConnection(ChatPeer.ChatConnection chatConnection) {this.chatConnection = chatConnection;}

}