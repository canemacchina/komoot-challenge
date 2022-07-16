package it.lorenzobugiani

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

object DynamoDbUtils {

    fun initDb(dynamoDbClient: DynamoDbClient) {
        dynamoDbClient.createTable {
            it.keySchema(
                KeySchemaElement.builder()
                    .keyType(KeyType.HASH)
                    .attributeName("pkey")
                    .build(),
                KeySchemaElement.builder()
                    .keyType(KeyType.RANGE)
                    .attributeName("id")
                    .build()
            ).attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("pkey")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("id")
                    .attributeType(ScalarAttributeType.N)
                    .build()
            ).provisionedThroughput(
                ProvisionedThroughput.builder().readCapacityUnits(1).writeCapacityUnits(1).build()
            ).tableName("users")
        }
    }

    fun cleanDb(dynamoDbClient: DynamoDbClient) {
        dynamoDbClient.deleteTable {
            it.tableName("users")
        }
    }

}