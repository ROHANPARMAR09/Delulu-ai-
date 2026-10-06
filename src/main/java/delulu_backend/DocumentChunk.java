package delulu_backend;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Column(name = "user_id")
    private String userId;

    public DocumentChunk() {
    }

    public DocumentChunk(String content, String embedding, String userId) {
        this.content = content;
        this.embedding = embedding;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getEmbedding() {
        return embedding;
    }

    public String getUserId() {
        return userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }
}
