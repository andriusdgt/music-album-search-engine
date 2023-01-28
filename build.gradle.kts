import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	kotlin("plugin.jpa") version "1.6.10"
	kotlin("plugin.serialization") version "1.6.10"
	application
}

group = "com.andriusdgt"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

application {
	mainClass.set("com.andriusdgt.musicalbumsengine.MusicAlbumEngineApplicationKt")
}

repositories {
	mavenCentral()
}

dependencies {
	val ktorVersion = "1.6.7"
	val kotlinCoroutinesVersion = "1.6.0"
	val junitVersion = "5.8.2"
	val mockitoVersion = "4.3.1"

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	implementation("io.ktor:ktor-client-core:$ktorVersion")
	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-jackson:$ktorVersion")
	implementation("io.ktor:ktor-client-serialization:$ktorVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.hsqldb:hsqldb:2.6.1")
	implementation("io.lettuce:lettuce-core:6.1.6.RELEASE")
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("it.ozimov:embedded-redis:0.7.3") {
		exclude("org.slf4j", "slf4j-simple")
	}

	testImplementation(platform("org.junit:junit-bom:$junitVersion"))
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
	testImplementation("org.mockito:mockito-core:$mockitoVersion")
	testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
	testImplementation("org.mockito:mockito-inline:$mockitoVersion")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
	testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
	testImplementation("org.assertj:assertj-core:3.22.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
