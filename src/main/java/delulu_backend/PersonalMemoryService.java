package delulu_backend;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalMemoryService {

    private final PersonalMemoryRepository repository;

    public PersonalMemoryService(PersonalMemoryRepository repository) {
        this.repository = repository;
    }

    // Save a new memory
    public PersonalMemory saveMemory(String content, String userId) {
        PersonalMemory memory = new PersonalMemory(content, userId);
        return repository.save(memory);
    }

    // Get all memories
    public List<PersonalMemory> getAllMemories(String userId) {
        return repository.findByUserId(userId);
    }
}
