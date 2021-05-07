package org.hibernate.bugs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	// Entities are auto-discovered, so just add them anywhere on class-path
	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		// initial situation: Alice has 1 book, Bob none
		Person alice = new Person( "Alice" );
		entityManager.persist( alice );

		Book book1 = new Book();
		book1.setOwner( alice );
		entityManager.persist( book1 );

		Person bob = new Person( "Bob" );
		entityManager.persist( bob );

		final TypedQuery<Person> orderByBroken = entityManager.createQuery( "SELECT table FROM Person table ORDER BY size(table.books) DESC", Person.class );
		final TypedQuery<Person> orderByWorking = entityManager.createQuery( "SELECT table FROM Person table ORDER BY table.books.size DESC", Person.class );

		List<Person> dbPeopleBroken = orderByBroken.getResultList();
		List<Person> dbPeopleWorking = orderByWorking.getResultList();
		assertEquals( Arrays.asList(alice, bob), dbPeopleWorking );
		assertEquals( dbPeopleWorking, dbPeopleBroken );

		// add 2 books to Bob
		Book book2 = new Book();
		book2.setOwner( bob );
		entityManager.persist( book2 );

		Book book3 = new Book();
		book3.setOwner( bob );
		entityManager.persist( book3 );

		dbPeopleBroken = orderByBroken.getResultList();
		dbPeopleWorking = orderByWorking.getResultList();
		assertEquals( Arrays.asList(bob, alice), dbPeopleWorking );
		assertEquals( dbPeopleWorking, dbPeopleBroken );

		// remove (soft-deleting) both Bob's books
		entityManager.remove(book2);
		entityManager.remove(book3);

		// result lists are not equal anymore
		dbPeopleBroken = orderByBroken.getResultList();
		dbPeopleWorking = orderByWorking.getResultList();
		assertEquals( Arrays.asList(alice, bob), dbPeopleWorking );
		assertEquals( dbPeopleWorking, dbPeopleBroken );

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
