package com.comp90015;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatPeer {
    @Option(name = "-p", usage = "listening port")
    private int pPort = 4444;

    @Option(name = "-i", usage = "connecting port")
    private int iPort = -1;

    private ChatPeerService chatPeerService;

    private Sender sender;
    private Receiver receiver;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        ChatPeer chatPeer = new ChatPeer();
        CmdLineParser parser = new CmdLineParser(chatPeer);

        try {
            parser.parseArgument(args);
            //System.out.println("option: -p " + chatPeer.pPort);
            //System.out.println("option: -i " + chatPeer.iPort);
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

        chatPeer.chatPeerService = new ChatPeerService(chatPeer.pPort, chatPeer.iPort);
        chatPeer.chatPeerService.init();
        chatPeer.handle(chatPeer);
    }

    public void handle(ChatPeer chatPeer) {
        try {
            this.serverSocket = new ServerSocket(pPort);
            System.out.printf("[ChatPeer] listening on port %d...\n", pPort);

            this.receiver = new Receiver();
            receiver.start();

            this.sender = new Sender(chatPeer, receiver);
            sender.start();

            while (true) {
                Socket socket = serverSocket.accept();
                //System.out.println("Port:" + socket.getPort());
                ChatConnection connection = new ChatConnection(socket);
                connection.start();
            }
        } catch (IOException e) {
            System.out.println("Peer process terminated...");
        }
    }

    class ChatConnection extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private boolean connectionAlive = false;

        public ChatConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        }

        @Override
        public void run() {
            connectionAlive = true;
            // register new client
            chatPeerService.serve("", this, null);
            while (connectionAlive) {
                try {
                    String in = reader.readLine();
                    if (in != null) {
                        //System.out.println("client: " + in);
                        chatPeerService.serve(in, this, null);
                        JSONObject msgObj = (JSONObject) new JSONParser().parse(in);
                        String type = (String) msgObj.get("type");
                        if(type.equals("quit")) {connectionAlive = false;}
                    } else {
                        // client disconnected
                        chatPeerService.serve("{\"type\":\"quit\"}", this, null);
                        connectionAlive = false;
                    }
                } catch (Exception e) {
                    chatPeerService.serve("{\"type\":\"quit\"}", this, null);
                    connectionAlive = false;
                }
            }
            close();
        }

        public String getInetAddress() {
            return socket.getInetAddress().toString().split("/")[1];
        }

        public int getiPort() {
            return socket.getPort();
        }

        public void close() {
            try {
                socket.close();
                reader.close();
                writer.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
            writer.flush();
        }
    }

    class Sender extends Thread {
        private BufferedReader reader;
        private SenderParser senderParser;
        private boolean alive;
        private Guest user;
        private Receiver receiver;
        private ChatPeer chatPeer;

        public Sender(ChatPeer chatPeer, Receiver receiver) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(System.in));
            this.alive = false;
            this.senderParser = new SenderParser();
            this.user = chatPeerService.isConnected()?null:chatPeerService.getChatConn2Guest().get(null);
            this.receiver = receiver;
            this.chatPeer = chatPeer;
        }

        @Override
        public void run() {
            alive = true;
            while(alive) {
                try {
                    if(!chatPeerService.isConnected()) {
                        System.out.print("[" + user.getCurrentChatRoom().getRoomId() + "] " + user.getIdentity() + "> ");
                    }
                    String msg = reader.readLine();
                    if(msg != null) {
                        JSONObject msgObj = senderParser.parse(msg);
                        if(msgObj!=null && !msgObj.isEmpty()) {
                            //System.out.println("sent: " + msgObj.toJSONString());
                            chatPeerService.serve(msgObj.toJSONString(), null, receiver);
                        }
                    }else {
                        //System.out.println("ctrl d");
                        chatPeerService.terminate();
                        chatPeer.receiver.interrupt();
                        chatPeer.serverSocket.close();
                        alive = false;
                        chatPeer.sender.interrupt();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    alive = false;
                }
            }
        }
    }

    class Receiver extends Thread {
        private volatile BufferedReader reader;
        private ReceiverParser receiverParser;
        private JSONParser jsonParser;
        private boolean alive;
        private Guest user;

        public Receiver() throws IOException {
            this.reader = null;
            this.alive = false;
            this.receiverParser = new ReceiverParser(chatPeerService);
            this.jsonParser = new JSONParser();
            this.user = chatPeerService.isConnected()?null:chatPeerService.getChatConn2Guest().get(null);
        }

        public void setReader(Socket socket) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //System.out.println("set receiver socket");
        }

        @Override
        public void run() {
            alive = true;
            while(alive) {
                if(this.reader != null) {
                    try {
                        String response = reader.readLine();
                        if(response != null) {
                            //System.out.println("received: " + response);
                            JSONObject msgObj = (JSONObject) jsonParser.parse(response);
                            alive = receiverParser.parse(msgObj);
                            if(alive == false) {
                                close();
                                alive = true;
                            }
                            if(chatPeerService.isConnected()) {
                                System.out.print("[" + chatPeerService.getPeerCurrentRoom() + "] " + chatPeerService.getPeerIdentity() + "> ");
                            }
                        }else {
                            System.out.println("Disconnected from peer!");
                            close();
                        }
                    } catch (IOException | ParseException e) {
                        if(receiver.reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                                //ex.printStackTrace();
                            }
                        }
                        //e.printStackTrace();
                        alive = false;
                    }
                }
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void close() {
            try {
                if(reader != null) {
                    reader.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            reader = null;
            chatPeerService.init();
        }
    }
}
