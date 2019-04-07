package tech.simter.reactive.jpa;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;

/**
 * Some method encapsulation from {@link EntityManager} with reactive result.
 *
 * @author RJ
 */
public interface ReactiveEntityManager {
  /**
   * Persist entities in a transaction with auto commit when this {@link Mono} be subscribed.
   *
   * @param entities the entities to persist
   * @param <E>      the entity type
   * @return a {@link Mono}
   */
  <E> Mono<Void> persist(E... entities);

  /**
   * Merge entities in a transaction with auto commit when this {@link Mono} be subscribed.
   *
   * @param entities the entities to merge
   * @param <E>      the entity type
   * @return a {@link Flux} with the merged entity
   */
  <E> Flux<E> merge(E... entities);

  /**
   * Create an instance of <code>ReactiveTypedQuery</code> for executing a Java Persistence query language statement.
   * The select list of the query must contain only a single item, which must be assignable to the type specified by
   * the <code>resultClass</code> argument.
   *
   * @param qlString    a Java Persistence query string
   * @param resultClass the type of the query result
   * @return the new query instance
   * @throws IllegalArgumentException if the query string is found to be invalid or if the query result is found to
   *                                  not be assignable to the specified type
   */
  <T> ReactiveTypedQuery<T> createQuery(String qlString, Class<T> resultClass);
}