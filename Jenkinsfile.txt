pipeline {
    agent any
    environment {
        SONAR_HOST_URL = 'http://192.168.50.4:9000'
        SONAR_PROJECT_KEY = 'gestion-station-ski'
        SONAR_LOGIN = credentials('sonar-token') // Assurez-vous que l'ID correspond bien à celui dans Jenkins Credentials
    }

    stages {
        stage('Checkout GIT') {
            steps {
                echo 'Checking out the repository...'
                git branch: 'arwaAli', url: 'https://github.com/arwaalii02/ski.git'
            }
        }

        stage('Maven Clean') {
            steps {
                echo 'Nettoyage du Projet : '
                sh 'mvn clean'
            }
        }

        stage('Maven Compile') {
            steps {
                echo 'Construction du Projet : '
                sh 'mvn compile'
            }
        }

        stage('Maven Test (Skip)') {
            steps {
                echo 'Test du Projet (skipped) : '
                sh 'mvn test -DskipTests'
            }
        }

        stage('JUNIT mockitto') {
            steps {
                echo 'test unitaire:'
                sh 'mvn test'
            }
        }

       
        stage('SonarQueb analysis') {
            steps {
                echo 'Analyse de la Qualité du Code : ';
                sh 'mvn sonar:sonar -Dsonar.login=admin -Dsonar.password=Allaharwa1234567@';
            }
        }

        stage('Nexus Deploy') {
            steps {
                echo 'Création du livrable : '
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Image') {
            steps {
                echo 'Création Image :'
                sh 'docker build -t arwaali1/gestion-station-ski-image:1.0.0 .'
            }
        }

        stage('Dockerhub') {
            steps {
                echo 'Push Image to Docker Hub...'
                sh 'docker login -u arwaali1 -p Allaharwa1234567'
                sh 'docker push arwaali1/gestion-station-ski-image:1.0.0'
            }
        }

        stage('Docker-Compose') {
            steps {
                echo 'Start Backend + DB : '
                sh 'docker compose up -d'
            }
        }
    }
}
