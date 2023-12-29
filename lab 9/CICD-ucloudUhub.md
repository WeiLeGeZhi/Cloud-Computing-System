# 基于 ucloud 云主机和阿里云镜像仓库实现 GitHub 的 CI/CD 

GitHub Actions是GitHub的一个持续集成和持续交付（CI/CD）的平台，可以自动化构建、测试和部署Pipelines。

#### Workflow（工作流） 

Workflow其实就是一个可配置的自动化过程，会运行一个或多个Job。Workflow定义在仓库的 .github/workflows文件夹的一个YML或者YAML文件中，并在仓库的Event触发时运行，也可以配置成手动触发运行，或者定时触发。一个Workflow中甚至可以引用另一个Workflow。

可以简单理解，一个YML配置文件就是一个Workflow，仓库可以在.github/workflows文件夹下拥有多个YML文件，即拥有多Workflows。每个Workflow可以执行一组不同的步骤。例如，一个仓库中有两个Workflows，就可以有一个Workflow用来构建和测试PR（Pull Request），另一个Workflow用来每次发布Release版本时自动部署应用。

#### Event（事件）

Event是仓库中的一个用来触发Workflow运行的特殊活动，有不同类型，如某人创建了一个PR、新建了一个Issue等，这些都是可以用来触发Workflow运行的Event。

#### Job（工作）

Job是Workflow中的一组步骤，在同一个Runner（运行者）上运行。每个步骤要么是将执行的Shell脚本，要么是将运行的Action。各步骤是按顺序执行的，并且相互依赖。由于各步骤是在同一个Runner上运行，因此可以将数据从一个步骤共享到另一个步骤。可以配置Job与其他Job的依赖关系；默认情况下，Job之间没有依赖关系，并且彼此并行运行。当一个Job依赖于另一个Job时，它将等待从属Job完成，然后才能运行。

#### Action

Action是GitHub Actions平台的自定义应用程序，用于执行复杂但经常重复的任务。Action可以减少在Workflow的YML文件中编写的重复代码。Action可以从GitHub上拉取仓库代码，为构建环境设置正确的工具链。我们可以编写自己的Action，也可以在GitHub Marketplace中寻找适合使用的Action。

#### Runner（运行者）

Runner是在Workflow被触发时运行它们的服务器。每个Runner一次可以运行一个Job。GitHub提供了Ubuntu Linux、Windows和macOS Runner来运行Workflow。每个Workflow都在全新的预先配置好的虚拟机中执行。如果我们需要不同的操作系统或者特定的硬件配置，可以用自己托管的Runner来代替GitHub提供的Runner。

## 实验目的和实验环境

#### 【实验目的】

（1）了解 GitHub Actions。

（2）初步了解 Workflow 语法并自行构建。

（3）实现 Spring Cloud 项目全自动化打包部署。

（4）熟悉整个 CI/CD 的原理。

#### 【实验环境】

（1）CentOS：7.9 及以上

（2）Docker：20.10.7 版。

（3）docker-compose：1.29.2 版，build 5becea4c。

（4）maven 3.9.5

（5）java openjdk-1.8.0

（6）本次实验建议使用 vscode 连接云主机，这样就可以直接在云主机上编辑，而不用总是上传文件。（这个可以自行上网学习，很方便，参考文档 https://blog.csdn.net/m0_49448331/article/details/126030161）

## 实验步骤

#### 1．复制项目代码

将 https://github.com/OpenEduTech/DaseDevOps/tree/master 复制到自己的账户下后，会生成DaseDevops仓库。注意：下面所有图中显示的devops_demo仓库其实对应的是DaseDevops仓库下的dasedevops_spring_demo项目。

![image-20231111152635356](./images/openedu_repo.png)

随后，在自己的云主机任意目录下（我这里是 /root ）执行 git clone 克隆仓库。使用 ssh 克隆。在克隆之前，可能会报没有 git 命令的错误，需要你手动安装一下 `yum install -y git`

请重点检查：是 fork 后的仓库，而不是 OpenEduTech 的仓库！！！不然会无法 push

![image-20231112145610434](./images/clone_my_repo.png)

在云主机上，生成本机密钥，用于免密认证。引号内的部分需要更换成你自己 github 绑定的邮箱。

```
cd .ssh
ssh-keygen -t rsa -C "102155014xx@stu.ecnu.edu.cn"
```

