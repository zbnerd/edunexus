package com.edunexus.common.repository;

import com.edunexus.common.exception.NotFoundException;
import com.edunexus.common.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface with common methods.
 * Provides consistent data access patterns across all services.
 *
 * @param <T> entity type
 * @param <ID> ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Finds entity by ID or throws NotFoundException.
     *
     * @param id the ID to search for
     * @return the entity
     * @throws NotFoundException if entity not found
     */
    default T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
                new NotFoundException(ErrorCode.ENTITY_NOT_FOUND, "ID: " + id));
    }

    /**
     * Finds entity by ID or throws NotFoundException with custom error code.
     *
     * @param id the ID to search for
     * @param errorCode the error code to use if not found
     * @return the entity
     * @throws NotFoundException if entity not found
     */
    default T findByIdOrThrow(ID id, ErrorCode errorCode) {
        return findById(id).orElseThrow(() ->
                new NotFoundException(errorCode, "ID: " + id));
    }

    /**
     * Finds entity by ID or returns a default value.
     *
     * @param id the ID to search for
     * @param defaultValue the default value to return if not found
     * @return the entity or default value
     */
    default T findByIdOrElse(ID id, T defaultValue) {
        return findById(id).orElse(defaultValue);
    }

    /**
     * Checks if entity exists by ID.
     *
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    /**
     * Finds all entities by IDs in a batch operation.
     * More efficient than individual findById calls.
     *
     * @param ids the list of IDs
     * @return list of entities
     */
    default List<T> findAllByIds(List<ID> ids) {
        return findAllById(ids);
    }
}
