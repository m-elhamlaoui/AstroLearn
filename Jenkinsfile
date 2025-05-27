pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "douaae"
        DOCKER_IMAGE_NAME = "${DOCKER_REGISTRY}/space-hub-backend"
        DOCKER_CREDENTIALS_ID = 'dockerhub-creds'
        KUBECONFIG = 'C:\\Users\\Usuario\\.kube\\config'
    }

    stages {
        stage('Checkout') {
                steps {
                    git branch: 'production', url: 'https://github.com/m-elhamlaoui/AstroLearn.git', 
                    credentialsId: 'github-credentials'
                }
        }

        stage('Build App') {
            steps {
                dir('Backend/demo') {
                    bat './mvnw clean package -DskipTests'
                }
            }
        }

        stage('Deploy PostgreSQL') {
            steps {
                bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/postgres.yaml --validate=false"
                echo "PostgreSQL déployé sur Kubernetes"
                
                bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=postgres"
                
                sleep(time: 15, unit: 'SECONDS')
                
                bat(script: "taskkill /F /IM kubectl.exe", returnStatus: true)
                sleep(time: 2, unit: 'SECONDS')
                
                bat(script: "start /B cmd /c kubectl --kubeconfig=${env.KUBECONFIG} port-forward service/postgres-service 5432:5432")
                echo "Port-forward configuré pour PostgreSQL: localhost:5432 -> service/postgres-service:5432"
                sleep(time: 10, unit: 'SECONDS')
            }
        }
        
        stage('Run Unit Tests') {
            steps {
                dir('Backend/demo') {
                    echo "Exécution des tests unitaires"
                    bat './mvnw test -Dtest=com.example.demo.service.unit.*Test'
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Run Integration Tests') {
            steps {
                bat "kubectl --kubeconfig=${env.KUBECONFIG} get service postgres-service"
                
                dir('Backend/demo') {
                    echo "Exécution des tests d'intégration avec le profil de test"
                    bat './mvnw test -Dtest=com.example.demo.service.integration.*Test -Dspring.profiles.active=test'
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('Backend/demo') {
                    script {
                        def imageTag = "${env.BUILD_NUMBER}"
                        def fullImageName = "${DOCKER_IMAGE_NAME}:${imageTag}"

                        bat "docker build -t ${fullImageName} ."

                        bat "docker tag ${fullImageName} ${DOCKER_IMAGE_NAME}:latest"
                    }
                }
            }
        }

        stage('Push Docker Image') {
             steps {
                 withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                     bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                     bat "docker push ${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                     bat "docker push ${DOCKER_IMAGE_NAME}:latest"
                 }
             }
             post {
                 always {
                     bat "docker logout"
                 }
             }
         }

        stage('Verify K8s Access') {
              steps {
                 bat 'kubectl config current-context'
                 bat 'kubectl get nodes'
               }
        }
        stage('Deploy Backend to K8s') {
             steps {
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/backend.yaml --validate=false"
                 
                 bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/postgres.yaml --validate=false", returnStatus: true)
                 
                 echo "Configuration Kubernetes appliquée. Les erreurs sur les champs immuables du StatefulSet sont ignorées."
                 
                 sleep(time: 30, unit: 'SECONDS')
                 
                 echo "Vérification de la base de données db11..."
                 bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=postgres", returnStatus: true)

                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout restart deployment astrolearn-backend-deployment"

                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout status deployment/astrolearn-backend-deployment --timeout=2m"
             }
         }


        stage('Build Frontend Docker Image') {
            steps {
                dir('Frontend') {
                    script {
                        echo "Vérification de la configuration frontend pour la production..."
                        
                        def imageTag = "${env.BUILD_NUMBER}"
                        def frontendImageName = "${DOCKER_REGISTRY}/astrolearn-frontend"
                        def fullFrontendImageName = "${frontendImageName}:${imageTag}"

                        bat "docker build -t ${fullFrontendImageName} ."

                        bat "docker tag ${fullFrontendImageName} ${frontendImageName}:latest"
                        
                        echo "Image Docker frontend construite avec succès, configurée pour utiliser le service backend interne de Kubernetes."
                    }
                }
            }
        }
        
        
        stage('Push Frontend Docker Image') {
             steps {
                 withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                     bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                     
                     script {
                         def frontendImageName = "${DOCKER_REGISTRY}/astrolearn-frontend"
                         
                         bat "docker push ${frontendImageName}:${env.BUILD_NUMBER}"
                         bat "docker push ${frontendImageName}:latest"
                     }
                 }
             }
             post {
                 always {
                     bat "docker logout"
                 }
             }
         }
         
        stage('Deploy Frontend to K8s') {
             steps {
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/frontend.yaml --validate=false"

                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout restart deployment astrolearn-frontend-deployment"
                 
                 sleep(time: 10, unit: 'SECONDS')
                 
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} describe deployment astrolearn-frontend-deployment"
                 
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=astrolearn-frontend"

                 script {
                     def rolloutStatus = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} rollout status deployment/astrolearn-frontend-deployment --timeout=5m", returnStatus: true)
                     
                     if (rolloutStatus != 0) {
                         unstable(message: "Le déploiement frontend n'a pas été complété dans le délai imparti de 5 minutes")
                         
                         echo "===== ATTENTION: DÉPLOIEMENT INCOMPLET ====="
                         echo "Le déploiement frontend n'a pas été complété dans le délai imparti de 5 minutes."
                         echo "Le pipeline est marqué comme INSTABLE (jaune) mais pas échoué (rouge)."
                         echo "Vérifiez manuellement l'état des pods avec: kubectl get pods"
                         
                         bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} logs -l app=astrolearn-frontend --tail=50", returnStatus: true)
                     } else {
                         echo "===== DÉPLOIEMENT FRONTEND RÉUSSI ====="
                         echo "Tous les pods sont prêts et en état de fonctionnement."
                         bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=astrolearn-frontend"
                     }
                 }
             }
         }
        stage('Verify Deployment') {
            steps {
                script {
                    def frontendNodePortCmd = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service astrolearn-frontend-service -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                    def backendNodePortCmd = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service astrolearn-backend-service -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                    
                    def frontendNodePort = frontendNodePortCmd.tokenize('\r\n').last().replaceAll("'", "")
                    def backendNodePort = backendNodePortCmd.tokenize('\r\n').last().replaceAll("'", "")
                    
                    echo "\n===== DÉPLOIEMENT TERMINÉ AVEC SUCCÈS ====="
                    echo "Application AstroLearn déployée avec succès sur Kubernetes!"
                    echo "\nAccès à l'application:"
                    echo "- Frontend: http://localhost:${frontendNodePort}"
                    echo "- Backend API: http://localhost:${backendNodePort}"
                    echo "\nPour un accès direct au backend sur le port 8088, exécutez:"
                    echo "kubectl port-forward service/astrolearn-backend-service 8088:8088"
                    echo "\nBase de données: PostgreSQL avec nom de base 'db11'"
                    echo "===== DÉPLOIEMENT TERMINÉ AVEC SUCCÈS ====="
                }
            }
        }
    }

    post {
        always {
            bat(script: "taskkill /F /IM kubectl.exe", returnStatus: true)
            sleep(time: 5, unit: 'SECONDS')
            cleanWs()
        }
        success {
            echo "Pipeline exécuté avec succès! L'application AstroLearn est maintenant déployée sur Kubernetes."
        }
    }
}
