package org.rischar.todo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class TestDB {

	@Before
	public void setUp() {
		DB.setEndpoint("http://localhost:8000");
		DB.create();
	}

	@After
	public void tearDown() {
		try {
			DB.deleteTable();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	@Test
	public void testInsert() {
		Optional<Date> completed = Optional.empty();

		ToDoItem item = new ToDoItem("carl@test.org", 1, "Initial Test", completed);
		DB.upsert(item.toDBItem());

	}

	@Test
	public void testList() {
		Optional<Date> completed = Optional.empty();

		ToDoItem item1 = new ToDoItem("carl@test.org", 1, "Initial Test", completed);
		ToDoItem item2 = new ToDoItem("carl@test.org", 1, "Second Test", completed);

		DB.upsert(item1.toDBItem());
		DB.upsert(item2.toDBItem());

		List<ToDoItem> items = DB.list("carl@test.org");

		Assert.assertTrue("Table should contain two items.", items.size() == 2);

	}

	@Test
	public void testList2() {
		Optional<Date> completed = Optional.empty();

		ToDoItem item1 = new ToDoItem("carl@test.org", 1, "Initial Test", completed);
		ToDoItem item2 = new ToDoItem("carl@test.org", 1, "Second Test", completed);
		ToDoItem item3 = new ToDoItem("john@test.org", 1, "Second Test", completed);

		DB.upsert(item1.toDBItem());
		DB.upsert(item2.toDBItem());
		DB.upsert(item3.toDBItem());

		List<ToDoItem> items = DB.list("john@rischar.org");
		Assert.assertTrue("Table should contain one items.", items.size() == 1);

	}

}
