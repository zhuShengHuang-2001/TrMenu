val taboolibVersion: String by project

plugins {
    id("org.gradle.java")
    id("org.gradle.maven-publish")
    kotlin("jvm") version "1.6.21" apply false
    id("io.izzel.taboolib") version "1.40" apply false
}

description = "Modern & Advanced Menu-Plugin for Minecraft Servers"

repositories {
    mavenCentral()
    maven("https://repo.tabooproject.org/repository/releases")
    maven("https://jitpack.io")
}

tasks.jar {
    onlyIf { false }
}

tasks.build {
    doLast {
        val plugin = project(":plugin")
        file("${plugin.buildDir}/libs").listFiles()?.find { it.endsWith("plugin-$version.jar") }?.copyTo(file("$buildDir/libs/${project.name}-$version.jar"))
    }
    dependsOn(project(":plugin").tasks.build)
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven("https://repo.tabooproject.org/repository/releases")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://jitpack.io")
    }
    dependencies {
        "compileOnly"(kotlin("stdlib"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val archiveName = if (project == rootProject)
        rootProject.name.toLowerCase()
    else
        "${rootProject.name.toLowerCase()}-${project.name.toLowerCase()}"

    val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer

    task<Jar>("sourcesJar") {
        from(sourceSets.named("main").get().allSource)
        archiveClassifier.set("sources")
    }

    tasks.jar {
        exclude("taboolib")
    }

    publishing {
        repositories {
            maven {
                url = uri("https://repo.mcage.cn/repository/trplugins/")
                credentials {
                    username = project.findProperty("user").toString()
                    password = project.findProperty("password").toString()
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
        publications {
            create<MavenPublication>("library") {
                from(components["java"])
                artifactId = archiveName

                artifact(tasks["sourcesJar"])

                pom {
                    allprojects.forEach {
                        repositories.addAll(it.repositories)
                    }
                }
            }
        }
    }
}