package com.github.triplet.gradle.play.internal

import com.github.triplet.gradle.androidpublisher.PlayPublisher
import com.github.triplet.gradle.play.PlayPublisherExtension
import org.gradle.api.provider.Property
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.reflect.full.declaredMemberProperties

internal fun PlayPublisherExtension.credentialStream(): InputStream {
    return serviceAccountCredentials.orNull?.inputStream() ?: ByteArrayInputStream(
            System.getenv(PlayPublisher.CREDENTIAL_ENV_VAR).toByteArray())
}

internal fun mergeExtensions(extensions: List<PlayPublisherExtension>): PlayPublisherExtension {
    requireNotNull(extensions.isNotEmpty()) { "At least one extension must be provided." }
    if (extensions.size == 1) return extensions.single()

    for (i in 1 until extensions.size) {
        for (property in PlayPublisherExtension::class.declaredMemberProperties) {
            val value = property.get(extensions[i - 1])
            if (value is Property<*>) {
                value.convention(property.get(extensions[i]) as Property<Nothing>)
            }
        }
    }

    return extensions.first()
}
