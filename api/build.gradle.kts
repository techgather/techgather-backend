plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

dependencies {
	implementation(project(":domain"))
    implementation(project(":application"))

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly ("org.projectlombok:lombok")
    annotationProcessor ("org.projectlombok:lombok")
}

tasks.bootJar {
	enabled = true
}

tasks.jar {
	enabled = false
}

