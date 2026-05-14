package com.quizai.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;

import java.time.Duration;

@Configuration
public class AgentConfig {

    @Value("${agent.python-service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @Value("${agent.python-service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${agent.python-service.response-timeout:600000}")
    private int responseTimeout;

    @Value("${agent.python-service.max-buffer-size:16777216}")
    private int maxBufferSize;

    @Bean
    public WebClient agentWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(responseTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);

        return WebClient.builder()
                .baseUrl(pythonServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBufferSize))
                .build();
    }
}
