package com.onur.bootcamp;

public class NoteUpdateRequest {
    private String content;
    private String title;
    private Urgency urgency;


    public NoteUpdateRequest() {} // Quarkus needs a no-arg constructor

    public NoteUpdateRequest(String content, String title, Urgency urgency) {
        this.content = content;
        this.title = title;
        this.urgency = urgency;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void validate() {
        if ((content != null && content.trim().isEmpty()) ||
                (title != null && title.trim().isEmpty())) {
            throw new IllegalArgumentException("Content and title cannot be blank");
        }
        if (content == null && title == null && urgency == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }
    }
    // Getters and setters (all nullable)
}
