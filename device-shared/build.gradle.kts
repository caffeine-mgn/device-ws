import pw.binom.publish.allTargets

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.binom.publish)
    id("maven-publish")
}

kotlin {
    allTargets{
        -"wasmJs"
    }
    wasmJs()
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.core)
            api(libs.kotlinx.serialization.protobuf)
            api(libs.kotlinx.serialization.json)
            api(libs.binom.io.core)
            api(libs.binom.tracing.zipkin)
        }
    }
}

extensions.getByType(pw.binom.publish.plugins.PublicationPomInfoExtension::class).apply {
    useApache2License()
    gitScm("https://github.com/caffeine-mgn/telegramClient")
    author(
        id = "subochev",
        name = "Anton Subochev",
        email = "caffeine.mgn@gmail.com"
    )
}