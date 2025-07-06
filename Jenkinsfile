pipeline {
    agent any

    environment {
        MINIMUM_COVERAGE = 70
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    sh 'pwd'

                    def changedFiles = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim()
                    echo "Changed files: ${changedFiles}"

                    def changedServices = []

                    if (changedFiles.contains('spring-petclinic-genai-service')) {
                        changedServices.add('genai')
                    }
                    if (changedFiles.contains('spring-petclinic-customers-service')) {
                        changedServices.add('customers')
                    }
                    if (changedFiles.contains('spring-petclinic-vets-service')) {
                        changedServices.add('vets')
                    }
                    if (changedFiles.contains('spring-petclinic-visits-service')) {
                        changedServices.add('visits')
                    }
                    if (changedFiles.contains('spring-petclinic-api-gateway')) {
                        changedServices.add('api-gateway')
                    }
                    if (changedFiles.contains('spring-petclinic-discovery-server')) {
                        changedServices.add('discovery')
                    }
                    if (changedFiles.contains('spring-petclinic-config-server')) {
                        changedServices.add('config')
                    }
                    if (changedFiles.contains('spring-petclinic-admin-server')) {
                        changedServices.add('admin')
                    }

                    if (changedServices.isEmpty()) {
                        changedServices = ['all']
                    }

                    echo "Detected changes in services: ${changedServices}"

                    CHANGED_SERVICES_LIST = changedServices
                    CHANGED_SERVICES_STRING = changedServices.join(',')
                    echo "Changed services: ${CHANGED_SERVICES_STRING}"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Testing all modules'
                        sh './mvnw clean test'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Testing modules: ${modules}"
                        sh "./mvnw clean test -pl ${modules}"
                    }
                }
            }
            post {
                always {
                    script {
                        // Collect test results
                        def testReportPattern = CHANGED_SERVICES_LIST.contains('all') ? 
                            '**/surefire-reports/TEST-*.xml' : 
                            CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service/target/surefire-reports/TEST-*.xml" }.join(',')
                        
                        def testFiles = sh(script: "find . -name 'TEST-*.xml' -type f", returnStdout: true).trim()
                        if (testFiles) {
                            echo "Found test reports: ${testFiles}"
                            junit testReportPattern
                        } else {
                            echo 'No test reports found, likely no tests were executed.'
                        }

                        // Check coverage for each service individually using recordCoverage
                        def servicesCoverageFailed = []
                        
                        CHANGED_SERVICES_LIST.each { service ->
                            if (service != 'all' && service in ['customers', 'visits', 'vets', 'genai', 'api-gateway', 'discovery', 'config', 'admin']) {
                                def servicePath = "spring-petclinic-${service}-service"
                                def jacocoExecFile = "${servicePath}/target/jacoco.exec"
                                
                                if (fileExists(jacocoExecFile)) {
                                    echo "Checking coverage for ${service}..."
                                    
                                    try {
                                        // Generate JaCoCo XML report for this service
                                        sh "./mvnw jacoco:report -pl ${servicePath}"
                                        
                                        // Use recordCoverage with quality gates for individual service
                                        def coverageResult = recordCoverage(
                                            tools: [[parser: 'JACOCO', pattern: "${servicePath}/target/site/jacoco/jacoco.xml"]],
                                            sourceDirectories: [[path: "${servicePath}/src/main/java"]],
                                            sourceCodeRetention: 'EVERY_BUILD',
                                            qualityGates: [
                                                [threshold: env.MINIMUM_COVERAGE.toInteger(), metric: 'LINE', baseline: 'PROJECT', unstable: true]
                                            ]
                                        )
                                        
                                        echo "Coverage check completed for ${service}"
                                        
                                        // Check if this service caused the build to become unstable
                                        if (currentBuild.result == 'UNSTABLE') {
                                            servicesCoverageFailed.add(service)
                                            // Reset build status to continue checking other services
                                            currentBuild.result = 'SUCCESS'
                                        }
                                        
                                    } catch (Exception e) {
                                        echo "Error checking coverage for ${service}: ${e.getMessage()}"
                                        servicesCoverageFailed.add(service)
                                    }
                                } else {
                                    echo "No JaCoCo exec file found for ${service}, assuming coverage failure"
                                    servicesCoverageFailed.add(service)
                                }
                            }
                        }
                        
                        // Generate overall coverage report
                        if (CHANGED_SERVICES_LIST.contains('all')) {
                            recordCoverage(
                                tools: [[parser: 'JACOCO']],
                                sourceDirectories: [[path: 'src/main/java']],
                                sourceCodeRetention: 'EVERY_BUILD'
                            )
                        } else {
                            def jacocoPatterns = CHANGED_SERVICES_LIST.collect { 
                                "spring-petclinic-${it}-service/target/site/jacoco/jacoco.xml" 
                            }.join(',')
                            
                            recordCoverage(
                                tools: [[parser: 'JACOCO', pattern: jacocoPatterns]],
                                sourceDirectories: CHANGED_SERVICES_LIST.collect { 
                                    [path: "spring-petclinic-${it}-service/src/main/java"] 
                                },
                                sourceCodeRetention: 'EVERY_BUILD'
                            )
                        }
                        
                        // Fail build if any service has insufficient coverage
                        if (!servicesCoverageFailed.isEmpty()) {
                            error "Code coverage below ${env.MINIMUM_COVERAGE}% for services: ${servicesCoverageFailed.join(', ')}"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    if (CHANGED_SERVICES_LIST.contains('all')) {
                        echo 'Building all modules'
                        sh './mvnw clean package -DskipTests'
                    } else {
                        def modules = CHANGED_SERVICES_LIST.collect { "spring-petclinic-${it}-service" }.join(',')
                        echo "Building modules: ${modules}"
                        sh "./mvnw clean package -DskipTests -pl ${modules}"
                    }
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }
    
    post {
        always {
            echo "Pipeline completed with result: ${currentBuild.currentResult}"
            echo "Completed at: ${new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm:ss').format(new Date())}"
        }
    }
}