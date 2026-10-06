package delulu_backend;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private final ChatClient chatClient;
    private final PersonalMemoryService memoryService;
    private final EmbeddingService embeddingService;
    private final RagService ragService;

    public ChatController(
            ChatClient.Builder builder,
            PersonalMemoryService memoryService,
            EmbeddingService embeddingService,
            RagService ragService) {

        this.chatClient = builder.build();
        this.memoryService = memoryService;
        this.embeddingService = embeddingService;
        this.ragService = ragService;
    }

    @PostMapping
    public String chat(@RequestBody String message,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

        // Find the most relevant document chunks
        List<DocumentChunk> relevantChunks =
                ragService.findRelevantChunks(
                        message,
                        5,
                        jwt.getSubject()
                );

        StringBuilder context =
                new StringBuilder();

        int sourceNumber = 1;
        for (DocumentChunk chunk : relevantChunks) {

            context
                    .append("SOURCE ")
                    .append(sourceNumber++)
                    .append(":\n")
                    .append(chunk.getContent().trim())
                    .append("\n\n---\n\n");
        }

        // Also keep personal memory available
        List<PersonalMemory> memories =
                memoryService.getAllMemories(jwt.getSubject());

        for (PersonalMemory memory : memories) {
            // Older uploads stored the entire PDF as a memory. Do not send
            // that unfiltered content to the model; RAG excerpts are better.
            if (memory.getContent().startsWith("Document uploaded: ")
                    || memory.getContent().startsWith("Document: ")) {
                continue;
            }

            context
                    .append("PERSONAL NOTE:\n")
                    .append(memory.getContent().trim())
                    .append("\n\n---\n\n");
        }

        return chatClient
                .prompt()
                .system("""
                        You are DELULU, a personal AI assistant.

                        Answer using only the SOURCE excerpts and PERSONAL NOTE
                        entries provided below.

                        Prefer precise facts from the SOURCE excerpts. When
                        possible, mention the source number you used, for
                        example "According to Source 2...".

                        If the answer is not supported by the provided context,
                        say:
                        "I don't have that information."

                        Never invent, infer, or use outside knowledge.

                        CONTEXT:
                        """ + context)
                .user(message)
                .call()
                .content();
    }

    @PostMapping("/context")
    public String addContext(
            @RequestBody String context,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

        memoryService.saveMemory(context, jwt.getSubject());

        return "Personal information saved successfully.";
    }

    @GetMapping("/memories")
    public List<PersonalMemory> getMemories(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

        return memoryService.getAllMemories(jwt.getSubject());
    }

    @GetMapping("/embedding-test")
    public String embeddingTest() {

        float[] embedding =
                embeddingService.createEmbedding(
                        "Java lecture is at 12 PM on Tuesday"
                );

        return "Embedding dimensions: "
                + embedding.length;
    }
    @GetMapping("/rag-test")
public String ragTest(
        @RequestParam String question,
        @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

    List<DocumentChunk> chunks =
            ragService.findRelevantChunks(
                    question,
                    3,
                    jwt.getSubject()
            );

    StringBuilder result =
            new StringBuilder();

    for (DocumentChunk chunk : chunks) {

        result
                .append("CHUNK:\n")
                .append(chunk.getContent())
                .append("\n\n");
    }

    return result.toString();
        }
}
