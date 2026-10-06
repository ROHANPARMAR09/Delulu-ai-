package delulu_backend;

import jakarta.persistence.*;

@Entity
@Table(name = "personal_memory")
public class PersonalMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "user_id")
    private String userId;

    public PersonalMemory() {
    }

    public PersonalMemory(String content, String userId) {
        this.content = content;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
