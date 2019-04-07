package tech.simter.reactive.jpa;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A wrapper interface for async run blocking JPA method.
 *
 * @author RJ
 */
public interface ReactiveJpaWrapper {
  /**
   * Create a {@link Mono} that completes empty once the provided {@link Runnable} has been executed.
   *
   * @param runnable {@link Runnable} that will be executed before emitting the completion signal
   * @param <T>      The generic type of the upstream, which is preserved by this operator
   * @return A {@link Mono}
   */
  <T> Mono<T> fromRunnable(Runnable runnable);

  /**
   * Create a {@link Mono} producing its value using the provided {@link Callable}.
   * If the {@link Callable} resolves to {@code null}, the resulting Mono completes empty.
   *
   * @param callable {@link Callable} that will produce the value
   * @param <T>      type of the expected value
   * @return A {@link Mono}
   */
  <T> Mono<T> fromCallable(Callable<? extends T> callable);

  /**
   * Create a {@link Flux} that emits the items contained in a {@link Iterable} created by the provided {@link Supplier} for each subscription.
   * A new iterator will be created for each subscriber.
   *
   * @param supplier the {@link Iterable} to read data from
   * @param <T>      the type of values in the source {@link Iterable}
   * @return a {@link Flux}
   */
  <T> Flux<T> fromIterable(Supplier<Iterable<? extends T>> supplier);

  /**
   * Create a {@link Flux} that emits the items contained in a {@link Stream} created by the provided {@link Supplier} for each subscription.
   * The {@link Stream} is {@link Stream#close() closed} automatically by the operator on cancellation, error or completion.
   *
   * @param supplier the {@link Supplier} that generates the {@link Stream} from which to read data
   * @param <T>      the type of values in the source {@link Stream}
   * @return a {@link Flux}
   */
  <T> Flux<T> fromStream(Supplier<Stream<? extends T>> supplier);
}