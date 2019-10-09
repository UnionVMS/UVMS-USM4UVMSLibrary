pipeline {
    agent any
	options {
	  buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '2', numToKeepStr: '5')
	  skipStagesAfterUnstable()
	}
	parameters { string(name: 'DEPLOY_ENV', defaultValue: 'staging', description: '') }
	stages{
		 stage('Clean repo') {
			steps{
		        // remove any snapshot, anything in the local repo that we might build 
		        // or artifacts that other modules of the project provide
		        // these will be downloaded again from the repo making it explicit
		        sh """
		        find .repo -name '*SNAPSHOT' -exec rm -rf {} + \
			    && rm -rf .repo/repository/eu/europa/ec/fisheries \
			    && rm -rf .repo/repository/eu/europa/ec/mare \
			    && rm -rf .repo/repository/fish/focus
			    """
		   }
		}
	   	stage('Clone') {
			steps{
	        // Checkout from GitHub repository
	        checkout scm
	   		}
	   	}
	   
	   	stage('Maven Build') {
	       steps{
	       		// Run the maven build
		      	withMaven(
		          // Maven installation declared in the Jenkins "Global Tool Configuration"
		          maven: 'M3',
		          // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
		          // We recommend to define Maven settings.xml globally at the folder level using
		          // navigating to the folder configuration in the section "Pipeline Maven Configuration / Override global Maven configuration"
		          // or globally to the entire master navigating to  "Manage Jenkins / Global Tools Configuration"
		          mavenSettingsConfig: 'sword-settings',
		          // use a local repo
		          mavenLocalRepo: '.repo') {
		          	sh 'mvn -Dclean deploy -DaltDeploymentRepository=swordnexus-repo-snapshot::default::http://nexus:8081/repository/unionvms-snapshots/'
		      	}
	      	}
	   	}
    }
}