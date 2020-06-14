package com.github.triplet.gradle.play.tasks.internal.workers

import com.github.triplet.gradle.androidpublisher.PlayPublisher
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.github.triplet.gradle.play.internal.credentialStream
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

internal abstract class PlayWorkerBase<T : PlayWorkerBase.PlayPublishingParams> : WorkAction<T> {
    protected val config = parameters.config.get()
    protected val publisher = config.credentialStream().use {
        PlayPublisher(it, parameters.appId.get())
    }

    internal interface PlayPublishingParams : WorkParameters {
        val config: Property<PlayPublisherExtension>
        val appId: Property<String>
    }
}
