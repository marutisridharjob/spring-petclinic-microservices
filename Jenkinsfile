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
        
        stage('Add JaCoCo Plugin') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Adding JaCoCo plugin to ${service}..."
                            
                            sh '''
                                if ! grep -q "jacoco-maven-plugin" pom.xml; then
                                    # Tạo file tạm để lưu nội dung cập nhật
                                    sed '/<\\/dependencies>/a\\
                                    <build>\\
                                        <plugins>\\
                                            <plugin>\\
                                                <groupId>org.jacoco</groupId>\\
                                                <artifactId>jacoco-maven-plugin</artifactId>\\
                                                <version>0.8.7</version>\\
                                                <executions>\\
                                                    <execution>\\
                                                        <goals>\\
                                                            <goal>prepare-agent</goal>\\
                                                        </goals>\\
                                                    </execution>\\
                                                    <execution>\\
                                                        <id>report</id>\\
                                                        <phase>prepare-package</phase>\\
                                                        <goals>\\
                                                            <goal>report</goal>\\
                                                        </goals>\\
                                                    </execution>\\
                                                    <execution>\\
                                                        <id>jacoco-check</id>\\
                                                        <goals>\\
                                                            <goal>check</goal>\\
                                                        </goals>\\
                                                        <configuration>\\
                                                            <rules>\\
                                                                <rule>\\
                                                                    <element>BUNDLE</element>\\
                                                                    <limits>\\
                                                                        <limit>\\
                                                                            <counter>INSTRUCTION</counter>\\
                                                                            <value>COVEREDRATIO</value>\\
                                                                            <minimum>0.70</minimum>\\
                                                                        </limit>\\
                                                                    </limits>\\
                                                                </rule>\\
                                                            </rules>\\
                                                        </configuration>\\
                                                    </execution>\\
                                                </executions>\\
                                            </plugin>\\
                                        </plugins>\\
                                    </build>\\
                                    ' pom.xml > pom_updated.xml
                                    mv pom_updated.xml pom.xml
                                fi
                            '''
                        }
                    }
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
                            // Chỉ chạy test và sử dụng JaCoCo sau khi đã thêm plugin
                            sh "mvn -B clean test"
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
                                // Collect JUnit test results
                                junit 'target/surefire-reports/*.xml'
                                
                                // Collect JaCoCo coverage results (nếu tệp tồn tại)
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
        
        stage('Coverage Check') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Checking code coverage for ${service}..."
                            
                            // Chạy JaCoCo để tạo báo cáo coverage
                            sh "mvn jacoco:report"
                            
                            // Kiểm tra độ phủ code
                            def jacocoResult = sh(script: """
                                # Extract coverage percentage from JaCoCo report
                                COVERAGE_FILE=\$(find target/site/jacoco -name "jacoco.xml" | head -n 1)
                                
                                if [ ! -f "\$COVERAGE_FILE" ]; then
                                    echo "No JaCoCo coverage file found!"
                                    exit 1
                                fi
                                
                                # Tính tỉ lệ phần trăm các dòng được test
                                COVERED=\$(grep -oP 'covered="\\d+"' \$COVERAGE_FILE | head -n 1 | grep -oP '\\d+')
                                MISSED=\$(grep -oP 'missed="\\d+"' \$COVERAGE_FILE | head -n 1 | grep -oP '\\d+')
                                
                                TOTAL=\$((\$COVERED + \$MISSED))
                                if [ \$TOTAL -eq 0 ]; then
                                    echo "No code to cover!"
                                    exit 1
                                fi
                                
                                COVERAGE_PCT=\$(echo "scale=2; 100 * \$COVERED / \$TOTAL" | bc)
                                echo "Code coverage: \$COVERAGE_PCT%"
                                
                                if (( \$(echo "\$COVERAGE_PCT < 70" | bc -l) )); then
                                    echo "Code coverage is below 70%"
                                    exit 1
                                else
                                    echo "Code coverage is sufficient (>=70%)"
                                fi
                            """, returnStatus: true)
                            
                            // Lấy giá trị coverage để cập nhật GitHub Checks
                            def codeCoverage = sh(script: """
                                COVERAGE_FILE=\$(find target/site/jacoco -name "jacoco.xml" | head -n 1)
                                COVERED=\$(grep -oP 'covered="\\d+"' \$COVERAGE_FILE | head -n 1 | grep -oP '\\d+')
                                MISSED=\$(grep -oP 'missed="\\d+"' \$COVERAGE_FILE | head -n 1 | grep -oP '\\d+')
                                TOTAL=\$((\$COVERED + \$MISSED))
                                echo \$(echo "scale=2; 100 * \$COVERED / \$TOTAL" | bc)
                            """, returnStdout: true).trim()
                            
                            // Cập nhật GitHub Checks bằng publishChecks
                            if (jacocoResult == 0) {
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
                                    summary: "Coverage must be at least 70%. Your coverage is ${codeCoverage}%.",
                                    text: 'Increase test coverage and retry the build.',
                                    detailsURL: env.BUILD_URL,
                                    conclusion: 'FAILURE'
                                )
                                error "Code coverage check failed for ${service}. Coverage must be at least 70%."
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