package it.lorenzobugiani.domain.entity

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test


internal class UserTest {

    @Test
    fun `Two user are equals if have same id`() {
        val user1 = User(1, "Lorenzo")
        val user2 = User(1, "Andrea")
        val user3 = User(2, "Lorenzo")

        (user1 == user2) shouldBe true

        (user1 == user3) shouldBe false
    }

    @Test
    fun `Hash code use only the id`() {
        val user1 = User(1, "Lorenzo")
        val user2 = User(1, "Andrea")
        val user3 = User(2, "Lorenzo")

        user1.hashCode() shouldBe user2.hashCode()
        user1.hashCode() shouldNotBe user3.hashCode()
    }

    @Test
    fun `toString implementation`() {
        User(1, "Lorenzo").toString() shouldBe "User(id=1, name='Lorenzo')"
    }
}