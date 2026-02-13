package com.edunexusgraphql.directive;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * GraphQL directive for authentication.
 * Verifies that the request has been authenticated by UserInterceptor.
 * The UserInterceptor validates JWT and sets user context, this directive checks it.
 */
@Component
public class AuthenticationDirective implements SchemaDirectiveWiring {

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
        GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
        GraphQLObjectType parentType = (GraphQLObjectType) environment.getFieldsContainer();

        // Get the original DataFetcher
        DataFetcher<?> originalDataFetcher = environment.getCodeRegistry()
                .getDataFetcher(parentType, fieldDefinition);

        // Create a new DataFetcher that performs authentication check
        DataFetcher<?> authDataFetcher = (DataFetchingEnvironment dataFetchingEnvironment) -> {
            String userId = dataFetchingEnvironment.getGraphQlContext().get("X-USER-ID");

            // UserInterceptor should have set this from validated JWT
            if (userId == null || userId.trim().isEmpty() || userId.equals("-1")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authentication required. Please provide a valid JWT token.");
            }

            return originalDataFetcher.get(dataFetchingEnvironment);
        };

        // Register the modified DataFetcher
        environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, authDataFetcher);

        return fieldDefinition;
    }
}
