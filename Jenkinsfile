// Function to detect changed service folders
def detectChanges() {
    def branch_name = ""
    // Determine the branch to compare against
    if (env.CHANGE_ID) {
        // For Pull Requests, compare against the target branch
        branch_name = "${env.CHANGE_TARGET}"
        echo "PR detected. Comparing against target branch: ${branch_name}"
        // Fetch the target branch explicitly
        sh "git fetch origin ${branch_name}:${branch_name} --no-tags --depth=10" // Added depth for efficiency
    } else {
        // For direct pushes, compare against the previous commit
        branch_name = "HEAD~1"
        echo "Direct push detected. Comparing against ${branch_name}"
    }

    // Get the list of changed files
    // Added --no-renames for simplicity in parsing paths
    def changedFilesOutput = sh(script: "git diff --name-only --no-renames ${branch_name} HEAD", returnStdout: true).trim()
    if (!changedFilesOutput) {
        echo "No changed files detected between ${branch_name} and HEAD."
        return [] // Return an empty list if no files changed
    }
    echo "Changed files:\n${changedFilesOutput}"
    def changedFiles = changedFilesOutput.split('\n')

    // List of known service directories
    def folderList = [
        'spring-petclinic-customers-service',
        'spring-petclinic-vets-service',
        'spring-petclinic-visits-service',
        'spring-petclinic-admin-server',
        'spring-petclinic-api-gateway',
        'spring-petclinic-config-server',
        'spring-petclinic-discovery-server',
        'spring-petclinic-genai-service'
        // Add other top-level directories that should trigger builds if needed
    ]

    // Filter to find unique, relevant service folders that have changed
    def changedFolders = changedFiles
        .collect { file ->
            // Handle files potentially not in a subdirectory
            def parts = file.split('/')
            return (parts.size() > 1 || folderList.contains(parts[0])) ? parts[0] : null
        }
        .findAll { it != null } // Remove nulls (files not in known folders)
        .unique() // Get unique folder names
        .findAll { folderList.contains(it) } // Ensure it's one of the target service folders

    echo "Relevant changed folders:\n${changedFolders.join('\n')}"

    // Return the list of changed service folders
    return changedFolders
}

