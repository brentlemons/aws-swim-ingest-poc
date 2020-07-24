#!/usr/bin/env bash

while getopts u:t:r: option
  do
  case "${option}"
  in
    u) GitHubUsername=${OPTARG};;
    t) GitHubToken=${OPTARG};;
    r) GitHubRepoName=${OPTARG};;
  esac
done

# Configuration
StackName=${GitHubRepoName}-pipeline
TemplateFile=packaged-${StackName}-template.yaml
BucketName=${StackName}

aws s3 mb s3://${BucketName}

# Package and deploy
aws cloudformation package \
--template-file service.yaml \
--s3-bucket ${BucketName} \
--output-template-file ${TemplateFile}

aws cloudformation deploy \
--stack-name ${StackName} \
--template-file ${TemplateFile} \
--parameter-overrides \
"GitHubUsername=${GitHubUsername}" \
"GitHubToken=${GitHubToken}" \
"GitHubRepoName=${GitHubRepoName}" \
--s3-bucket ${BucketName} \
--capabilities CAPABILITY_IAM