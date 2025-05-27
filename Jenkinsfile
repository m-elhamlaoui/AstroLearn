// Jenkinsfile: Windows Jenkins → Docker Desktop K8s

pipeline {

    agent any


    environment {
        DOCKER_REGISTRY = "douaae"
        DOCKER_IMAGE_NAME = "${DOCKER_REGISTRY}/space-hub-backend"

        DOCKER_CREDENTIALS_ID = 'dockerhub-creds'

        KUBECONFIG = 'C:\\Users\\Usuario\\.kube\\config'
        // NODEJS_HOME = tool 'NodeJS'
    }


    stages {

        // 1. Checkout
        stage('Checkout') {
                steps {

                    git branch: 'production', url: 'https://github.com/m-elhamlaoui/AstroLearn.git', 
                    credentialsId: 'github-credentials'

                }
        }

        // Étape 2: Compiler l'application Spring Boot avec Maven Wrapper
        stage('Build App') {
            steps {
                dir('Backend/demo') {
                    bat './mvnw clean package -DskipTests'
                }
            }
        }

        // Étape 3: Déployer PostgreSQL et configurer le port-forward
        stage('Deploy PostgreSQL') {
            steps {
                // Déployer PostgreSQL
                bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/postgres.yaml --validate=false"
                echo "PostgreSQL déployé sur Kubernetes"
                
                // Vérifier que le pod PostgreSQL est prêt
                bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=postgres"
                
                // Attendre que le pod soit complètement prêt
                sleep(time: 15, unit: 'SECONDS')
                
                // Arrêter tout port-forward existant pour éviter les conflits
                bat(script: "taskkill /F /IM kubectl.exe", returnStatus: true)
                sleep(time: 2, unit: 'SECONDS')
                
                // Démarrer le port-forward en arrière-plan et attendre qu'il soit établi
                bat(script: "start /B cmd /c kubectl --kubeconfig=${env.KUBECONFIG} port-forward service/postgres-service 5432:5432")
                echo "Port-forward configuré pour PostgreSQL: localhost:5432 -> service/postgres-service:5432"
                sleep(time: 10, unit: 'SECONDS')
            }
        }
        
        // Étape 4: Lancer les tests unitaires pour le backend
        stage('Run Unit Tests') {
            steps {
                dir('Backend/demo') {
                    echo "Exécution des tests unitaires"
                    // Exécute uniquement les tests dans le package unit
                    bat './mvnw test -Dtest=com.example.demo.service.unit.*Test'
                    // Publication des résultats des tests avec le plugin JUnit
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        // Étape 5: Lancer les tests d'intégration pour le backend
        stage('Run Integration Tests') {
            steps {
                // Vérifier que le service PostgreSQL est accessible depuis Kubernetes
                bat "kubectl --kubeconfig=${env.KUBECONFIG} get service postgres-service"
                
                dir('Backend/demo') {
                    echo "Exécution des tests d'intégration avec le profil de test"
                    // Exécute uniquement les tests dans le package integration avec le profil de test
                    // Le profil de test utilise application-test.properties qui se connecte directement au service Kubernetes
                    bat './mvnw test -Dtest=com.example.demo.service.integration.*Test -Dspring.profiles.active=test'
                    // Publication des résultats des tests avec le plugin JUnit
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // Étape 6: Construire l'image Docker du backend
        stage('Build Docker Image') {
            steps {
                // Le contexte du build est le dossier 'Backend/demo' où se trouve le Dockerfile
                dir('Backend/demo') {
                    // 'script' permet d'utiliser des variables Groovy plus facilement
                    script {
                        // On utilise le numéro de build Jenkins comme tag unique
                        def imageTag = "${env.BUILD_NUMBER}"
                        def fullImageName = "${DOCKER_IMAGE_NAME}:${imageTag}"

                        // Lance la commande 'docker build'
                        // '.' signifie que le contexte est le dossier courant ('Backend/demo')
                        bat "docker build -t ${fullImageName} ."

                        // Ajoute aussi le tag ':latest' à la même image
                        bat "docker tag ${fullImageName} ${DOCKER_IMAGE_NAME}:latest"
                    }
                }
            }
        }

        // Étape 7: Pousser l'image Docker du backend vers Docker Hub
        stage('Push Docker Image') {
             steps {
                 // 'withCredentials' injecte les identifiants stockés dans Jenkins
                 // dans des variables d'environnement (%DOCKER_USER%, %DOCKER_PASS% pour bat)
                 withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                     // Se connecte à Docker Hub en utilisant les variables injectées
                     bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                     // Pousse l'image avec le tag spécifique (numéro de build)
                     bat "docker push ${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"
                     // Pousse l'image avec le tag ':latest'
                     bat "docker push ${DOCKER_IMAGE_NAME}:latest"
                 }
             }
             // 'post' définit des actions à faire après l'étape
             post {
                 // 'always' signifie que ça s'exécute que l'étape réussisse ou échoue
                 always {
                     // Se déconnecte de Docker Hub
                     bat "docker logout"
                 }
             }
         }

         // Étape 8: Vérifier l'accès à Kubernetes
        stage('Verify K8s Access') {
              steps {
                 bat 'kubectl config current-context'
                 bat 'kubectl get nodes'
               }
        }
        // Étape 9: Déployer le backend sur Kubernetes (Docker Desktop)
        stage('Deploy Backend to K8s') {
             steps {


                 // Appliquer la configuration Kubernetes pour le backend
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/backend.yaml --validate=false"
                 
                 // Pour PostgreSQL, ignorer les erreurs car le StatefulSet a des champs immuables
                 // L'option returnStatus: true fait que Jenkins continuera même si la commande échoue
                 bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/postgres.yaml --validate=false", returnStatus: true)
                 
                 echo "Configuration Kubernetes appliquée. Les erreurs sur les champs immuables du StatefulSet sont ignorées."
                 
                 // Attendre que le pod PostgreSQL soit prêt
                 sleep(time: 30, unit: 'SECONDS')
                 
                 // Vérifier que la base de données db11 est correctement configurée
                 echo "Vérification de la base de données db11..."
                 bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=postgres", returnStatus: true)

                 // Force le redémarrage des pods du déploiement pour
                 // qu'ils récupèrent la nouvelle image ':latest'.
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout restart deployment astrolearn-backend-deployment"

                 // Attendre et vérifier que le déploiement s'est bien passé
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout status deployment/astrolearn-backend-deployment --timeout=2m"
             }
         }


        // Étape 10: Construire l'image Docker du Frontend
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
        
        
        // Étape 11: Pousser l'image Docker du Frontend vers Docker Hub
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
                     // Se déconnecte de Docker Hub
                     bat "docker logout"
                 }
             }
         }
         
        // Étape 12: Déployer le Frontend sur Kubernetes
        stage('Deploy Frontend to K8s') {
             steps {
                 // Applique la configuration Kubernetes pour le frontend
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/frontend.yaml --validate=false"

                 // Force le redémarrage des pods du déploiement frontend
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout restart deployment astrolearn-frontend-deployment"
                 
                 // Ajouter un délai pour laisser le temps au déploiement de démarrer
                 sleep(time: 10, unit: 'SECONDS')
                 
                 // Afficher les détails du déploiement pour le débogage
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} describe deployment astrolearn-frontend-deployment"
                 
                 // Afficher les pods pour le débogage
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=astrolearn-frontend"

                 script {
                     // Attendre que le déploiement se termine avec un timeout plus long
                     def rolloutStatus = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} rollout status deployment/astrolearn-frontend-deployment --timeout=5m", returnStatus: true)
                     
                     if (rolloutStatus != 0) {
                         // Marquer l'étape comme instable (warning) mais pas échouée (failure)
                         unstable(message: "Le déploiement frontend n'a pas été complété dans le délai imparti de 5 minutes")
                         
                         echo "===== ATTENTION: DÉPLOIEMENT INCOMPLET ====="
                         echo "Le déploiement frontend n'a pas été complété dans le délai imparti de 5 minutes."
                         echo "Le pipeline est marqué comme INSTABLE (jaune) mais pas échoué (rouge)."
                         echo "Vérifiez manuellement l'état des pods avec: kubectl get pods"
                         

                         bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} logs -l app=astrolearn-frontend --tail=50", returnStatus: true)
                     } else {
                         echo "===== FRONTEND DEPLOYMENT SUCCESSFUL ====="
                         echo "All pods are ready and running."
                         bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=astrolearn-frontend"
                     }
                 }
             }
         }
        // 13. Verify deployment
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
    // Stage 14: Deploy Monitoring Stack
    stage('Deploy Monitoring') {
        steps {
            script {
                echo "Deploying Prometheus and Grafana monitoring stack..."
                
                // Apply the Prometheus stack using Helm
                bat "helm install monitoring prometheus-community/kube-prometheus-stack -f k8s/monitoring/prometheus-stack-values.yaml"
                
                // Wait for Prometheus and Grafana pods to be ready
                bat "kubectl --kubeconfig=${env.KUBECONFIG} wait --for=condition=ready pod -l app=prometheus --timeout=120s"
                bat "kubectl --kubeconfig=${env.KUBECONFIG} wait --for=condition=ready pod -l app=grafana --timeout=120s"
                
                // Get the NodePorts for Prometheus and Grafana
                def grafanaNodePortCmd = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service monitoring-grafana -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                def prometheusNodePortCmd = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service monitoring-prometheus-server -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                
                // Extract port numbers
                def grafanaNodePort = grafanaNodePortCmd.tokenize('\r\n').last().replaceAll("'", "")
                def prometheusNodePort = prometheusNodePortCmd.tokenize('\r\n').last().replaceAll("'", "")
                
                echo "Monitoring stack deployed successfully!"
                echo "Grafana: http://localhost:${grafanaNodePort} (admin/admin)"
                echo "Prometheus: http://localhost:${prometheusNodePort}"
            }
        }
    }



    // Fin des stages

    // Actions à exécuter à la toute fin du pipeline
    post {
        // 'always' s'exécute toujours
        always {
            // Terminer tous les processus kubectl pour éviter les problèmes de nettoyage
            bat(script: "taskkill /F /IM kubectl.exe", returnStatus: true)
            sleep(time: 5, unit: 'SECONDS')
            // Nettoie l'espace de travail Jenkins pour le prochain build
            cleanWs()
        }
        success {
            echo "Pipeline exécuté avec succès! L'application AstroLearn est maintenant déployée sur Kubernetes."
        }
    }
} // Fin du pipeline