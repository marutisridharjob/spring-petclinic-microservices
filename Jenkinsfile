def services = [
    'spring-petclinic-admin-server',
    'spring-petclinic-api-gateway',
    'spring-petclinic-config-server',
    'spring-petclinic-customers-service',
    'spring-petclinic-discovery-server',
    'spring-petclinic-genai-service',
    'spring-petclinic-vets-service',
    'spring-petclinic-visits-service'
]


// Detect which services have changes
def detectChanges() {
    def changedServices = []
    
    // For pull requests
    if (env.CHANGE_ID) {
        echo "Processing Pull Request #${env.CHANGE_ID}"
        for (service in services) {
            def changes = sh(script: "git diff --name-only origin/${env.CHANGE_TARGET} HEAD | grep ^${service}/", returnStatus: true)
            if (changes == 0) {
                echo "Detected changes in ${service}"
                changedServices.add(service)
            }
        }
    } 
    // For branches
    else {
        echo "Processing branch ${env.BRANCH_NAME}"
        for (service in services) {
            def changes = sh(script: "git diff --name-only HEAD~1 HEAD | grep ^${service}/", returnStatus: true)
            if (changes == 0) {
                echo "Detected changes in ${service}"
                changedServices.add(service)
            }
        }
    }
    
    if (changedServices.size() == 0) {
        echo "No service-specific changes detected, will build common components"
        return ['spring-petclinic-config-server', 'spring-petclinic-discovery-server']
    }
    
    return changedServices
}

pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK 17'
    }
    
    stages {
        stage('Determine Changes') {
            steps {
                script {
                    env.CHANGED_SERVICES = detectChanges().join(',')
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Testing ${service}..."
                            sh "mvn test"
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        def changedServicesList = env.CHANGED_SERVICES.split(',')
                        for (service in changedServicesList) {
                            dir(service) {
                                junit 'target/surefire-reports/*.xml'
                                jacoco(
                                    execPattern: 'target/jacoco.exec',
                                    classPattern: 'target/classes',
                                    sourcePattern: 'src/main/java',
                                    exclusionPattern: 'src/test*'
                                )
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Building ${service}..."
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}