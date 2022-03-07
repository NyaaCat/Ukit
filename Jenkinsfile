pipeline {
    agent any
    stages {
            stage('Build') {
                tools {
                    jdk "jdk17"
                }
                steps {
                    sh 'mvn -DmavenLocalDistDir=${MAVEN_DIR} deploy'
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
