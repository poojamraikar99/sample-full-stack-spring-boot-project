plugins {
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.example"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
