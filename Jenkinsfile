pipeline {
    agent any
    stages {
            stage('Build') {
                tools {
                    jdk "jdk17"
                }
                steps {
                    sh 'mvn -DjenkinsBuildNumber=${BUILD_NUMBER} clean package'
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
