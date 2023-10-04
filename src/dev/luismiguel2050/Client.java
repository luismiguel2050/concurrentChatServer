package dev.luismiguel2050;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e){
            closeEverything(socket, reader, writer);
        }
    }

    public void sendMessage(){
        try{
            writer.write(username);
            writer.newLine();
            writer.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                if(messageToSend.equals("/exit")){
                    System.exit(0);
                }
                writer.write(username + ": " + messageToSend);
                writer.newLine();
                writer.flush();

            }
        }catch (IOException e){
            closeEverything(socket, reader, writer);
        }
    }



    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;

                while ((socket.isConnected())){
                    try {
                        messageFromGroupChat = reader.readLine();
                        if(messageFromGroupChat == null){
                            closeEverything(socket,reader,writer);
                            System.out.println("Server is down! Come back later!");
                            System.exit(0);
                        }else  System.out.println(messageFromGroupChat);

                    }catch (IOException e){
                        closeEverything(socket, reader, writer);

                    }

                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

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

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 4000);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}

