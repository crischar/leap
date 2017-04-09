package org.rischar.todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.amazonaws.services.dynamodbv2.document.Item;

/**
 * ToDoItem to provide validation and conversion methods between this object and JSON.
 * 
 * @author carl
 *
 */
public class ToDoItem {

	private static String DateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static String userValidation = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

	private String user = null;
	private Integer priority = 9;
	private String description = "";
	private Optional<Date> completed = null;

	public ToDoItem() {
	}

	public ToDoItem(String user, Integer priority, String description, Optional<Date> completed) {
		setUser(user);
		setPriority(priority);
		setDescription(description);
		setCompleted(completed);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		if (user == null)
			throw new IllegalArgumentException("user cannot be null");
		if (user.length() < 1 || user.length() > 254)
			throw new IllegalArgumentException("user cannot have a length of zero or be greater than 254.");
		if (user.matches(userValidation))
			this.user = user;
		else
			throw new IllegalArgumentException("user must be a valid email address.");
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		if (priority < 0 || priority > 9) {
			throw new IllegalArgumentException("priority must be between 0 and 9.");
		}
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (description == null || description.length() < 1)
			throw new IllegalArgumentException("description must not be null and at least 1 character.");

		this.description = description;
	}

	public Optional<Date> getCompleted() {
		return completed;
	}

	public void setCompleted(Optional<Date> completed) {
		this.completed = completed;
	}

	public Item toDBItem() {
		SimpleDateFormat df = new SimpleDateFormat(DateFormat);
		return new Item().withString(FieldNames.UserKey, getUser()).withString(FieldNames.User, getUser())
				.withInt(FieldNames.Priority, getPriority()).withString(FieldNames.Description, getDescription())
				.with(FieldNames.Completed, completed.isPresent() ? df.format(completed.get()) : null);
	}

	public static ToDoItem fromDbItem(Item value) {
		SimpleDateFormat df = new SimpleDateFormat(DateFormat);

		Optional<Date> completed = Optional.empty();
		try {
			completed = Optional.ofNullable(df.parse(value.getString(FieldNames.Completed)));
		} catch (Exception e) {
		}

		return new ToDoItem(value.getString(FieldNames.User), value.getInt(FieldNames.Priority),
				value.getString(FieldNames.Description), completed);
	}

	public static ToDoItem fromMap(Map<String, String> map) {
		SimpleDateFormat df = new SimpleDateFormat(DateFormat);

		String user = map.get("user");
		String description = map.get("description");
		String priority = map.get("priority");
		String strCompleted = map.getOrDefault("completed", "");

		Optional<Date> completed = Optional.empty();
		try {
			completed = Optional.ofNullable(df.parse(strCompleted));
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		return new ToDoItem(user, Integer.parseInt(priority), description, completed);
	}

	public static Map<String, Object> toPairs(ToDoItem item) {
		SimpleDateFormat df = new SimpleDateFormat(DateFormat);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", item.getUser());
		result.put("description", item.getDescription());
		result.put("priority", item.getPriority());
		if (item.getCompleted().isPresent()) {
			result.put("completed", df.format(item.getCompleted().get()));
		}
		return result;

	}

	public static List<Map<String, Object>> toList(List<ToDoItem> values) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (ToDoItem value : values) {
			result.add(toPairs(value));
		}
		return result;
	}

}
