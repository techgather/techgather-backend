plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(project(":domain"))
    implementation(project(":application"))

	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.spring.boot.starter.oauth2.resource.server)

	implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.security)
    testImplementation(libs.spring.security.test)

	implementation(libs.spring.cloud.aws.starter.parameter.store)
	implementation(libs.aws.secretsmanager)

    runtimeOnly(libs.mysql.connector.j)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks.bootJar {
	enabled = true
}

tasks.jar {
	enabled = false
}

