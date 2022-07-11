package it.lorenzobugiani.domain.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository
import it.lorenzobugiani.domain.usecase.GenerateNotificationMessage
import it.lorenzobugiani.domain.usecase.GetUser
import it.lorenzobugiani.domain.usecase.NotificationMessage
import it.lorenzobugiani.domain.usecase.SaveUser
import it.lorenzobugiani.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WelcomeMessageServiceTest {
    private lateinit var repository: UserRepository
    private lateinit var service: WelcomeMessageService
    private lateinit var saveUser: SaveUser
    private lateinit var getUser: GetUser
    private lateinit var notificationService: NotificationService
    private lateinit var generateNotificationMessage: GenerateNotificationMessage

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
        saveUser = SaveUser(repository)
        getUser = GetUser(repository)
        generateNotificationMessage = GenerateNotificationMessage(repository)
        notificationService = mockk(relaxUnitFun = true)
        service = WelcomeMessageService(
            generateNotificationMessage,
            saveUser,
            getUser,
            notificationService
        )
    }

    @Test
    fun `Send the notification message`() {
        val newUser = User(1589278470, "Marcus")

        val user1 = User(1, "Lorenzo")
        val user2 = User(304390273, "Stephen")
        val user3 = User(1093883245, "Anna")
        val user4 = User(627362498, "Lise")

        repository.store(user1)
        repository.store(user2)
        repository.store(user3)
        repository.store(user4)

        service.sendTo(newUser)

        val expectedMessage = NotificationMessage(
            "Hi Marcus, welcome to komoot. Lise, Anna and Stephen also joined recently.",
            listOf(627362498, 1093883245, 304390273),
            newUser.id
        )
        verify(exactly = 1) { notificationService.send(expectedMessage) }
    }

    @Test
    fun `Save the new user`() {
        val newUser = User(1589278470, "Marcus")

        val user1 = User(1, "Lorenzo")
        val user2 = User(304390273, "Stephen")
        val user3 = User(1093883245, "Anna")
        val user4 = User(627362498, "Lise")

        repository.store(user1)
        repository.store(user2)
        repository.store(user3)
        repository.store(user4)

        service.sendTo(newUser)

        val savedUser = repository.getLastRegisteredUsers(1);

        savedUser.shouldHaveSize(1)
        savedUser[0] shouldBe newUser
    }

    @Test
    fun `Don't send two notifications to the same user`() {
        val newUser = User(1589278470, "Marcus")

        service.sendTo(newUser)
        service.sendTo(newUser)

        verify(exactly = 1) { notificationService.send(any()) }

        val savedUser = repository.getLastRegisteredUsers(2);
        savedUser.shouldHaveSize(1)
        savedUser[0] shouldBe newUser
    }
}