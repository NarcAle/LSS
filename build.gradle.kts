plugins {
    // No magic: calls a method running behind the scenes the same of id("org.jetbrains.kotlin-" + "jvm")
    jacoco
    kotlin("jvm")
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-gradle-plugin` // interno a gradle perchè ha i backtick e non ha la versione
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "0.12.0"
    id("it.unibo.lss2020.greetings-plugin") version "0.1.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.1"
    id("org.jetbrains.dokka") version "1.4.10"
    id("org.danilopianini.publish-on-central") version "0.3.0"
}

group = "io.github.narcale"

gitSemVer {
    version = computeGitSemVer()
}

// Configuration block, into DLS gradle, of software sources
repositories {
    jcenter() /*{
        content {
            onlyForConfigurations("runtimeClasspath")
        }
    }*/
    // mavenCentral() // points to Maven Central instead or additionally
}

dependencies {
    // implementation --> configuraton directly created by the kotlin plugin
    // "kotlin" is an extension method of DependencyHandler
    implementation(kotlin("stdlib-jdk8")) // Introduced by the Kotlin plugin
    // The call to "kotlin" passing `module`, returns a String "org.jetbrains.kotlin:kotlin-$module:<KotlinVersion>"

    implementation(gradleApi()) // Built-in method, returns a `Dependency` to the current Gradle version
    testImplementation(gradleTestKit()) // Test implementation: available for testing compile and runtime
    val kotestVersion = "_"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion") // for kotest core assertions
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion") // for kotest core jvm assertions
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}

tasks.withType<Test> {
    useJUnitPlatform() // Use JUnit 5 engine
    testLogging.showStandardStreams = true
    testLogging {
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// This task creates a file with a classpath descriptor, to be used in tests
val createClasspathManifest by tasks.registering { // This delegate uses the variable name as task name
    val outputDir = file("$buildDir/$name") // We will write in this folder
    outputs.dir(outputDir) // we register the output directory as an output of the task
    inputs.files(sourceSets.main.get().runtimeClasspath) // Our input is a ready runtime classpath
    // Note: due to the line above, this task implicitly requires our plugin to be compiled!
    doLast { // This is the task the action will execute
        outputDir.mkdirs() // Create the directory infrastructure
        // Write a file with one classpath entry per line
        file("$outputDir/plugin-classpath.txt").writeText(sourceSets.main.get().runtimeClasspath.joinToString("\n"))
    }
}

dependencies {
// This way "createClasspathManifest" is always executed before the tests!
// Gradle auto-resolves dependencies if there are dependencies on inputs/outputs
    testRuntimeOnly(files(createClasspathManifest))
}

// to publish into gradle plugin portal
pluginBundle { // These settings are set for the whole plugin bundle
    website = "https://danysk.github.io/Course-Laboratory-of-Software-Systems/"
    vcsUrl = "https://github.com/DanySK/Course-Laboratory-of-Software-Systems"
    tags = listOf("example", "greetings", "lss", "unibo")
}

gradlePlugin {
    plugins {
        create("GreetingPlugin") { // One entry per plugin
            id = "${project.group}.${project.name}"
            displayName = "LSS Greeting plugin"
            description = "Example plugin for the LSS course"
            implementationClass = "it.unibo.lss.firstplugin.GreetingPlugin"
        }
    }
}

greetings {
    greetWith { "Ciaone da un bellissimo" }
}

tasks.register<it.unibo.lss.firstplugin.Greet>("japaneseGreet") {
    greetIntro.set("Konichiwa")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true // Useful for processing results automatically
        html.isEnabled = true // Useful for human inspection
    }
}

// Detekt configuration
detekt {
    failFast = true // fail build on any finding
    buildUponDefaultConfig = true // preconfigure defaults
    // config = files("$projectDir/config/detekt.yml") // Custom additional rules
}

publishOnCentral {
    projectDescription.set("description") // Defaults to "No description provided"
    projectLongName.set("full project name") // Defaults to the project name
    licenseName.set("your license") // Default "Apache License, Version 2.0"
    licenseUrl.set("link to your license") // Default http://www.apache.org/licenses/LICENSE-2.0
    projectUrl.set("website url") // Default "https://github.com/DanySK/${project.name}"
    scmConnection.set("git:git@github.com:youruser/yourrepo") // Default "git:git@github.com:DanySK/${project.name}"
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Alessia Cerami")
                        email.set("alessia.cerami@studio.unibo.it")
                        url.set("http://www.danilopianini.org/")
                    }
                }
            }
        }
    }
}