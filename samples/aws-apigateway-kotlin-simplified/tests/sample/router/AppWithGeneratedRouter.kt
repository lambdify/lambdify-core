package sample.router

import lambdify.apigateway.kotlin.App

/**
 *
 */
class AppWithGeneratedRouter:App({

    routers(
        GroupRepositoryRouter()
    )
})