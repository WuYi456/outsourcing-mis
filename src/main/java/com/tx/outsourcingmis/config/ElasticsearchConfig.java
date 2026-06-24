package com.tx.outsourcingmis.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.net.ssl.SSLContext;

/**
 * Elasticsearch 客户端配置
 *
 * <p>配置 ES 客户端连接，支持 HTTPS + 用户名密码认证。
 * <p>注意：开发环境跳过 SSL 证书验证，生产环境应使用正式证书。
 */
@Slf4j
@Configuration
public class ElasticsearchConfig {

    /** ES 服务地址，默认 https://localhost:9200 */
    @Value("${spring.elasticsearch.uris:https://localhost:9200}")
    private String esUris;

    /** ES 用户名，默认 elastic */
    @Value("${spring.elasticsearch.username:elastic}")
    private String username;

    /** ES 密码 */
    @Value("${spring.elasticsearch.password:}")
    private String password;

    /** 连接超时时间（毫秒） */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /** Socket 超时时间（毫秒） */
    private static final int SOCKET_TIMEOUT_MS = 60000;

    /**
     * 创建 Elasticsearch 客户端
     *
     * <p>初始化失败时返回 null，应用可降级运行（日志功能不可用）
     *
     * @return ElasticsearchClient 实例，失败时返回 null
     */
    @Bean
    @Lazy
    public ElasticsearchClient elasticsearchClient() {
        try {
            // 解析主机和端口
            String uri = esUris.replace("https://", "").replace("http://", "");
            String[] parts = uri.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;

            log.info("正在连接 Elasticsearch: {}:{}", host, port);

            // 配置用户名密码认证
            CredentialsProvider credentials = new BasicCredentialsProvider();
            credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            // 构建 RestClient（跳过 SSL 证书验证，仅开发环境使用）
            RestClient restClient = RestClient.builder(new HttpHost(host, port, "https"))
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        try {
                            // 信任所有证书（开发环境）
                            SSLContext sslContext = SSLContextBuilder.create()
                                    .loadTrustMaterial((chain, authType) -> true)
                                    .build();
                            httpClientBuilder.setSSLContext(sslContext)
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        } catch (Exception e) {
                            log.warn("SSL 配置失败，跳过验证: {}", e.getMessage());
                        }
                        httpClientBuilder.setDefaultCredentialsProvider(credentials);
                        return httpClientBuilder;
                    })
                    .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                            .setConnectTimeout(CONNECT_TIMEOUT_MS)
                            .setSocketTimeout(SOCKET_TIMEOUT_MS))
                    .build();

            // 配置 JSON 序列化（支持 Java 8 时间类型）
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper(objectMapper)
            );

            ElasticsearchClient client = new ElasticsearchClient(transport);

            // 测试连接
            if (client.ping().value()) {
                log.info("Elasticsearch 连接成功，版本: {}", client.info().version().number());
            }

            return client;

        } catch (Exception e) {
            log.warn("Elasticsearch 初始化失败，操作日志功能将降级: {}", e.getMessage());
            return null;
        }
    }
}