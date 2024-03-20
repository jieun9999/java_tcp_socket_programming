package multipleclientschat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    // 사용자가 메세지를 보낼때는 스레드를 사용하지 않는 것처럼 보이는 이유?
    // 메인 스레드에서 처리하기 때문!
    // 보내는 것은 메인스레드로 하고, 읽어들이는 것은 보조 스레드
    // 메시지를 보내는 작업(sendMessage)은 사용자의 입력을 받아 즉시 처리할 수 있으므로 메인 스레드에서 실행되는 것이 일반적입니다. 반면, 메시지를 수신하는 작업(listenForMessage
    // 은 서버로부터 메시지가 언제 도착할지 알 수 없으므로 지속적인 대기 상태가 필요하고, 이는 별도의 스레드에서 처리하는 것이 바람직합니다.
    public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username+ ": "+messageToSend);
                //소켓의 출력 스트림에 문자열 데이터를 쓰기
                bufferedWriter.newLine();
                // 메시지의 끝을 나타냄
                bufferedWriter.flush();
                // 버퍼에 저장된 모든 데이터를 즉시 출력 스트림으로 보내고, 그 것이 연결된 서버의 소켓까지 이동함
                // 연결된 서버의 소켓은 데이터를 받고 나서 모든 클라이언트 소켓에게 다시 데이터를 전달(글쓴이 제외)
            }
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()){
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    }catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
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

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username for the group chat: ");
        String username = scanner.nextLine();

        Socket socket = new Socket("localHost", 1234);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}
