package com.learning.ecommerce.identityservice.grpc;

import com.learning.ecommerce.auth.CustomerServiceGrpc;
import com.learning.ecommerce.auth.ValidateUserRequest;
import com.learning.ecommerce.auth.ValidateUserResponse;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @GrpcClient("customer-core")
    private CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceBlockingStub;

    public UserDetails loadUserByUsernameAndPassword(String username, String password) {

        // Create Metadata
        Metadata metadata = new Metadata();
        Metadata.Key<String> tenantIdKey = Metadata.Key.of("tenantId", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(tenantIdKey, "devel");

        ClientInterceptor metadataInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9092).usePlaintext().build();
        CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceBlockingStub =
                CustomerServiceGrpc.newBlockingStub(channel).withInterceptors(metadataInterceptor);

        // 🔹 Call gRPC Customer Service to validate user
        ValidateUserRequest request = ValidateUserRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)  // ✅ Pass the password
                .build();

        ValidateUserResponse response = customerServiceBlockingStub.validateUser(request);

        if (response == null || !response.getIsValid()) {
            throw new UsernameNotFoundException("Invalid username or password");
        }

        // ✅ Hardcoded user details for now (Later, fetch full user details)
        return User.builder()
                .username(username)
                .password(password) // Since password is validated via gRPC, store it as-is
                .roles("USER") // Replace with actual roles from Customer Service
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        throw new UnsupportedOperationException("Use loadUserByUsernameAndPassword instead");
    }
}
