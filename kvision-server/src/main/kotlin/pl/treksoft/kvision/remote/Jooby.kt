/*
 * Copyright (c) 2017-present Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package pl.treksoft.kvision.remote

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Unconfined
import org.jooby.Kooby
import org.jooby.Session
import org.jooby.json.Jackson
import org.pac4j.core.profile.CommonProfile
import kotlinx.coroutines.experimental.async as coroutinesAsync

/**
 * A Jooby based server.
 */
actual open class JoobyServer(init: JoobyServer.() -> Unit) : Kooby() {
    init {
        @Suppress("LeakingThis")
        assets("/", "index.html")
        @Suppress("LeakingThis")
        assets("/**").onMissing(0)
        val mapper = jacksonObjectMapper()
        @Suppress("LeakingThis")
        use(Jackson(mapper))
        @Suppress("LeakingThis")
        init.invoke(this)
    }
}

/**
 * A server request.
 */
actual typealias Request = org.jooby.Request

/**
 * A user profile.
 */
actual typealias Profile = CommonProfile

/**
 * A helper extension function for asynchronous request processing.
 */
fun <RESP> Request?.async(block: (Request) -> RESP): Deferred<RESP> = this?.let { req ->
    coroutinesAsync(Unconfined) {
        block(req)
    }
} ?: throw IllegalStateException("Request not set!")

/**
 * A helper extension function for asynchronous request processing with session.
 */
fun <RESP> Request?.async(block: (Request, Session) -> RESP): Deferred<RESP> = this?.let { req ->
    val session = req.session()
    coroutinesAsync(Unconfined) {
        block(req, session)
    }
} ?: throw IllegalStateException("Request not set!")

/**
 * A helper extension function for asynchronous request processing with session and user profile.
 */
fun <RESP> Request?.async(block: (Request, Session, Profile) -> RESP): Deferred<RESP> = this?.let { req ->
    val session = req.session()
    val profile = req.require(CommonProfile::class.java)
    coroutinesAsync(Unconfined) {
        block(req, session, profile)
    }
} ?: throw IllegalStateException("Request not set!")
