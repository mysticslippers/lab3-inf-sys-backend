package me.ifmo.backend.storage;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

    @Bean
    public ApplicationRunner ensureBucket(MinioProperties props, ObjectStorageService storage) {
        return args -> {
            if (props.isCreateBucket()) {
                storage.ensureBucket();
            }
        };
    }
}
