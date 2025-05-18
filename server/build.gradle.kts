import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.johnrengelman.shadow)
    alias(libs.plugins.bmuschko.docker)
}

kotlin {
    jvm()
    linuxX64 {
        binaries {
            executable {
                entryPoint = "pw.binom.main"
            }
        }
    }
    mingwX64 {
        binaries {
            executable {
                entryPoint = "pw.binom.main"
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
//            implementation(project(":deviceShared"))
            implementation(libs.binom.io.strong.webServer)
            implementation(libs.binom.io.strong.properties.ini)
            implementation(libs.binom.io.strong.properties.yaml)
            implementation(libs.binom.io.strong.nats.client)
            implementation(libs.binom.io.signal)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
val dockerImage = System.getenv("DOCKER_IMAGE_NAME") ?: throw IllegalArgumentException("DOCKER_IMAGE_NAME is not set")
val dockerLogin = System.getenv("DOCKER_REGISTRY_USERNAME")
val dockerPassword = System.getenv("DOCKER_REGISTRY_PASSWORD")
val dockerHost = System.getenv("DOCKER_REGISTRY_HOST")

docker {
    registryCredentials {
        url.set(dockerHost)
        if (dockerLogin != null) {
            username.set(dockerLogin)
        }
        if (dockerPassword != null) {
            password.set(dockerPassword)
        }
    }
}

tasks {

    val jvmJar by getting(Jar::class)

    val shadowJar by register("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        from(jvmJar.archiveFile)
        group = "build"
        configurations = listOf(project.configurations["jvmRuntimeClasspath"])
        exclude(
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/*.txt",
            "META-INF/NOTICE",
            "LICENSE",
        )
        manifest {
            attributes("Main-Class" to "pw.binom.JvmMain")
        }
        archiveFileName.set("full-application.jar")
    }

    val buildImage by register("buildDockerImage", DockerBuildImage::class) {
        group = "docker"
        dependsOn(shadowJar)
        inputDir.set(projectDir)
        images.add(dockerImage)
    }
    register("pushDockerImage", DockerPushImage::class) {
        group = "docker"
        dependsOn(buildImage)
        images.addAll(buildImage.images)
    }
}