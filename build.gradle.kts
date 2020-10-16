plugins {
    // No magic: calls a method running behind the scenes the same of id("org.jetbrains.kotlin-" + "jvm")
    jacoco
    kotlin("jvm") version "1.3.72" // version is necessary
    id("org.danilopianini.git-sensitive-semantic-versioning") version "0.2.2"
    `java-gradle-plugin` // interno a gradle perchè ha i backtick e non ha la versione
    id("com.gradle.plugin-publish") version "0.12.0"
    id("it.unibo.lss2020.greetings-plugin") version "0.1.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.1"
}

group = "it.unibo.lss2020"

gitSemVer {
    version = computeGitSemVer()
}

// Configuration block, into DLS gradle, of software sources
repositories {
    jcenter() {
        content { onlyForConfigurations("detekt") }
    }/*{
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
    val kotestVersion = "4.1.3"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion") // for kotest core assertions
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion") // for kotest core jvm assertions
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.1")
}

//Detekt configuration
detekt {
    failFast = true // fail build on any finding
    buildUponDefaultConfig = true // preconfigure defaults
    config = files("$projectDir/config/detekt.yml") // Custom additional rules
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

tasks.jacocoTestReport {
    reports {
        // xml.isEnabled = true // Useful for processing results automatically
        html.isEnabled = true // Useful for human inspection
    }
}
