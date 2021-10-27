package com.comp90015;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.PrintWriter;
import java.net.Socket;
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
    private volatile Socket peerSocket;
    private volatile PrintWriter peerWriter;


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
        peerSocket = null;
        peerWriter = null;
    }

    private void peerMessage(String msg) {
        peerWriter.println(msg);
        peerWriter.flush();
    }

    public void serve(String msg, ChatPeer.ChatConnection chatConnection, ChatPeer.Receiver receiver) {
        try {
            JSONObject msgObj = (JSONObject) new JSONParser().parse(msg);
            String type = (String) msgObj.get("type");
            if(chatConnection == null) { // local
                if(peerSocket != null) { // connected to remote peer
                    if(type.equals("createroom") || type.equals("kick") || type.equals("delete")) {
                        System.out.println("Local commands are not allowed!");
                    }else if(type.equals("connect")) {
                        System.out.println("Already connected!");
                    }else if(type.equals("message")) {
                        peerMessage(msg);
                    }
                }
                Guest user = chatConn2Guest.get(null);
                if (type.equals("connect")) {
                    connect((String) msgObj.get("ip"), (String) msgObj.get("port"), user, receiver);
                }else if (type.equals("join")) {
                    join((String) msgObj.get("roomid"), user);
                }else if (type.equals("who")) {
                    who((String) msgObj.get("roomid"), user);
                }else if (type.equals("list")) {
                    list(user);
                }else if (type.equals("createroom")) {
                    createRoom((String) msgObj.get("roomid"), user);
                }else if (type.equals("delete")) {
                    delete((String) msgObj.get("roomid"), user);
                }else if (type.equals("listneighbors")) {
                    listneighbors(user);
                }else if (type.equals("searchnetwork")) {
                    searchnetwork();
                }else if (type.equals("kick")) {
                    kick((String) msgObj.get("identity"), user);
                }else if (type.equals("quit")) {
                    System.out.println("You are not connected to a remote peer! Use Ctrl-D to terminate this peer process.");
                }else if (type.equals("help")) {
                    help();
                }else if (type.equals("message")) {
                    message(msg, user);
                }

            }else{ // remote
                if (!this.chatConn2Guest.containsKey(chatConnection)) { // new client
                    newIdentity(chatConnection);
                } else {
                    Guest guest = chatConn2Guest.get(chatConnection);
                    //System.out.printf("[client] %s: %s\n", guest.getIdentity(), msg);
                    if (type.equals("hostchange")) {
                        hostchange((String) msgObj.get("host"), guest);
                    } else if (type.equals("join")) {
                        join((String) msgObj.get("roomid"), guest);
                    } else if (type.equals("who")) {
                        who((String) msgObj.get("roomid"), guest);
                    } else if (type.equals("list")) {
                        list(guest);
                    } else if (type.equals("createroom")) {
                        createRoom((String) msgObj.get("roomid"), guest);
                    } else if (type.equals("delete")) {
                        delete((String) msgObj.get("roomid"), guest);
                    } else if (type.equals("quit")) {
                        quit(chatConnection);
                    } else if (type.equals("message")) {
                        message(msg, guest);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private void connect(String ip, String port, Guest user, ChatPeer.Receiver receiver) {

    }

    private void newIdentity(ChatPeer.ChatConnection chatConnection) {

    }

    private void hostchange(String host, Guest guest) {

    }

    private void join(String roomid, Guest guest) {

    }

    private void who(String roomid, Guest guest) {

    }

    private void list(Guest guest) {

    }

    private void createRoom(String roomid, Guest guest) {

    }

    private void quit(ChatPeer.ChatConnection chatConnection) {

    }

    private void delete(String roomid, Guest guest) {

    }

    private void listneighbors(Guest guest) {

    }

    private void searchnetwork() {

    }

    private void kick(String identity, Guest guest) {

    }

    private void help() {

    }

    private void message(String msg, Guest guest) {

    }

    private synchronized void broadcast(ArrayList<BroadcastMsg> broadcastMsgs) {
        for(BroadcastMsg broadcastMsg : broadcastMsgs) {
            for(ChatPeer.ChatConnection chatConnection : broadcastMsg.getChatConnections()) {
                chatConnection.sendMessage(broadcastMsg.getMsg());
            }
        }
    }

    /**
     * This class stores the broadcast message and defines the audience.
     */
    class BroadcastMsg {
        private String msg;
        private ArrayList<ChatPeer.ChatConnection> chatConnections;

        public BroadcastMsg() {this.chatConnections = new ArrayList<>();}

        public String getMsg() {return msg;}

        public ArrayList<ChatPeer.ChatConnection> getChatConnections() {return chatConnections;}

        public void setMsg(String msg) {this.msg = msg;}

        public void addChatConnection(ChatPeer.ChatConnection chatConnection) {this.chatConnections.add(chatConnection);}
    }

    public HashMap<ChatPeer.ChatConnection, Guest> getChatConn2Guest() {return this.chatConn2Guest;}
}
