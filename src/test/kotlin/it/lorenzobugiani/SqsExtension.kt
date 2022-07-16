package it.lorenzobugiani

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer

class SqsExtension : QuarkusTestResourceLifecycleManager {

    private val sqsContainer =
        GenericContainer("roribio16/alpine-sqs:latest")
            .withExposedPorts(9324)
            .withFileSystemBind(
                "./docker/sqs/elasticmq.conf",
                "/opt/custom/elasticmq.conf",
                BindMode.READ_ONLY
            )

    override fun start(): MutableMap<String, String> {
        sqsContainer.start()
        return mutableMapOf(
            "quarkus.sqs.endpoint-override" to "http://${sqsContainer.host}:${sqsContainer.firstMappedPort}",
            "sqs.queue.url" to "http://${sqsContainer.host}:${sqsContainer.firstMappedPort}/queue/default"
        )
    }

    override fun stop() {
        sqsContainer.stop()
    }
}