pipeline {
    agent any
    stages {
            stage('Build') {
                tools {
                    jdk "jdk17"
                }
                steps {
                    sh 'mvn clean package'
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
