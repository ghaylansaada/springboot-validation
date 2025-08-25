import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.2.10"
	`maven-publish`
	`java-library`
}

group = "io.ghaylan.springboot"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework:spring-aop:6.2.10")
	implementation("org.springframework:spring-webflux:6.2.10")
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.5.5")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.36")

    // Kotlin support
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("com.googlecode.libphonenumber:libphonenumber:9.0.12")
    implementation("org.aspectj:aspectjweaver:1.9.24")
	implementation("org.jsoup:jsoup:1.21.1")
}

kotlin {
	compilerOptions {
		javaParameters = true
		jvmTarget.set(JvmTarget.JVM_21)
		freeCompilerArgs.addAll("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
	}
}

java {
	withSourcesJar()
	withJavadocJar()
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            setUrl("https://maven.pkg.github.com/ghaylansaada/springboot-validation")
            credentials {
                username = project.findProperty("gpr.user") as String?
                password = project.findProperty("gpr.token") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "io.ghaylan.springboot"
            artifactId = "validation"
            version = "1.0.0"
        }
    }
}