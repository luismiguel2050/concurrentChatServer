package dev.luismiguel2050;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private final ServerSocket serverSocket;
    private final int PORT;

    public Server(){
        try {
            PORT = 4000;
            this.serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startServer(){
        System.out.println("Server starting");
        System.out.println("Listening on port " + PORT);

        try{

            while(!serverSocket.isClosed()){

                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                System.out.println(clientHandler.clientUsername);

                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket(){

        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable{

        public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientUsername;

        public ClientHandler(Socket socket){
            try {
                this.socket = socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.clientUsername = bufferedReader.readLine();
                clientHandlers.add(this);
                broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");


            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

        @Override
        public void run() {
            String messageFromClient;
            try {
                bufferedWriter.write("Welcome " + clientUsername + ". Type /help for useful commands");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                while (socket.isConnected()) {
                    try {
                        messageFromClient = bufferedReader.readLine();
                        if (messageFromClient == null) {
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            return;
                        } else if (messageFromClient.contains("/help")) {
                            bufferedWriter.write("/username - send private message\n/users - show users in chat\n/exit - exit chat");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();

                        }
                        else
                        broadcastMessage(messageFromClient);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        public void broadcastMessage(String messageToSend){
            for(ClientHandler clientHandler : clientHandlers){
                try{
                    if(!clientHandler.equals(this)){
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                }catch (IOException e){
                    closeEverything(socket, bufferedReader,bufferedWriter);

                }
            }
        }

        public void removeClientHandler(){
            clientHandlers.remove((this));
            System.out.println(clientUsername + " has left");
            broadcastMessage("SERVER: " + clientUsername + " has left the chat!");

        }

        public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
            removeClientHandler();
            try{

                if(bufferedReader != null) {
                    bufferedReader.close();
                }
                if(bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
