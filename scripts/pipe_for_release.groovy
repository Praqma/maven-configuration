properties([parameters([booleanParam(defaultValue: false, description: '', name: 'isRelease')])])

//TODO: We could put this in a shared library
def buildMaven(args = [:]) {
    git credentialsId: args.credentialsId ?: 'github', url: args.repo
    docker.image(args.buildImage ?: "maven:3.5.3-jdk-8").inside("-v maven-repo:/root/.m2") {
        sh args.buildCommand ?: "mvn clean package"
    }    
}

//TODO: We could put this in a shared library
def releaseMaven(args = [:]) {
    sh 'curl https://raw.githubusercontent.com/Praqma/maven-info/master/settings.xml -O'
    withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'passRelease', usernameVariable: 'userRelease'), string(credentialsId: 'jenkins-artifactory', variable: 'RELEASE_PW')]) {
        docker.image(args.buildImage ?: "maven:3.5.3-jdk-8").inside("-v maven-repo:/root/.m2") {
            sh 'git config user.email "release@praqma.net" && git config user.name "Praqma Release User"'
            sh 'mvn clean release:prepare release:perform -B -s settings.xml -Dusername=$userRelease -Dpassword=$passRelease'
        }            
    }    
}

stage("build") {
    node("dockerhost1") {
        buildMaven(repo: 'https://github.com/Praqma/drmemory-plugin.git', buildCommand: 'mvn clean package')
    }
}

stage("release") {
    if(!params?.isRelease) {
        echo "Release build is not enabled"
    } else {
        echo "Release build enabled. Running release."
        node("dockerhost1") {
            releaseMaven()
        }
    }
}