package it.lorenzobugiani

import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.POST
import javax.ws.rs.Path


@ApplicationScoped
@Path("/echo")
class EchoNotification() {

    @POST
    fun post(body: String) {
        println(body)
    }
}