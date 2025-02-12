package com.springdocex.global.springDoc;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "API 서버", version = "v1"))
@SecurityScheme(
	name = "Authorization",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
public class SpringDocConfig {

	@Bean
	public GroupedOpenApi groupApiV1() {
		return GroupedOpenApi.builder()
			.group("apiV1")
			.pathsToMatch("/api/v1/**")
			.build();
	}

	@Bean
	public GroupedOpenApi groupController() {
		return GroupedOpenApi.builder()
			.group("controller")
			.pathsToExclude("/api/**")
			.build();
	}
}
