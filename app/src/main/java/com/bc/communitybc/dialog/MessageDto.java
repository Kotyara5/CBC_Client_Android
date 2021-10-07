package com.bc.communitybc.dialog;

public class MessageDto {
    private long id;
    private long dialog_id;
    private long sender_id;
    private String sender_name;
    private String content;
    private String timestamp;
    private EnumMessageType type;
    private EnumMessageStatus status;

    public MessageDto() {}
    public MessageDto(long dialog_id, long sender_id, String sender_name, String content, EnumMessageType type, String timestamp) {
        this.dialog_id = dialog_id;
        this.sender_id = sender_id;
        this.sender_name = sender_name;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDialog_id() {
        return dialog_id;
    }

    public void setDialog_id(long dialog_id) {
        this.dialog_id = dialog_id;
    }

    public long getSender_id() {
        return sender_id;
    }

    public void setSender_id(long sender_id) {
        this.sender_id = sender_id;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public EnumMessageType getType() {
        return type;
    }

    public void setType(EnumMessageType type) {
        this.type = type;
    }

    public EnumMessageStatus getStatus() {
        return status;
    }

    public void setStatus(EnumMessageStatus status) {
        this.status = status;
    }
}
