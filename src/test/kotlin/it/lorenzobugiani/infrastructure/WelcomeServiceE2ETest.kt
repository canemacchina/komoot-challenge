package it.lorenzobugiani.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import it.lorenzobugiani.DynamoDbExtension
import it.lorenzobugiani.DynamoDbUtils
import it.lorenzobugiani.SqsExtension
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.infrastructure.service.HttpNotification
import javax.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.sqs.SqsClient

@Testcontainers
@QuarkusTest
@QuarkusTestResource(DynamoDbExtension::class, restrictToAnnotatedClass = true)
@QuarkusTestResource(SqsExtension::class, restrictToAnnotatedClass = true)
class WelcomeServiceE2ETest {

    @Inject
    private lateinit var reader: SQSMessageReader

    @Inject
    private lateinit var sqsClient: SqsClient

    @Inject
    private lateinit var dynamoDbClient: DynamoDbClient

    @ConfigProperty(name = "sqs.queue.url")
    private lateinit var queueUrl: String

    @Inject
    private lateinit var mapper: ObjectMapper

    @ConfigProperty(name = "notification.sender")
    private lateinit var sender: String

    @BeforeEach
    fun setUp() {
        DynamoDbUtils.initDb(dynamoDbClient)
    }

    @AfterEach
    fun tearDown() {
        DynamoDbUtils.cleanDb(dynamoDbClient)
    }

    @Test
    fun `Send notification`() {
        insertUSer(User(1, "Lorenzo"))
        insertUSer(User(2, "Maria"))
        insertUSer(User(3, "Andrea"))
        insertUSer(User(4, "Mimma"))

        stubFor(post("/").willReturn(ok()))

        sqsClient.sendMessage { builder ->
            builder.messageBody(getMessage(5, "Marcus")).queueUrl(queueUrl)
        }

        val message = "Hi Marcus, welcome to komoot. Mimma, Andrea and Maria also joined recently."

        val expectedHttpMessage =
            mapper.writeValueAsString(HttpNotification(sender, 5, message, listOf(4, 3, 2)))

        reader.run()

        verify(
            postRequestedFor(urlEqualTo("/"))
                .withRequestBody(equalTo(expectedHttpMessage))
        )
    }

    @Test
    fun `Avoid send notification for already notified user`() {
        insertUSer(User(1, "Lorenzo"))
        insertUSer(User(2, "Maria"))
        insertUSer(User(3, "Andrea"))
        insertUSer(User(4, "Mimma"))

        sqsClient.sendMessage { builder ->
            builder.messageBody(getMessage(3, "Andrea")).queueUrl(queueUrl)
        }

        reader.run()

        verify(exactly(0), postRequestedFor(urlEqualTo("/")))
    }

    private fun insertUSer(user: User) {
        val item: MutableMap<String, AttributeValue> = HashMap()
        item["id"] = AttributeValue.builder().n(user.id.toString()).build()
        item["pkey"] = AttributeValue.builder().s("user").build()
        item["name"] = AttributeValue.builder().s(user.name).build()
        val request = PutItemRequest.builder()
            .tableName("users")
            .item(item)
            .build()

        dynamoDbClient.putItem(request)
    }

    private fun getMessage(id: Long, name: String) =
        "{\"MessageId\":\"9c63e4a7-18ed-4aaf-a9d4-abb8c213c39b\",\"Message\":\"{ \\\"name\\\": \\\"$name\\\", \\\"id\\\": $id, \\\"created_at\\\": \\\"2020-05-12T16:11:54.000\\\" }\",\"Type\":\"Notification\"}"

}