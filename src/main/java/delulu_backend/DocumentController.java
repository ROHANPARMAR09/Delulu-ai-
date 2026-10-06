package delulu_backend;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final PersonalMemoryService memoryService;
    private final DocumentChunkService chunkService;

    public DocumentController(
            PersonalMemoryService memoryService,
            DocumentChunkService chunkService) {

        this.memoryService = memoryService;
        this.chunkService = chunkService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

        try {

            if (file.isEmpty()) {
                return "No file uploaded.";
            }

            String filename = file.getOriginalFilename();

            if (filename == null ||
                    !filename.toLowerCase().endsWith(".pdf")) {

                return "Please upload a PDF file.";
            }

            PDDocument document =
                    Loader.loadPDF(file.getBytes());

            PDFTextStripper stripper =
                    new PDFTextStripper();

            String text =
                    stripper.getText(document);

            document.close();

            if (text == null || text.isBlank()) {
                return "No readable text found in the PDF.";
            }

            // Store only a record of the upload as personal memory. The text
            // itself is kept in searchable chunks; placing the whole PDF in
            // the prompt makes answers less accurate.
            memoryService.saveMemory("Document uploaded: " + filename, jwt.getSubject());

            // Use overlapping chunks so a sentence split at a boundary keeps
            // its surrounding context during retrieval.
            int chunkSize = 900;
            int chunkOverlap = 150;

            for (int i = 0; i < text.length(); i += chunkSize - chunkOverlap) {

                int end =
                        Math.min(i + chunkSize, text.length());

                String chunk =
                        text.substring(i, end);

                // Create embedding and save chunk
                chunkService.saveChunk(chunk, jwt.getSubject());
            }

            return "PDF uploaded, chunked, embedded and saved.";

        } catch (IOException e) {

            return "Failed to process PDF: "
                    + e.getMessage();
        }
    }
}
