import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish` // for jitpack
    kotlin("jvm") version "1.4.32"
    jacoco
}

group = "com.github.kokorins"

repositories {
    mavenCentral()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    withType<Test> {
        testLogging {
            outputs.upToDateWhen { false }
            showStandardStreams = true
        }
        useJUnitPlatform()
    }
}

val slf4j = "1.7.30"
val kotest = "4.4.3"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:$slf4j")
    implementation("org.slf4j:slf4j-simple:$slf4j")

    testImplementation("io.kotest:kotest-runner-junit5:$kotest")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

task("fat-jar", Jar::class) {
    archiveBaseName.set("md-generator")
    manifest {
        attributes["Implementation-Title"] = "Markdown Generator"
        attributes["Implementation-Version"] = archiveVersion
    }
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}
