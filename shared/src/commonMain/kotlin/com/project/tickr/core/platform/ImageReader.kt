package com.project.tickr.core.platform

expect suspend fun readImageBytes(uriPath: String): ByteArray?