pipeline {
    agent any

    tools {
        maven 'Maven' // Ensure 'Maven' matches the name configured in Jenkins Global Tool Configuration
        jdk 'JDK 17'   // Ensure 'JDK 17' matches the name configured in Jenkins Global Tool Configuration
    }

    environment {
        // Define environment variables if needed globally
    }

    stages {
        stage('Determine Changes') {
            steps {
                script {
                    // Ensure workspace is clean before checkout if needed, though agent any usually provides one
                    // cleanWs() // Optional: uncomment if you need explicit cleaning before checkout

                    // Checkout SCM step is implicitly handled by Jenkins for multibranch pipelines typically
                    // If not, add a checkout step here:
                    // checkout scm

                    def servicesToBuild = detectChanges()
                    env.CHANGED_SERVICES = servicesToBuild.join(',')
                    echo "Services determined to have changes: ${env.CHANGED_SERVICES ?: 'None'}"
                }
            }
        }

        // JaCoCo plugin should be permanently added to the pom.xml files in your repository.
        // The dynamic 'Add JaCoCo Plugin' stage has been removed.

        stage('Test & Coverage Check') {
            steps {
                script {
                    // Split the comma-separated string from env var into an array
                    def changedServicesList = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : []

                    // *** FIX: Check array length instead of using isEmpty() ***
                    if (changedServicesList && changedServicesList.length > 0 && changedServicesList[0]) {
                        echo "Running tests for changed services: ${changedServicesList.join(', ')}"
                        // Run tests in parallel for efficiency
                        parallel changedServicesList.collectEntries { serviceName ->
                            // Ensure serviceName is not empty or null before creating the entry
                            if (serviceName) {
                                return ["Testing ${serviceName}": {
                                    dir(serviceName) {
                                        echo "Entering directory: ${pwd()}"
                                        echo "Testing ${serviceName} (includes JaCoCo report/check via Maven build)..."
                                        try {
                                            // This command runs clean, test, and JaCoCo goals (prepare-agent, report, check)
                                            // as configured in the pom.xml's build lifecycle
                                            sh "mvn -B -Djava.awt.headless=true clean test" // Added headless=true, often useful on servers
                                            echo "Tests and coverage check successful for ${serviceName}."
                                        } catch (err) {
                                            echo "ERROR: Test or Coverage Check failed for ${serviceName}."
                                            // Print error message for debugging
                                            echo err.getMessage()
                                            // Mark the overall build result as FAILURE. Jenkins parallel automatically handles this
                                            // but setting it explicitly can be clearer.
                                            currentBuild.result = 'FAILURE'
                                            // Use error step to ensure stage fails if any parallel branch fails
                                            error("Test or Coverage Check failed for ${serviceName}")
                                        }
                                    }
                                }]
                            } else {
                                return [:] // Return empty map entry if serviceName is somehow empty
                            }
                        } // End parallel
                    } else {
                        echo "No services changed or detected, skipping Test & Coverage Check stage."
                    }
                } // End script
            } // End steps
            post {
                always {
                   script {
                        // Collect results even if the stage failed partially
                        def changedServicesList = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : []

                        // *** FIX: Check array length instead of using isEmpty() ***
                        if (changedServicesList && changedServicesList.length > 0 && changedServicesList[0]) {
                            echo "Collecting results for services: ${changedServicesList.join(', ')}"
                            for (service in changedServicesList) {
                                // Ensure service name is valid before proceeding
                                if (service) {
                                    // Use a try-catch as test/report files might not exist if the 'mvn test' step failed very early
                                    try {
                                        dir(service) {
                                            echo "Collecting results in directory: ${pwd()}"
                                            // Check if report files exist before attempting to archive
                                            def junitReportsExist = fileExists 'target/surefire-reports/*.xml'
                                            def jacocoExecExists = fileExists 'target/jacoco.exec'
                                            def jacocoSiteExists = fileExists 'target/site/jacoco/index.html'

                                            if (junitReportsExist) {
                                                echo "Collecting JUnit results for ${service}..."
                                                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                                            } else {
                                                echo "WARN: No JUnit reports found in target/surefire-reports for ${service}."
                                            }

                                            if (jacocoExecExists) {
                                                 echo "Collecting JaCoCo execution data for ${service}..."
                                                // The Jenkins JaCoCo plugin uses the .exec file and source/class files
                                                jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java'
                                            } else {
                                                echo "WARN: No JaCoCo execution file found at target/jacoco.exec for ${service}."
                                            }

                                            // --- Publish Checks Logic ---
                                            // Determine status based on existence of report and Maven execution result
                                            // Note: currentBuild.result reflects the *overall* build status up to this point.
                                            // A finer-grained check might parse Maven output or check specific file presence/content.
                                            def coverageStatus = 'INCONCLUSIVE' // Default status
                                            def summaryText = "Coverage check status for ${service} is inconclusive."

                                            if (jacocoSiteExists) {
                                                // Basic check: If report exists, assume Maven check goal *passed* if build didn't fail earlier.
                                                // This assumes the 'error' step in the 'try/catch' above correctly failed the stage.
                                                if (currentBuild.result == null || currentBuild.result == 'SUCCESS' || currentBuild.result == 'UNSTABLE') {
                                                     coverageStatus = 'SUCCESS'
                                                     summaryText = "Coverage check presumed passed for ${service} (report generated)."
                                                } else {
                                                     coverageStatus = 'FAILURE'
                                                     summaryText = "Coverage check presumed failed for ${service} (Maven build failed, but report might exist)."
                                                }
                                            } else {
                                                 // If report doesn't exist, it definitely failed or didn't run
                                                 coverageStatus = 'FAILURE'
                                                 summaryText = "Coverage check failed for ${service} (JaCoCo report not found)."
                                            }

                                            // Check if publishChecks step exists before calling it
                                            if (steps.metaClass.respondsTo(steps, 'publishChecks')) {
                                                echo "Publishing checks for ${service} with status: ${coverageStatus}"
                                                publishChecks(
                                                    name: "Code Coverage (${service})",
                                                    title: "Code Coverage Result: ${coverageStatus}",
                                                    summary: summaryText,
                                                    // Link to the specific service's report within the Jenkins build UI
                                                    detailsURL: "${env.BUILD_URL}jacoco/${service}/", // Adjust path based on JaCoCo plugin structure if needed
                                                    conclusion: coverageStatus
                                                )
                                            } else {
                                                echo "WARN: publishChecks step not available. Skipping GitHub check publication."
                                            }
                                        } // End dir
                                    } catch(e) {
                                         echo "ERROR: Exception while collecting results/publishing checks for ${service}: ${e.getMessage()}"
                                         // Optionally publish a failure check here too if the step exists
                                         if (steps.metaClass.respondsTo(steps, 'publishChecks')) {
                                              publishChecks(
                                                    name: "Code Coverage (${service})",
                                                    title: "Code Coverage Result: ERROR",
                                                    summary: "Error processing coverage results for ${service}: ${e.getMessage()}",
                                                    conclusion: 'FAILURE'
                                                )
                                         }
                                    } // End try-catch
                                } // End if(service)
                            } // End for loop
                        } // End if check changedServicesList length
                   } // End script
                } // End always
            } // End post
        } // End stage Test & Coverage Check

        // The separate 'Coverage Check' stage is removed as the check is integrated
        // into the 'Test & Coverage Check' stage via the Maven jacoco:check goal.

        stage('Build Package') {
            // Only run this stage if the previous stages (including tests) were successful
            when {
                 expression { return currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : []

                    // *** FIX: Check array length instead of using isEmpty() ***
                    if (changedServicesList && changedServicesList.length > 0 && changedServicesList[0]) {
                        echo "Building packages for changed services: ${changedServicesList.join(', ')}"
                        parallel changedServicesList.collectEntries { serviceName ->
                             if (serviceName) {
                                return ["Building ${serviceName}": {
                                    dir(serviceName) {
                                        echo "Entering directory: ${pwd()}"
                                        echo "Building package for ${serviceName}..."
                                        try {
                                            // Clean and package, skip tests as they already ran
                                            sh "mvn -B -Djava.awt.headless=true clean package -DskipTests"
                                            echo "Package built successfully for ${serviceName}."
                                        } catch (err) {
                                            echo "ERROR: Package build failed for ${serviceName}."
                                            echo err.getMessage()
                                            // Fail the pipeline decisively if packaging fails
                                            error "Package build failed for ${serviceName}"
                                        }
                                    }
                                }]
                             } else {
                                 return [:]
                             }
                        } // End parallel
                    } else {
                         echo "No services changed or prior stage failed, skipping Build Package stage."
                    }
                } // End script
            } // End steps
        } // End stage Build Package
    } // End stages

    post {
        // Runs regardless of build status
        always {
            echo "Pipeline finished with status: ${currentBuild.currentResult}"
            // Clean up the workspace
            cleanWs()
            echo "Workspace cleaned."
        }
        success {
            echo 'Pipeline completed successfully!'
            // Add success notifications if needed (e.g., Slack, email)
        }
        failure {
            echo 'Pipeline failed!'
            // Add failure notifications if needed
        }
        unstable {
             echo 'Pipeline finished unstable (e.g., tests failed but build continued).'
             // Add unstable notifications if needed
        }
        aborted {
             echo 'Pipeline was aborted.'
             // Add aborted notifications if needed
        }
    } // End post
} // End pipeline