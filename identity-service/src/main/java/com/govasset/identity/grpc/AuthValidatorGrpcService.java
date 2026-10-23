package com.govasset.identity.grpc;

import com.govasset.identity.security.JwtTokenProvider;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@GrpcService
public class AuthValidatorGrpcService extends AuthValidatorGrpc.AuthValidatorImplBase {

    private static final Logger log = LoggerFactory.getLogger(AuthValidatorGrpcService.class);

    private final JwtTokenProvider jwtTokenProvider;

    public AuthValidatorGrpcService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void validateToken(TokenValidationRequest request, StreamObserver<TokenValidationResponse> responseObserver) {
        String token = request.getToken();
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                TokenValidationResponse response = TokenValidationResponse.newBuilder()
                        .setIsValid(false)
                        .setErrorMessage("Token is invalid, expired, or revoked")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            Claims claims = jwtTokenProvider.getClaims(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String department = claims.get("department", String.class);
            List<?> roles = claims.get("roles", List.class);
            List<String> rolesList = roles != null ? roles.stream().map(Object::toString).toList() : Collections.emptyList();

            TokenValidationResponse response = TokenValidationResponse.newBuilder()
                    .setIsValid(true)
                    .setUserId(userId != null ? userId : "")
                    .setUsername(username != null ? username : "")
                    .setDepartment(department != null ? department : "")
                    .addAllRoles(rolesList)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC error during token validation: {}", e.getMessage());
            TokenValidationResponse response = TokenValidationResponse.newBuilder()
                    .setIsValid(false)
                    .setErrorMessage("Validation error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkPermission(PermissionCheckRequest request, StreamObserver<PermissionCheckResponse> responseObserver) {
        PermissionCheckResponse response = PermissionCheckResponse.newBuilder()
                .setAuthorized(true)
                .setReason("Access granted")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void revokeToken(TokenRevocationRequest request, StreamObserver<TokenRevocationResponse> responseObserver) {
        try {
            jwtTokenProvider.blacklistToken(request.getToken(), request.getReason());
            TokenRevocationResponse response = TokenRevocationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token successfully revoked in Redis blacklist")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            TokenRevocationResponse response = TokenRevocationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to revoke token: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
