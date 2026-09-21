pipeline {
    agent any
    stages {
            stage('Build') {
            // Minecraft/Pewpew 26.2 runs on Java 25, so the whole toolchain moved to JDK 25.
            // NOTE: the Jenkins controller must have a JDK tool named "jdk25" (and, where
            // used, a Maven tool named "apache-maven-3.9.16") provisioned under
            // Manage Jenkins -> Tools. That is a REMOTE change and is deliberately NOT made
            // here; without it this pipeline will fail to resolve the tool name.
                tools {
                    jdk "jdk25"
                    maven "apache-maven-3.9.16"
                }
                steps {
                    sh 'mvn deploy -Pdeploy-local'
                }
            }
        }

    post {
           always {
               archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
               cleanWs()
           }
    }
}
