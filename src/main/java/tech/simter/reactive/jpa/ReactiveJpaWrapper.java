package tech.simter.reactive.jpa;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A wrapper interface with default behavior for async run blocking JPA method.
 * <p>
 * By default all blocking JPA method run on {@link Schedulers#elastic()}.
 *
 * @author RJ
 */
public interface ReactiveJpaWrapper {
  /**
   * The scheduler to run blocking JPA method.
   * <p>
   * It is {@link Schedulers#elastic()} by default and is used by the rest methods.
   */
  default Scheduler getScheduler() {
    return Schedulers.elastic();
  }

  /**
   * Create a {@link Mono} that completes empty once the provided {@link Runnable} has been executed.
   * <p>
   * By default {@link Runnable} run on {@link #getScheduler()} with the blocking code.
   *
   * @param runnable {@link Runnable} that will be executed before emitting the completion signal
   * @param <T>      The generic type of the upstream, which is preserved by this operator
   * @return A {@link Mono}
   */
  default <T> Mono<T> fromRunnable(Runnable runnable) {
    return Mono.<T>fromRunnable(runnable).subscribeOn(getScheduler());
  }

  /**
   * Create a {@link Mono} producing its value using the provided {@link Callable}.
   * If the {@link Callable} resolves to {@code null}, the resulting Mono completes empty.
   *
   * <p>
   * By default {@link Callable} run on {@link #getScheduler()} with the blocking code.
   *
   * @param callable {@link Callable} that will produce the value
   * @param <T>      type of the expected value
   * @return A {@link Mono}
   */
  default <T> Mono<T> fromCallable(Callable<? extends T> callable) {
    return Mono.<T>fromCallable(callable).subscribeOn(getScheduler());
  }

  /**
   * Create a {@link Flux} that emits the items contained in a {@link Iterable} created by the provided {@link Supplier} for each subscription.
   * A new iterator will be created for each subscriber.
   * <p>
   * By default {@link Supplier} run on {@link #getScheduler()} with the blocking code.
   *
   * @param supplier the {@link Iterable} to read data from
   * @param <T>      the type of values in the source {@link Iterable}
   * @return a {@link Flux}
   */
  default <T> Flux<T> fromIterable(Supplier<Iterable<? extends T>> supplier) {
    return Flux.defer(() -> Flux.<T>fromIterable(supplier.get()).subscribeOn(getScheduler()));
  }

  /**
   * Create a {@link Flux} that emits the items contained in a {@link Stream} created by the provided {@link Supplier} for each subscription.
   * The {@link Stream} is {@link Stream#close() closed} automatically by the operator on cancellation, error or completion.
   * <p>
   * By default {@link Supplier} run on {@link #getScheduler()} with the blocking code.
   *
   * @param supplier the {@link Supplier} that generates the {@link Stream} from which to read data
   * @param <T>      the type of values in the source {@link Stream}
   * @return a {@link Flux}
   */
  default <T> Flux<T> fromStream(Supplier<Stream<? extends T>> supplier) {
    return Flux.fromStream(supplier).subscribeOn(getScheduler());
  }
}