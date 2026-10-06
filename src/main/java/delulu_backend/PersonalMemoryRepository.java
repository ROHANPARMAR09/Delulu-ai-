package delulu_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonalMemoryRepository
        extends JpaRepository<PersonalMemory, Long> {
    List<PersonalMemory> findByUserId(String userId);
}
