package it.lorenzobugiani.infrastructure.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import it.lorenzobugiani.WireMockExtensions
import it.lorenzobugiani.domain.usecase.NotificationMessage
import javax.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource(WireMockExtensions::class)
internal class HttpNotificationServiceTest {

    @Inject
    private lateinit var mapper: ObjectMapper

    @Inject
    private lateinit var notificationService: HttpNotificationService

    @ConfigProperty(name = "notification.sender")
    private lateinit var sender: String


    @Test
    fun `Post the welcome message`() {
        stubFor(post("/").willReturn(ok()))

        val message = "message"
        val recentUsers = listOf(1L, 2L)
        val receiver = 3L
        val notificationMessage = NotificationMessage(message, recentUsers, receiver)

        notificationService.send(notificationMessage)

        val expectedHttpMessage =
            mapper.writeValueAsString(HttpNotification(sender, receiver, message, recentUsers))

        verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo(expectedHttpMessage)))
    }

}