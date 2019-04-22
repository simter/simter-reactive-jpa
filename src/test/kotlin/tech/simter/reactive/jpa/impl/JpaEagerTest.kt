package tech.simter.reactive.jpa.impl

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.test
import tech.simter.reactive.jpa.ReactiveEntityManager
import tech.simter.reactive.jpa.ReactiveJpaWrapper
import javax.persistence.*

@SpringJUnitConfig(JpaEagerTest.Cfg::class)
class JpaEagerTest @Autowired constructor(
  private val emf: EntityManagerFactory,
  private val rem: ReactiveEntityManager
) {
  @Configuration
  open class Cfg {
    @Bean
    open fun entityManagerFactory(): EntityManagerFactory {
      return Persistence.createEntityManagerFactory("default")
    }

    @Bean
    open fun reactiveJpaWrapper(): ReactiveJpaWrapper {
      return ReactiveJpaWrapperImpl(null)
    }

    @Bean
    open fun reactiveEntityManager(wrapper: ReactiveJpaWrapper, emf: EntityManagerFactory): ReactiveEntityManager {
      return ReactiveEntityManagerImpl(wrapper, emf)
    }
  }

  @Test
  fun `load children eager`() {
    // do persist
    val parent = Parent(id = "1", name = "parent")
    var children = LinkedHashSet<Child>()
    children.add(Child(id = "c1", name = "cn1", parent = parent))
    children.add(Child(id = "c2", name = "cn2", parent = parent))
    parent.children = children
    //parent.children = List(2) { Child(id = it.toString(), name = "c-$it", parent = parent) }.toSet()
    rem.persist(parent).test().verifyComplete()

    val em = emf.createEntityManager()
    val parent2 = em.find(Parent::class.java, parent.id)
    parent2.children?.forEach { println(it) }
  }
}

@Entity
data class Parent(
  @Id var id: String?,
  var name: String?
) {
  constructor() : this(null, null)

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "parent", orphanRemoval = true)
  var children: Set<Child>? = null //LinkedHashSet()
}

@Entity
data class Child(
  @Id var id: String?,
  var name: String?,
  @ManyToOne
  @JoinColumn(name = "pid")
  var parent: Parent?
) {
  constructor() : this(null, null, null)
}