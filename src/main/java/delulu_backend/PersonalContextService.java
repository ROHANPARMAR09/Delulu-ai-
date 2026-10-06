package delulu_backend;

import org.springframework.stereotype.Service;

@Service
public class PersonalContextService {

    private String context = "";

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }
}