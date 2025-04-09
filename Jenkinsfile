
def detectChanges() {
def branch_name = ""
// Xác định branch để so sánh
if (env.CHANGE_ID) {
    branch_name = "${env.CHANGE_TARGET}"
    // Fetch branch chính nếu là Pull Request
    sh "git fetch origin ${branch_name}:${branch_name} --no-tags"
} else {
    branch_name = "HEAD~1"
}

// Lấy danh sách các file thay đổi
def changedFiles = sh(script: "git diff --name-only ${branch_name}", returnStdout: true).trim()
echo "Changed files:\n${changedFiles}"

// Danh sách các thư mục service
def folderList = [
    'spring-petclinic-customers-service',
    'spring-petclinic-vets-service',
    'spring-petclinic-visits-service',
    'spring-petclinic-admin-server',
    'spring-petclinic-api-gateway',
    'spring-petclinic-config-server',
    'spring-petclinic-discovery-server',
    'spring-petclinic-genai-service'
]

// Lọc các thư mục service có thay đổi
def changedFolders = changedFiles.split('\n')
    .collect { it.split('/')[0] } // Lấy thư mục gốc
    .unique() // Loại bỏ trùng lặp
    .findAll { folderList.contains(it) } // Chỉ giữ lại các thư mục trong danh sách folderList

echo "Changed Folders:\n${changedFolders.join('\n')}"

// Trả về danh sách các service có thay đổi
return changedFolders
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

        // STAGE REMOVED - JaCoCo plugin is now in the source pom.xml files
        // stage('Add JaCoCo Plugin') { ... }

        stage('Test & Coverage Check') { // Combined stage - Maven handles check
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    // Ensure list is not empty before processing
                    if (changedServicesList && !changedServicesList.isEmpty() && changedServicesList[0]) {
                         // Run tests in parallel for efficiency
                        parallel changedServicesList.collectEntries { service ->
                            ["Testing ${service}": {
                                dir(service) {
                                    echo "Testing ${service} (includes JaCoCo report/check)..."
                                    try {
                                        // This command now runs tests AND JaCoCo report/check
                                        sh "mvn -B clean test"
                                        // If Maven succeeds, coverage met the check goal
                                        // Publish success check (optional here, maybe better in post?)
                                    } catch (err) {
                                        echo "Test or Coverage Check failed for ${service}: ${err.getMessage()}"
                                        // Mark build as unstable or failed
                                        currentBuild.result = 'FAILURE'
                                        // Publish failure check (optional here, maybe better in post?)
                                    }
                                }
                            }]
                        }
                    } else {
                        echo "No services changed, skipping Test & Coverage Check stage."
                    }
                }
            }
            post {
                always {
                   script {
                        def changedServicesList = env.CHANGED_SERVICES.split(',')
                        if (changedServicesList && !changedServicesList.isEmpty() && changedServicesList[0]) {
                            for (service in changedServicesList) {
                                // Use a try-catch as test/report files might not exist if the 'mvn test' step failed early
                                try {
                                    dir(service) {
                                        // Collect JUnit results regardless of success/failure of tests
                                        junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

                                        // Collect JaCoCo data (HTML report is in target/site/jacoco/)
                                        // The Jenkins JaCoCo plugin uses the .exec file
                                        jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java'

                                        // Optional: Parse report for publishChecks if needed
                                        // You might need error handling if report doesn't exist
                                        def coverageStatus = fileExists('target/site/jacoco/index.html') ? 'SUCCESS' : 'FAILURE' // Basic check
                                        def summaryText = "Check JaCoCo report for details."
                                        def coverageDetail = ""
                                        if (coverageStatus == 'SUCCESS'){
                                             // Simple success message or parse XML for actual %
                                              summaryText = "Coverage check passed (based on Maven execution)."
                                        } else {
                                             summaryText = "Coverage check failed (based on Maven execution) or report not found."
                                        }

                                         publishChecks(
                                            name: "Code Coverage (${service})",
                                            title: "Code Coverage Result",
                                            summary: summaryText,
                                            detailsURL: "${env.BUILD_URL}jacoco", // Link to Jenkins JaCoCo report
                                            conclusion: coverageStatus
                                        )
                                    }
                                } catch(e) {
                                     echo "Error collecting results/publishing checks for ${service}: ${e.getMessage()}"
                                      publishChecks(
                                            name: "Code Coverage (${service})",
                                            title: "Code Coverage Result",
                                            summary: "Error processing coverage results for ${service}.",
                                            conclusion: 'FAILURE'
                                        )
                                }
                            }
                        }
                   }
                }
            }
        }

        // Coverage Check stage might be removed or simplified as check is in 'Test' stage now
        // stage('Coverage Check') { ... }

        stage('Build Package') { // Renamed for clarity
            when {
                 expression {
                    // Only run build if tests were successful overall
                    return currentBuild.result == null || currentBuild.result == 'SUCCESS'
                 }
            }
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    if (changedServicesList && !changedServicesList.isEmpty() && changedServicesList[0]) {
                        parallel changedServicesList.collectEntries { service ->
                            ["Building ${service}": {
                                dir(service) {
                                    echo "Building package for ${service}..."
                                    try {
                                        // Skip tests as they already ran
                                        sh "mvn -B clean package -DskipTests"
                                    } catch (err) {
                                        echo "Package build failed for ${service}: ${err.getMessage()}"
                                        error "Package build failed for ${service}" // Fail the pipeline
                                    }
                                }
                            }]
                        }
                    } else {
                         echo "No services changed or prior stage failed, skipping Build Package stage."
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
            echo 'Pipeline successful!'
            // Maybe add final success notification/check
        }
        failure {
            echo 'Pipeline failed!'
             // Maybe add final failure notification/check
        }
    }
}