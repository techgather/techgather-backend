plugins {
	kotlin("jvm")
}

group = "com.project"
version = "0.0.1-SNAPSHOT"
description = "woowahan-core"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("io.ktor:ktor-client-core:3.3.0")
	implementation("io.ktor:ktor-client-cio:3.3.0")
	implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
	implementation("io.ktor:ktor-serialization-jackson:3.3.0")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	//html 파싱
	implementation("org.jsoup:jsoup:1.17.2")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	//kafka
	implementation("org.springframework.kafka:spring-kafka")

	//test
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
