package org.rischar.todo;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;

/**
 * Simple lambda action that will pass inbound api requests to each of the lambed functions. A
 * lambda function must be installed in AWS for each of the methods.  I have taken the liberty 
 * of making add/update be the same based on the email address and description.
 * 
 * This whole lambda things seems pretty cool.  Though I think java is a bit overkill for what 
 * this is doing.
 * @author carl
 *
 */
public class TodoActions {

	public String add(Map<String, String> input, Context context) {
		context.getLogger().log("add action");
		Item item = ToDoItem.fromMap(input).toDBItem();
		DB.upsert(item);
		return "ok";
	}

	public String update(Map<String, String> input, Context context) {
		context.getLogger().log("update action");
		Item item = ToDoItem.fromMap(input).toDBItem();
		DB.upsert(item);
		return "ok";
	}

	public String delete(Map<String, String> input, Context context) {
		context.getLogger().log("delete action");
		String user = input.get("user");
		String description = input.get("description");
		DB.delete(user, description);
		return "ok";
	}

	public List<Map<String, Object>> list(Map<String, String> input, Context context) {
		context.getLogger().log("list action");
		String user = input.get("user");
		List<ToDoItem> results = DB.list(user);
		return ToDoItem.toList(results);
	}

}
