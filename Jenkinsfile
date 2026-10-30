pipeline {
    agent any

    environment {
        REGISTRY = 'registry.local:5000'
        BACKEND_IMAGE = "${REGISTRY}/rims-backend"
        FRONTEND_IMAGE = "${REGISTRY}/rims-frontend"
        IMAGE_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        KUBECONFIG_CREDENTIALS_ID = 'k8s-kubeconfig'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Backend Build & Test') {
            steps {
                dir('backend') {
                    echo 'Building and running unit tests for backend...'
                    sh './mvnw clean test package'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    echo 'Installing dependencies and compiling frontend...'
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker container images with tag ${IMAGE_TAG}..."
                sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ./backend"
                sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ./frontend"
            }
        }

        stage('Docker Push') {
            steps {
                echo "Pushing images to container registry ${REGISTRY}..."
                sh "docker push ${BACKEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${BACKEND_IMAGE}:latest"
                sh "docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${FRONTEND_IMAGE}:latest"
            }
        }

        stage('Deploy with kubectl') {
            steps {
                echo 'Deploying application manifests to Kubernetes...'
                sh 'kubectl apply -f ./k8s/namespace.yaml'
                sh 'kubectl apply -f ./k8s/'
                sh 'kubectl rollout status deployment/rims-mysql -n rims --timeout=120s'
                sh 'kubectl rollout status deployment/rims-backend -n rims --timeout=180s'
                sh 'kubectl rollout status deployment/rims-frontend -n rims --timeout=90s'
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully! RIMS deployed to Kubernetes namespace 'rims'."
        }
        failure {
            echo 'Pipeline failed. Check stage logs for details.'
        }
    }
}
