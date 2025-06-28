pipeline {
    agent any
    
    tools {
        maven 'Maven 3'
    }
    
    environment {
        MINIMUM_COVERAGE = '70'
        GITHUB_APP_CREDENTIAL = credentials('Iv23liEOntjPuDy6ItpO')
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Debug Environment') {
            steps {
                script {
                    echo "Build information:"
                    echo "- Branch: ${env.BRANCH_NAME}"
                    echo "- Pull Request: ${env.CHANGE_ID ? 'Yes (#' + env.CHANGE_ID + ')' : 'No'}"
                    echo "- Target Branch: ${env.CHANGE_TARGET ?: 'N/A'}"
                    echo "- Build URL: ${env.BUILD_URL}"
                }
            }
        }
        
        stage('Determine Changed Services') {
            steps {
                script {
                    // Define SERVICES in script block
                    def SERVICES = [
                        'customers-service': 'spring-petclinic-customers-service',
                        'vets-service': 'spring-petclinic-vets-service',
                        'visits-service': 'spring-petclinic-visits-service',
                        'api-gateway': 'spring-petclinic-api-gateway',
                        'discovery-server': 'spring-petclinic-discovery-server',
                        'config-server': 'spring-petclinic-config-server',
                        'admin-server': 'spring-petclinic-admin-server',
                        'genai-service': 'spring-petclinic-genai-service'
                    ]
                    
                    // Initialize empty list for changed services
                    env.CHANGED_SERVICES = ""
                    
                    // For pull requests, compare with target branch
                    if (env.CHANGE_ID) {
                        echo "Processing Pull Request #${env.CHANGE_ID}"
                        sh "git fetch origin main:refs/remotes/origin/main"
                        // Set initial GitHub check for PR
                        if (env.CHANGE_ID) {
                            try {
                                setGitHubPullRequestStatus(
                                    context: "CI Pipeline",
                                    state: 'PENDING',
                                    message: "CI Pipeline Running: Analyzing changed services and running tests..."
                                )
                            } catch (Exception e) {
                                echo "Warning: Failed to set GitHub PR status: ${e.message}"
                            }
                        }
                        
                        SERVICES.each { service, path ->
                            def changes = sh(
                                script: "git diff origin/${env.CHANGE_TARGET}...HEAD --name-only | grep -E '^${path}/' || true",
                                returnStdout: true
                            ).trim()
                            
                            if (changes) {
                                echo "Changes detected in ${service}"
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            } else {
                                echo "No changes detected in ${service}"
                            }
                        }
                        
                        // Debug output to verify what was detected
                        echo "Detected changes in these services: ${env.CHANGED_SERVICES}"
                    } 
                    // For direct branch builds, compare with previous commit
                    else {
                        echo "Processing branch ${env.BRANCH_NAME}"
                        SERVICES.each { service, path ->
                            def changes = sh(
                                script: "git diff HEAD^ HEAD --name-only | grep ^${path}/ || true",
                                returnStdout: true
                            ).trim()
                            
                            if (changes) {
                                echo "Changes detected in ${service}"
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                    }
                    
                    // If no specific service changes detected, print warning and only include files from PR
                    if (env.CHANGED_SERVICES.trim().isEmpty()) {
                        echo "WARNING: No service changes detected using path-based approach"
                        
                        // Get all changed files in PR to check directly
                        def allChangedFiles = sh(
                            script: "git diff origin/${env.CHANGE_TARGET}...HEAD --name-only || true",
                            returnStdout: true
                        ).trim()
                        
                        echo "All changed files in PR: ${allChangedFiles}"
                        
                        // Directly check each service by looking at file paths
                        SERVICES.each { service, path ->
                            def serviceMatch = sh(
                                script: "echo '${allChangedFiles}' | grep -E '^${path}/' || true",
                                returnStdout: true
                            ).trim()
                            
                            if (serviceMatch) {
                                echo "Manual check: Changes detected in ${service}"
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                        
                        // If still empty, then only build the services explicitly mentioned in commit messages
                        if (env.CHANGED_SERVICES.trim().isEmpty()) {
                            echo "Still no changes detected, will check commit messages"
                            
                            def commitMessages = sh(
                                script: "git log origin/${env.CHANGE_TARGET}...HEAD --pretty=format:%s || true",
                                returnStdout: true
                            ).trim()
                            
                            echo "Commit messages: ${commitMessages}"
                            
                            SERVICES.each { service, path ->
                                // Check if service name is mentioned in commit messages
                                if (commitMessages.toLowerCase().contains(service.toLowerCase())) {
                                    echo "Service ${service} mentioned in commit messages"
                                    env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                                }
                            }
                            
                            // If still empty after all checks, fallback to only modified files from PR
                            if (env.CHANGED_SERVICES.trim().isEmpty()) {
                                echo "After all checks, no specific services identified"
                                echo "Only building services with files modified in the PR"
                                
                                // Check Jenkinsfile change
                                if (allChangedFiles.contains("Jenkinsfile")) {
                                    echo "Jenkinsfile was modified, but no services will be built unless explicitly changed"
                                }
                            }
                        }
                    }
                    
                    echo "Services to process: ${env.CHANGED_SERVICES}"
                    
                    // Store SERVICES map for later stages
                    env.SERVICES_JSON = groovy.json.JsonOutput.toJson(SERVICES)
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    def SERVICES = readJSON text: env.SERVICES_JSON
                    def servicesToTest = env.CHANGED_SERVICES.trim().split(" ")
                    
                    // Track overall results
                    def allServicesPass = true
                    def allCoverageResults = [:]
                    
                    servicesToTest.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Running tests for ${service}"
                                
                                // Set service check to pending if this is a PR
                                if (env.CHANGE_ID) {
                                    try {
                                        setGitHubPullRequestStatus(
                                            context: "Test Code Coverage - ${service}",
                                            state: 'PENDING',
                                            message: "Running tests for ${service}..."
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to set GitHub PR status: ${e.message}"
                                    }
                                }
                                
                                // Run tests
                                def testResult = sh(script: "mvn clean test", returnStatus: true)
                                
                                // Publish JUnit test results
                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                
                                if (testResult != 0) {
                                    allServicesPass = false
                                    echo "Tests failed for ${service}"
                                    
                                    if (env.CHANGE_ID) {
                                        try {
                                            setGitHubPullRequestStatus(
                                                context: "Test Code Coverage - ${service}",
                                                state: 'FAILURE',
                                                message: "Tests for ${service} have failed"
                                            )
                                        } catch (Exception e) {
                                            echo "Warning: Failed to update GitHub PR status: ${e.message}"
                                        }
                                    }
                                    
                                    // Skip rest of processing for this service
                                    return // Using return instead of continue
                                }
                                
                                try {
                                    // Publish coverage report using Coverage Plugin
                                    recordCoverage(
                                        tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                        id: "${service}-coverage", 
                                        name: "${service} - JaCoCo Coverage",
                                        qualityGates: [
                                            [threshold: 70.0, metric: 'LINE']
                                        ]
                                    )
                                    
                                    // Backup using PublishHTML
                                    publishHTML([
                                        allowMissing: true,
                                        alwaysLinkToLastBuild: true,
                                        keepAll: true,
                                        reportDir: 'target/site/jacoco',
                                        reportFiles: 'index.html',
                                        reportName: "${service} - JaCoCo Coverage Report"
                                    ])
                                    
                                    // Check coverage with a safer approach
                                    def coverageScript = """
                                        if [ -f target/site/jacoco/jacoco.csv ]; then
                                            COVERAGE=\$(awk -F"," '{ instructions += \$4 + \$5; covered += \$5 } END { print (covered/instructions) * 100 }' target/site/jacoco/jacoco.csv)
                                            echo "Code coverage: \$COVERAGE%"
                                            
                                            # Use awk for comparison instead of bc
                                            if (( \$(awk 'BEGIN {print (\$COVERAGE < ${MINIMUM_COVERAGE}) ? 1 : 0}') )); then
                                                echo "Coverage below minimum threshold of ${MINIMUM_COVERAGE}%"
                                                echo "\${COVERAGE}" > coverage-result.txt
                                                exit 1
                                            else
                                                echo "Coverage meets minimum threshold of ${MINIMUM_COVERAGE}%"
                                                echo "\${COVERAGE}" > coverage-result.txt
                                            fi
                                        else
                                            echo "No coverage data found, skipping coverage check"
                                            echo "0" > coverage-result.txt
                                        fi
                                    """
                                    def coverageResult = sh(script: coverageScript, returnStatus: true)
                                    
                                    // Read actual coverage for GitHub check
                                    def codeCoverage = sh(script: "cat coverage-result.txt", returnStdout: true).trim()
                                    allCoverageResults[service] = codeCoverage
                                    
                                    // Update GitHub check for code coverage
                                    if (env.CHANGE_ID) {
                                        def state = coverageResult == 0 ? 'SUCCESS' : 'FAILURE'
                                        def coverageFormatted = codeCoverage.indexOf(".") > 0 ? 
                                            codeCoverage.substring(0, codeCoverage.indexOf(".") + 2) : 
                                            codeCoverage
                                            
                                        try {
                                            setGitHubPullRequestStatus(
                                                context: "Test Code Coverage - ${service}",
                                                state: state,
                                                message: "Coverage: ${coverageFormatted}% (min: ${MINIMUM_COVERAGE}%)"
                                            )
                                        } catch (Exception e) {
                                            echo "Warning: Failed to update GitHub PR status: ${e.message}"
                                        }
                                    }
                                    
                                    // If coverage below threshold, mark unstable (not fail)
                                    if (coverageResult != 0) {
                                        allServicesPass = false
                                        unstable "Test coverage for ${service} is below the required threshold of ${MINIMUM_COVERAGE}%"
                                    }
                                } catch (Exception e) {
                                    echo "Warning: Coverage reporting failed for ${service}: ${e.message}"
                                    allServicesPass = false
                                    
                                    if (env.CHANGE_ID) {
                                        try {
                                            setGitHubPullRequestStatus(
                                                context: "Test Code Coverage - ${service}",
                                                state: 'FAILURE',
                                                message: "Failed to analyze code coverage for ${service}"
                                            )
                                        } catch (Exception e2) {
                                            echo "Warning: Failed to update GitHub PR status: ${e2.message}"
                                        }
                                    }
                                }
                            }
                        } else {
                            echo "Path not found for service: ${service}"
                        }
                    }
                    
                    // Create summary report for PR
                    if (env.CHANGE_ID && !allCoverageResults.isEmpty()) {
                        def summaryText = "Coverage Summary - Minimum required: ${MINIMUM_COVERAGE}%"
                        
                        try {
                            setGitHubPullRequestStatus(
                                context: "Overall Coverage Summary",
                                state: allServicesPass ? 'SUCCESS' : 'FAILURE',
                                message: summaryText
                            )
                        } catch (Exception e) {
                            echo "Warning: Failed to create summary status: ${e.message}"
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    def SERVICES = readJSON text: env.SERVICES_JSON
                    def servicesToBuild = env.CHANGED_SERVICES.trim().split(" ")
                    def buildSuccess = true
                    
                    servicesToBuild.each { service ->
                        def path = SERVICES[service]
                        if (path) {
                            dir(path) {
                                echo "Building ${service}"
                                
                                // Update GitHub check if this is a PR
                                if (env.CHANGE_ID) {
                                    try {
                                        setGitHubPullRequestStatus(
                                            context: "Build - ${service}",
                                            state: 'PENDING',
                                            message: "Building ${service}..."
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to set GitHub PR status: ${e.message}"
                                    }
                                }
                                
                                def buildResult = sh(script: "mvn clean package -DskipTests", returnStatus: true)
                                
                                // Archive the artifacts
                                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
                                
                                if (buildResult != 0) {
                                    buildSuccess = false
                                    echo "Build failed for ${service}"
                                }
                                
                                // Update GitHub check for build result
                                if (env.CHANGE_ID) {
                                    def state = buildResult == 0 ? 'SUCCESS' : 'FAILURE'
                                    try {
                                        setGitHubPullRequestStatus(
                                            context: "Build - ${service}",
                                            state: state,
                                            message: "${service} build ${state == 'SUCCESS' ? 'completed successfully' : 'failed'}"
                                        )
                                    } catch (Exception e) {
                                        echo "Warning: Failed to update GitHub PR status: ${e.message}"
                                    }
                                }
                            }
                        } else {
                            echo "Path not found for service: ${service}"
                        }
                    }
                    
                    // Update overall CI status for PR
                    if (env.CHANGE_ID) {
                        try {
                            setGitHubPullRequestStatus(
                                context: "CI Pipeline",
                                state: buildSuccess ? 'SUCCESS' : 'FAILURE',
                                message: buildSuccess ? 
                                    "All services built successfully." : 
                                    "One or more services failed to build."
                            )
                        } catch (Exception e) {
                            echo "Warning: Failed to update GitHub PR status: ${e.message}"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        unstable {
            echo 'Pipeline completed but with unstable status (e.g. test coverage below threshold)'
        }
        failure {
            echo 'Pipeline failed!'
            
            // Update overall GitHub check if it failed
            script {
                if (env.CHANGE_ID) {
                    try {
                        setGitHubPullRequestStatus(
                            context: "CI Pipeline",
                            state: 'FAILURE',
                            message: "The pipeline encountered errors and could not complete successfully."
                        )
                    } catch (Exception e) {
                        echo "Warning: Failed to update GitHub PR status: ${e.message}"
                    }
                }
            }
        }
    }
}
