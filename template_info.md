# 源码自动生成模板 spring-boot

### 概述

* 模板: spring-boot
* 模板使用时间: 2020-02-12 19:31:53

### Docker
* Image: registry.cn-beijing.aliyuncs.com/code-template/spring-boot
* Tag: 20190731
* SHA256: b2d9b97c2b1a6f9b7571d9aad3aab64ce069a8b6a4b226bda0e84bc80c948aca

### 用户输入参数
* repoUrl: "git@code.aliyun.com:75452-mobile-app/adv_sdk_android.git" 
* needDockerfile: "N" 
* appName: "adv_sdk_android" 
* operator: "aliyun_822658" 
* appReleaseContent: "# 
* 请参考: 请参考 
* https://help.aliyun.com/document_detail/59293.html: https://help.aliyun.com/document_detail/59293.html 
* 了解更多关于release文件的编写方式: 了解更多关于release文件的编写方式 
* [NEWLINE][NEWLINE]#: [NEWLINE][NEWLINE]# 
* 构建源码语言类型[NEWLINE]code.language: oracle-jdk1.8[NEWLINE][NEWLINE]# 
* 构建打包使用的打包文件[NEWLINE]build.output: target/adv_sdk_android.jar" 

### 上下文参数
* appName: adv_sdk_android
* operator: aliyun_822658
* gitUrl: git@code.aliyun.com:75452-mobile-app/adv_sdk_android.git
* branch: master


### 命令行
	sudo docker run --rm -v `pwd`:/workspace -e repoUrl="git@code.aliyun.com:75452-mobile-app/adv_sdk_android.git" -e needDockerfile="N" -e appName="adv_sdk_android" -e operator="aliyun_822658" -e appReleaseContent="# 请参考 https://help.aliyun.com/document_detail/59293.html 了解更多关于release文件的编写方式 [NEWLINE][NEWLINE]# 构建源码语言类型[NEWLINE]code.language=oracle-jdk1.8[NEWLINE][NEWLINE]# 构建打包使用的打包文件[NEWLINE]build.output=target/adv_sdk_android.jar"  registry.cn-beijing.aliyuncs.com/code-template/spring-boot:20190731

