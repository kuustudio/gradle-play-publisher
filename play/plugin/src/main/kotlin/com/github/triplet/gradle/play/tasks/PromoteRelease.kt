package com.github.triplet.gradle.play.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.github.triplet.gradle.play.tasks.internal.PublishArtifactTaskBase
import com.github.triplet.gradle.play.tasks.internal.UpdatableTrackExtensionOptions
import com.github.triplet.gradle.play.tasks.internal.workers.PublishArtifactWorkerBase
import com.github.triplet.gradle.play.tasks.internal.workers.paramsForBase
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal abstract class PromoteRelease @Inject constructor(
        extension: PlayPublisherExtension,
        variant: ApplicationVariant
) : PublishArtifactTaskBase(extension, variant), UpdatableTrackExtensionOptions {
    init {
        // Always out-of-date since we don't know what's changed on the network
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun promote() {
        project.serviceOf<WorkerExecutor>().noIsolation().submit(Promoter::class) {
            paramsForBase(this)
        }
    }

    abstract class Promoter : PublishArtifactWorkerBase<PublishArtifactWorkerBase.ArtifactPublishingParams>() {
        override fun upload() {
            val fromTrack = config.fromTrack.orNull ?: edits.findLeastStableTrackName()
            checkNotNull(fromTrack) { "No tracks to promote. Did you mean to run publish?" }
            val promoteTrack = config.promoteTrack.orNull ?: fromTrack

            edits.promoteRelease(
                    promoteTrack,
                    fromTrack,
                    config.releaseStatus.orNull,
                    findReleaseName(promoteTrack),
                    findReleaseNotes(fromTrack),
                    config.userFraction.orNull,
                    config.updatePriority.orNull,
                    config.retain.artifacts.orNull
            )
        }
    }
}
