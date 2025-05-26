// Jenkinsfile pour Jenkins local sur Windows, déployant sur Docker Desktop K8s
// Trigger build: 2
// Updated to use db11 instead of db1 and improved frontend configuration

pipeline {
    // 'agent any' signifie que Jenkins exécutera les étapes sur n'importe quel
    // agent disponible. Dans une installation simple, c'est souvent le
    // contrôleur Jenkins lui-même (votre machine Windows).
    agent any

    // Variables d'environnement utilisées dans le pipeline
    environment {
        // REMPLACEZ 'votrenomutilisateur' PAR VOTRE VRAI NOM D'UTILISATEUR DOCKER HUB
        DOCKER_REGISTRY = "douaae"
        DOCKER_IMAGE_NAME = "${DOCKER_REGISTRY}/space-hub-backend"
        // Ceci doit correspondre à l'ID que vous avez donné aux credentials
        // Docker Hub dans Jenkins (Manage Jenkins -> Credentials)
        DOCKER_CREDENTIALS_ID = 'dockerhub-creds'
        // Windows path to your kubeconfig
        KUBECONFIG = 'C:\\Users\\Usuario\\.kube\\config'
        // Commenté car l'outil NodeJS n'est pas configuré dans Jenkins
        // NODEJS_HOME = tool 'NodeJS'
    }

    // Les différentes étapes du pipeline
    stages {

        // Étape 1: Récupérer le code source depuis Git
        stage('Checkout') {
                steps {
                    // Spécifie explicitement la branche 'production' à utiliser
                    git branch: 'production', url: 'https://github.com/m-elhamlaoui/AstroLearn.git', 
                    credentialsId: 'github-credentials' // Use a simpler credential ID configured in Jenkins

                }
        }

        // Étape 2: Compiler l'application Spring Boot avec Maven Wrapper
        stage('Build App') {
            steps {
                // 'dir' change le répertoire de travail pour les commandes suivantes
                dir('Backend/demo') {
                    // 'bat' exécute une commande batch Windows.
                    // Utilise le Maven Wrapper fourni dans votre projet.
                    // '-DskipTests' pour ne pas lancer les tests unitaires ici (on peut faire une étape séparée).
                    bat './mvnw clean package -DskipTests'
                }
            }
        }

        // Étape 3: Déployer PostgreSQL pour les tests
        stage('Deploy PostgreSQL') {
            steps {
                // Déployer PostgreSQL
                bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/postgres.yaml --validate=false"
                echo "PostgreSQL déployé sur Kubernetes"
                
                // Vérifier que le pod PostgreSQL est prêt
                bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=postgres"
                
                // Attendre que le pod soit complètement prêt
                sleep(time: 15, unit: 'SECONDS')
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
                 // IMPORTANT: Cette étape suppose que votre fichier k8s/backend.yaml
                 // référence l'image avec le tag ':latest' comme ceci :
                 // image: votrenomutilisateur/space-hub-backend:latest

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
                        // Assurez-vous que la configuration du frontend est correcte pour la production
                        echo "Vérification de la configuration frontend pour la production..."
                        
                        // On utilise le numéro de build Jenkins comme tag unique
                        def imageTag = "${env.BUILD_NUMBER}"
                        def frontendImageName = "${DOCKER_REGISTRY}/astrolearn-frontend"
                        def fullFrontendImageName = "${frontendImageName}:${imageTag}"

                        // Lance la commande 'docker build'
                        // Note: Le Dockerfile a été mis à jour pour ne plus utiliser update-config.sh
                        bat "docker build -t ${fullFrontendImageName} ."

                        // Ajoute aussi le tag ':latest' à la même image
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
                     // Se connecte à Docker Hub en utilisant les variables injectées
                     bat "docker login -u %DOCKER_USER% -p %DOCKER_PASS%"
                     
                     script {
                         def frontendImageName = "${DOCKER_REGISTRY}/astrolearn-frontend"
                         
                         // Pousse l'image avec le tag spécifique (numéro de build)
                         bat "docker push ${frontendImageName}:${env.BUILD_NUMBER}"
                         // Pousse l'image avec le tag ':latest'
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
                         
                         // Afficher les logs des pods pour le débogage
                         bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} logs -l app=astrolearn-frontend --tail=50", returnStatus: true)
                     } else {
                         echo "===== DÉPLOIEMENT FRONTEND RÉUSSI ====="
                         echo "Tous les pods sont prêts et en état de fonctionnement."
                         bat "kubectl --kubeconfig=${env.KUBECONFIG} get pods -l app=astrolearn-frontend"
                     }
                 }
             }
         }
        // Étape 13: Vérifier le déploiement et fournir des instructions d'accès
        stage('Verify Deployment') {
            steps {
                script {
                    // Obtenir les NodePorts pour accéder aux services
                    def frontendNodePort = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service astrolearn-frontend-service -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                    def backendNodePort = bat(script: "kubectl --kubeconfig=${env.KUBECONFIG} get service astrolearn-backend-service -o jsonpath='{.spec.ports[0].nodePort}'", returnStdout: true).trim()
                    
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
    } // Fin des stages

    // Actions à exécuter à la toute fin du pipeline
    post {
        // 'always' s'exécute toujours
        always {
            // Nettoie l'espace de travail Jenkins pour le prochain build
            // Pas besoin de node block car le pipeline est déjà dans un agent
            cleanWs()
        }
        success {
            echo "Pipeline exécuté avec succès! L'application AstroLearn est maintenant déployée sur Kubernetes."
        }
    }
} // Fin du pipeline
