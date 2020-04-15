package tech.simter.reactive.jpa.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.kotlin.test.test
import tech.simter.reactive.jpa.ReactiveEntityManager
import tech.simter.reactive.jpa.ReactiveJpaWrapper
import java.util.*
import javax.persistence.Entity
import javax.persistence.EntityManagerFactory
import javax.persistence.Id
import javax.persistence.Persistence

@SpringJUnitConfig(ReactiveJpaWrapperImplTest.Cfg::class)
class ReactiveJpaWrapperImplTest @Autowired constructor(
  private val emf: EntityManagerFactory,
  private val rem: ReactiveEntityManager
) {
  @Configuration
  class Cfg {
    @Bean
    fun entityManagerFactory(): EntityManagerFactory {
      return Persistence.createEntityManagerFactory("default")
    }

    @Bean
    fun reactiveJpaWrapper(): ReactiveJpaWrapper {
      return ReactiveJpaWrapperImpl(null)
    }

    @Bean
    fun reactiveEntityManager(wrapper: ReactiveJpaWrapper, emf: EntityManagerFactory): ReactiveEntityManager {
      return ReactiveEntityManagerImpl(wrapper, emf)
    }
  }

  private fun randomString(): String = UUID.randomUUID().toString()

  private fun createBooks(vararg books: Book) {
    val em = emf.createEntityManager()
    em.transaction.begin()
    for (book in books) em.persist(book)
    em.transaction.commit()
    em.close()
  }

  private fun findBookById(id: String): Book? {
    val em = emf.createEntityManager()
    em.transaction.begin()
    val book = em.find(Book::class.java, id)
    em.transaction.commit()
    em.close()
    return book
  }

  private fun findBooks(sql: String, params: Map<String, Any>): List<Book> {
    val em = emf.createEntityManager()
    em.transaction.begin()
    val query = em.createQuery(sql, Book::class.java)
    params.forEach { query.setParameter(it.key, it.value) }
    em.transaction.commit()
    val list = query.resultList
    em.close()
    return list
  }

  @Test
  fun `persist one`() {
    // do persist
    val book = Book(id = randomString(), title = "test")
    rem.persist(book).test().verifyComplete()

    // verify persisted
    val found = findBookById(book.id!!)
    assertEquals(book, found)
  }

  @Test
  fun `persist many`() {
    // do persist
    val books = List(2) { Book(id = randomString(), title = "test") }
    rem.persist(*books.toTypedArray()).test().verifyComplete()

    // verify persisted
    val list = findBooks("select b from Book b where b.id in :ids", mapOf("ids" to books.map { it.id }))
    assertEquals(2, list.size)
    list.forEach { assertTrue(books.contains(it)) }
  }


  @Test
  fun `remove one`() {
    // prepare data
    val book = Book().apply { id = randomString(); title = "test" }
    createBooks(book)

    // do remove
    rem.remove(book).test().verifyComplete()

    // verify removed
    val found = findBookById(book.id!!)
    assertNull(found)
  }

  @Test
  fun `remove many`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // do remove
    rem.remove(*books.toTypedArray()).test().verifyComplete()

    // verify removed
    val list = findBooks("select b from Book b where b.id in :ids", mapOf("ids" to books.map { it.id }))
    assertEquals(0, list.size)
  }

  @Test
  fun `found it`() {
    // prepare data
    val book = Book().apply { id = randomString(); title = "test" }
    createBooks(book)

    // invoke and verify
    rem.find(Book::class.java, book.id!!)
      .test().expectNext(book).verifyComplete()
  }

  @Test
  fun `found nothing`() {
    rem.find(Book::class.java, randomString())
      .test().verifyComplete()
  }

  @Test
  fun `query list with type`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // query and verify
    rem.createQuery("select b from Book b where b.id in :ids", Book::class.java)
      .setParameter("ids", books.map { it.id })
      .resultList.collectList()
      .test()
      .consumeNextWith { list ->
        assertEquals(2, list.size)
        list.forEach { assertTrue(books.contains(it)) }
      }
      .verifyComplete()
  }

  @Test
  fun `query list without type`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // query and verify
    rem.createQuery("select b from Book b where b.id in :ids")
      .setParameter("ids", books.map { it.id })
      .getResultList<Book>().collectList()
      .test()
      .consumeNextWith { list ->
        assertEquals(2, list.size)
        list.forEach { assertTrue(books.contains(it)) }
      }
      .verifyComplete()
  }

  @Test
  fun `query single with type`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // query and verify
    rem.createQuery("select b from Book b where b.id = :id", Book::class.java)
      .setParameter("id", books[0].id!!)
      .singleResult
      .test()
      .expectNext(books[0])
      .verifyComplete()
  }

  @Test
  fun `query single without type`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // query and verify
    rem.createQuery("select b from Book b where b.id = :id")
      .setParameter("id", books[0].id!!)
      .getSingleResult<Book>()
      .test()
      .expectNext(books[0])
      .verifyComplete()
  }

  @Test
  fun `execute delete`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // execute and verify
    rem.createQuery("delete from Book b where b.id in :ids")
      .setParameter("ids", books.map { it.id })
      .executeUpdate()
      .test()
      .expectNext(books.size)
      .verifyComplete()
  }

  @Test
  fun `execute update`() {
    // prepare data
    val books = List(2) { Book(id = randomString(), title = "test") }
    createBooks(*books.toTypedArray())

    // execute update
    val newTitle = UUID.randomUUID().toString()
    rem.createQuery("update Book b set b.title = :title where b.id = :id")
      .setParameter("id", books[0].id!!)
      .setParameter("title", newTitle)
      .executeUpdate()
      .test()
      .expectNext(1)
      .verifyComplete()

    // verify updated
    val found = findBookById(books[0].id!!)!!
    assertEquals(found.title, newTitle)
  }
}

@Entity
data class Book(@Id var id: String?, var title: String?) {
  constructor() : this(null, null)
}