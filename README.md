# simter-reactive-jpa

A reactive jpa encapsulation.

[JPA] is the abbreviation of Java Persistence API. Its latest specification is [JSR-338]. It's blocking natively and not compatible with reactive program by default. 
This module target to build the compatibility between [JPA] and reactive program.
It uses a [Reactor Scheduler] that diff to reactor main thread to avoid JPA block the reactor main thread. 
And this scheduler could be customized or just use the default behavior `Schedulers.elastic()`.

## Usage

Maven: 

```xml
<dependency>
  <groupId>tech.simter.reactive</groupId>
  <artifactId>simter-reactive-jpa</artifactId>
  <version>{version}</version>
</dependency>
<dependency>
  <groupId>tech.simter.reactive</groupId>
  <artifactId>simter-reactive-test</artifactId>
  <version>{version}</version>
</dependency>
```

Java: 

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.test;
import tech.simter.reactive.jpa.ReactiveEntityManager;
import tech.simter.reactive.test.jpa.ReactiveDataJpaTest;

@SpringJUnitConfig(tech.simter.reactive.jpa.ModuleConfiguration.class)
@ReactiveDataJpaTest
public class TheTest {
  @Autowired
  private ReactiveEntityManager rem;

  @Test
  public test() {
    // save
    MyPo po = new MyPo();
    StepVerifier.create(rem.persist(po))
      .expectNext(po).verifyComplete();

    // find one
    StepVerifier.create(
      rem.createQuery("select t from MyPo t where id = :id", MyPo.class)
      .setParameter("id", 123)
      .getSingleResult()
    ).expectNext(po).verifyComplete();

    // find list
    StepVerifier.create(
      rem.createQuery("select t from MyPo t", MyPo.class)
      .getResultList()
    ).expectNext(po).verifyComplete();
  }
}
```

> `@ReactiveDataJpaTest` comes from [simter-reactive-test].


[JPA]: https://en.wikipedia.org/wiki/Java_Persistence_API
[JSR-338]: https://jcp.org/en/jsr/detail?id=338
[Reactor Scheduler]: https://projectreactor.io/docs/core/release/reference/#schedulers
[simter-reactive-test]: https://github.com/simter/simter-reactive-test