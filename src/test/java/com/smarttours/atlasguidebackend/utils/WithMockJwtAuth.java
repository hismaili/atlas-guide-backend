package com.smarttours.atlasguidebackend.utils;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtAuthSecurityContextFactory.class)
public @interface WithMockJwtAuth {
    String subject() default "testuser";
    String[] authorities() default {};
    String[] claims() default {};
}