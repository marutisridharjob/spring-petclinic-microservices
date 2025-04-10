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
    if (changedFolders.isEmpty()) {
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
        
        stage('Add JaCoCo Plugin') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES.split(',')
                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Adding JaCoCo plugin to ${service}..."
                            
                            sh '''
                                if ! grep -q "<build>" pom.xml; then
                                    # Thêm thẻ <build> nếu chưa tồn tại
                                    sed -i '/<\\/dependencies>/a\\
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
                                    </build>' pom.xml
                                fi
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Add JaCoCo Plugin') {
            steps {
                script {
                    def changedServicesList = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : []
                    
                    // Nếu không có service nào thay đổi, sử dụng danh sách service mặc định
                    if (changedServicesList.isEmpty()) {
                        echo "No services changed. Adding JaCoCo plugin to default services."
                        changedServicesList = ['spring-petclinic-customers-service', 'spring-petclinic-vets-service']
                    }

                    for (service in changedServicesList) {
                        dir(service) {
                            echo "Adding JaCoCo plugin to ${service}..."
                            
                            sh '''
                                if ! grep -q "<build>" pom.xml; then
                                    # Thêm thẻ <build> nếu chưa tồn tại
                                    sed -i '/<\\/dependencies>/a\\
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
                                    </build>' pom.xml
                                fi
                            '''
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