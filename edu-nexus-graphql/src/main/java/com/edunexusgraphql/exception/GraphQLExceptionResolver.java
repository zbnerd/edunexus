package com.edunexusgraphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof HttpClientErrorException.NotFound) {
            return errorHandling(ex, env, ErrorType.NOT_FOUND);
        }

        if (ex instanceof HttpClientErrorException.Unauthorized) {
            return errorHandling(ex, env, ErrorType.UNAUTHORIZED);
        }

        if (ex instanceof HttpClientErrorException.Forbidden) {
            return errorHandling(ex, env, ErrorType.FORBIDDEN);
        }

        if (ex instanceof HttpClientErrorException) {
            if (((HttpClientErrorException) ex).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(401)))
                return errorHandling(ex, env, ErrorType.UNAUTHORIZED);

            if (((HttpClientErrorException) ex).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(403)))
                return errorHandling(ex, env, ErrorType.FORBIDDEN);

            return errorHandling(ex, env, ErrorType.BAD_REQUEST);
        }

        return null;
    }

    private GraphQLError errorHandling(Throwable ex, DataFetchingEnvironment env, ErrorType errorType) {
        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}
