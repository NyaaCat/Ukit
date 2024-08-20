pipeline {
    agent any
    stages {
            stage('Build') {
                tools {
                    jdk "jdk21"
                    maven "apache-maven-3.9.9"
                }
                steps {
                    sh 'mvn package'
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
