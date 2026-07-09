plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(libs.kotlin.reflect)

	implementation(project(":domain"))
    implementation(project(":application"))

	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.validation)

	implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.security)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.kotlin.test.junit5)

    runtimeOnly(libs.mysql.connector.j)
}

tasks.bootJar {
	enabled = true
}

tasks.jar {
	enabled = false
}
