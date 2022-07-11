package it.lorenzobugiani.infrastructure.repository

import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository
import javax.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest

private const val PKEY_FIELD = "pkey"
private const val PKEY_DEFAULT_VALUE = "user"
private const val ID_FIELD = "id"
private const val NAME_FIELD = "name"

@ApplicationScoped
class DynamoDbRepository(
    private val dynamoDbClient: DynamoDbClient,
    @ConfigProperty(name = "dynamo.db.table-name") private val tableName: String
) : UserRepository {
    override fun store(user: User) {
        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(user.toItem())
            .build()

        dynamoDbClient.putItem(request)
    }

    override fun getUser(id: Long): User? {
        val getRequest = GetItemRequest.builder()
            .tableName(tableName)
            .key(
                mapOf(
                    PKEY_FIELD to AttributeValue.builder().s(PKEY_DEFAULT_VALUE).build(),
                    ID_FIELD to AttributeValue.builder().n("$id").build()
                )
            )
            .attributesToGet(ID_FIELD, NAME_FIELD)
            .consistentRead(true)
            .build()

        val response = dynamoDbClient.getItem(getRequest)

        return if (response.hasItem()) {
            response.item().fromItem()
        } else {
            null
        }
    }

    override fun getLastRegisteredUsers(n: Int): List<User> {
        val queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("$PKEY_FIELD = :key")
            .expressionAttributeValues(
                mapOf(
                    ":key" to AttributeValue.builder().s(PKEY_DEFAULT_VALUE).build()
                )
            )
            .scanIndexForward(false)
            .limit(n)
            .build()

        return dynamoDbClient.query(queryRequest).items().map { it.fromItem() }
    }

    private fun User.toItem(): Map<String, AttributeValue> {
        val item: MutableMap<String, AttributeValue> = HashMap()
        item[ID_FIELD] = AttributeValue.builder().n(id.toString()).build()
        item[PKEY_FIELD] = AttributeValue.builder().s(PKEY_DEFAULT_VALUE).build()
        item[NAME_FIELD] = AttributeValue.builder().s(name).build()
        return item
    }

    private fun Map<String, AttributeValue>.fromItem() =
        User(this[ID_FIELD]!!.n().toLong(), this[NAME_FIELD]!!.s())
}