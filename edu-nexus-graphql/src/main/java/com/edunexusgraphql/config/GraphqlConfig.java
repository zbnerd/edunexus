package com.edunexusgraphql.config;

import com.edunexusgraphql.directive.AuthenticationDirective;
import com.edunexusgraphql.directive.AuthorizationDirective;
import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphqlConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer(
            AuthenticationDirective authenticationDirective,
            AuthorizationDirective authorizationDirective
    ) {
        return wirinbBuilder -> wirinbBuilder
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.GraphQLLong)
                .directive("authenticate", authenticationDirective)
                .directive("authorize",authorizationDirective);

    }
}
