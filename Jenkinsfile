#!/usr/bin/env groovy

/*
 * Jenkins pipeline file for deploying kubernetes app on the k8s/minikube cluster
 * 
 */

properties ([
        buildDiscarder(logRotator(daysToKeepStr: '5', numToKeepStr: '5')),
        pipelineTriggers([cron('15 00 * * *')])
])

ansiColor('') {
        timestamps {
                try {
			@Library('node-project')_

                        stage ('Build images') {
                                node("master") {
                                        codeGitCheckout()
					buildDockerImage()
					trivyScanDockerImage 'priteshvaviya11/node-project:latest'
					pushDockerImageOnHub()
                                }
                        }

                        stage ('Deploy') {
				//In real life scenario this can be the node running the kubernetes cluster
                                node("master") {
					launchK8SResouces()
                                }
                        }

                        currentBuild.result = "SUCCESS"
							
                } catch (err) {
                        currentBuild.result = "FAILURE"
                        throw err
                } 

        }
}

/*
 * Get whats changed in source control from the current build, and format in a string
 * Format is:
 * 	<author> <timestamp>, <commit-message>:
 *		<file-path> (<change-type>)
 *
 */
@NonCPS
def getChangeLogAsFormattedString() {
        def changeLog = ""
        def changeLogSets = this.currentBuild.changeSets
        for (int i = 0; i < changeLogSets.size(); i++) {
                def entries = changeLogSets[i].items
                for (int j = 0; j < entries.length; j++) {
                        def entry = entries[j]
                        changeLog += "\n${entry.author} on ${new Date(entry.timestamp)}, ${entry.msg}: \n"
                        def files = new ArrayList(entry.affectedFiles)
                        for (int k = 0; k < files.size(); k++) {
                                def file = files[k]
                                changeLog += "\t${file.path} (${file.editType.name})\n"
                        }
                }
        }
        return changeLog
}

/*
 * returns true if changes where on the dev branch
 *
 */
def isDevelopBranch() {
	return env.BRANCH_NAME.equalsIgnoreCase("dev")
}


/*
 * Check out the code git repo into the folder code. It takes the top from the branch that kicked off the build
 *
 */
def codeGitCheckout () {
        def scmVars = checkout([
                $class: 'GitSCM',
                branches: scm.branches,
                extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "code"]] + scm.extensions,
                userRemoteConfigs: scm.userRemoteConfigs
        ])
    return scmVars.GIT_BRANCH
}


def webAppBuild(String projectName){
	def versionNumber = ""
	def releaseArgs = ""
	try {
		dir ("code") {
			sh '''
				mkdir -p tmp
				PACKAGE_VERSION=$(node -p -e "require('./package.json').version")
				BUILD_VERSION=${PACKAGE_VERSION%%-*}.${BUILD_NUMBER}
				echo PACKAGE_VERSION=${PACKAGE_VERSION} > tmp/versions.props
				echo BUILD_VERSION=${BUILD_VERSION} >> tmp/versions.props
			'''
			def props = readProperties file: "tmp/versions.props"
			def packageVersion = props["PACKAGE_VERSION"]
			versionNumber = props["BUILD_VERSION"]
		
			sh '''
				rm -rf www
				npm i
				npm run build-prod
			'''
		}
		dir ('code/') {
			withEnv(["PROJECT=${projectName}","VERSION=${versionNumber}"]) {
	      sh 'tar -cvf "${PROJECT}"-"${VERSION}".tar www'
      }
			stash includes: "${projectName}-${versionNumber}.tar", name: "${projectName}-${versionNumber}"
		}
		currentBuild.result = "SUCCESS"
	} catch (err) {
		currentBuild.result = "FAILURE"
		throw err
	}
	return versionNumber
}

/*
 *  Deploy the web application
 *
 */
def deployWebAppManual (String projectName, String versionNumber, String testMailList){
	try {
		dir ('code/deploy') {
			withEnv(["JENKINS_NODE_COOKIE=dontKillMe", "PROJECT=${projectName}","BRANCH=${env.BRANCH_NAME}", "VERSION=${versionNumber}"]) {
				sh 'mkdir -p scratch'
				unstashDeployables projectName, "scratch", "config", versionNumber
				sh './deploy.sh "${PROJECT}" "${VERSION}" "${BRANCH}"'
			}
		}
		currentBuild.result = "SUCCESS"
	} catch (err) {
		currentBuild.result = "FAILURE"
		throw err
	}

}

def buildDockerImage(){
	try{
		dir ("code") {
                        sh 'docker build -t priteshvaviy11/node-project:latest .'
		}

		currentBuild.result = "SUCCESS"
        } catch (err) {
                currentBuild.result = "FAILURE"
                throw err
        }

}


def pushDockerImageOnHub(){
        try{
                dir ("code") {
                        sh 'docker push priteshvaviy11/node-project:latest'
                }

                currentBuild.result = "SUCCESS"
        } catch (err) {
                currentBuild.result = "FAILURE"
                throw err
        }

}

def pushDockerImageOnHub(){
        try{
                dir ("code/kubernetes") {
                        sh '''
				kubectl create -f node-project.yml
				kubectl create -f ingress.yml
			'''
                }

                currentBuild.result = "SUCCESS"
        } catch (err) {
                currentBuild.result = "FAILURE"
                throw err
        }

}

