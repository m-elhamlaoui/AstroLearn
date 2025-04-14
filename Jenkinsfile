// Jenkinsfile pour Jenkins local sur Windows, déployant sur Docker Desktop K8s

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

    }

    // Les différentes étapes du pipeline
    stages {

        // Étape 1: Récupérer le code source depuis Git
        stage('Checkout') {
                steps {
                    // Spécifie explicitement la branche 'main' à utiliser
                    git branch: 'main', url: 'https://github.com/m-elhamlaoui/AstroLearn.git' 
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

        // Étape 3 (Optionnelle mais recommandée): Lancer les tests unitaires
        // stage('Run Tests') {
        //     steps {
        //         dir('Backend/demo') {
        //             bat './mvnw test'
        //             // Ici, on pourrait ajouter des étapes pour publier les résultats des tests
        //             // par exemple avec le plugin JUnit:
        //             // junit 'target/surefire-reports/*.xml'
        //         }
        //     }
        // }

        // Étape 4: Construire l'image Docker
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

        // Étape 5: Pousser l'image Docker vers Docker Hub
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

         stage('Verify K8s Access') {
              steps {
                 bat 'kubectl config current-context'
                 bat 'kubectl get nodes'
               }
        }
        // Étape 6: Déployer l'application sur Kubernetes (Docker Desktop)
        stage('Deploy to K8s') {
             steps {
                 // IMPORTANT: Cette étape suppose que votre fichier k8s/backend.yaml
                 // référence l'image avec le tag ':latest' comme ceci :
                 // image: votrenomutilisateur/space-hub-backend:latest

                 // Applique les fichiers de configuration Kubernetes qui sont
                 // dans le dossier 'k8s/' de votre projet.
                 // Assurez-vous que 'kubectl' est dans le PATH de Windows.
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} apply -f k8s/ --validate=false"

                 // Force le redémarrage des pods du déploiement pour
                 // qu'ils récupèrent la nouvelle image ':latest'.
                 // Assurez-vous que 'space-hub-backend-deployment' est bien le nom
                 // de votre Deployment dans k8s/backend.yaml.
                 bat "kubectl --kubeconfig=${env.KUBECONFIG} rollout restart deployment space-hub-backend-deployment"

                 // Optionnel: Attendre et vérifier que le déploiement s'est bien passé
                 // bat "kubectl rollout status deployment/space-hub-backend-deployment --timeout=2m"
             }
         }
    } // Fin des stages

    // Actions à exécuter à la toute fin du pipeline
    post {
        // 'always' s'exécute toujours
        always {
            // 'cleanWs' nettoie l'espace de travail Jenkins pour le prochain build
            cleanWs()
        }
    }
} // Fin du pipeline