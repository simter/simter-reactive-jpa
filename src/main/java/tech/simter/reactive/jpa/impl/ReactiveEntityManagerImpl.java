package tech.simter.reactive.jpa.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.simter.reactive.jpa.ReactiveEntityManager;
import tech.simter.reactive.jpa.ReactiveJpaWrapper;
import tech.simter.reactive.jpa.ReactiveQuery;
import tech.simter.reactive.jpa.ReactiveTypedQuery;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A {@link ReactiveEntityManager} implementation for async run blocking JPA method.
 *
 * @author RJ
 */
@Component
public class ReactiveEntityManagerImpl implements ReactiveEntityManager {
  private final EntityManagerFactory emf;
  private final ReactiveJpaWrapper wrapper;

  @Autowired
  public ReactiveEntityManagerImpl(ReactiveJpaWrapper wrapper, EntityManagerFactory emf) {
    this.emf = emf;
    this.wrapper = wrapper;
  }

  private EntityManager createEntityManager() {
    return emf.createEntityManager();
  }

  @SafeVarargs
  public final <E> Mono<Void> persist(E... entities) {
    if (entities == null || entities.length == 0) return Mono.empty();
    else {
      return wrapper.fromRunnable(() -> {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        for (E entity : entities) em.persist(entity);
        em.getTransaction().commit();
        em.close();
      });
    }
  }

  @SafeVarargs
  public final <E> Flux<E> merge(E... entities) {
    if (entities == null || entities.length == 0) return Flux.empty();
    else {
      return wrapper.fromIterable(() -> {
        List<E> merged = new ArrayList<>();
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        for (E entity : entities) merged.add(em.merge(entity));
        em.getTransaction().commit();
        em.close();
        return merged;
      });
    }
  }

  @Override
  public <E> Mono<Void> remove(E... entities) {
    if (entities == null || entities.length == 0) return Mono.empty();
    else {
      return wrapper.fromRunnable(() -> {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        for (E entity : entities) em.remove(em.merge(entity));
        em.getTransaction().commit();
        em.close();
      });
    }
  }

  @Override
  public <T> Mono<T> find(Class<T> entityClass, Object primaryKey) {
    return wrapper.fromCallable(() -> {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      T entity = em.find(entityClass, primaryKey);
      em.getTransaction().commit();
      em.close();
      return entity;
    });
  }

  @Override
  public <T> ReactiveTypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
    return new ReactiveTypedQueryImpl<>(qlString, resultClass);
  }

  @Override
  public ReactiveQuery createQuery(String qlString) {
    return new ReactiveQueryImpl(qlString);
  }

  private class ReactiveTypedQueryImpl<T> implements ReactiveTypedQuery<T> {
    private final Map<String, Object> params = new HashMap<>();
    private String qlString;
    private Class<T> resultClass;
    private int startPosition;
    private int maxResult;

    ReactiveTypedQueryImpl(String qlString, Class<T> resultClass) {
      this.qlString = qlString;
      this.resultClass = resultClass;
    }

    @Override
    public ReactiveTypedQuery<T> setParameter(String name, Object value) {
      params.put(name, value);
      return this;
    }

    @Override
    public ReactiveTypedQuery<T> setFirstResult(int startPosition) {
      this.startPosition = startPosition;
      return this;
    }

    @Override
    public ReactiveTypedQuery<T> setMaxResults(int maxResult) {
      this.maxResult = maxResult;
      return this;
    }

    @Override
    public Mono<T> getSingleResult() {
      return wrapper.fromCallable(() -> doInTransaction(TypedQuery::getSingleResult));
    }

    @Override
    public Flux<T> getResultList() {
      return wrapper.fromIterable(() -> doInTransaction(TypedQuery::getResultList));
    }

    private <R> R doInTransaction(Function<TypedQuery<T>, R> fn) {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      try {
        TypedQuery<T> query = em.createQuery(qlString, resultClass);
        if (!params.isEmpty()) params.forEach(query::setParameter);
        if (startPosition > 0) query.setFirstResult(startPosition);
        if (maxResult > 0) query.setMaxResults(maxResult);

        R result = fn.apply(query);

        em.getTransaction().commit();
        em.close();
        return result;
      } catch (Exception e) {
        em.getTransaction().rollback();
        throw e;
      }
    }
  }

  private class ReactiveQueryImpl implements ReactiveQuery {
    private final Map<String, Object> params = new HashMap<>();
    private String qlString;
    private int startPosition;
    private int maxResult;

    ReactiveQueryImpl(String qlString) {
      this.qlString = qlString;
    }

    @Override
    public ReactiveQuery setParameter(String name, Object value) {
      params.put(name, value);
      return this;
    }

    @Override
    public ReactiveQuery setFirstResult(int startPosition) {
      this.startPosition = startPosition;
      return this;
    }

    @Override
    public ReactiveQuery setMaxResults(int maxResult) {
      this.maxResult = maxResult;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> getSingleResult() {
      return wrapper.fromCallable(() -> doInTransaction(query -> (T) query.getSingleResult()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Flux<T> getResultList() {
      return wrapper.fromIterable(() -> doInTransaction(Query::getResultList));
    }

    @Override
    public Mono<Integer> executeUpdate() {
      return wrapper.fromCallable(() -> doInTransaction(Query::executeUpdate));
    }

    private <R> R doInTransaction(Function<Query, R> fn) {
      EntityManager em = createEntityManager();
      em.getTransaction().begin();
      try {
        Query query = em.createQuery(qlString);
        if (!params.isEmpty()) params.forEach(query::setParameter);
        if (startPosition > 0) query.setFirstResult(startPosition);
        if (maxResult > 0) query.setMaxResults(maxResult);

        R result = fn.apply(query);

        em.getTransaction().commit();
        em.close();
        return result;
      } catch (Exception e) {
        em.getTransaction().rollback();
        throw e;
      }
    }
  }
}
