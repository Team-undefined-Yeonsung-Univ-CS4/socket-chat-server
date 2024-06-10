
public class Room {
    private String roomId;
    private String title;
    private int currentMembers;
    private int maxMembers;
    private long createdAt;
    private String creatorUid;

    public Room(String roomId, String title, int maxMembers, String creatorUid) {
        this.roomId = roomId;
        this.title = title;
        this.currentMembers = 0;
        this.maxMembers = maxMembers;
        this.createdAt = System.currentTimeMillis(); // 방 순서 정렬을 위한 생성 시점 기록
        this.creatorUid = creatorUid; //생성한 사람 uid
    }

    public String getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public int getCurrentMembers() {
        return currentMembers;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCurrentMembers(int currentMembers) {
        this.currentMembers = currentMembers;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public void addMember() {
        if (currentMembers < maxMembers) {
            currentMembers++;
        }
    }

    public void removeMember() {
        if (currentMembers > 0) {
            currentMembers--;
        }
    }
}
