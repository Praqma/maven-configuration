stage("build") {
    node("dockerhost1") {
        git credentialsId: 'github', url: 'https://github.com/Praqma/drmemory-plugin.git'
        docker.image("maven").inside("-v maven-repo:/root/.m2") {
            sh 'mvn clean package'
        }
    }
}


/**
    Requirements
        1. HTTPS developer and SCM connections in .pom

    TODOs:
        1. We need to not delete the /root folder every time (deletes the volume we just mounted)
        2. We need to use a specific version of the maven docker image
**/
stage("release") {
    node("dockerhost1") {
        sh 'curl https://raw.githubusercontent.com/Praqma/maven-info/master/settings.xml -O'
        withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'passRelease', usernameVariable: 'userRelease'), string(credentialsId: 'jenkins-artifactory', variable: 'RELEASE_PW')]) {
            docker.image("maven").inside("-v maven-repo:/root/.m2") {
                sh 'git config user.email "release@praqma.net" && git config user.name "Praqma Release User"'
                sh 'mvn clean release:prepare release:perform -B -s settings.xml -Dusername=$userRelease -Dpassword=$passRelease'
            }            
        }
    }
}