package lambdify.apigateway.kotlin

import lambdify.apigateway.Methods.POST
import lambdify.apigateway.ann.Body
import lambdify.apigateway.ann.PathParam
import lambdify.apigateway.ann.Route
import java.util.concurrent.atomic.AtomicLong

/**
 *
 */
class PollRepository( val userResource: UserResource ){

    val pollVoting = mutableMapOf<Long,Poll>()

    @Route( url = "/api/poll/:id", method = POST )
    fun vote( @PathParam("id") id:Long, @Body vote:Vote )
    {
        if ( userResource.retrieveUserById( vote.userId ) == null )
            throw RuntimeException("Not Authorized")

        val poll = pollVoting.computeIfAbsent( id, { Poll() } )
        poll.votes.computeIfAbsent( vote.option, { AtomicLong(0) } ).incrementAndGet()
    }
}

data class Vote (
    val option:Long,
    val userId:String
)

data class Poll (
    val id:Long = System.nanoTime(),
    val votes:MutableMap<Long, AtomicLong> = mutableMapOf()
)