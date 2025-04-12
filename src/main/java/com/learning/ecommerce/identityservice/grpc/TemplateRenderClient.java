package com.learning.ecommerce.identityservice.grpc;

import com.learning.ecommerce.Chunk;
import com.learning.ecommerce.RenderRequest;
import com.learning.ecommerce.TemplateRenderServiceGrpc;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class TemplateRenderClient {

    private final String tenantId = "Devel";

    @GrpcClient("template-service")
    private TemplateRenderServiceGrpc.TemplateRenderServiceBlockingStub templateStub;

    public String fetchRenderedTemplate(String filename, Map<String, String> data) {
        // Build the gRPC request
        RenderRequest request = RenderRequest.newBuilder()
                .setFilename(filename)
                .putAllData(data)
                .build();

        // Create metadata and add TenantID
        Metadata metadata = new Metadata();
        Metadata.Key<String> tenantKey = Metadata.Key.of("TenantID", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(tenantKey, tenantId);

        // Attach metadata using an interceptor
        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);
        TemplateRenderServiceGrpc.TemplateRenderServiceBlockingStub stubWithMetadata =
                templateStub.withInterceptors(interceptor);

        // Make the gRPC call with metadata
        Iterator<Chunk> chunks = stubWithMetadata.renderTemplate(request);
        StringBuilder htmlBuilder = new StringBuilder();

        while (chunks.hasNext()) {
            htmlBuilder.append(chunks.next().getB().toStringUtf8());
        }

        return htmlBuilder.toString();
    }
}

