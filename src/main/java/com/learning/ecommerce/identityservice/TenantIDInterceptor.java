package com.learning.ecommerce.identityservice;

import com.learning.ecommerce.identityservice.tenant.TenantContext;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@GrpcGlobalServerInterceptor
public class TenantIDInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantIDInterceptor.class);
    private static final Metadata.Key<String> TENANT_ID = Metadata.Key.of("TenantID", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String tenantId = metadata.get(TENANT_ID);

        if (StringUtils.isEmpty(tenantId)) {
            tenantId = TenantContext.getTenantId();  // Use existing TenantID if set
            logger.debug("TenantID not found in gRPC. Using default: '{}'", tenantId);
        }

        logger.info("Processing gRPC request: {} with TenantID: {}",
                serverCall.getMethodDescriptor().getFullMethodName(), tenantId);

        TenantContext.setTenantId(tenantId); // 🔹 Store in ThreadLocal

        // Store TenantID in gRPC Context
        Context context = Context.current().withValue(Context.key("TenantID"), tenantId);

        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
