package example.micronaut

import geb.spock.GebSpec
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ !sys['geb.env'] })
//tag::clazz[]
class SessionAuthenticationSpec extends GebSpec {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    def "verify session based authentication works"() {
        given:
        browser.baseUrl = "http://localhost:${embeddedServer.port}"

        when:
        to HomePage

        then:
        at HomePage

        when:
        HomePage homePage = browser.page HomePage

        then: 'As we are not logged in, there is no username'
        homePage.username() == null

        when: 'click the login link'
        homePage.login()

        then:
        at LoginPage

        when: 'fill the login form, with invalid credentials'
        LoginPage loginPage = browser.page LoginPage
        loginPage.login('foo', 'foo')

        then: 'the user is still in the login form'
        at LoginPage

        and: 'and error is displayed'
        loginPage.hasErrors()

        when: 'fill the form with wrong credentials'
        loginPage.login('sherlock', 'foo')

        then: 'we get redirected to the home page'
        at LoginFailedPage

        when: 'fill the form with valid credentials'
        loginPage.login('sherlock', 'password')

        then: 'we get redirected to the home page'
        at HomePage

        when:
        homePage = browser.page HomePage

        then: 'the username is populated'
        homePage.username() == 'sherlock'

        when: 'click the logout button'
        homePage.logout()

        then: 'we are in the home page'
        at HomePage

        when:
        homePage = browser.page HomePage

        then: 'but we are no longer logged in'
        homePage.username() == null
    }
}
//end::clazz[]
