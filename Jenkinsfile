pipeline {
  agent any


  stages {

    stage('Build') {
      steps {
        echo 'Building Maven Project'
        sh 'mvn clean package -DskipTests'
      }
    }
    }
  }
