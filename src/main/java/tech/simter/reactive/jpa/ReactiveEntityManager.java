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
   * @return a complete {@link Mono} signal
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
   * Remove the entities in a transaction with auto commit when this {@link Mono} be subscribed.
   *
   * @param entities the entities to remove
   * @param <E>      the entity type
   * @return a complete {@link Mono} signal
   */
  <E> Mono<Void> remove(E... entities);

  /**
   * Find by primary key in a transaction with auto commit when this {@link Mono} be subscribed.
   *
   * @param entityClass entity class
   * @param primaryKey  primary key
   * @return a {@link Mono} with the found entity or {@link Mono#empty()} if the entity does not exist
   */
  <T> Mono<T> find(Class<T> entityClass, Object primaryKey);

  /**
   * Reactive encapsulation for {@link EntityManager#createQuery(String, Class)}.
   *
   * @param qlString    a Java Persistence query string
   * @param resultClass the type of the query result
   * @return a {@link Mono} with the new query instance
   */
  <T> ReactiveTypedQuery<T> createQuery(String qlString, Class<T> resultClass);

  /**
   * Reactive encapsulation for {@link EntityManager#createQuery(String)}.
   *
   * @param qlString a Java Persistence query string
   * @return a {@link Mono} with the new query instance
   */
  ReactiveQuery createQuery(String qlString);
}