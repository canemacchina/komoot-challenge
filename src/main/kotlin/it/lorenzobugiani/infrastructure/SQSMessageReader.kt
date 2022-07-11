package it.lorenzobugiani.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.scheduler.Scheduled
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.service.WelcomeMessageService
import javax.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.Message

private val log: Logger = Logger.getLogger(SQSMessageReader::class.java)
private const val MESSAGE_BATCH_SIZE = 10

@ApplicationScoped
class SQSMessageReader(
    @ConfigProperty(name = "sqs.queue.url") private val queueUrl: String,
    private val sqs: SqsClient,
    private val mapper: ObjectMapper,
    private val welcomeMessageService: WelcomeMessageService
) {
    @Scheduled(every = "{message-reader.scheduling-interval}")
    fun run() {

        log.debug("Invoked scheduled task")

        do {
            val messages: List<Message> = readMessages()

            log.debug("found ${messages.size} messages")

            messages.forEach { message ->
                log.debug("processing message ${message.messageId()}")

                val user = message.parse()

                welcomeMessageService.sendTo(user)

                sqs.deleteMessage { builder ->
                    builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle())
                }
            }
        } while (messages.isNotEmpty())

        log.debug("Finish processing messages. Sleeping")
    }

    private fun readMessages() = sqs.receiveMessage { builder ->
        builder.maxNumberOfMessages(MESSAGE_BATCH_SIZE).queueUrl(queueUrl)
    }.messages()

    private fun Message.parse(): User {
        val messageBody = mapper.readValue(body(), MessageBody::class.java)
        val payload = mapper.readValue(messageBody.payload, MessagePayload::class.java)
        return User(payload.id, payload.name)
    }
}

private data class MessageBody(
    @JsonProperty("MessageId") val id: String,
    @JsonProperty("Message") val payload: String
)

private data class MessagePayload(
    val name: String,
    val id: Long,
    @JsonProperty("created_at") val createdAt: String
)