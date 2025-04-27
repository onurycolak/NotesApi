package com.onur.bootcamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Integer id;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 300, message = "Content must not exceed 300 characters")
    private String content;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 50, message = "Title must not exceed 50 characters")
    private String title;

    @Enumerated(EnumType.ORDINAL)
    private Urgency urgency;

    public Note() {} // Quarkus needs a no-arg constructor

    public Note(Integer id, String content, String title, Urgency urgency) {
        this.id = id;
        this.content = content;
        this.title = title;
        this.urgency = urgency;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Urgency getUrgency() { return urgency; }
    public void setUrgency(Urgency urgency) { this.urgency = urgency; }
}