package com.edunexus.common.mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Functional interface for mapping between types.
 * Provides consistent mapping pattern across all services.
 *
 * @param <S> source type
 * @param <T> target type
 */
@FunctionalInterface
public interface Mapper<S, T> {

    /**
     * Maps source to target.
     *
     * @param source the source object
     * @return the mapped target object
     */
    T map(S source);

    /**
     * Maps a list of sources to targets.
     *
     * @param sources the list of source objects
     * @return the list of mapped target objects
     */
    default List<T> mapList(List<S> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
