package it.lorenzobugiani

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.GenericContainer

class DynamoDbExtension : QuarkusTestResourceLifecycleManager {

    private val dynamoDbContainer =
        GenericContainer("amazon/dynamodb-local:1.11.477")
            .withExposedPorts(8000)

    override fun start(): MutableMap<String, String> {
        dynamoDbContainer.start()
        return mutableMapOf(
            "quarkus.dynamodb.endpoint-override" to "http://${dynamoDbContainer.host}:${dynamoDbContainer.firstMappedPort}"
        )
    }

    override fun stop() {
        dynamoDbContainer.stop()
    }
}