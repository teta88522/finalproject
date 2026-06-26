package com.pixcel.app.web;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@Configuration
public class WebMvcConfig implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {

        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();

        mapping.setPathPrefixes(
            Map.of(
                "/project/{projectId}",
                HandlerTypePredicate.forAnnotation(AllProjectController.class)
            )
        );

        return mapping;
    }
}
