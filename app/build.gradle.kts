import java.util.Properties

plugins {
    id("com.android.application")
}

val nomeVersao = (project.findProperty("assistpro.versionName") as String?) ?: "1.0"
val partesVersao = nomeVersao.substringBefore("-").split(".")
val codigoVersao = (partesVersao.getOrNull(0)?.toIntOrNull() ?: 1) * 10000 +
    (partesVersao.getOrNull(1)?.toIntOrNull() ?: 0) * 100 +
    (partesVersao.getOrNull(2)?.toIntOrNull() ?: 0)

val repoAtualizacao =
    (project.findProperty("assistpro.updateRepo") as String?) ?: "seu-usuario/assistPro"

val propsKeystore = Properties()
val arquivoKeystore = rootProject.file("keystore.properties")
if (arquivoKeystore.exists()) arquivoKeystore.inputStream().use { propsKeystore.load(it) }

android {
    namespace = "br.com.assistpro"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.assistpro"
        minSdk = 24
        targetSdk = 35
        versionCode = codigoVersao
        versionName = nomeVersao
        buildConfigField("String", "UPDATE_REPO", "\"$repoAtualizacao\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (arquivoKeystore.exists()) {
            create("release") {
                storeFile = rootProject.file(propsKeystore.getProperty("storeFile"))
                storePassword = propsKeystore.getProperty("storePassword")
                keyAlias = propsKeystore.getProperty("keyAlias")
                keyPassword = propsKeystore.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (arquivoKeystore.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
