package varietyofchatrooms;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int userId;


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter userId for the group chat: ");
        int userId = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter chatRoomId for the group chat: ");
        int chatRoomId = Integer.parseInt(scanner.nextLine());

        Client client = new Client("localhost",1234, userId, chatRoomId);
        // 생성자에서 서버 연결과 초기 데이터 전송을 처리
        client.listenForMessage();
        client.sendMessage();

    }


    public Client(String host, int port, int userId, int chatRoomId) {
        try {
            this.socket = new Socket(host, port);
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userId = userId;

            // 서버에 사용자 ID와 채팅방 ID 전송
            sendInitialData(userId, chatRoomId);

        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }


    private  void sendInitialData(int userId, int chatRoomId){
        try {
            bufferedWriter.write(String.valueOf(userId)); //BufferedWriter가 텍스트 데이터를 사용
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(String.valueOf(chatRoomId));
            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e) {
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
                        //readLine 메소드는 서버로부터 새로운 메시지(줄)가 도착할 때까지 대기
                        System.out.println(msgFromGroupChat);
                        //수신된 메시지(msgFromGroupChat)는 콘솔에 출력
                    }catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void sendMessage(){
        try {
            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();

                if ("exit".equalsIgnoreCase(messageToSend)) {
                    bufferedWriter.write("exit");
                } else if ("yes".equalsIgnoreCase(messageToSend)) {
                    bufferedWriter.write("yes");
                }
                else if ("no".equalsIgnoreCase(messageToSend)) {
                    bufferedWriter.write("no");
                }
                else if ("1".equalsIgnoreCase(messageToSend)) {
                    bufferedWriter.write("1");
                }
                else if ("2".equalsIgnoreCase(messageToSend)) {
                    bufferedWriter.write("2");
                }else {
                    bufferedWriter.write("User" + userId + ": " + messageToSend);
                }

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

    // 사용자가 메세지를 보낼때는 스레드를 사용하지 않는 것처럼 보이는 이유?
    // 메인 스레드에서 처리하기 때문!
    // 보내는 것은 메인스레드로 하고, 읽어들이는 것은 보조 스레드
    // 메시지를 보내는 작업(sendMessage)은 사용자의 입력을 받아 즉시 처리할 수 있으므로 메인 스레드에서 실행되는 것이 일반적입니다. 반면, 메시지를 수신하는 작업(listenForMessage
    // 은 서버로부터 메시지가 언제 도착할지 알 수 없으므로 지속적인 대기 상태가 필요하고, 이는 별도의 스레드에서 처리하는 것이 바람직합니다.


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

}
