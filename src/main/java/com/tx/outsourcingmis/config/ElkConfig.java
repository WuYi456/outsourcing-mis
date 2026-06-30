package com.tx.outsourcingmis.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
public class ElkConfig {

    @Value("${elk.enabled:true}")
    private boolean enabled;

    @Value("${elk.logstash.host:localhost}")
    private String logstashHost;

    @Value("${elk.logstash.port:5044}")
    private int logstashPort;

    @Value("${elk.elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elk.elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elk.index.name:outsourcing-mis-logs}")
    private String indexName;

    @Value("${app.name:outsourcing-mis}")
    private String appName;

    @Value("${app.environment:dev}")
    private String environment;

    @PostConstruct
    public void init() {
        if (enabled) {
            log.info("ELK 已启用");
            log.info("  Logstash: {}:{}", logstashHost, logstashPort);
            log.info("  Elasticsearch: {}:{}", elasticsearchHost, elasticsearchPort);
            log.info("  Index: {}", indexName);
        } else {
            log.info("ELK 未启用，日志将仅输出到控制台和文件");
        }
    }
}