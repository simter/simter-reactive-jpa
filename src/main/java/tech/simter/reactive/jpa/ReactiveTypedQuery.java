package tech.simter.reactive.jpa;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.TypedQuery;

/**
 * A interface relative to {@link TypedQuery} but with minimal usage api for reactive world.
 *
 * @author RJ
 */
public interface ReactiveTypedQuery<T> {
  ReactiveTypedQuery<T> setParameter(String name, Object value);

  ReactiveTypedQuery<T> setFirstResult(int startPosition);

  ReactiveTypedQuery<T> setMaxResults(int maxResult);

  Mono<T> getSingleResult();

  Flux<T> getResultList();
}
