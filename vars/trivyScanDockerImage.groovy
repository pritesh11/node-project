#!/usr/bin/env groovy

def call(String imageName) {
  sh '''
	trivy image ${imageName}
  '''	
}
