package it.lorenzobugiani.domain.usecase

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository
import it.lorenzobugiani.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetUserTest {

    private lateinit var repository: UserRepository
    private lateinit var useCase: GetUser

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
        useCase = GetUser(repository)
    }

    @Test
    fun `Get user by id`() {
        val user = User(1, "Lorenzo")
        repository.store(user)

        val savedUser = useCase.execute(1)

        savedUser.shouldNotBeNull()
        savedUser.id shouldBe user.id
        savedUser.name shouldBe user.name
    }


    @Test
    fun `Return null if user don't exists`() {
        val savedUser = useCase.execute(1)

        savedUser.shouldBeNull()
    }
}