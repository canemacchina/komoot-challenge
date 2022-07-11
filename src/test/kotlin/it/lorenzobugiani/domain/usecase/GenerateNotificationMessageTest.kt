package it.lorenzobugiani.domain.usecase

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository
import it.lorenzobugiani.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GenerateNotificationMessageTest {

    private lateinit var repository: UserRepository
    private lateinit var useCase: GenerateNotificationMessage

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
        useCase = GenerateNotificationMessage(repository)
    }

    @Test
    fun `Create the user notification message`() {
        val newUser = User(1589278470, "Marcus")

        val user1 = User(1, "Lorenzo")
        val user2 = User(304390273, "Stephen")
        val user3 = User(1093883245, "Anna")
        val user4 = User(627362498, "Lise")

        repository.store(user1)
        repository.store(user2)
        repository.store(user3)
        repository.store(user4)

        val actual = useCase.execute(newUser)

        actual.message shouldBe "Hi Marcus, welcome to komoot. Lise, Anna and Stephen also joined recently."
        actual.ids shouldBe listOf(627362498, 1093883245, 304390273)
        actual.receiver shouldBe newUser.id
    }

    @Test
    fun `Create the user notification message for the first user`() {
        val newUser = User(1589278470, "Marcus")

        val actual = useCase.execute(newUser)

        actual.message shouldBe "Hi Marcus, welcome to komoot."
        actual.ids.shouldBeEmpty()
        actual.receiver shouldBe newUser.id
    }

    @Test
    fun `Create the user notification message when no enough user are registered`() {
        val newUser = User(1589278470, "Marcus")

        val user1 = User(1093883245, "Anna")
        val user2 = User(627362498, "Lise")

        repository.store(user1)
        repository.store(user2)

        val actual = useCase.execute(newUser)

        actual.message shouldBe "Hi Marcus, welcome to komoot. Lise and Anna also joined recently."
        actual.ids shouldBe listOf(627362498, 1093883245)
        actual.receiver shouldBe newUser.id
    }
}