package varietyofchatrooms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {
    private final Set<ClientHandler> members;
    //Set 인터페이스는 중복을 허용X

    public ChatRoom() {
        this.members = Collections.synchronizedSet(new HashSet<>());
        // Collections.synchronizedSet(Set<T> s) : 멀티 스레드 환경에서 공유되는 컬렉션의 동기화된 접근
        // 즉, 한 스레드가 members 컬렉션을 수정하는 동안 다른 스레드가 동시에 같은 컬렉션을 수정하려고 할 때,
        // Collections.synchronizedSet에 의해 동기화 처리가 이루어져 한 번에 하나의 스레드만 변경을 수행할 수 있게 됩니다.
        // HashSet<> 같은 값을 가진 객체를 두 번 추가할 수 없음
    }

    public Set<ClientHandler> getMembers() {
        return members;
    }

    public synchronized void addMember(ClientHandler member){
        members.add(member);
    }
    //코드 블록 앞에 synchronized 붙이면, 해당 메서드나 코드 블록은 한 번에 하나의 스레드만 실행
    //다른 스레드가 이미 synchronized 메서드나 블록을 실행하고 있다면, 추가적인 스레드들은 그 실행이 완료될 때까지 대기해야

    public synchronized void removeMember(ClientHandler member){
        members.remove(member);

    }

    public synchronized void broadcastMessage(String message, ClientHandler sender){
        for (ClientHandler member : members){
            if(member != sender){
                member.sendMessage(message);
            }
        }
    }
}