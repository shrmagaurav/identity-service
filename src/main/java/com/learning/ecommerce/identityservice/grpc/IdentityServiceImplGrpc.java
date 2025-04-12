package com.learning.ecommerce.identityservice.grpc;

import com.learning.ecommerce.auth.AuthRequest;
import com.learning.ecommerce.auth.AuthResponse;
import com.learning.ecommerce.auth.CustomerServiceGrpc;
import com.learning.ecommerce.auth.IdentityServiceGrpc;
import com.learning.ecommerce.auth.ValidateUserRequest;
import com.learning.ecommerce.auth.ValidateUserResponse;
import com.learning.ecommerce.identityservice.JwtHelper;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class IdentityServiceImplGrpc extends IdentityServiceGrpc.IdentityServiceImplBase {

    private final JwtHelper jwtHelper;
    private final CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceStub;

    @Autowired
    public IdentityServiceImplGrpc(JwtHelper jwtHelper, ManagedChannel channel) {
        this.jwtHelper = jwtHelper;
        this.customerServiceStub = CustomerServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void authenticateUser(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
        // 🔹 Call Customer Service via gRPC to validate user
        ValidateUserRequest validateRequest = ValidateUserRequest.newBuilder()
                .setUsername(request.getUsername())
                .setPassword(request.getPassword())
                .build();

        ValidateUserResponse validateResponse = customerServiceStub.validateUser(validateRequest);

        if (validateResponse.getIsValid()) {
            // ✅ Create UserDetails object
            UserDetails userDetails = User.builder()
                    .username(request.getUsername())
                    .password("") // No need to store password in token
                    .roles("USER") // Assign role (you can fetch it from Customer Service if available)
                    .build();

            // ✅ Generate JWT Token
            String token = jwtHelper.generateToken(userDetails);

            AuthResponse response = AuthResponse.newBuilder()
                    .setToken(token)
                    .setMessage("Authentication successful")
                    .build();

            responseObserver.onNext(response);
        } else {
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setToken("")
                    .setMessage("Invalid credentials")
                    .build());
        }

        responseObserver.onCompleted();
    }


}
