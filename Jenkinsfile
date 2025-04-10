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
        'spring-petclinic-admin-server',
        'spring-petclinic-api-gateway',
        'spring-petclinic-config-server',
        'spring-petclinic-customers-service',
        'spring-petclinic-discovery-server',
        'spring-petclinic-genai-service',
        'spring-petclinic-vets-service',
        'spring-petclinic-visits-service'
    ]

    // Lọc các thư mục service có thay đổi
    def changedFolders = changedFiles.split('\n')
        .collect { it.split('/')[0] } // Lấy thư mục gốc
        .unique() // Loại bỏ trùng lặp
        .findAll { folderList.contains(it) } // Chỉ giữ lại các thư mục trong danh sách folderList

    echo "Changed Folders:\n${changedFolders.join('\n')}"

    // Nếu không có thay đổi, trả về hai service mặc định
    if (changedFolders.size() == 0) {
        echo "No changes detected. Using default services."
        changedFolders = ['spring-petclinic-customers-service', 'spring-petclinic-vets-service']
    }

    // Trả về danh sách các service có thay đổi hoặc mặc định
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
                
        stage('Test') {
            steps {
                script {
                    def modules = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : []

                    for (module in modules) {
                        def testCommand = "mvn test -pl ${module}"
                        echo "Running tests for affected modules: ${module}"
                        sh "${testCommand}"

                        // Generate JaCoCo HTML Report
                        jacoco(
                            classPattern: "**/${module}/target/classes",
                            execPattern: "**/${module}/target/coverage-reports/jacoco.exec",
                            sourcePattern: "**/${module}/src/main/java",
                            runAlways: true
                        )

                        // Publish HTML Artifact of Code Coverage Report
                        publishHTML(
                            target: [
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: "${module}/target/site/jacoco",
                                reportFiles: "index.html",
                                reportName: "${module}_code_coverage_report_${env.COMMIT_HASH}_${env.BUILD_ID}"
                            ]
                        )

                        // Get Code Coverage
                        def codeCoverages = []
                        def coverageReport = readFile(file: "${WORKSPACE}/${module}/target/site/jacoco/index.html")
                        def matcher = coverageReport =~ /<tfoot>(.*?)<\/tfoot>/
                        if (matcher.find()) {
                            def coverage = matcher[0][1]
                            def instructionMatcher = coverage =~ /<td class="ctr2">(.*?)<\/td>/
                            if (instructionMatcher.find()) {
                                def coveragePercentage = instructionMatcher[0][1]
                                echo "Overall code coverage of ${module}: ${coveragePercentage}%"

                                codeCoverages.add(coveragePercentage)
                            }
                        }

                        env.CODE_COVERAGES = codeCoverages.join(',')

                        // Cập nhật GitHub Checks bằng publishChecks
                        if (coveragePercentage.toFloat() >= 70) {
                            publishChecks(
                                name: 'Test Code Coverage',
                                title: 'Code Coverage Check Success!',
                                summary: 'All test code coverage is greater than 70%',
                                text: 'Check Success!',
                                detailsURL: env.BUILD_URL,
                                conclusion: 'SUCCESS'
                            )
                        } else {
                            publishChecks(
                                name: 'Test Code Coverage',
                                title: 'Code Coverage Check Failed!',
                                summary: "Coverage must be at least 70%. Your coverage is ${coveragePercentage}%.",
                                text: 'Increase test coverage and retry the build.',
                                detailsURL: env.BUILD_URL,
                                conclusion: 'FAILURE'
                            )
                            error "Code coverage check failed for ${module}. Coverage must be at least 70%."
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
                            sh "mvn package -DskipTests"
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