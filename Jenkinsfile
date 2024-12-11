pipeline {
  agent any
  environment {
    SONAR_TOKEN = credentials('jenkins_sonar')
  }

  stages {
    stage('Build') {
      steps {
        echo 'Building Maven Project'
        sh 'mvn clean package -DskipTests'
      }
    }
    stage('SonarQube Analysis') {
          steps {
            echo 'Static Analysis with SonarQube'
            sh """
              mvn sonar:sonar \
                -Dsonar.login=${SONAR_TOKEN}
            """
          }
        }
  }
}
