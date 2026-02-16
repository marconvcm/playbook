package ws.py3kl.playbook.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RestController
@RequestMapping("/app")
public class FrontendProxyController {

    @Value("${frontend.port}")
    private int frontendPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/**", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {

        String path = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        String query = request.getQueryString();
        String url = "http://localhost:" + frontendPort + path + (query != null ? "?" + query : "");

        // Copy incoming headers
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name ->
            headers.put(name, Collections.list(request.getHeaders(name)))
        );

        // IMPORTANT: fix/override proxy-related headers for the upstream (Next.js)
        headers.remove(HttpHeaders.HOST); // let us set it correctly
        headers.set(HttpHeaders.HOST, "localhost:" + frontendPort);

        // Standard reverse-proxy headers
        headers.set("X-Forwarded-Proto", request.getScheme());
        headers.set("X-Forwarded-Host", request.getHeader("Host") != null ? request.getHeader("Host") : request.getServerName());
        headers.set("X-Forwarded-Port", String.valueOf(request.getServerPort()));
        headers.set("X-Forwarded-Prefix", "/app"); // <— tells upstream it’s mounted under /app

        // X-Forwarded-For (append if already present)
        String existingXff = request.getHeader("X-Forwarded-For");
        String clientIp = request.getRemoteAddr();
        headers.set("X-Forwarded-For", existingXff == null ? clientIp : (existingXff + ", " + clientIp));

        // Forward body (for POST/PUT/PATCH etc.)
        byte[] body = request.getInputStream().readAllBytes();
        HttpEntity<byte[]> entity = new HttpEntity<>(body.length == 0 ? null : body, headers);

        ResponseEntity<byte[]> upstream = restTemplate.exchange(
            url,
            HttpMethod.valueOf(request.getMethod()),
            entity,
            byte[].class
        );

        // Pass upstream response status + headers back to client
        HttpHeaders out = new HttpHeaders();
        out.putAll(upstream.getHeaders());
        return new ResponseEntity<>(upstream.getBody(), out, upstream.getStatusCode());
    }
}
