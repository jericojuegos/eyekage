import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
    source.setFrom(
        files(
            "core/src",
            "ui/src",
            "platform/src",
            "feature/src",
            "app/src"
        )
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

tasks.register("checkMergedManifestPermissions") {
    group = "verification"
    description = "Enforces strict manifest allowlist (CAMERA, RECORD_AUDIO) and denylist (INTERNET, etc.)"
    dependsOn(":app:processDebugMainManifest")

    val approvedHardwarePermissions = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO"
    )
    val prohibitedPermissions = setOf(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    doLast {
        val appBuildDir = project(":app").layout.buildDirectory.asFile.get()
        val candidatePaths = listOf(
            File(appBuildDir, "intermediates/merged_manifest/detectorMediapipeDebug/processDetectorMediapipeDebugMainManifest/AndroidManifest.xml"),
            File(appBuildDir, "intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml"),
            File(appBuildDir, "intermediates/merged_manifests/detectorMediapipeDebug/AndroidManifest.xml"),
            File(appBuildDir, "intermediates/merged_manifests/debug/AndroidManifest.xml")
        )
        val manifestFile = candidatePaths.firstOrNull { it.exists() }
            ?: throw GradleException("checkMergedManifestPermissions: Could not locate merged AndroidManifest.xml. Ensure manifest merging task ran.")

        println("Auditing merged manifest permissions in: ${manifestFile.absolutePath}")

        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = builder.parse(manifestFile)
        val permissionNodes = doc.getElementsByTagName("uses-permission")

        val foundPermissions = mutableSetOf<String>()
        for (i in 0 until permissionNodes.length) {
            val node = permissionNodes.item(i)
            val nameAttr = node.attributes.getNamedItem("android:name")?.nodeValue
            if (nameAttr != null) {
                foundPermissions.add(nameAttr)
            }
        }

        println("Found permissions in merged manifest: $foundPermissions")

        // 1. Prohibited permissions check
        val violations = foundPermissions.intersect(prohibitedPermissions)
        if (violations.isNotEmpty()) {
            throw GradleException("PRIVACY VIOLATION: Merged manifest contains strictly prohibited permissions: $violations")
        }

        // 2. Hardware permissions allowlist check (exclude internal AndroidX DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION)
        val externalPermissions = foundPermissions.filterNot { it.endsWith(".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION") }.toSet()
        val unapproved = externalPermissions - approvedHardwarePermissions
        if (unapproved.isNotEmpty()) {
            throw GradleException("ALLOWLIST VIOLATION: Merged manifest contains unapproved hardware/platform permissions: $unapproved. Approved: $approvedHardwarePermissions")
        }

        println("✅ checkMergedManifestPermissions PASSED. Merged manifest strictly complies with privacy allowlist.")
    }
}
