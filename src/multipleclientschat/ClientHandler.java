package multipleclientschat;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    //This list tracks all active clients connected to the server

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;

    public ClientHandler(Socket socket) throws IOException {
        // 1. Initializing the ClientHandler
        try {
            this.socket = socket;
            //represents the connection to a client.
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //This setup allows the server to send text data to the client efficiently.
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //This setup enables the server to read text data sent by the client efficiently
            this.clientUserName = bufferedReader.readLine();
            // BufferedReader 객체를 통해 입력 스트림으로부터 한 줄의 텍스트를 읽습니다.
            clientHandlers.add(this);
            // This list keeps track of all active client handlers, allowing the server to broadcast messages to all connected clients
            broadcastMessage("SERVER: "+ clientUserName + " has entered the chat!");
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    @Override
    public void run() {
        // Infinite Loop for Listening to Client Messages
        // When the run method is executed (which happens in a separate thread for each client),
        // it enters an infinite loop that continuously listens for messages from the connected client.
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                // This is a blocking call; it waits until a line of text is received from the client or until the connection is closed.
                if(messageFromClient == null){
                    // readLine() returns null, it indicates that the connection to the client has been closed
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    //step1. server needs to clean up resources associated with this client
                    notifyDisconnection();
                    //step2. notify other clients about the disconnection
                    break;
                    //step3. exit the loop
                }
                broadcastMessage(messageFromClient);
            }catch (IOException e){
                //This method is designed to close the socket and the input/output streams associated with this client, ensuring that all resources are properly released
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public  void broadcastMessage(String messageToSend){
        //Iterating Over All Connected Clients
        for (ClientHandler clientHandler: clientHandlers){
            try {
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    //checks if the clientUserName is not the same as the sender's username
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    //adds a new line to indicate the end of the message
                    clientHandler.bufferedWriter.flush();
                    //flushes the stream to ensure the message is sent immediately.
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                //(which might happen if the client's connection has been lost), the server again calls closeEverything for that client to clean up resources.
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        //Removing the Client Handler

        //to close the socket and the input/output streams associated with this client, ensuring that all resources are properly released
        try{
            if(socket != null){
                socket.close();
            }
            if (bufferedReader!= null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void notifyDisconnection(){
        if(clientUserName != null){
            broadcastMessage("SERVER: "+ clientUserName + " has left this chat!");
            // This message is sent to all remaining clients connected to the server, informing them of the disconnection.
        }
    }


    public void removeClientHandler(){
        clientHandlers.remove(this);
        //removeClientHandler 메소드는 clientHandlers 리스트에서 현재 ClientHandler 인스턴스를 제거하는 역할만 수행합니다.
        //이 메소드는 ClientHandler 인스턴스의 필드 값, 특히 clientUserName의 값에는 영향을 주지 않습니다.
    }


}
