package com.comp90015;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class ChatPeer {
    @Option(name = "-p", usage = "listening port")
    private int pPort = 4444;

    @Option(name = "-i", usage = "connecting port")
    private int iPort = -1;

    private ChatPeerService chatPeerService;

    public static void main(String[] args) {
        ChatPeer chatPeer = new ChatPeer();
        CmdLineParser parser = new CmdLineParser(chatPeer);

        try {
            parser.parseArgument(args);
            System.out.println("option: -p " + chatPeer.pPort);
            System.out.println("option: -i " + chatPeer.iPort);
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

        chatPeer.chatPeerService = new ChatPeerService(chatPeer.pPort, chatPeer.iPort);
        chatPeer.chatPeerService.init();
        chatPeer.handle();
    }

    public void handle() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(pPort);
            System.out.printf("[ChatPeer] listening on port %d...\n", pPort);

            Sender sender = new Sender();
            sender.start();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[ChatPeer] new client connected");
                ChatConnection connection = new ChatConnection(socket);
                connection.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            //serverWorker.work("", this);
            while (connectionAlive) {
                try {
                    String in = reader.readLine();
                    if (in != null) {
                        //serverWorker.work(in, this);
                        JSONObject msgObj = (JSONObject) new JSONParser().parse(in);
                        String type = (String) msgObj.get("type");
                        if(type.equals("quit")) {connectionAlive = false;}
                    } else {
                        // client disconnected
                        //serverWorker.work("{\"type\":\"quit\"}", this);
                        connectionAlive = false;
                    }
                } catch (Exception e) {
                    //serverWorker.work("{\"type\":\"quit\"}", this);
                    connectionAlive = false;
                }
            }
            close();
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
        private ParseSend parser;
        private boolean alive;

        public Sender() throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(System.in));
            this.alive = false;
            this.parser = new ParseSend(chatPeerService);
        }

        @Override
        public void run() {
            alive = true;
            while(alive) {
                try {
                    String msg = reader.readLine();
                    System.out.println("> " + msg);
//                    JSONObject msgObj = parser.parse(msg);
//                    if(msgObj!=null && !msgObj.isEmpty()) {
//                        chatPeerService.serve();
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    alive = false;
                }
            }
        }
    }
}
