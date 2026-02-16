package ws.py3kl.playbook.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class FrontendProcessManager implements SmartLifecycle {

    public record Status(
        boolean running,
        Instant startedAt,
        Long pid,
        Integer lastExitCode,
        String lastError
    ) {}

    private final FrontendProps props;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<Process> processRef = new AtomicReference<>();
    private final AtomicReference<Status> statusRef =
        new AtomicReference<>(new Status(false, null, null, null, null));

    public FrontendProcessManager(FrontendProps props) {
        this.props = props;
    }

    public Status getStatus() {
        var p = processRef.get();
        boolean alive = p != null && p.isAlive();
        var s = statusRef.get();
        return new Status(alive, s.startedAt(), s.pid(), s.lastExitCode(), s.lastError());
    }

    public boolean pingHttp() {
        try {
            URL url = new URL("http://127.0.0.1:" + props.getPort() + props.getPingPath());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setRequestMethod("GET");
            int code = con.getResponseCode();
            return code >= 200 && code < 400;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void start() {
        if (!props.isEnabled()) return;
        if (running.getAndSet(true)) return;

        executor.submit(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(props.getCommand());
                processBuilder.directory(new File(props.getWorkdir()));
                processBuilder.redirectErrorStream(true);

                Process startedProcess = processBuilder.start();
                processRef.set(startedProcess);

                statusRef.set(new Status(true, Instant.now(), safeProcessId(startedProcess), null, null));

                try (BufferedReader br = new BufferedReader(new InputStreamReader(startedProcess.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.info("[Frontend] {}", line);
                    }
                }

                int exit = startedProcess.waitFor();
                statusRef.set(new Status(false, statusRef.get().startedAt(), safeProcessId(startedProcess), exit, null));

            } catch (Exception e) {
                statusRef.set(new Status(false, statusRef.get().startedAt(), null, null, e.toString()));
            } finally {
                running.set(false);
            }
        });

        // Optional: wait briefly for HTTP to be reachable (non-blocking callers still ok)
        long deadline = System.currentTimeMillis() + props.getStartupTimeoutSeconds() * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (pingHttp()) break;
            if (waitFor(250L)) break;
        }
    }

    @Override
    public void stop() {
        Process p = processRef.getAndSet(null);
        if (p != null && p.isAlive()) {
            p.destroy();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            if (p.isAlive()) p.destroyForcibly();
        }
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        Process p = processRef.get();
        return p != null && p.isAlive();
    }

    @Override
    public int getPhase() {
        // Start early (lower = earlier). Adjust if you want backend up first.
        return Integer.MIN_VALUE;
    }

    private static boolean waitFor(Long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {
            return true;
        }
        return false;
    }

    private static Long safeProcessId(Process p) {
        try { return p.pid(); } catch (Throwable t) { return null; }
    }
}
