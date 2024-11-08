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
}

tasks.test {
    useJUnitPlatform()
}

jlink {
    options.set(listOf(
        "--strip-debug",
        "--compress=2",
        "--no-header-files",
        "--no-man-pages"
    ))

    launcher {
        name = "imagecarousel"
    }

    jpackage {
        // Common options
        installerName = "ImageCarousel"
        appVersion = "1.0.0"

        // Windows-specific options
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerOptions = listOf("--win-dir-chooser", "--win-menu", "--win-shortcut")
        }

        // macOS-specific options
        if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
            installerOptions = listOf("--mac-package-name", "Image Carousel")
        }

        // Linux-specific options
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerOptions = listOf("--linux-shortcut")
        }
    }
}