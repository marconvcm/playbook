package ws.py3kl.playbook.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "frontend")
public class FrontendProps {
    private boolean enabled = true;
    private String workdir = "../frontend";
    private List<String> command = List.of("bun", "run", "start");
    private int port = 3000;
    private int startupTimeoutSeconds = 30;
    private String pingPath = "/api/health";
}

