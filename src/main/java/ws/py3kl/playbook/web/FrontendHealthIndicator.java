package ws.py3kl.playbook.web;


import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class FrontendHealthIndicator implements HealthIndicator {

    private final FrontendProcessManager manager;
    private final FrontendProps props;

    public FrontendHealthIndicator(FrontendProcessManager manager, FrontendProps props) {
        this.manager = manager;
        this.props = props;
    }

    @Override
    public Health health() {
        if (!props.isEnabled()) {
            return Health.up().withDetail("frontend", "disabled").build();
        }

        var s = manager.getStatus();
        boolean alive = s.running();
        boolean httpOk = manager.pingHttp();

        if (alive && httpOk) {
            return Health.up()
                .withDetail("pid", s.pid())
                .withDetail("startedAt", s.startedAt())
                .withDetail("http", "ok")
                .build();
        }

        return Health.down()
            .withDetail("processAlive", alive)
            .withDetail("httpOk", httpOk)
            .withDetail("pid", s.pid())
            .withDetail("lastExitCode", s.lastExitCode())
            .withDetail("lastError", s.lastError())
            .build();
    }
}
