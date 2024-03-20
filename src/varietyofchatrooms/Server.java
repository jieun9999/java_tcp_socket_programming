package varietyofchatrooms;

import java.util.Collections;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final ServerSocket serverSocket;

    // Server 클래스는 채팅방 관리(채팅방 생성/삭제/목록 관리) 등에 더 집중하고,
    // 채팅방 안에 있는 멤버 관리는 ChatRoom 클래스에서 관리할 수 있도록 함
    private static final Map<Integer, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    //  이 맵은 서버가 관리하는 모든 채팅방을 저장하고 관리하는 데 사용됨
    // chatRooms 맵의 키(Integer)는 각 채팅방을 고유하게 식별하는 데 사용되는 채팅방 ID를 나타내며, 값(ChatRoom)은 해당 채팅방 ID에 해당하는 ChatRoom 객체
    // 멀티스레드 환경에서의 스레드 안전(Thread Safety): Server 클래스는 동시에 여러 클라이언트의 요청을 처리할 수 있어야 하며, 이 과정에서 여러 스레드가 chatRooms 맵에 동시에 접근
    // ConcurrentHashMap은 스레드 안전한 Map 구현으로, 여러 스레드가 동시에 맵을 수정하거나 조회할 때 발생할 수 있는 동시성 문제를 내부적으로 처리합니다

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;

    }

    public void startServer() throws IOException {
        try{
            while (!serverSocket.isClosed()){
                // 아래 과정이 반복되며, 서버는 계속해서 새로운 클라이언트 연결을 수락하고 각 연결에 대해 새로운 스레드를 생성하여 처리합니다.

                Socket socket = serverSocket.accept();
                // serverSocket.accept() 메소드 호출은 새로운 클라이언트 연결이 들어올 때까지 블로킹(대기) 상태가 됩니다.
                // 새로운 클라이언트가 연결을 시도하면, 이 메소드는 클라이언트와의 통신을 위한 Socket 인스턴스를 반환
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public static void addClientToChatRoom(int chatRoomId, ClientHandler clientHandler){
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        if(chatRoom == null){
            chatRoom = createChatRoom(chatRoomId);
        }
        chatRoom.addMember(clientHandler);
        clientHandler.setChatRoom(chatRoom);
    }

    public static ChatRoom getChatRoom(int chatRoomId){
        return chatRooms.get(chatRoomId);
    }

    public static ChatRoom createChatRoom(int chatRoomId){
        ChatRoom chatRoom = new ChatRoom();
        chatRooms.put(chatRoomId, chatRoom);
        return chatRoom;
    }

    public static void removeClientInChatRoom(ClientHandler clientHandler){
        ChatRoom chatRoom = clientHandler.getChatRoom();
        chatRoom.removeMember(clientHandler);

        if(chatRoom.getMembers().isEmpty()){
            removeChatRoom(chatRoom);
        }
    }

    public static void removeChatRoom(ChatRoom chatRoom){
        Integer roomId = findChatRoomIdByChatRoom(chatRoom);
        if(roomId != null){
            chatRooms.remove(roomId);
        }

    }

    public  static Integer findChatRoomIdByChatRoom(ChatRoom chatRoom){
        for(Map.Entry<Integer, ChatRoom> entry : chatRooms.entrySet()){
            if(entry.getValue().equals(chatRoom)){
                return entry.getKey();
            }
        }
        return null;
    }


    public static Map<Integer, ChatRoom> getAvailableChatRooms() {
        return Collections.unmodifiableMap(chatRooms);
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


}
