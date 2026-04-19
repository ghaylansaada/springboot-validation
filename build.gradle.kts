import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.3.20"
	`maven-publish`
	`java-library`
}

group = "io.ghaylan.springboot"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Import Spring Boot's dependency versions (BOM)
	implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))

    // Spring modules
    implementation("org.springframework:spring-aop")
    implementation("org.springframework:spring-webflux")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Swagger
	implementation("io.swagger.core.v3:swagger-annotations:2.2.48")

    // Kotlin libraries
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Utilities
	implementation("org.jsoup:jsoup:1.22.1")
	implementation("org.aspectj:aspectjweaver:1.9.25.1")
	implementation("com.googlecode.libphonenumber:libphonenumber:9.0.28")
}

kotlin {
	compilerOptions {
		javaParameters = true
		jvmTarget.set(JvmTarget.JVM_25)
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        freeCompilerArgs.add("-Xjsr305=strict")                             // strict nullability interop
        freeCompilerArgs.add("-Xemit-jvm-type-annotations")                 // better interop for frameworks
        freeCompilerArgs.add("-Xstring-concat=indy-with-constants")         // allow experimental APIs
        freeCompilerArgs.add("-Xannotation-default-target=param-property")  // best string concat on JDK 25
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