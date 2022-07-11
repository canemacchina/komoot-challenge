package it.lorenzobugiani.infrastructure.repository

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.lorenzobugiani.domain.entity.User
import org.junit.jupiter.api.Test

internal class InMemoryUserRepositoryTest {
    private val repository = InMemoryUserRepository()

    @Test
    fun `Store the user`() {
        val user = User(1, "Lorenzo")

        repository.store(user)

        val userStored = repository.getLastRegisteredUsers()

        userStored.size shouldBe 1
        userStored[0] shouldBe user
    }

    @Test
    fun `Get user by id`() {
        val user = User(1, "Lorenzo")
        repository.store(user)

        val savedUser = repository.getUser(1)

        savedUser.shouldNotBeNull()
        savedUser.id shouldBe user.id
        savedUser.name shouldBe user.name
    }


    @Test
    fun `Return null if user don't exists`() {
        val savedUser = repository.getUser(1)

        savedUser.shouldBeNull()
    }

    @Test
    fun `Get last saved users`() {
        val user1 = User(1, "Lorenzo")
        val user2 = User(2, "Andrea")
        val user3 = User(3, "Maria")

        repository.store(user1)
        repository.store(user2)
        repository.store(user3)

        val lastUsers = repository.getLastRegisteredUsers(2)

        lastUsers.size shouldBe 2
        lastUsers[0] shouldBe user3
        lastUsers[1] shouldBe user2
    }

    @Test
    fun `Get last saved users when there are less user than requested`() {
        val user1 = User(1, "Lorenzo")
        val user2 = User(2, "Andrea")

        repository.store(user1)
        repository.store(user2)

        val lastUsers = repository.getLastRegisteredUsers(3)

        lastUsers.size shouldBe 2
        lastUsers[0] shouldBe user2
        lastUsers[1] shouldBe user1
    }

    @Test
    fun `Get empty list when there are no users`() {
        val lastUsers = repository.getLastRegisteredUsers(3)

        lastUsers.shouldBeEmpty()
    }
}
