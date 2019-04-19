package tech.simter.reactive.jpa.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.simter.reactive.jpa.ReactiveEntityManager;
import tech.simter.reactive.jpa.ReactiveJpaWrapper;
import tech.simter.reactive.jpa.ReactiveTypedQuery;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
        return merged;
      });
    }
  }

  @Override
  public <T> ReactiveTypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
    return new ReactiveTypedQueryImpl<>(qlString, resultClass);
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

    @Override
    public Mono<Integer> executeUpdate() {
      return wrapper.fromCallable(() -> doInTransaction(TypedQuery::executeUpdate));
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
        return result;
      } catch (Exception e) {
        em.getTransaction().rollback();
        throw e;
      }
    }
  }
}
