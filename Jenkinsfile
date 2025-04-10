def detectChanges() {
    def branch_name = ""

    if (env.CHANGE_ID) {
        branch_name = "${env.CHANGE_TARGET}"
        sh "git fetch origin ${branch_name}:${branch_name} --no-tags"
    } else {
        branch_name = "HEAD~1"
    }

    def changedFiles = sh(script: "git diff --name-only ${branch_name}", returnStdout: true).trim()
    echo "Changed files:\n${changedFiles}"

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

    def changedFolders = changedFiles.split('\n')
        .collect { it.split('/')[0] }
        .unique()
        .findAll { folderList.contains(it) }

    echo "Changed Folders:\n${changedFolders.join('\n')}"

    if (changedFolders.size() == 0) {
        echo "No changes detected. Using default services."
        changedFolders = ['spring-petclinic-discovery-server', 'spring-petclinic-config-server']
    }

    return changedFolders
}

def getCoveragePercentage(module) {
    def reportPath = "${WORKSPACE}/${module}/target/site/jacoco/index.html"
    def coverageReport = readFile(file: reportPath)
    def matcher = coverageReport =~ /<tfoot>(.*?)<\/tfoot>/
    def coveragePercentage = "0"

    if (matcher.find()) {
        def coverage = matcher[0][1]
        def instructionMatcher = coverage =~ /<td class="ctr2">(.*?)<\/td>/
        if (instructionMatcher.find()) {
            coveragePercentage = instructionMatcher[0][1]
        }
    }

    return coveragePercentage
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
                    def codeCoverages = []

                    for (module in modules) {
                        echo "Running tests for module: ${module}"
                        sh "mvn test -pl ${module}"

                        jacoco(
                            classPattern: "**/${module}/target/classes",
                            execPattern: "**/${module}/target/coverage-reports/jacoco.exec",
                            sourcePattern: "**/${module}/src/main/java",
                            runAlways: true
                        )

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

                        def coveragePercentage = getCoveragePercentage(module)
                        echo "Overall code coverage of ${module}: ${coveragePercentage}%"

                        codeCoverages.add("${module}: ${coveragePercentage}%")

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

                    env.CODE_COVERAGES = codeCoverages.join(',')
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
