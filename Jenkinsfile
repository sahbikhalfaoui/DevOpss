pipeline {
  agent any
  environment {
    SONAR_TOKEN = credentials('jenkins-sonar')
  }

  stages {

    stage('Build') {
      steps {
        echo 'Building Maven Project'
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Unit Tests') {
      steps {
        echo 'Running Unit Tests with Coverage'
        sh 'mvn -Dtest=EventServicesImplTest test jacoco:report'
      }
      post {
        always {
          junit '**/target/surefire-reports/TEST-*.xml'
          jacoco execPattern: '**/target/jacoco.exec'
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        echo 'Static Analysis with SonarQube'
        sh """
          mvn sonar:sonar \
            -Dsonar.login=${SONAR_TOKEN} \
            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
        """
      }
    }

    stage('Build Docker Image') {
      steps {
        echo 'Building Docker Image'
      //  sh 'docker build -t eventsproject .'
      }
    }

    stage('Start Docker Compose') {
      steps {
        echo 'Starting Docker Compose for Integration Tests'
      //  sh 'docker compose up -d'
      }
    }
  } // Closing brace for 'stages'
  
  post {
    always {
      echo 'Pipeline completed.'
      // Add any additional post actions if necessary
    }
  }
} // Closing brace for 'pipeline'
