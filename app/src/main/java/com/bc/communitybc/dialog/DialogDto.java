package com.bc.communitybc.dialog;

public class DialogDto {
    private long id;
    private String name;
    private long user_id;
    private String lastMessage;

    public DialogDto() {}
    public DialogDto(String name, long user_id, String lastMessage) {
        this.name = name;
        this.user_id = user_id;
        this.lastMessage = lastMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
