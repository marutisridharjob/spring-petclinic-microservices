def VALID_SERVICES = [
    'spring-petclinic-admin-server',
    'spring-petclinic-api-gateway',
    'spring-petclinic-config-server',
    'spring-petclinic-genai-service',
    'spring-petclinic-customers-service',
    'spring-petclinic-discovery-server',
    'spring-petclinic-vets-service',
    'spring-petclinic-visits-service',
]

pipeline {
    agent any

    tools {
        maven 'Maven-3.9.4'
        jdk 'OpenJDK-17'
    }

    environment {
        DOCKER_REGISTRY = 'anhkhoa217'
        GITHUB_CREDENTIALS_ID = 'github_pat'
        DOCKER_HUB_CREDENTIALS_ID = 'dockerhub_credentials'
        GITHUB_REPO_URL = 'https://github.com/kiin21/spring-petclinic-microservices.git'
        AFFECTED_SERVICES = ''
        CONTAINER_TAG = ''
    }

    stages {
        stage('Clone Code') {
            steps {
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '**']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [
                            [$class: 'CloneOption', noTags: false, depth: 0, shallow: false],
                            [$class: 'PruneStaleBranch']
                        ],
                        userRemoteConfigs: [[
                            url: env.GITHUB_REPO_URL,
                            credentialsId: env.GITHUB_CREDENTIALS_ID
                        ]]
                    ])
                    
                    // Explicitly set GIT_COMMIT if it's not already set
                    if (!env.GIT_COMMIT) {
                        env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    }
                    
                    echo "Current commit: ${env.GIT_COMMIT}"
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // Check for tag build first
                    if (env.TAG_NAME) {
                        echo "A new release found with tag ${env.TAG_NAME}"
                        env.AFFECTED_SERVICES = VALID_SERVICES.join(' ')
                        env.CONTAINER_TAG = env.TAG_NAME
                        return
                    }
                    
                    // Regular build with change detection
                    def changedFiles = sh(
                        script: """
                            # Get the last successful commit hash or use HEAD~1
                            LAST_COMMIT=\$(git rev-parse HEAD~1 2>/dev/null || git rev-parse origin/main)

                            # Get the changed files
                            git diff --name-only \$LAST_COMMIT HEAD
                        """,
                        returnStdout: true
                    ).trim()

                    if (changedFiles.isEmpty()) {
                        echo "No changes detected."
                        env.AFFECTED_SERVICES = ''
                        return
                    }

                    // Determine which services were affected by the changes
                    def affectedServices = []
                    changedFiles.split("\n").each { file ->
                        VALID_SERVICES.each { service ->
                            if (file.startsWith("${service}/") && !affectedServices.contains(service)) {
                                affectedServices.add(service)
                            }
                        }
                    }

                    env.AFFECTED_SERVICES = affectedServices.join(' ')
                    
                    // Set the container tag to the commit hash (short version)
                    env.CONTAINER_TAG = env.GIT_COMMIT.substring(0, 7)

                    if (!env.AFFECTED_SERVICES || env.AFFECTED_SERVICES.trim().isEmpty()) {
                        echo "No valid service changes detected. Skipping pipeline."
                        currentBuild.result = 'SUCCESS'
                    } else {
                        echo "Changed services: ${env.AFFECTED_SERVICES}"
                    }
                }
            }
        }

        stage('Login to DockerHub') {
            when {
                expression { return env.AFFECTED_SERVICES != '' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_HUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                }
            }
        }

        stage('Build and Push Docker Images') {
            when {
                expression { return env.AFFECTED_SERVICES != '' }
            }
            steps {
                script {
                    def services = env.AFFECTED_SERVICES.split(' ')
                    
                    // Double check that we have a valid container tag
                    if (!env.CONTAINER_TAG) {
                        env.CONTAINER_TAG = env.GIT_COMMIT.substring(0, 7)
                        echo "Using commit hash as container tag: ${env.CONTAINER_TAG}"
                    }
                    
                    for (service in services) {
                        echo "Building and pushing Docker image for ${service}"
                        sh """
                            cd ${service}
                            mvn clean install -P buildDocker -Dmaven.test.skip=true \
                                -Ddocker.image.prefix=${env.DOCKER_REGISTRY} \
                                -Ddocker.image.tag=${env.CONTAINER_TAG}
                            docker push ${env.DOCKER_REGISTRY}/${service}:${env.CONTAINER_TAG}
                            cd ..
                        """
                    }
                }
            }
        }

        stage('Clean Up') {
            when {
                expression { return env.AFFECTED_SERVICES != '' }
            }
            steps {
                sh "docker system prune -af"
                sh "docker logout"
                echo "Docker cleanup and logout completed"
            }
        }
    }

    post {
        always {
            cleanWs()
            echo "Workspace cleaned"
        }
        success {
            echo "Pipeline completed successfully"
            script {
                // Only update GitHub status if we have commit information
                if (env.GIT_COMMIT) {
                    try {
                        step([
                            $class: 'GitHubCommitStatusSetter',
                            commitShaSource: [$class: 'ManuallyEnteredShaSource', sha: env.GIT_COMMIT],
                            contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'ci/jenkins/build-status'],
                            statusResultSource: [$class: 'ConditionalStatusResultSource', results: [
                                [$class: 'AnyBuildResult', message: 'Pipeline completed successfully', state: 'SUCCESS']
                            ]]
                        ])
                    } catch (Exception e) {
                        echo "Failed to set GitHub commit status: ${e.getMessage()}"
                    }
                }
            }
        }
        failure {
            echo "Pipeline failed"
            script {
                // Only update GitHub status if we have commit information
                if (env.GIT_COMMIT) {
                    try {
                        step([
                            $class: 'GitHubCommitStatusSetter',
                            commitShaSource: [$class: 'ManuallyEnteredShaSource', sha: env.GIT_COMMIT],
                            contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'ci/jenkins/build-status'],
                            statusResultSource: [$class: 'ConditionalStatusResultSource', results: [
                                [$class: 'AnyBuildResult', message: 'Pipeline failed', state: 'FAILURE']
                            ]]
                        ])
                    } catch (Exception e) {
                        echo "Failed to set GitHub commit status: ${e.getMessage()}"
                    }
                }
            }
        }
    }
}