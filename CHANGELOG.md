# simter-reactive-jpa changelog

## 0.6.0 - 2022-06-21

- Upgrade to simter-dependencies-3.0.0 (jdk-17)

## 0.5.0 - 2020-11-19

- Upgrade to simter-dependencies-2.0.0

## 0.4.0 - 2020-06-02

- Upgrade to simter-dependencies-2.0.0-M1

## 0.3.0 - 2020-04-15

- Upgrade to simter-dependencies-1.3.0-M14

## 0.2.0 - 2020-03-01

- Upgrade to simter-dependencies-1.3.0-M13

## 0.1.0 - 2019-07-03

- Change parent to simter-dependencies-1.2.0
- Initial
    > Build the compatibility between [JPA] and reactive program. It uses a [Reactor Scheduler] that diff to reactor main thread to avoid JPA block the reactor main thread. And this scheduler can be customized or just use the default behavior `Schedulers.elastic()`.


[JPA]: https://en.wikipedia.org/wiki/Java_Persistence_API
[JSR-338]: https://jcp.org/en/jsr/detail?id=338
[Reactor Scheduler]: https://projectreactor.io/docs/core/release/reference/#schedulers