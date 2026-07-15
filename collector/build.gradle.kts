plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
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
	implementation(libs.kotlin.reflect)
	implementation(project(":application"))

	implementation(libs.bundles.ktor)

	implementation(libs.bundles.jackson)

	implementation(libs.jsoup)
	implementation(libs.okhttp)

	implementation(libs.spring.kafka)

	testImplementation(libs.kotlin.test.junit5)
	testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
