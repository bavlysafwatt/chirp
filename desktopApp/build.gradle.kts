import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    implementation(libs.firebase.common)
    implementation(libs.firebase.auth)
}

compose.desktop {
    application {
        mainClass = "com.bavly.chirp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.bavly.chirp"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
            }

            linux {
                iconFile.set(project.file("src/main/resources/logo.png"))
            }
        }
    }
}