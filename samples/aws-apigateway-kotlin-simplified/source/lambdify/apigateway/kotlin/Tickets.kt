package lambdify.apigateway.kotlin

import lambdify.apigateway.APIGateway
import lambdify.apigateway.Methods
import lambdify.apigateway.Router

/**
 *
 */
class TicketsRouter:Router {

    val tickets = mutableMapOf<Long, Ticket>()

    override fun getRoutes() = arrayOf(
        Methods.GET and "/api/tickets" with ::retrieveTickets
    )

    fun retrieveTickets():APIGateway.Response {
        return APIGateway.Response.ok( tickets.values )
    }
}

class Ticket(
    val id:Long = System.nanoTime()
)