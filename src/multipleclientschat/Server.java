package multipleclientschat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    //for listening ro clients who wish to connect

    private ServerSocket serverSocket;
    //responsible for listening for incoming connections or clients and
    //creating a socket object to communicate with them

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;

    }

    public void startServer() throws IOException {
        try{

            while (!serverSocket.isClosed()){
                //waiting for a client to connect
                Socket socket = serverSocket.accept();
                //serverSocket.accept == block method : program will be halted here until a client connects.
                //however when a client does connect : a socket object is returned which can be used to communicate with the client.
                System.out.println("A new client has connected!");

                ClientHandler clientHandler = new ClientHandler(socket);
                //ClientHandler: each object of this class will be responsible for communicating with a client.
                //              this class will implement the interface runnable(is implemented on a class whose instances will each be executed by a separate thread)
                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public void closeServerSocket(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        // port number has to match when we create our client
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
