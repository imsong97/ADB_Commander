plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val desktopMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.exposed.jdbc)
                implementation(libs.exposed.kotlin.datetime)
                implementation(libs.sqlite.jdbc)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }
        jvmMain {
            dependsOn(desktopMain)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
