package delulu_backend;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DocumentChunkService {

    private final DocumentChunkRepository repository;
    private final EmbeddingService embeddingService;

    public DocumentChunkService(
            DocumentChunkRepository repository,
            EmbeddingService embeddingService) {

        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public DocumentChunk saveChunk(String text, String userId) {

        float[] embedding =
                embeddingService.createEmbedding(text);

        String embeddingString =
                Arrays.toString(embedding);

        DocumentChunk chunk =
                new DocumentChunk(
                        text,
                        embeddingString,
                        userId
                );

        return repository.save(chunk);
    }
}
