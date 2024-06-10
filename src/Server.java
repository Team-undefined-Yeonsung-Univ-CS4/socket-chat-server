
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static final int PORT = 9998;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    private static Map<String, Room> roomMap = new HashMap<>();
    private static int userCount = 0;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("서버가 시작되었습니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 클라이언트 접속: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 현재 접속자 수 업데이트
     */
    private static void broadcastUserCount() {
        String userCntMsg = "USER_COUNT:" + userCount;
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(userCntMsg);
        }
    }

    private static void broadcastRoomList() {
        StringBuilder roomListMsg = new StringBuilder("ROOM_LIST:");

        // 방 목록을 생성 시점 기준으로 최신 순 정렬
        List<Room> sortedRooms = new ArrayList<>(roomMap.values());
        sortedRooms.sort((room1, room2) -> Long.compare(room2.getCreatedAt(), room1.getCreatedAt()));

        for (Room room : sortedRooms) {
            roomListMsg.append(room.getRoomId()).append(",")
                    .append(room.getTitle()).append(",")
                    .append(room.getCurrentMembers()).append("/")
                    .append(room.getMaxMembers()).append(",")
                    .append(room.getCreatorUid()).append(";");
        }
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(roomListMsg.toString());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String userId; // unique id

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 클라이언트가 접속하면 사용자 ID를 수신하고 접속자 수 증가
                userId = in.readLine();
                if (userId != null) {
                    synchronized (Server.class) {
                        userCount++;
                        System.out.println("현재 접속자 수: " + userCount);
                        broadcastUserCount();
                    }
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                    handleClientMessage(message);
                }
            } catch (IOException e) {
                System.out.println("클라이언트가 연결을 해제했습니다.");
            } finally {
                try {
                    clientSocket.close();
                    clientHandlers.remove(this);

                    // 클라이언트가 접속을 끊으면 접속자 수 차감
                    synchronized (Server.class) {
                        userCount--;
                        System.out.println("현재 접속자 수: " + userCount);
                        broadcastUserCount();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 클라이언트 소켓의 메시지를 분기 처리하는 함수
         * @param message 클라이언트 소켓으로부터 수신한 메시지
         */
        private void handleClientMessage(String message) {
            if (message.startsWith("109:")) {
                String[] parts = message.split(":");
                String title = parts[1];
                int membersCnt = Integer.parseInt(parts[2]);
                String uid = parts[3];

                String roomId = "room" + (roomMap.size() + 1);
                Room room = new Room(roomId, title, membersCnt, uid);
                roomMap.put(roomId, room);

                broadcastRoomList();
            } else {
                for (ClientHandler handler : clientHandlers) {
                    handler.sendMessage(message);
                }
            }
        }

        /**
         * 클라이언트에게 메시지 전송
         * @param message 메시지
         */
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
