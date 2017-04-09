package org.rischar.todo;

/**
 * Constants for Keys uses in maps, json, and dynamodb tables.
 * 
 * @author carl
 *
 */
public class FieldNames {
	public static final String UserKey = "emailkey";
	public static final String User = "email";
	public static final String Description = "desciption";
	public static final String Priority = "priority";
	public static final String Completed = "completed";
	
	public static final String UserIndex = "emailIndex";
	public static final String TableName = "ToDo";
	public static final String CompletedIndex = "completedIndex";
}
