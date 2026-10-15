package com.example.nammanivasa.data.repository

import java.io.IOException

/**
 * Why a repository read did not produce data.
 *
 * These categories are deliberately coarse. The UI only needs to tell "you are
 * signed out" apart from "you are offline" apart from "the server is broken" apart
 * from "there is genuinely nothing here". Finer detail stays in the data layer.
 */
sealed interface RepoError {

    /** No active community is selected, so a community-scoped read cannot be made. */
    data object NoActiveCommunity : RepoError

    /** Credentials are missing or rejected (HTTP 401). */
    data object Unauthenticated : RepoError

    /** Authenticated but not permitted for this community or resource (HTTP 403). */
    data object Forbidden : RepoError

    /** Connectivity, DNS, TLS or timeout failure. */
    data class Network(val detail: String? = null) : RepoError

    /** The server answered with a non-2xx status. */
    data class Server(val code: Int, val detail: String? = null) : RepoError

    /** Anything else: parsing, programming error, unexpected throwable. */
    data class Unexpected(val detail: String? = null) : RepoError
}

/**
 * Outcome of a repository read.
 *
 * [Success] means the request completed and the payload is authoritative. The
 * payload may legitimately be an empty collection — that is a different state from
 * [Failure], and callers must never collapse a [Failure] into an empty payload.
 * On financial screens in particular, "the request failed" and "nothing is owed"
 * must never be made to look alike.
 */
sealed interface RepoResult<out T> {
    data class Success<out T>(val data: T) : RepoResult<T>
    data class Failure(val error: RepoError) : RepoResult<Nothing>
}

/** The payload when successful, otherwise null. */
val <T> RepoResult<T>.dataOrNull: T?
    get() = (this as? RepoResult.Success)?.data

/** The error when failed, otherwise null. */
val RepoResult<*>.errorOrNull: RepoError?
    get() = (this as? RepoResult.Failure)?.error

/** Thrown when a community-scoped read has no active community to target. */
class NoActiveCommunityException : IllegalStateException("No active community is selected")

/**
 * Maps a thrown exception onto a [RepoError]. [NoActiveCommunityException] is
 * recognised explicitly so the UI can prompt for a community selection instead of
 * reporting a generic failure.
 */
fun repoErrorFrom(t: Throwable): RepoError = when (t) {
    is NoActiveCommunityException -> RepoError.NoActiveCommunity
    is IOException -> RepoError.Network(t.message)
    else -> RepoError.Unexpected(t.message)
}

/** Maps a non-2xx HTTP status onto a [RepoError]. */
fun repoErrorFrom(code: Int, message: String?): RepoError = when (code) {
    401 -> RepoError.Unauthenticated
    403 -> RepoError.Forbidden
    else -> RepoError.Server(code, message)
}

/** Short, human-readable explanation suitable for an error state. */
fun RepoError.userMessage(): String = when (this) {
    RepoError.NoActiveCommunity -> "Choose a community to see its details."
    RepoError.Unauthenticated -> "Your session has ended. Please sign in again."
    RepoError.Forbidden -> "You don't have access to this community."
    is RepoError.Network -> "No connection. Check your internet and try again."
    is RepoError.Server -> "The server couldn't complete that request. Please try again."
    is RepoError.Unexpected -> "Something went wrong. Please try again."
}
