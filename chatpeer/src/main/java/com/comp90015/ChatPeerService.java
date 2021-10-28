package com.comp90015;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatPeerService {
    private volatile int pPort;
    private volatile int iPort;
    private volatile ChatRoom emptyRoom;
    private volatile ArrayList<ChatRoom> chatRooms;
    private volatile ArrayList<String> identities;
    private volatile ArrayList<String> banList;
    private volatile HashMap<String, ChatRoom> roomId2ChatRoom;
    private volatile HashMap<ChatPeer.ChatConnection, Guest> chatConn2Guest;
    private volatile Socket peerSocket;
    private volatile PrintWriter peerWriter;
    private volatile String peerIdentity;
    private volatile String peerCurrentRoom;

    public ChatPeerService(int pPort, int iPort) {
        this.pPort = pPort;
        this.iPort = iPort;
        this.emptyRoom = new ChatRoom("", null);
        this.chatRooms = new ArrayList<>();
        this.identities = new ArrayList<>();
        this.banList = new ArrayList<>();
        this.roomId2ChatRoom = new HashMap<>();
        this.chatConn2Guest = new HashMap<>();

        this.chatRooms.add(emptyRoom);
        this.roomId2ChatRoom.put(emptyRoom.getRoomId(), emptyRoom);
    }

    public void init() {
        Guest user = new Guest("localhost", pPort, iPort, emptyRoom, null);
        identities.add(user.getIdentity());
        chatConn2Guest.put(user.getChatConnection(), user);
        emptyRoom.addGuest(user);
        peerSocket = null;
        peerWriter = null;
    }

    public void serve(String msg, ChatPeer.ChatConnection chatConnection, ChatPeer.Receiver receiver) {
        try {
            if(chatConnection == null) { // local
                JSONObject msgObj = (JSONObject) new JSONParser().parse(msg);
                String type = (String) msgObj.get("type");
                if(peerSocket != null) { // connected to remote peer
                    if(type.equals("createroom") || type.equals("kick") || type.equals("delete")) {
                        System.out.println("Local commands are not allowed!");
                    }else if(type.equals("connect")) {
                        System.out.println("Already connected!");
                    }else {
                        peerMessage(msg); // forward msg to remote peer
                    }
                    System.out.print("[" + peerCurrentRoom + "] " + peerIdentity + "> ");

                }else {
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
                }
            }else{ // remote
                if(!this.chatConn2Guest.containsKey(chatConnection)) { // new client
                    newIdentity(chatConnection);
                }else {
                    JSONObject msgObj = (JSONObject) new JSONParser().parse(msg);
                    String type = (String) msgObj.get("type");
                    Guest guest = chatConn2Guest.get(chatConnection);
                    if (type.equals("hostchange")) {
                        hostchange((String) msgObj.get("host"), guest);
                    }else if (type.equals("join")) {
                        join((String) msgObj.get("roomid"), guest);
                    }else if (type.equals("who")) {
                        who((String) msgObj.get("roomid"), guest);
                    }else if (type.equals("list")) {
                        list(guest);
                    }else if (type.equals("listneighbors")) {
                        listneighbors(guest);
                    }else if (type.equals("quit")) {
                        quit(chatConnection);
                    }else if (type.equals("message")) {
                        message(msg, guest);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private synchronized void connect(String host, String port, Guest user, ChatPeer.Receiver receiver) {
        String[] hostParts = host.split(":");
        String hostName = hostParts[0];
        int hostPort = Integer.parseInt(hostParts[1]);

        try { //connect to the remote peer
            Socket s = new Socket();
            if(!port.equals("")) { //port number is provided
                s.bind(new InetSocketAddress(Integer.parseInt(port)));
                s.connect(new InetSocketAddress(hostName, hostPort));
            }else {
                if(this.iPort == -1) { //iPort is not provided
                    s = new Socket(hostName, hostPort);
                }else {
                    s.bind(new InetSocketAddress(this.iPort));
                    s.connect(new InetSocketAddress(hostName, hostPort));
                }
            }
            this.peerSocket = s;
            this.peerWriter = new PrintWriter(new OutputStreamWriter(this.peerSocket.getOutputStream(), "UTF-8"), true);
            receiver.setReader(this.peerSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject msgObj = ClientMessages.hostChange(user.getIdentity());
        peerMessage(msgObj.toJSONString());

        roomId2ChatRoom.get(user.getCurrentChatRoom().getRoomId()).removeGuest(user);
        chatConn2Guest.remove(null);
        identities.remove(user.getIdentity());
    }

    private void peerMessage(String msg) {
        peerWriter.println(msg);
        peerWriter.flush();
    }

    private void newIdentity(ChatPeer.ChatConnection chatConnection) {
        Guest guest = new Guest(
                chatConnection.getInetAddress(),
                -1,
                chatConnection.getiPort(),
                roomId2ChatRoom.get(""),
                chatConnection
        );
        chatConn2Guest.put(chatConnection, guest);
    }

    private void hostchange(String host, Guest guest) {
        String[] hostParts = host.split(":");
        //String hostName = hostParts[0];
        int hostPort = Integer.parseInt(hostParts[1]);
        guest.setpPort(hostPort);
        String ip = guest.getChatConnection().getInetAddress();
        ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
        BroadcastMsg broadcastMsg = new BroadcastMsg();;
        if(banList.contains(ip)) {
            JSONObject msgObj = ServerMessages.roomChange(guest.getIdentity(), "-", "*");
            broadcastMsg.setMsg(msgObj.toJSONString());
        }else {
            JSONObject msgObj = ServerMessages.roomChange(guest.getIdentity(), "-", "");
            broadcastMsg.setMsg(msgObj.toJSONString());
            identities.add(guest.getIdentity());
        }
        broadcastMsg.addChatConnection(guest.getChatConnection());
        broadcastMsgs.add(broadcastMsg);
        broadcast(broadcastMsgs);
    }

    private synchronized void join(String roomId, Guest guest) {
        ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
        BroadcastMsg broadcastMsg;
        JSONObject msgObj;

        if(!chatRooms.contains(roomId2ChatRoom.get(roomId)) || guest.getCurrentChatRoom().getRoomId().equals(roomId)) {
            if(guest.getChatConnection() == null) {System.out.println("The requested room is invalid or non existent");}
            msgObj = ServerMessages.roomChange(guest.getIdentity(), guest.getCurrentChatRoom().getRoomId(), guest.getCurrentChatRoom().getRoomId());
            broadcastMsg = new BroadcastMsg();
            broadcastMsg.setMsg(msgObj.toJSONString());
            broadcastMsg.addChatConnection(guest.getChatConnection());
            broadcastMsgs.add(broadcastMsg);
            broadcast(broadcastMsgs);
        }else {
            ArrayList<Guest> formerGuests = new ArrayList<>();
            ArrayList<Guest> latterGuests = new ArrayList<>();
            String formerRoomId = guest.getCurrentChatRoom().getRoomId();
//            if(!isConnected()) {
//                if(guest.getCurrentChatRoom() != null) {
//                    Guest user = chatConn2Guest.get(null);
//                    if(guest.getCurrentChatRoom().getRoomId().equals(user.getCurrentChatRoom().getRoomId())) {
//                        // same curent room
//                    }
//                }
//            }

            if(!roomId.equals("")) {
                latterGuests = roomId2ChatRoom.get(roomId).getGuests();
            }else {
                latterGuests.add(guest);
            }

            if(!guest.getCurrentChatRoom().getRoomId().equals("-")) {
                guest.getCurrentChatRoom().removeGuest(guest);
                if(!guest.getCurrentChatRoom().getRoomId().equals("")) {
                    formerGuests = guest.getCurrentChatRoom().getGuests();
                }

                if(guest.getCurrentChatRoom().getRoomId().equals("")) {
                    System.out.printf("%s moved to %s.\n", guest.getIdentity(), roomId);
                }else if(roomId.equals("")) {
                    System.out.printf("%s moved out from %s.\n",guest.getIdentity(),guest.getCurrentChatRoom().getRoomId());
                }else {
                    System.out.printf("%s moved from %s to %s.\n",guest.getIdentity(), guest.getCurrentChatRoom().getRoomId(), roomId);
                }
            }else {
                System.out.printf("%s joined the server\n", guest.getIdentity());
            }

            guest.setCurrentChatRoom(roomId2ChatRoom.get(roomId));
            roomId2ChatRoom.get(roomId).addGuest(guest);

            msgObj = ServerMessages.roomChange(guest.getIdentity(), formerRoomId, roomId);
            broadcastMsg = new BroadcastMsg();
            broadcastMsg.setMsg(msgObj.toJSONString());
            for(Guest g : formerGuests) {broadcastMsg.addChatConnection(g.getChatConnection());}
            for(Guest g : latterGuests) {broadcastMsg.addChatConnection(g.getChatConnection());}
            broadcastMsgs.add(broadcastMsg);
            broadcast(broadcastMsgs);
        }
    }

    private synchronized void who(String roomId, Guest guest) {
        if(!roomId2ChatRoom.containsKey(roomId)) {return;}
        if(guest.getChatConnection() == null) {
            ChatRoom chatRoom = roomId2ChatRoom.get(roomId);
            if(chatRoom.getGuests().size() == 0) {
                System.out.println(roomId + " is empty");
            }else {
                System.out.print(roomId + " contains");
                for(Guest g : chatRoom.getGuests()) {
                    System.out.print(" " + g.getIdentity());
                }
                System.out.println();
            }
        }else {
            ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
            BroadcastMsg broadcastMsg;
            JSONObject msgObj = ServerMessages.roomContents(roomId2ChatRoom.get(roomId));
            broadcastMsg = new BroadcastMsg();
            broadcastMsg.setMsg(msgObj.toJSONString());
            broadcastMsg.addChatConnection(guest.getChatConnection());
            broadcastMsgs.add(broadcastMsg);
            broadcast(broadcastMsgs);
        }
    }

    private synchronized void list(Guest guest) {
        if(guest.getChatConnection() == null) {
            if(chatRooms.size() == 1) {
                System.out.println("There are no rooms.");
            }else {
                for(ChatRoom chatRoom: chatRooms) {
                    if(chatRoom.getRoomId() != "") {
                        System.out.println( chatRoom.getRoomId() + " : " + chatRoom.getGuests().size() + ((chatRoom.getGuests().size()>1)?(" guests"):(" guest")));
                    }
                }
            }
        }else {
            ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
            BroadcastMsg broadcastMsg;
            JSONObject msgObj = ServerMessages.roomList(chatRooms);
            broadcastMsg = new BroadcastMsg();
            broadcastMsg.setMsg(msgObj.toJSONString());
            broadcastMsg.addChatConnection(guest.getChatConnection());
            broadcastMsgs.add(broadcastMsg);
            broadcast(broadcastMsgs);
        }
    }

    private synchronized void createRoom(String roomId, Guest user) {
        if(!roomId2ChatRoom.containsKey(roomId)) {
            ChatRoom newChatRoom = new ChatRoom(roomId, user);
            this.chatRooms.add(newChatRoom);
            this.roomId2ChatRoom.put(roomId, newChatRoom);
            System.out.printf("%s created.\n", roomId);
        }else {
            System.out.printf("Room %s is invalid or already in use.\n", roomId);
        }
    }

    private synchronized void quit(ChatPeer.ChatConnection chatConnection) {
        // construct quit msg to all clients in the same room
        ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
        Guest guest = chatConn2Guest.get(chatConnection);
        JSONObject msgObj = ServerMessages.roomChange(guest.getIdentity(), guest.getCurrentChatRoom().getRoomId(), "-");
        BroadcastMsg broadcastMsg = new BroadcastMsg();
        broadcastMsg.setMsg(msgObj.toJSONString());
        if(guest.getCurrentChatRoom().getRoomId() == "") {
            broadcastMsg.addChatConnection(guest.getChatConnection());
        }else {
            for(Guest g : guest.getCurrentChatRoom().getGuests()) {
                broadcastMsg.addChatConnection(g.getChatConnection());
            }
        }
        broadcastMsgs.add(broadcastMsg);

        guest.getCurrentChatRoom().removeGuest(guest);
        identities.remove(guest.getIdentity());
        chatConn2Guest.remove(chatConnection);
        broadcast(broadcastMsgs);
    }

    private synchronized void delete(String roomId, Guest guest) throws ParseException {
        // finish join first
    }

    private void listneighbors(Guest guest) {

    }

    private void searchnetwork() {

    }

    private void kick(String identity, Guest guest) {

    }

    private void help() {
        System.out.println("#help - list this information");
        System.out.println("#connect IP[:port] [local port] - connect to another peer");
        System.out.println("#createroom [roomId] - create a room");
        System.out.println("#delete [roomId] - delete a room");
        System.out.println("#join [roomId] - join a room");
        System.out.println("#list - list all rooms");
        System.out.println("#who [roomId] - show guests in a room");
        System.out.println("#kick [identity] - kick a user");
        System.out.println("#listneighbors - get a list of peers’ network addresses");
        System.out.println("#searchnetwork - get a list of chat rooms over all accessible peers");
        System.out.println("#quit - disconnect from a peer");
    }

    private synchronized void message(String msg, Guest guest) throws ParseException {
        if(guest.getCurrentChatRoom().getRoomId() != "") {
            ArrayList<BroadcastMsg> broadcastMsgs = new ArrayList<>();
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(msg);
            String content = (String) jsonObject.get("content");
            JSONObject msgObj = ServerMessages.message(guest.getIdentity(), content);
            BroadcastMsg broadcastMsg = new BroadcastMsg();
            broadcastMsg.setMsg(msgObj.toJSONString());
            for(Guest g : guest.getCurrentChatRoom().getGuests()) {
                if(g != guest) {
                    if(g.getChatConnection() != null) {
                        broadcastMsg.addChatConnection(g.getChatConnection());
                    }else {
                        System.out.printf("%s: %s\n", guest.getIdentity(), content);
                    }

                }
            }
            broadcastMsgs.add(broadcastMsg);
            broadcast(broadcastMsgs);
        }
    }

    private synchronized void broadcast(ArrayList<BroadcastMsg> broadcastMsgs) {
        for(BroadcastMsg broadcastMsg : broadcastMsgs) {
            for(ChatPeer.ChatConnection chatConnection : broadcastMsg.getChatConnections()) {
                if(chatConnection != null) {
                    chatConnection.sendMessage(broadcastMsg.getMsg());
                    //System.out.println("broadcast: " + broadcastMsg.getMsg());
                }
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

    public String getPeerIdentity() {return this.peerIdentity;}

    public String getPeerCurrentRoom() {return this.peerCurrentRoom;}

    public synchronized void setPeerIdentity(String peerIdentity) {this.peerIdentity = peerIdentity;}

    public synchronized void  setPeerCurrentRoom(String peerCurrentRoom) {this.peerCurrentRoom = peerCurrentRoom;}

    public boolean isConnected() {return (this.peerSocket != null)?(true):(false);}
}
