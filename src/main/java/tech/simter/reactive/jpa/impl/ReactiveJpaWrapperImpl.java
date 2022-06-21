package tech.simter.reactive.jpa.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import tech.simter.reactive.jpa.ReactiveJpaWrapper;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link ReactiveJpaWrapper} implementation for async run blocking JPA method.
 * <p>
 * By default, all blocking JPA method run on {@link Schedulers#boundedElastic()}.
 * This {@link Scheduler} could be replaced by a spring bean with name 'reactiveJpaScheduler'.
 *
 * @author RJ
 */
@Component
public class ReactiveJpaWrapperImpl implements ReactiveJpaWrapper {
  @Deprecated
  private ReactiveJpaWrapperImpl() {
    this.scheduler = Schedulers.elastic();
  }

  @Autowired(required = false)
  public ReactiveJpaWrapperImpl(@Qualifier("reactiveJpaScheduler") Scheduler scheduler) {
    this.scheduler = scheduler;
    if (this.scheduler == null) this.scheduler = Schedulers.boundedElastic();
  }

  private Scheduler scheduler;

  @Override
  public <T> Mono<T> fromRunnable(Runnable runnable) {
    return Mono.<T>fromRunnable(runnable).subscribeOn(scheduler);
  }

  @Override
  public <T> Mono<T> fromCallable(Callable<? extends T> callable) {
    return Mono.<T>fromCallable(callable).subscribeOn(scheduler);
  }

  @Override
  public <T> Flux<T> fromIterable(Supplier<Iterable<? extends T>> supplier) {
    return Flux.defer(() -> Flux.<T>fromIterable(supplier.get()).subscribeOn(scheduler));
  }

  @Override
  public <T> Flux<T> fromStream(Supplier<Stream<? extends T>> supplier) {
    return Flux.fromStream(supplier).subscribeOn(scheduler);
  }
}