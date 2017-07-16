package com.logansoft.lubo.loganmeeting.beans;

/**
 * Created by logansoft on 2017/7/14.
 */

public class RoomInfoBean {
    private String roomName;
    private String moderator;
    private String roomNumber;
    private String waitCount;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getWaitCount() {
        return waitCount;
    }

    public void setWaitCount(String waitCount) {
        this.waitCount = waitCount;
    }
}
