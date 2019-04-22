package tech.simter.reactive.jpa;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.*;

/**
 * A interface relative to {@link Query} but with minimal usage api for reactive world.
 *
 * @author RJ
 */
public interface ReactiveQuery {
  /**
   * Bind an argument value to a named parameter.
   *
   * @param name  parameter name
   * @param value parameter value
   * @return the same query instance
   * @throws IllegalArgumentException if the parameter name does not correspond to a parameter of the query
   *                                  or if the argument is of incorrect type
   */
  ReactiveQuery setParameter(String name, Object value);

  /**
   * Set the position of the first result to retrieve.
   *
   * @param startPosition position of the first result, numbered from 0
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ReactiveQuery setFirstResult(int startPosition);

  /**
   * Set the maximum number of results to retrieve.
   *
   * @param maxResult maximum number of results to retrieve
   * @return the same query instance
   * @throws IllegalArgumentException if the argument is negative
   */
  ReactiveQuery setMaxResults(int maxResult);

  /**
   * Execute a SELECT query that returns a single result.
   *
   * @return {@link Mono#just(Object)} with the result or {@link Mono#error(Throwable)} with:
   * <ul>
   * <li>{@link NoResultException} if there is no result.
   * <li>{@link NonUniqueResultException} if more than one result.
   * <li>{@link IllegalStateException} if called for a Java Persistence query language UPDATE or DELETE statement.
   * <li>{@link QueryTimeoutException} if the query execution exceeds the query timeout value set and only the statement is rolled back.
   * <li>{@link TransactionRequiredException} if a lock mode other than <code>NONE</code> has been set and there is no transaction or the persistence context has not been joined to the transaction.
   * <li>{@link PessimisticLockException} if pessimistic locking fails and the transaction is rolled back.
   * <li>{@link LockTimeoutException} if pessimistic locking fails and only the statement is rolled back.
   * <li>{@link PersistenceException} if the query execution exceeds the query timeout value set and the transaction is rolled back.
   * </ul>
   */
  <T> Mono<T> getSingleResult();

  /**
   * Execute a SELECT query and return the query results.
   * <p>
   * By default implementation this method call {@link TypedQuery#getResultStream()} inner.
   *
   * @return {@link Flux} with the stream data or {@link Flux#error(Throwable)} with:
   * <ul>
   * <li>{@link IllegalStateException} if called for a Java Persistence query language UPDATE or DELETE statement.
   * <li>{@link QueryTimeoutException} if the query execution exceeds the query timeout value set and only the statement is rolled back.
   * <li>{@link TransactionRequiredException} if a lock mode other than <code>NONE</code> has been set and there is no transaction or the persistence context has not been joined to the transaction.
   * <li>{@link PessimisticLockException} if pessimistic locking fails and the transaction is rolled back.
   * <li>{@link LockTimeoutException} if pessimistic locking fails and only the statement is rolled back.
   * <li>{@link PersistenceException} if the query execution exceeds the query timeout value set and the transaction is rolled back.
   * </ul>
   */
  <T> Flux<T> getResultList();

  /**
   * Execute an update or delete statement.
   *
   * @return {@link Mono} with the number of entities updated or deleted or {@link Mono#error(Throwable)} with:
   * <ul>
   * <li>{@link IllegalStateException} if called for a Java Persistence query language UPDATE or DELETE statement.
   * <li>{@link TransactionRequiredException} if there is no transaction or the persistence context has not been joined to the transaction.
   * <li>{@link QueryTimeoutException} if the statement execution exceeds the query timeout value set and only the statement is rolled back.
   * <li>{@link PersistenceException} if the query execution exceeds the query timeout value set and the transaction is rolled back.
   * </ul>
   */
  Mono<Integer> executeUpdate();
}
