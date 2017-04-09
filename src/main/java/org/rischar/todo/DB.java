package org.rischar.todo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

/**
 * Set of methods to create dynamo db table, and add, update and retrieve information 
 * based on an email address for todo list.
 * 
 * @author carl
 *
 */
public class DB {

	public static AmazonDynamoDB client = null;
	public static DynamoDB instance = null;
	public static String endpoint = null;

	public static String getEndpoint() {
		return endpoint;
	}

	public static void setEndpoint(String endpoint) {
		DB.endpoint = endpoint;
	}

	private static AmazonDynamoDB getClient() {
		if (client == null) {
			client = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain());
			if (endpoint != null)
				client.setEndpoint(endpoint);
		}
		return client;
	}

	private static DynamoDB getInstance() {
		if (instance == null)
			instance = new DynamoDB(getClient());
		return instance;
	}
	


	/**
	 * Create and provision the ToDo table along with a secondary index to allow
	 * the retrieval of all todo items by the @FieldNames.User.
	 * 
	 */
	public static Table create() {
		System.out.println("Issue create table request.");
		DynamoDB dynamoDB = getInstance();
		CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(FieldNames.TableName);
		// ProvisionedThroughput
		createTableRequest.setProvisionedThroughput(
				new ProvisionedThroughput().withReadCapacityUnits((long) 1).withWriteCapacityUnits((long) 1));

		// All the attribute for the key and what we may sub search on.
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		attributeDefinitions
				.add(new AttributeDefinition().withAttributeName(FieldNames.UserKey).withAttributeType("S"));
		attributeDefinitions
				.add(new AttributeDefinition().withAttributeName(FieldNames.Description).withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName(FieldNames.User).withAttributeType("S"));

		createTableRequest.setAttributeDefinitions(attributeDefinitions);

		// Master Index
		ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();
		tableKeySchema.add(new KeySchemaElement().withAttributeName(FieldNames.UserKey).withKeyType(KeyType.HASH)); // Partition
																													// key
		tableKeySchema.add(new KeySchemaElement().withAttributeName(FieldNames.Description).withKeyType(KeyType.RANGE)); // Sort
																															// key
		createTableRequest.setKeySchema(tableKeySchema);

		ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();
		indexKeySchema.add(new KeySchemaElement().withAttributeName(FieldNames.UserKey).withKeyType(KeyType.HASH)); // Partition
		indexKeySchema.add(new KeySchemaElement().withAttributeName(FieldNames.User).withKeyType(KeyType.RANGE)); // Sort

		Projection projection = new Projection().withProjectionType(ProjectionType.INCLUDE);
		ArrayList<String> nonKeyAttributes = new ArrayList<String>();
		nonKeyAttributes.add(FieldNames.Priority);
		nonKeyAttributes.add(FieldNames.Description);
		nonKeyAttributes.add(FieldNames.Completed);
		projection.setNonKeyAttributes(nonKeyAttributes);

		LocalSecondaryIndex localSecondaryIndex = new LocalSecondaryIndex().withIndexName(FieldNames.UserIndex)
				.withKeySchema(indexKeySchema).withProjection(projection);

		
		ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
		localSecondaryIndexes.add(localSecondaryIndex);
			
		createTableRequest.setLocalSecondaryIndexes(localSecondaryIndexes);

		Table table = dynamoDB.createTable(createTableRequest);
		return table;
	}

	public static void deleteTable() {
		Table table = getInstance().getTable(FieldNames.TableName);
		try {
			System.out.println("Issuing DeleteTable request for " + FieldNames.TableName);
			table.delete();

			System.out.println("Waiting for " + FieldNames.TableName + " to be deleted...this may take a while...");

			table.waitForDelete();
		} catch (Exception e) {
			System.err.println("DeleteTable request failed for " + FieldNames.TableName);
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Insert/Update a todo note based on the userkey and description.
	 * 
	 * @param value
	 *            map of data to add to the todo table.
	 * @return
	 */
	public static PutItemOutcome upsert(Item value) {
		return getInstance().getTable(FieldNames.TableName).putItem(value);
	}

	/**
	 * delete an item based on the user an description.
	 * 
	 * @param user
	 * @param description
	 * @return
	 */
	public static DeleteItemOutcome delete(String user, String description) {
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
				.withPrimaryKey(new PrimaryKey(FieldNames.UserKey, user, FieldNames.Description, description));

		return getInstance().getTable(FieldNames.TableName).deleteItem(deleteItemSpec);
	}

	public static List<ToDoItem> list(String user) {
		DynamoDB dynamoDB = getInstance();
		Table table = dynamoDB.getTable(FieldNames.TableName);
		Index index = table.getIndex(FieldNames.UserIndex);

		QuerySpec spec = new QuerySpec()
				.withKeyConditionExpression(
						String.format("%s = :v_userkey and %s = :v_user", FieldNames.UserKey, FieldNames.User))
				.withValueMap(new ValueMap().withString(":v_userkey", user).withString(":v_user", user));

		ItemCollection<QueryOutcome> items = index.query(spec);

		List<ToDoItem> results = new ArrayList<ToDoItem>();
		for (Item item : items) {
			results.add(ToDoItem.fromDbItem(item));
		}
		return results;

	}

}
