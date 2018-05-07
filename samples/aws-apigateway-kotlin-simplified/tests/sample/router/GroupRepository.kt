package sample.router

import lambdify.apigateway.Methods.GET
import lambdify.apigateway.ann.Body
import lambdify.apigateway.ann.Route

/**
 *
 */
data class Group( val id:Long = System.nanoTime() )

class GroupRepository {

    @Route( url = "/api/group", method = GET )
    fun doSomething( @Body group: Group) {
        println( group )
    }
}
