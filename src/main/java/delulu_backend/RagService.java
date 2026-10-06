package delulu_backend;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RagService {

    private final DocumentChunkRepository repository;
    private final EmbeddingService embeddingService;

    public RagService(
            DocumentChunkRepository repository,
            EmbeddingService embeddingService) {

        this.repository = repository;
        this.embeddingService = embeddingService;
    }

    public List<DocumentChunk> findRelevantChunks(
            String question,
            int topK,
            String userId) {

        // Create embedding for the user's question
        float[] questionEmbedding =
                embeddingService.createEmbedding(question);

        List<DocumentChunk> chunks = repository.findByUserId(userId);

        // Calculate similarity for every stored chunk
        List<ScoredChunk> scoredChunks =
                new ArrayList<>();

        for (DocumentChunk chunk : chunks) {

            float[] chunkEmbedding =
                    parseEmbedding(chunk.getEmbedding());

            double similarity =
                    cosineSimilarity(
                            questionEmbedding,
                            chunkEmbedding
                    );

            scoredChunks.add(
                    new ScoredChunk(
                            chunk,
                            similarity
                    )
            );
        }

        // Highest similarity first
        scoredChunks.sort(
                Comparator.comparingDouble(
                        ScoredChunk::score
                ).reversed()
        );

        // Return the most relevant chunks
        return scoredChunks.stream()
                .limit(topK)
                .map(ScoredChunk::chunk)
                .toList();
    }

    private float[] parseEmbedding(String embedding) {

        String cleaned =
                embedding
                        .replace("[", "")
                        .replace("]", "");

        String[] values =
                cleaned.split(",");

        float[] result =
                new float[values.length];

        for (int i = 0; i < values.length; i++) {

            result[i] =
                    Float.parseFloat(
                            values[i].trim()
                    );
        }

        return result;
    }

    private double cosineSimilarity(
            float[] a,
            float[] b) {

        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;

        for (int i = 0; i < a.length; i++) {

            dotProduct +=
                    a[i] * b[i];

            magnitudeA +=
                    a[i] * a[i];

            magnitudeB +=
                    b[i] * b[i];
        }

        if (magnitudeA == 0 ||
                magnitudeB == 0) {

            return 0;
        }

        return dotProduct /
                (Math.sqrt(magnitudeA) *
                 Math.sqrt(magnitudeB));
    }

    private record ScoredChunk(
            DocumentChunk chunk,
            double score) {
    }
}
