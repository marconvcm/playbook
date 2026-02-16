package ws.py3kl.playbook.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FrontendProps.class)
class FrontendConfig { }
