package interplay

import sbt.{AutoPlugin, ThisBuild}
import sbt.Keys.{version, thisProjectRef}
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import com.jsuereth.sbtpgp.PgpKeys

/**
 * Base Plugin for releasing.
 *
 * Generally this should only be enabled for the root project.
 */
object PlayReleaseBase extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = PlayBuildBase && ReleasePlugin

  import PlayBuildBase.autoImport._

  override def projectSettings = Seq(
    playBuildExtraPublish := { () },
    playBuildExtraTests := { () },

    // Release settings
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseTagName := (ThisBuild / version).value,
    releaseProcess := {
      import ReleaseTransformations._

      Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        releaseStepCommandAndRemaining("+test"),

        releaseStepTask(thisProjectRef.value / playBuildExtraTests),
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,

        releaseStepCommandAndRemaining("+publishSigned"),

        releaseStepTask(thisProjectRef.value / playBuildExtraPublish),
        // Using `playBuildPromoteSonatype` is obsolete now.
        // ifDefinedAndTrue(playBuildPromoteSonatype, releaseStepCommand("sonatypeBundleRelease")),
        releaseStepCommand("sonatypeBundleRelease"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  )

}
