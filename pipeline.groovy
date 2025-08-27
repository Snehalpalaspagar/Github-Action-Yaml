
pipeline{
     agent any
     
     tools{
         jdk 'jdk21'
         nodejs 'node16'
     }
     environment {
        SCANNER_HOME=tool 'sonarqube-scanner'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
     }
     
     stages {
         stage('Clean Workspace'){
             steps{
                 cleanWs()
             }
         }
         stage('Checkout from Git'){
             steps{
                 git branch: 'main', url: 'https://github.com/Snehalpalaspagar/a-swiggy-clone.git'
             }
         }
         stage("Sonarqube Analysis "){
             steps{
                 withSonarQubeEnv('SonarQube-Server') {
                     sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=Swiggy-CI \
                     -Dsonar.projectKey=Swiggy-CI '''
                 }
             }
         }
         stage("Quality Gate"){
            steps {
                 script {
                     waitForQualityGate abortPipeline: false, credentialsId: 'SonarQube-Token' 
                 }
             } 
         }
         stage('Install Dependencies') {
             steps {
                 sh "npm install"
             }
         }
          stage("Docker Build & Push"){
             steps{
                //sh "docker build . -t sneha-devops:$BUILD_NUMBER"
                  script{
                     withDockerRegistry(credentialsId: 'docker', toolName: 'docker'){   
                         sh "docker build . -t snehalpalaspagar310/sneha-devops:$BUILD_NUMBER"
                         sh "docker push snehalpalaspagar310/sneha-devops:$BUILD_NUMBER"
                    }
                  }
             }
         }
         stage('Run Docker Container') {
            steps {
                // Run a simple ubuntu container, print hostname, then exit
                sh "docker rm -f sneha-devops"
                sh "docker run --name sneha-devops -d -p 3000:3000 snehalpalaspagar310/sneha-devops:$BUILD_NUMBER"
            }
        }

     }
 }
