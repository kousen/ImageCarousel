import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
}

group = "edu.trincoll"
version = "1.0-SNAPSHOT"

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.AZUL
    }
}

application {
    mainModule.set("edu.trincoll.imagecarousel")
    mainClass.set("edu.trincoll.ImageCarousel")
}

repositories {
    mavenCentral()
}

dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-controls:21.0.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.3")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")

    testImplementation("org.hamcrest:hamcrest:3.0")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf(
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.tk.quantum=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.runtime=ALL-UNNAMED"
    )
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
}

jlink {
    options.set(listOf(
        "--strip-debug",
        "--compress=2",
        "--no-header-files",
        "--no-man-pages"
    ))

    launcher {
        name = "ImageCarousel"
    }

    jpackage {
        // Common options for all platforms
        imageOptions = listOf(
            "--vendor", "Trinity College",
            "--copyright", "Copyright 2024",
            "--name", "ImageCarousel",
            "--description", "Image Carousel Application"
        )
        skipInstaller = false
    }
}

tasks {
    register<org.beryx.jlink.JPackageTask>("jpackageMac") {
        dependsOn("jlink")
        doFirst {
            jpackageData.apply {
                targetPlatformName = "mac"
                imageOptions = listOf(
                    "--vendor", "Trinity College",
                    "--copyright", "Copyright 2024",
                    "--name", "ImageCarousel",
                    "--description", "Image Carousel Application"
                )
                installerType = "pkg"
                installerOptions = listOf("--mac-package-name", "ImageCarousel")
            }
        }
    }

    register<org.beryx.jlink.JPackageTask>("jpackageWin") {
        dependsOn("jlink")
        doFirst {
            jpackageData.apply {
                targetPlatformName = "win"
                imageOptions = listOf(
                    "--vendor", "Trinity College",
                    "--copyright", "Copyright 2024",
                    "--name", "ImageCarousel",
                    "--description", "Image Carousel Application"
                )
                installerType = "exe"
                installerOptions = listOf(
                    "--win-dir-chooser",
                    "--win-menu",
                    "--win-shortcut",
                    "--win-per-user-install"
                )
            }
        }
    }
}