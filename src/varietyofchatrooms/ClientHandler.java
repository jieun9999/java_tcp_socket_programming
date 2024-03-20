package varietyofchatrooms;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int userId;
    private ChatRoom chatRoom;
    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    public ChatRoom getChatRoom() {
        return this.chatRoom;
    }



    public ClientHandler(Socket socket) throws IOException {

        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userId = Integer.parseInt(bufferedReader.readLine()); // 첫 번째 줄로 사용자 ID를 읽음
            int chatRoomId = Integer.parseInt(bufferedReader.readLine()); // 두 번째 줄로 채팅방 ID를 읽음

            //서버 콘솔에 출력
            System.out.println("A new client has connected to ChatRoom"+ chatRoomId);

            Server.addClientToChatRoom(chatRoomId, this);
            chatRoom.broadcastMessage("/// User"+userId + " has entered ChatRoom" + chatRoomId+ " ///",this);

        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }


    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                if("exit".equals(messageFromClient)){
                    Server.removeClientInChatRoom(this);
                    notifyDisconnection();
                    promptForNewRoom();
                    // 이 메서드 안에서 사용자의 응답에 따라 다른 채팅방에 입장하거나 연결을 종료합니다.
                    break;
                }
                // 채팅방에 메시지를 보낼 때는 chatRoom의 broadcastMessage를 사용
                chatRoom.broadcastMessage(messageFromClient, this);

            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    // 사용자가 exit을 쳤을때, 채팅방에 들어갈 것인지 물어보고, 만약 안들어간다고 하면은 그대로 종료하고,
    // 다른 채팅방에 들어간다고 하면은 계속 다른 채팅방으로 연결

    private void notifyDisconnection(){
            chatRoom.broadcastMessage("XXX User"+ userId + " has left this chatRoom XXX", this);

    }

    private void promptForNewRoom() throws IOException {
        bufferedWriter.write("Would you like to enter another chat room? (yes/no)\n");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        String response = bufferedReader.readLine();

        if("yes".equalsIgnoreCase(response)){
            printAvailableRooms();
            String roomChoice = bufferedReader.readLine();
            int newChatRoomId = Integer.parseInt(roomChoice);
            switchRoom(newChatRoomId);
            run();

        } else if("no".equalsIgnoreCase((response))) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            // 왜 null 오류가 나는지 모르겠음...
        }

    }

    private void printAvailableRooms() throws IOException {
        Map<Integer, ChatRoom> availableRooms = Server.getAvailableChatRooms();
        if (availableRooms.isEmpty()) {
            bufferedWriter.write("There are no available chat rooms at the moment.\n");
        } else {
            bufferedWriter.write("Available chat rooms are:\n");
            for (Map.Entry<Integer, ChatRoom> entry : availableRooms.entrySet()) {
                bufferedWriter.write("Room ID: " + entry.getKey() + "\n");
            }
        }
        bufferedWriter.write("Please select a chat room by ID:\n");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }



    private void switchRoom(int newChatRoomId) throws IOException {
        Server.addClientToChatRoom(newChatRoomId, this);
        chatRoom = Server.getChatRoom(newChatRoomId);
        chatRoom.broadcastMessage("/// User" + userId + " has entered ChatRoom" + newChatRoomId + " ///", this);
    }

    public  void sendMessage(String message){
            try {
                    bufferedWriter.write(message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);

            }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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


}
