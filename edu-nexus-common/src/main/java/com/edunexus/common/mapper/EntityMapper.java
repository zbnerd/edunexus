package com.edunexus.common.mapper;

import java.util.List;

/**
 * Base interface for entity-to-DTO mapping.
 * Extends Mapper with additional methods for entity-specific operations.
 *
 * @param <E> entity type
 * @param <D> DTO type
 */
public interface EntityMapper<E, D> extends Mapper<E, D> {

    /**
     * Maps DTO to entity.
     *
     * @param dto the DTO to map
     * @return the mapped entity
     */
    E toEntity(D dto);

    /**
     * Maps entity to DTO.
     *
     * @param entity the entity to map
     * @return the mapped DTO
     */
    @Override
    default D map(E entity) {
        return toDto(entity);
    }

    /**
     * Maps entity to DTO (alias for map).
     *
     * @param entity the entity to map
     * @return the mapped DTO
     */
    D toDto(E entity);

    /**
     * Maps list of entities to DTOs.
     *
     * @param entities the list of entities
     * @return the list of DTOs
     */
    default List<D> toDtoList(List<E> entities) {
        return mapList(entities);
    }

    /**
     * Maps list of DTOs to entities.
     *
     * @param dtos the list of DTOs
     * @return the list of entities
     */
    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
