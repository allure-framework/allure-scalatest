pipeline {
    agent { label 'java' }
    parameters {
        booleanParam(name: 'RELEASE', defaultValue: false, description: 'Perform release?')
        string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Release version')
        string(name: 'NEXT_VERSION', defaultValue: '', description: 'Next version (without SNAPSHOT suffix)')
    }
    stages {
        stage('Build') {
            steps {
                sh "${tool name: '0.13', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt \"compile\""
            }
        }
        stage('Release') {
            when { expression { return params.RELEASE } }
            steps {
                configFileProvider([configFile(fileId: 'global.sbt', targetLocation: '/home/jenkins/.sbt/0.13/global.sbt')]) {
                    configFileProvider([configFile(fileId: 'plugins.sbt', targetLocation: '/home/jenkins/.sbt/0.13/plugins/plugins.sbt')]) {
                        configFileProvider([configFile(fileId: 'sonatype.sbt', targetLocation: '/home/jenkins/.sbt/0.13/sonatype.sbt')]) {
                            configFileProvider([configFile(fileId: 'gpg.sbt', targetLocation: '/home/jenkins/.sbt/0.13/gpg.sbt')]) {
                                configFileProvider([configFile(fileId: 'qameta-ci.asc', targetLocation: '/home/jenkins/.gnupg/qameta-ci.asc')]) {
                                    sshagent(['qameta-ci_ssh']) {
                                        sh 'git checkout master && git pull origin master'
                                        sh "${tool name: '0.13', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt \"release " +
                                                "with-defaults " +
                                                "release-version ${RELEASE_VERSION} " +
                                                "next-version ${NEXT_VERSION}-SNAPSHOT\""
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}