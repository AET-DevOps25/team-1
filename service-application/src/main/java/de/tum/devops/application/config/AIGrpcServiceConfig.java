package de.tum.devops.application.config;

import de.tum.devops.grpc.ai.AIServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class AIGrpcServiceConfig {
    @Bean
    AIServiceGrpc.AIServiceBlockingStub blockingStub(GrpcChannelFactory channels) {
        return AIServiceGrpc.newBlockingStub(channels.createChannel("gen-ai"));
    }

    @Bean
    AIServiceGrpc.AIServiceStub asyncStub(GrpcChannelFactory channels) {
        return AIServiceGrpc.newStub(channels.createChannel("gen-ai"));
    }
}