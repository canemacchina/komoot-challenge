package it.lorenzobugiani.infrastructure

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.service.WelcomeMessageService
import java.net.URI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest

@Testcontainers
internal class SQSMessageReaderTest {

    private val sqsContainer =
        GenericContainer("roribio16/alpine-sqs:latest").withExposedPorts(9324)
            .withFileSystemBind(
                "./docker/sqs/elasticmq.conf",
                "/opt/custom/elasticmq.conf",
                BindMode.READ_ONLY
            )

    private val mapper =
        jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    private val welcomeMessageService: WelcomeMessageService = mockk()

    private lateinit var sqsClient: SqsClient
    private lateinit var queueUrl: String

    private lateinit var reader: SQSMessageReader

    @BeforeEach
    fun setUp() {
        sqsContainer.start()
        sqsClient = spyk(getSqsClient())
        queueUrl = "http://${sqsContainer.host}:${sqsContainer.firstMappedPort}/queue/default"
        reader = SQSMessageReader(queueUrl, sqsClient, mapper, welcomeMessageService)
    }

    @AfterEach
    fun tearDown() {
        sqsContainer.stop()
    }

    @Test
    fun `Call the WelcomeMessageService with the user`() {
        every { welcomeMessageService.sendTo(any()) } just Runs
        val name = "Marcus"
        val id = 1589278470L
        val newUser = User(id, name)
        val message = getMessage(id, name)

        sqsClient.sendMessage { builder ->
            builder.queueUrl(queueUrl).messageBody(message)
        }

        reader.run()

        verify(exactly = 1) { welcomeMessageService.sendTo(newUser) }
    }

    @Test
    fun `When run read all pending messages`() {
        every { welcomeMessageService.sendTo(any()) } just Runs
        val name = "Marcus"
        val id = 1589278470L
        val newUser = User(id, name)
        val message = getMessage(id, name)

        for (i in 1..100) {
            sqsClient.sendMessage { builder ->
                builder.queueUrl(queueUrl).messageBody(message)
            }
        }

        reader.run()

        verify(exactly = 100) { welcomeMessageService.sendTo(newUser) }
    }

    @Test
    fun `Delete messages after processing them`() {
        every { welcomeMessageService.sendTo(any()) } just Runs
        val message = getMessage(1589278470L, "Marcus")
        sqsClient.sendMessage { builder ->
            builder.queueUrl(queueUrl).messageBody(message)
        }

        reader.run()

        verify { sqsClient.deleteMessage(any<DeleteMessageRequest>()) }
    }

    private fun getSqsClient() = SqsClient.builder()
        .region(Region.EU_WEST_1)
        .endpointOverride(URI.create("http://${sqsContainer.host}:${sqsContainer.firstMappedPort}"))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("FAKE", "FAKE")
            )
        )
        .build()

    private fun getMessage(id: Long, name: String) =
        "{\"MessageId\":\"9c63e4a7-18ed-4aaf-a9d4-abb8c213c39b\",\"Message\":\"{ \\\"name\\\": \\\"$name\\\", \\\"id\\\": $id, \\\"created_at\\\": \\\"2020-05-12T16:11:54.000\\\" }\",\"Type\":\"Notification\"}"


}