一路回车，待成功生成密钥后，执行命令 `cat id_rsa.pub` 将打印出的公钥复制到 github 中。

在 github 上配置云主机的 ssh key，点击头像，

![image-20231112145800806](./images/ssh_key.png)

密钥 Title 设置为 action，内容复制进去，这样就可以直接在云主机上免密 git push 了。



#### 2．建立服务器A环境

这里使用的是GitHub托管的Runner，因此服务器A的环境其实已经由GitHub处理好了，不需要我们做任何操作。如果使用自托管的Runner，感兴趣的同学可以参考相关网页。

#### 3．建立服务器B环境

首先，创建一个 centOS 云主机，1 核 2G 配置，20M 带宽流量计费，数据盘设置为 50 G

安装Docker(参照https://gitea.shuishan.net.cn/xslu_dase_ecnu_edu_cn/cloud-computing-course/src/branch/master/Assignment2.md)，

安装docker-compose。

##### docker-compose 安装过程：

通过 curl 下载 docker compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

添加可执行权限

```bash
sudo chmod +x /usr/local/bin/docker-compose
```

将文件 copy 到 /usr/bin/ 目录下

```bash
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
```

查看版本

```
docker-compose --version
```

显示以下信息说明安装成功。

![image-20231110221616117](./images/docker_compose.png)

##### 安装 jdk

请注意，本次实验仅支持手动安装，不支持使用 yum 安装！！

浏览器访问 oracle 官网 https://www.oracle.com/java/technologies/downloads/#license-lightbox 下拉到 Java SE8，下载 x64 版本的压缩包

![image-20231112152329365](./images/java8.png)

在云主机的 /usr 目录下创建 java/ 文件夹

```
cd /usr
mkdir java
cd java
```

把下载好的文件上传到 /usr/java 下

因为文件有 100M 大小，建议等待一段时间，等上传完成以后进行后续操作。

解压

```
tar -zxvf jdk-8u391-linux-x64.tar.gz
```

解压以后，配置环境变量

```
vi /etc/profile
```

在文件末尾添加 

```
export JAVA_HOME=/usr/java/jdk1.8.0_391
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib
```

更新环境变量

```
source /etc/profile
```

输入 java -version 应该可以看到安装成功的消息。

![image-20231112155129640](./images/java_version.png)

##### 安装 maven

浏览器访问 maven 的下载页面 https://maven.apache.org/download.cgi

点击链接下载

![image-20231112145016939](./images/maven.png)

之后上传到 ucloud 云主机的 /usr/local 目录下，进入 /usr/local 目录，解压

```bash
tar -xvf apache-maven-3.9.5-bin.tar.gz
```

执行 `vi /etc/profile` 配置环境变量

```
MAVEN_HOME=/usr/local/apache-maven-3.9.5
export PATH=${MAVEN_HOME}/bin:${PATH}
```

重载环境变量

```
source /etc/profile
```

查看是否安装成功，输入 `mvn -v` 命令，显示相关信息

![image-20231112151248101](./images/maven_version.png)

由于 maven 官方源在国内速度比较慢，我们需要配置阿里 maven 源。cd 到 maven 的安装目录下

修改 conf 文件夹下的 settings.xml

```
sudo vi conf/settings.xml
```

修改 mirrors 下没有被注释的那一部分

![image-20231112165227354](./images/maven_mirror.png)

我这边是已经修改过的，同学们需要根据以下代码修改原有的代码

```
    <mirror>
      <id>alimaven</id>
      <mirrorOf>central</mirrorOf>  
      <name>aliyun maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>      
    </mirror>
```

需要把原有的 `<block>true</block>` 去掉

#### 4．建立 ~~ucloud~~ 阿里云容器镜像服务

本来是想通过 ucloud 使用容器镜像服务的，但是奈何 ucloud 镜像仓库无法通过 github action 的 workflow 进行登录并且操作。无奈之下只能使用阿里云的 [aliyun/acr-login](https://github.com/aliyun/acr-login) 进行 yml 文件登录到 docker 的操作。

为什么不能通过 ucloud 创建容器镜像服务然后再通过登录阿里云镜像仓库然后把镜像推送到 ucloud 中呢？不行。就像 github 中无法直接把自己的代码推送到别人的仓库中一样。

所以为了完成本次实验，需要麻烦大家再在阿里云上创建一个容器镜像服务。

首先，访问 https://www.aliyun.com/ 点击右上角的 “注册/登录” 按钮，完成注册与登录操作

![image-20231111104809589](./images/aliyunlogin.png)

可以直接使用支付宝扫码登录

登录成功后，访问实例列表 https://cr.console.aliyun.com/cn-shanghai/instances 点击创建个人实例。

需要通过 RAM 授权，按照流程点击确认即可。

![image-20231111105335421](./images/instances.png)

![image-20231111105406205](./images/personal_instance.png)

创建完个人版实例以后，需要设置 registry 登录密码。

然后点击 命名空间 -> 创建命名空间 -> 给自己的命名空间起一个名字吧

推荐使用自己的姓名拼音之间加 "-" 号进行命名。如果出现重名，就再加一些符号。

![image-20231111105841358](./images/namespace.png)

![image-20231112155904281](./images/personal_page.png)

### **需要把 默认仓库类型 设置为公开。**

## `作业 1： 创建完成以后，进入个人实例-命名空间页面，截图插入实验报告。`

#### 5.接下来，编写本项目的 workflow 文件。在项目根目录下新建 .github/workflows 文件夹将以下代码保存在 docker-publish.yml

```yml
name: SpringCloud CI/CD with Docker
on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]

env:
  MODULE_1: gateway
  MODULE_2: hello
  MODULE_3: login
  MODULE_4: provider_one
  MODULE_5: provider_two
  MODULE_6: provider_three

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
        with:
          distribution: 'temurin'
          java-version: '8'
          cache: 'maven'
      - name: Build with Maven
        run: |
          whoami
          pwd
          cd dasedevops_spring_demo
          mvn -B package --file pom.xml
          pwd
      - name: Login to Aliyun Container Registry (ACR)
        uses: aliyun/acr-login@v1
        with:
          login-server: https://registry.cn-shanghai.aliyuncs.com
          region-id: cn-shanghai  # 3
          username: "${{ secrets.ACR_USERNAME }}"
          password: "${{ secrets.ACR_PASSWORD }}"
      - name: Build and push image
        run: |
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_1:latest ./dasedevops_spring_demo/$MODULE_1
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_1:latest
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_2:latest ./dasedevops_spring_demo/$MODULE_2
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_2:latest
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_3:latest ./dasedevops_spring_demo/$MODULE_3
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_3:latest
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_4:latest ./dasedevops_spring_demo/$MODULE_4
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_4:latest
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_5:latest ./dasedevops_spring_demo/$MODULE_5
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_5:latest
          docker build -t ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_6:latest ./dasedevops_spring_demo/$MODULE_6
          docker push ${{ secrets.ALI_NAMESPACE_URL }}/$MODULE_6:latest
 
      - name: Copy single file to remote
        uses: garygrossgarten/github-action-scp@release
        with:
          local: ./dasedevops_spring_demo/docker-compose.yml
          remote: scp/devops_demo/docker-compose.yml
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USER_NAME }}
          password: ${{ secrets.USER_PASSWORD }}
          port: ${{ secrets.PORT }}
 
  depoly:
    needs: [ build ]
    name: Docker Pull And Docker-compose Run
    runs-on: ubuntu-latest
    steps:
      - name: executing remote ssh commands using password
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USER_NAME }}
          password: ${{ secrets.USER_PASSWORD }}
          port: ${{ secrets.PORT }}
          script: |
            docker-compose -f scp/devops_demo/docker-compose.yml up -d
            docker-compose -f scp/devops_demo/docker-compose.yml stop
            docker-compose -f scp/devops_demo/docker-compose.yml pull
            docker-compose -f scp/devops_demo/docker-compose.yml up -d
            docker image prune -f
```

相关 secrets 解释：

为什么需要用到 github secrets? 我们都知道，很多情况下，项目有一些隐秘信息，不能直接配置在项目内，包括但不仅限于: github token, 各种账号的用户名密码，私钥信息，各种网站的 api key, app key, secret key 等等。这时候就需要用到 secrets。所以根据 secrets 的特性，一经设置就不可查看！所以设置 secrets 时务必仔细确认！

secrets.ALI_NAMESPACE_URL: 对应阿里云容器镜像服务的 “所在地域 + 命名空间” ，我这里是 “registry.cn-shanghai.aliyuncs.com/easonlin-devops-practice” ，前面的 registry.cn-shanghai.aliyuncs.com 是地域，后面的 easonlin-devops-practice 是命名空间。地域和命名空间很重要，在我们登录阿里云 registry 和拉取、推送镜像时都会用到。

![image-20231111163606733](./images/detail_info.png)

secrets.ACR_USERNAME：阿里云容器服务registry的用户名。

secrets.ACR_PASSWORD：阿里云容器服务registry的用户密码。

secrets.HOST：服务器B的IP地址，就是 ucloud 云主机的公有 ip

secrets.USER_NAME：服务器B的登录用户名，默认为 root

secrets.USER_PASSWORD：服务器B的登录密码。

secrets.PORT：服务器B的SSH开放端口，默认是22。

这些 secrets都可以在代码仓库的“Setting →Secrets → Actions”界面进行设置，使用自己账号的阿里云容器镜像服务中的参数。

![image-20231111161358868](./images/secrets.png)

新建 secret：点击 New repository secret 按钮

![image-20231111161513709](./images/new_secret.png)

输入 secret name 与 value 即可

![image-20231111161537636](./images/new_secret_detail.png)

随后，进入微服务文件夹 dasedevops_spring_demo 下，进入以下六个文件夹的 Dockerfile 进行编辑。

<img src="./images/files.png" alt="files.png" style="zoom:50%;" />

注意到了吗，这六个文件夹就和 workflow 中的六个 MODULE 命名相同。

```
env:
  MODULE_1: gateway
  MODULE_2: hello
  MODULE_3: login
  MODULE_4: provider_one
  MODULE_5: provider_two
  MODULE_6: provider_three
```

编辑 DockerFile 也是为了分别将这六个 module 打包成镜像，上传到镜像仓库中。这么看，这个 yml 文件是不是也不那么复杂？🐶

gateway微服务的Dockerfile文件内容如下：
**在六个文件夹下分别创建名为dockerfile的文件**

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /gateway.jar
ENTRYPOINT ["java","-jar","/gateway.jar"]
EXPOSE 8080
```

hello微服务的Dockerfile文件内容如下：

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /hello.jar
ENTRYPOINT ["java","-jar","/hello.jar"]
EXPOSE 8001
```

login微服务的Dockerfile文件内容如下：

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /login.jar
ENTRYPOINT ["java","-jar","/login.jar"]
EXPOSE 8000
```

provider_one微服务的Dockerfile文件内容如下：

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /provider_one.jar
ENTRYPOINT ["java","-jar","/provider_one.jar"]
EXPOSE 8666
```

provider_two微服务的Dockerfile文件内容如下：

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /provider_two.jar
ENTRYPOINT ["java","-jar","/provider_two.jar"]
EXPOSE 8667
```

provider_three微服务的Dockerfile文件内容如下：

```dockerfile
FROM openjdk:8
MAINTAINER ningzhicheng
VOLUME /tmp
ADD ./target/*.jar /provider_three.jar
ENTRYPOINT ["java","-jar","/provider_three.jar"]
EXPOSE 8668
```

#### 6.编写 docker-compose.yml 文件

在 dasedevops_spring_demo 下新建一个 docker-compose.yml 文件，将以下内容复制到文件中

```dockerfile
version: "3"

services:
  providerOne:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/provider_one:latest
    ports:
      - "8666:8666"

  gateway:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/gateway:latest
    ports:
      - "8080:8080"

  login:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/login:latest
    ports:
      - "8000:8000"

  hello:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/hello:latest
    ports:
      - "8001:8001"

  providerTwo:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/provider_two:latest
    ports:
      - "8667:8667"

  providerThree:
    image: ${{ secrets.ALI_NAMESPACE_URL }}/provider_three:latest
    ports:
      - "8668:8668"
```

文件目录如下：
![image](./images/schema.png)

随后将所有文件提交到暂存区，创建提交信息，推送提交。

```
git add .
git commit -m "test"
git push
```

查看自己仓库的 Actions 界面

![image-20231112165619307](./images/my_action.png)

## `作业 2： 将成功的截图（示例如下）插入到实验报告中。`

![image-20231112165720555](./images/action_detail.png)

## `作业 3：将阿里云容器镜像服务-个人实例-镜像仓库页面截图，插入实验报告。`

示例如下图，必须包括这六个仓库。

![image-20231112170627251](./images/my_image_repo.png)

## `本次实验已结束，请同学们立即释放所有 ucloud 云主机资源！`

作业请提交到坚果云 https://send2me.cn/OMVsV_xl/QLS56Ze4wBqJ4Q 
