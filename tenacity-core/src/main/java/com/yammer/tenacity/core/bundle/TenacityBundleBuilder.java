package com.yammer.tenacity.core.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.yammer.tenacity.core.errors.TenacityContainerExceptionMapper;
import com.yammer.tenacity.core.errors.TenacityExceptionMapper;
import io.dropwizard.Configuration;
import io.dropwizard.servlets.tasks.Task;

import javax.ws.rs.ext.ExceptionMapper;
import java.lang.Throwable;

public class TenacityBundleBuilder<T extends Configuration> {
    protected final ImmutableList.Builder<ExceptionMapper<? extends Throwable>> exceptionMapperBuilder = ImmutableList.builder();
    protected Optional<HystrixCommandExecutionHook> executionHook = Optional.absent();
    protected TenacityBundleConfigurationFactory<T> configurationFactory;
    protected final ImmutableList.Builder<HealthCheck> healthCheckBuilder = ImmutableList.builder();
    protected final ImmutableList.Builder<Task> taskBuilder = ImmutableList.builder();

    public static <T extends Configuration> TenacityBundleBuilder<T> newBuilder() {
        return new TenacityBundleBuilder<>();
    }

    public <E extends Throwable> TenacityBundleBuilder<T> addExceptionMapper(ExceptionMapper<E> exceptionMapper) {
        exceptionMapperBuilder.add(exceptionMapper);
        return this;
    }

    public TenacityBundleBuilder<T> addHealthCheck(HealthCheck healthCheck) {
        healthCheckBuilder.add(healthCheck);
        return this;
    }

    public TenacityBundleBuilder<T> addTask(Task task) {
        taskBuilder.add(task);
        return this;
    }

    public TenacityBundleBuilder<T> mapAllHystrixRuntimeExceptionsTo(int statusCode) {
        exceptionMapperBuilder.add(new TenacityExceptionMapper(statusCode));
        exceptionMapperBuilder.add(new TenacityContainerExceptionMapper(statusCode));
        return this;
    }

    public TenacityBundleBuilder<T> commandExecutionHook(HystrixCommandExecutionHook executionHook) {
        this.executionHook = Optional.of(executionHook);
        return this;
    }

    public TenacityBundleBuilder<T> configurationFactory(TenacityBundleConfigurationFactory<T> configurationFactory) {
        this.configurationFactory = configurationFactory;
        return this;
    }

    public TenacityConfiguredBundle<T> build() {
        if (configurationFactory == null) {
            throw new IllegalArgumentException("Must supply a Configuration Factory");
        }

        return new TenacityConfiguredBundle<>(configurationFactory, executionHook, exceptionMapperBuilder.build(), healthCheckBuilder.build(), taskBuilder.build());
    }
}
