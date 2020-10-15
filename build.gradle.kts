plugins {
    // No magic: calls a method running behind the scenes the same of id("org.jetbrains.kotlin-" + "jvm")
    kotlin("jvm") version "1.4.10" // version is necessary
    id ("org.danilopianini.git-sensitive-semantic-versioning") version "0.2.2"
}

version = "0.1.0"

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
    //implementation --> configuraton directly created by the kotlin plugin
    //"kotlin" is an extension method of DependencyHandler
    implementation(kotlin("stdlib-jdk8")) // Introduced by the Kotlin plugin
    // The call to "kotlin" passing `module`, returns a String "org.jetbrains.kotlin:kotlin-$module:<KotlinVersion>"

    implementation(gradleApi()) // Built-in method, returns a `Dependency` to the current Gradle version
    testImplementation(gradleTestKit()) // Test implementation: available for testing compile and runtime
    testImplementation("io.kotest:kotest-runner-junit5:4.2.5") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:4.2.5") // for kotest core assertions
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.2.5") // for kotest core jvm assertions
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