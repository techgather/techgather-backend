plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
}

dependencies {
	implementation(project(":domain"))
    implementation(project(":application"))

	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.validation)

	implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.security)
    testImplementation(libs.spring.security.test)

    runtimeOnly(libs.mysql.connector.j)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.bootJar {
	enabled = true
}

tasks.jar {
	enabled = false
}

