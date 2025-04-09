pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9.6'
        jdk 'JDK 17'
    }
    
    environment {
        // Define service paths for easier reference
        CONFIG_SERVER_PATH = "spring-petclinic-config-server"
        DISCOVERY_SERVER_PATH = "spring-petclinic-discovery-server"
        ADMIN_SERVER_PATH = "spring-petclinic-admin-server"
        API_GATEWAY_PATH = "spring-petclinic-api-gateway"
        CUSTOMERS_SERVICE_PATH = "spring-petclinic-customers-service"
        VISITS_SERVICE_PATH = "spring-petclinic-visits-service"
        VETS_SERVICE_PATH = "spring-petclinic-vets-service"
        GENAI_SERVICE_PATH = "spring-petclinic-genai-service"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh "git fetch --all"
            }
        }
        
        stage('Detect Changes') {
            steps {
                script {
                    // Initialize change flags - default to false
                    env.CONFIG_SERVER_CHANGED = "false"
                    env.DISCOVERY_SERVER_CHANGED = "false"
                    env.ADMIN_SERVER_CHANGED = "false"
                    env.API_GATEWAY_CHANGED = "false"
                    env.CUSTOMERS_SERVICE_CHANGED = "false"
                    env.VISITS_SERVICE_CHANGED = "false"
                    env.VETS_SERVICE_CHANGED = "false"
                    env.GENAI_SERVICE_CHANGED = "false"
                    
                    // For first build or when specifically requested, build everything
                    if (params.FORCE_BUILD_ALL == true || currentBuild.number == 1) {
                        echo "Building all services (first build or forced build)"
                        env.CONFIG_SERVER_CHANGED = "true"
                        env.DISCOVERY_SERVER_CHANGED = "true"
                        env.ADMIN_SERVER_CHANGED = "true"
                        env.API_GATEWAY_CHANGED = "true"
                        env.CUSTOMERS_SERVICE_CHANGED = "true"
                        env.VISITS_SERVICE_CHANGED = "true"
                        env.VETS_SERVICE_CHANGED = "true"
                        env.GENAI_SERVICE_CHANGED = "true"
                    } else {
                        try {
                            // Get the current commit hash
                            def currentCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            
                            // Get the previous successful build's commit
                            def previousCommit = ""
                            if (currentBuild.previousSuccessfulBuild) {
                                previousCommit = sh(script: "git rev-parse HEAD~1", returnStdout: true).trim()
                            } else {
                                echo "No previous successful build found, comparing with previous commit"
                                previousCommit = sh(script: "git rev-parse HEAD~1", returnStdout: true).trim()
                            }
                            
                            echo "Comparing changes between ${previousCommit} and ${currentCommit}"
                            
                            // Get the changed files between the two commits
                            def changeSet = sh(script: "git diff --name-only ${previousCommit} ${currentCommit}", returnStdout: true).trim()
                            
                            if (changeSet) {
                                changeSet = changeSet.split('\n')
                                echo "Changed files: ${changeSet.join(', ')}"
                                
                                // Check for changes in parent POM or shared files
                                def parentPomChanged = false
                                changeSet.each { change ->
                                    if (change == "pom.xml" || 
                                        change.startsWith("docker/") || 
                                        change.startsWith(".github/") || 
                                        change.startsWith("Jenkinsfile")) {
                                        parentPomChanged = true
                                    }
                                }
                                
                                if (parentPomChanged) {
                                    echo "Parent POM or shared files changed - building all services"
                                    env.CONFIG_SERVER_CHANGED = "true"
                                    env.DISCOVERY_SERVER_CHANGED = "true"
                                    env.ADMIN_SERVER_CHANGED = "true" 
                                    env.API_GATEWAY_CHANGED = "true"
                                    env.CUSTOMERS_SERVICE_CHANGED = "true"
                                    env.VISITS_SERVICE_CHANGED = "true"
                                    env.VETS_SERVICE_CHANGED = "true"
                                    env.GENAI_SERVICE_CHANGED = "true"
                                } else {
                                    // Check each path in the change set
                                    changeSet.each { change ->
                                        if (change.startsWith(CONFIG_SERVER_PATH)) {
                                            env.CONFIG_SERVER_CHANGED = "true"
                                            echo "Config Server changes detected"
                                        }
                                        else if (change.startsWith(DISCOVERY_SERVER_PATH)) {
                                            env.DISCOVERY_SERVER_CHANGED = "true" 
                                            echo "Discovery Server changes detected"
                                        }
                                        else if (change.startsWith(ADMIN_SERVER_PATH)) {
                                            env.ADMIN_SERVER_CHANGED = "true"
                                            echo "Admin Server changes detected"
                                        }
                                        else if (change.startsWith(API_GATEWAY_PATH)) {
                                            env.API_GATEWAY_CHANGED = "true"
                                            echo "API Gateway changes detected"
                                        }
                                        else if (change.startsWith(CUSTOMERS_SERVICE_PATH)) {
                                            env.CUSTOMERS_SERVICE_CHANGED = "true"
                                            echo "Customers Service changes detected"
                                        }
                                        else if (change.startsWith(VISITS_SERVICE_PATH)) {
                                            env.VISITS_SERVICE_CHANGED = "true"
                                            echo "Visits Service changes detected"
                                        }
                                        else if (change.startsWith(VETS_SERVICE_PATH)) {
                                            env.VETS_SERVICE_CHANGED = "true"
                                            echo "Vets Service changes detected"
                                        }
                                        else if (change.startsWith(GENAI_SERVICE_PATH)) {
                                            env.GENAI_SERVICE_CHANGED = "true"
                                            echo "GenAI Service changes detected"
                                        }
                                    }
                                    
                                    // Handle infrastructure service changes that would affect other services
                                    if (env.CONFIG_SERVER_CHANGED == "true") {
                                        echo "Config Server changes may affect all services"
                                        // Optionally build all services if config server changes
                                    }
                                }
                            } else {
                                echo "No changes detected or git diff returned empty result"
                                currentBuild.result = 'ABORTED'
                                error("No changes detected to build")
                            }
                        } catch (Exception e) {
                            echo "Error detecting changes: ${e.message}"
                            echo "Building all services as fallback"
                            env.CONFIG_SERVER_CHANGED = "true"
                            env.DISCOVERY_SERVER_CHANGED = "true"
                            env.ADMIN_SERVER_CHANGED = "true"
                            env.API_GATEWAY_CHANGED = "true"
                            env.CUSTOMERS_SERVICE_CHANGED = "true"
                            env.VISITS_SERVICE_CHANGED = "true"
                            env.VETS_SERVICE_CHANGED = "true"
                            env.GENAI_SERVICE_CHANGED = "true"
                        }
                    }
                    
                    // Output summary of what will be built
                    echo "Services to build:"
                    echo "Config Server: ${env.CONFIG_SERVER_CHANGED}"
                    echo "Discovery Server: ${env.DISCOVERY_SERVER_CHANGED}"
                    echo "Admin Server: ${env.ADMIN_SERVER_CHANGED}" 
                    echo "API Gateway: ${env.API_GATEWAY_CHANGED}"
                    echo "Customers Service: ${env.CUSTOMERS_SERVICE_CHANGED}"
                    echo "Visits Service: ${env.VISITS_SERVICE_CHANGED}"
                    echo "Vets Service: ${env.VETS_SERVICE_CHANGED}"
                    echo "GenAI Service: ${env.GENAI_SERVICE_CHANGED}"
                }
            }
        }
        
        stage('Build') {
            parallel {
                stage('Config Server') {
                    when {
                        expression { return env.CONFIG_SERVER_CHANGED == "true" }
                    }
                    steps {
                        dir(CONFIG_SERVER_PATH) {
                            sh 'mvn clean compile'
                            echo "Config Server built"
                        }
                    }
                }
                
                stage('Discovery Server') {
                    when {
                        expression { return env.DISCOVERY_SERVER_CHANGED == "true" }
                    }
                    steps {
                        dir(DISCOVERY_SERVER_PATH) {
                            sh 'mvn clean compile'
                            echo "Discovery Server built"
                        }
                    }
                }
                
                stage('Admin Server') {
                    when {
                        expression { return env.ADMIN_SERVER_CHANGED == "true" }
                    }
                    steps {
                        dir(ADMIN_SERVER_PATH) {
                            sh 'mvn clean compile'
                            echo "Admin Server built"
                        }
                    }
                }
                
                stage('API Gateway') {
                    when {
                        expression { return env.API_GATEWAY_CHANGED == "true" }
                    }
                    steps {
                        dir(API_GATEWAY_PATH) {
                            sh 'mvn clean compile'
                            echo "API Gateway built"
                        }
                    }
                }
                
                stage('Customers Service') {
                    when {
                        expression { return env.CUSTOMERS_SERVICE_CHANGED == "true" }
                    }
                    steps {
                        dir(CUSTOMERS_SERVICE_PATH) {
                            sh 'mvn clean compile'
                            echo "Customers Service built"
                        }
                    }
                }
                
                stage('Visits Service') {
                    when {
                        expression { return env.VISITS_SERVICE_CHANGED == "true" }
                    }
                    steps {
                        dir(VISITS_SERVICE_PATH) {
                            sh 'mvn clean compile'
                            echo "Visits Service built"
                        }
                    }
                }
                
                stage('Vets Service') {
                    when {
                        expression { return env.VETS_SERVICE_CHANGED == "true" }
                    }
                    steps {
                        dir(VETS_SERVICE_PATH) {
                            sh 'mvn clean compile'
                            echo "Vets Service built"
                        }
                    }
                }
                
                stage('GenAI Service') {
                    when {
                        expression { return env.GENAI_SERVICE_CHANGED == "true" }
                    }
                    steps {
                        dir(GENAI_SERVICE_PATH) {
                            sh 'mvn clean compile'
                            echo "GenAI Service built"
                        }
                    }
                }
            }
        }
        
        stage('Test') {
            parallel {
                stage('Customers Service') { 
                    when { 
                        expression { return env.CUSTOMERS_SERVICE_CHANGED == "true" } 
                    } 
                    steps { 
                        dir(CUSTOMERS_SERVICE_PATH) { 
                            sh 'mvn verify' 
                            echo "Customers Service tests and coverage completed"

                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java',
                                exclusionPattern: '**/test/**',
                                changeBuildStatus: true,
                            )
                        }
                    } 
                    post { 
                        always { 
                            junit allowEmptyResults: true, testResults: "${CUSTOMERS_SERVICE_PATH}/target/surefire-reports/TEST-*.xml" 
                        } 
                    } 
                } 

                stage('Visits Service') { 
                    when { 
                        expression { return env.VISITS_SERVICE_CHANGED == "true" } 
                    } 
                    steps { 
                        dir(VISITS_SERVICE_PATH) { 
                            sh 'mvn verify' 
                            echo "Visits Service tests and coverage completed"

                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java',
                                exclusionPattern: '**/test/**',
                                changeBuildStatus: true,
                            ) 
                        } 
                    } 
                    post { 
                        always { 
                            junit allowEmptyResults: true, testResults: "${VISITS_SERVICE_PATH}/target/surefire-reports/TEST-*.xml" 
                        } 
                    } 
                } 

                stage('Vets Service') { 
                    when { 
                        expression { return env.VETS_SERVICE_CHANGED == "true" } 
                    } 
                    steps { 
                        dir(VETS_SERVICE_PATH) { 
                            sh 'mvn verify' 
                            echo "Vets Service tests and coverage completed" 

                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java',
                                exclusionPattern: '**/test/**',
                                changeBuildStatus: true,
                            )
                        } 
                    } 
                    post { 
                        always { 
                            junit allowEmptyResults: true, testResults: "${VETS_SERVICE_PATH}/target/surefire-reports/TEST-*.xml" 
                        } 
                    } 
                }
                
            }
        }
    }
    
    post {
        success {
            echo 'Build and Test successful!'
        }
        failure {
            echo 'Build or Test failed!'
        }
        always {
            echo 'Cleaning workspace...'
            cleanWs()
        }
    }
}