package com.govasset.identity.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.62.2)",
    comments = "Source: auth.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class AuthValidatorGrpc {

  private AuthValidatorGrpc() {}

  public static final java.lang.String SERVICE_NAME = "govasset.auth.AuthValidator";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenValidationRequest,
      com.govasset.identity.grpc.TokenValidationResponse> getValidateTokenMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateToken",
      requestType = com.govasset.identity.grpc.TokenValidationRequest.class,
      responseType = com.govasset.identity.grpc.TokenValidationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenValidationRequest,
      com.govasset.identity.grpc.TokenValidationResponse> getValidateTokenMethod() {
    io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenValidationRequest, com.govasset.identity.grpc.TokenValidationResponse> getValidateTokenMethod;
    if ((getValidateTokenMethod = AuthValidatorGrpc.getValidateTokenMethod) == null) {
      synchronized (AuthValidatorGrpc.class) {
        if ((getValidateTokenMethod = AuthValidatorGrpc.getValidateTokenMethod) == null) {
          AuthValidatorGrpc.getValidateTokenMethod = getValidateTokenMethod =
              io.grpc.MethodDescriptor.<com.govasset.identity.grpc.TokenValidationRequest, com.govasset.identity.grpc.TokenValidationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateToken"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.TokenValidationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.TokenValidationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AuthValidatorMethodDescriptorSupplier("ValidateToken"))
              .build();
        }
      }
    }
    return getValidateTokenMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.govasset.identity.grpc.PermissionCheckRequest,
      com.govasset.identity.grpc.PermissionCheckResponse> getCheckPermissionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckPermission",
      requestType = com.govasset.identity.grpc.PermissionCheckRequest.class,
      responseType = com.govasset.identity.grpc.PermissionCheckResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.govasset.identity.grpc.PermissionCheckRequest,
      com.govasset.identity.grpc.PermissionCheckResponse> getCheckPermissionMethod() {
    io.grpc.MethodDescriptor<com.govasset.identity.grpc.PermissionCheckRequest, com.govasset.identity.grpc.PermissionCheckResponse> getCheckPermissionMethod;
    if ((getCheckPermissionMethod = AuthValidatorGrpc.getCheckPermissionMethod) == null) {
      synchronized (AuthValidatorGrpc.class) {
        if ((getCheckPermissionMethod = AuthValidatorGrpc.getCheckPermissionMethod) == null) {
          AuthValidatorGrpc.getCheckPermissionMethod = getCheckPermissionMethod =
              io.grpc.MethodDescriptor.<com.govasset.identity.grpc.PermissionCheckRequest, com.govasset.identity.grpc.PermissionCheckResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckPermission"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.PermissionCheckRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.PermissionCheckResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AuthValidatorMethodDescriptorSupplier("CheckPermission"))
              .build();
        }
      }
    }
    return getCheckPermissionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenRevocationRequest,
      com.govasset.identity.grpc.TokenRevocationResponse> getRevokeTokenMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RevokeToken",
      requestType = com.govasset.identity.grpc.TokenRevocationRequest.class,
      responseType = com.govasset.identity.grpc.TokenRevocationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenRevocationRequest,
      com.govasset.identity.grpc.TokenRevocationResponse> getRevokeTokenMethod() {
    io.grpc.MethodDescriptor<com.govasset.identity.grpc.TokenRevocationRequest, com.govasset.identity.grpc.TokenRevocationResponse> getRevokeTokenMethod;
    if ((getRevokeTokenMethod = AuthValidatorGrpc.getRevokeTokenMethod) == null) {
      synchronized (AuthValidatorGrpc.class) {
        if ((getRevokeTokenMethod = AuthValidatorGrpc.getRevokeTokenMethod) == null) {
          AuthValidatorGrpc.getRevokeTokenMethod = getRevokeTokenMethod =
              io.grpc.MethodDescriptor.<com.govasset.identity.grpc.TokenRevocationRequest, com.govasset.identity.grpc.TokenRevocationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RevokeToken"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.TokenRevocationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.govasset.identity.grpc.TokenRevocationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new AuthValidatorMethodDescriptorSupplier("RevokeToken"))
              .build();
        }
      }
    }
    return getRevokeTokenMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AuthValidatorStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthValidatorStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthValidatorStub>() {
        @java.lang.Override
        public AuthValidatorStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthValidatorStub(channel, callOptions);
        }
      };
    return AuthValidatorStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AuthValidatorBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthValidatorBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthValidatorBlockingStub>() {
        @java.lang.Override
        public AuthValidatorBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthValidatorBlockingStub(channel, callOptions);
        }
      };
    return AuthValidatorBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AuthValidatorFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AuthValidatorFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AuthValidatorFutureStub>() {
        @java.lang.Override
        public AuthValidatorFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AuthValidatorFutureStub(channel, callOptions);
        }
      };
    return AuthValidatorFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Validate token signature, claims and active session in Redis
     * </pre>
     */
    default void validateToken(com.govasset.identity.grpc.TokenValidationRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenValidationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateTokenMethod(), responseObserver);
    }

    /**
     * <pre>
     * Check specific RBAC role permission
     * </pre>
     */
    default void checkPermission(com.govasset.identity.grpc.PermissionCheckRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.PermissionCheckResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckPermissionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Revoke token during logout / security incidents
     * </pre>
     */
    default void revokeToken(com.govasset.identity.grpc.TokenRevocationRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenRevocationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRevokeTokenMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AuthValidator.
   */
  public static abstract class AuthValidatorImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AuthValidatorGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AuthValidator.
   */
  public static final class AuthValidatorStub
      extends io.grpc.stub.AbstractAsyncStub<AuthValidatorStub> {
    private AuthValidatorStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthValidatorStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthValidatorStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate token signature, claims and active session in Redis
     * </pre>
     */
    public void validateToken(com.govasset.identity.grpc.TokenValidationRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenValidationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Check specific RBAC role permission
     * </pre>
     */
    public void checkPermission(com.govasset.identity.grpc.PermissionCheckRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.PermissionCheckResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckPermissionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Revoke token during logout / security incidents
     * </pre>
     */
    public void revokeToken(com.govasset.identity.grpc.TokenRevocationRequest request,
        io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenRevocationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRevokeTokenMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AuthValidator.
   */
  public static final class AuthValidatorBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AuthValidatorBlockingStub> {
    private AuthValidatorBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthValidatorBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthValidatorBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate token signature, claims and active session in Redis
     * </pre>
     */
    public com.govasset.identity.grpc.TokenValidationResponse validateToken(com.govasset.identity.grpc.TokenValidationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateTokenMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Check specific RBAC role permission
     * </pre>
     */
    public com.govasset.identity.grpc.PermissionCheckResponse checkPermission(com.govasset.identity.grpc.PermissionCheckRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckPermissionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Revoke token during logout / security incidents
     * </pre>
     */
    public com.govasset.identity.grpc.TokenRevocationResponse revokeToken(com.govasset.identity.grpc.TokenRevocationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRevokeTokenMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AuthValidator.
   */
  public static final class AuthValidatorFutureStub
      extends io.grpc.stub.AbstractFutureStub<AuthValidatorFutureStub> {
    private AuthValidatorFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AuthValidatorFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AuthValidatorFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate token signature, claims and active session in Redis
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.govasset.identity.grpc.TokenValidationResponse> validateToken(
        com.govasset.identity.grpc.TokenValidationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Check specific RBAC role permission
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.govasset.identity.grpc.PermissionCheckResponse> checkPermission(
        com.govasset.identity.grpc.PermissionCheckRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckPermissionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Revoke token during logout / security incidents
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.govasset.identity.grpc.TokenRevocationResponse> revokeToken(
        com.govasset.identity.grpc.TokenRevocationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRevokeTokenMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_VALIDATE_TOKEN = 0;
  private static final int METHODID_CHECK_PERMISSION = 1;
  private static final int METHODID_REVOKE_TOKEN = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_VALIDATE_TOKEN:
          serviceImpl.validateToken((com.govasset.identity.grpc.TokenValidationRequest) request,
              (io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenValidationResponse>) responseObserver);
          break;
        case METHODID_CHECK_PERMISSION:
          serviceImpl.checkPermission((com.govasset.identity.grpc.PermissionCheckRequest) request,
              (io.grpc.stub.StreamObserver<com.govasset.identity.grpc.PermissionCheckResponse>) responseObserver);
          break;
        case METHODID_REVOKE_TOKEN:
          serviceImpl.revokeToken((com.govasset.identity.grpc.TokenRevocationRequest) request,
              (io.grpc.stub.StreamObserver<com.govasset.identity.grpc.TokenRevocationResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getValidateTokenMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.govasset.identity.grpc.TokenValidationRequest,
              com.govasset.identity.grpc.TokenValidationResponse>(
                service, METHODID_VALIDATE_TOKEN)))
        .addMethod(
          getCheckPermissionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.govasset.identity.grpc.PermissionCheckRequest,
              com.govasset.identity.grpc.PermissionCheckResponse>(
                service, METHODID_CHECK_PERMISSION)))
        .addMethod(
          getRevokeTokenMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.govasset.identity.grpc.TokenRevocationRequest,
              com.govasset.identity.grpc.TokenRevocationResponse>(
                service, METHODID_REVOKE_TOKEN)))
        .build();
  }

  private static abstract class AuthValidatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AuthValidatorBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.govasset.identity.grpc.AuthProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AuthValidator");
    }
  }

  private static final class AuthValidatorFileDescriptorSupplier
      extends AuthValidatorBaseDescriptorSupplier {
    AuthValidatorFileDescriptorSupplier() {}
  }

  private static final class AuthValidatorMethodDescriptorSupplier
      extends AuthValidatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AuthValidatorMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AuthValidatorGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AuthValidatorFileDescriptorSupplier())
              .addMethod(getValidateTokenMethod())
              .addMethod(getCheckPermissionMethod())
              .addMethod(getRevokeTokenMethod())
              .build();
        }
      }
    }
    return result;
  }
}
