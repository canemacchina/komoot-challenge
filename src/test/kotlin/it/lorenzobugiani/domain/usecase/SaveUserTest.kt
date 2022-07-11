package it.lorenzobugiani.domain.usecase

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository
import it.lorenzobugiani.infrastructure.repository.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SaveUserTest {

    private lateinit var repository: UserRepository
    private lateinit var useCase: SaveUser

    @BeforeEach
    fun setUp() {
        repository = InMemoryUserRepository()
        useCase = SaveUser(repository)
    }

    @Test
    fun `Save the user`(){
        val user = User(1, "Lorenzo")

        useCase.execute(user)

        val savedUser = repository.getLastRegisteredUsers(1);

        savedUser.shouldHaveSize(1)
        savedUser[0] shouldBe user
    }
}