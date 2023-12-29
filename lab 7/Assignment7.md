# Assignment 7 指南

## `******注意，实验结束请立即删除云主机、UFS文件存储，节省费用******` 

## `******注意2，实验未结束且短期内不会继续实验，也请删除所有上述资源。下次实验时重新创建******` 


##  实验内容

- 创建文件存储: `实验步骤 一)` 
- 创建云主机，并挂载文件存储：`实验步骤 二)`
- 在水杉码园创建一个仓库，并下载至文件存储：`实验步骤 三)`
- 拉取python镜像，在容器内测试环境: `实验步骤  四)`
- 使用python容器训练识别MNIST手写数字的神经网络，并将所有内容同步到水杉码园：`实验步骤 五)`

## 实验要求

- 完成所有步骤，并在实验报告（[模板下载](file/assignment7/学号-实验七.docx))中完成穿插在本指南中的作业1～作业5）。实验报告上传至https://send2me.cn/WHPPHFRE/S4-CtxqCKQ6qig
- 实验报告上传deadline： `11月10日23:59`

## 使用UCloud产品 

云主机UHost、文件存储UFS、镜像库UHub、私有网络VPC、基础网络UNet

## 需要权限

云主机UHost、文件存储UFS、镜像库UHub、基础网络UNet


## 基础知识


`MNIST:` MNIST是一个手写数字数据库，包含60000个训练样本和10000个测试样本，是一个能够快速上手的、用于尝试机器学习和模式识别技术的数据集。


## 实验步骤

### 一）创建一个文件存储

#### 1）在产品->存储中选择“文件存储UFS”，然后点击创建文件系统。

#### 2）如下图，存储类型选择SSD性能型，100GB(新版本最低只能设置为500G)，按时付费。

<kbd>
  <img src="img/assignment7/ass7-createufs.png">
</kbd>

#### 3）创建完毕后，如下图所示在弹窗中点击确定设置挂载点，接着选择一个VPC网络，使得相应的子网是DefaultNetwork，点击确定。这样我们等一下在DefaultNetwork下面创建一个云主机，就能把这个文件存储挂载到云主机上。

<kbd>
  <img src="img/assignment7/setGuazai.png">
</kbd>

<kbd>
  <img src="img/assignment7/vpc.png">
</kbd>

#### 4）点击“管理挂载”，查看挂载信息，记住文件存储所在的ip地址，第二）步中我们把这个文件存储挂载到云主机上。

<kbd>
  <img src="img/assignment7/guazai.png">
</kbd>
<kbd>
  <img src="img/assignment7/guazaiIP.png">
</kbd>

## `**************作业1：请将含有文件存储ip地址信息的页面截图，并插入实验报告***************`


### 二）将文件存储挂载到云主机上，使得它在逻辑上成为云主机的一个分区

#### 1）创建一个1核2G（1G可能不够！！！）的云主机(后续需要用到docker，可从带docker的镜像创建主机），绑定弹性IP，按时付费（这个云主机必须在文件存储所挂载的子网中，否则无法和文件存储通信）

#### 2）登录云主机，安装NFS

```
sudo yum install -y nfs-utils
```

NFS（Network File System）是一个能够使得本地主机访问远程主机文件系统的应用程序。因为步骤一）创建的文件存储对于当前的云主机来讲是一个远程存储（网络存储），使用NFS协议才能将其挂载到当前云主机上。

#### 3）在云主机上挂载文件存储，挂载点为/mnt

```
sudo mount -t nfs4 你的文件存储IP地址:/ /mnt
```

#### 4）运行如下命令查看当前云主机的文件系统

```
df -hT
```

你应该看到如下图所示内容

<kbd>
  <img src="img/assignment7/ass7-df.png">
</kbd>


## `**************作业2：请将df -hT的运行后界面截图，并插入实验报告***************`



### 三）在水杉码园创建一个仓库，并下载至文件存储

#### 1）登录[水杉在线](https://www.shuishan.net.cn/)，并从水杉在线门户进入“水杉码园”。创建一个仓库mnist（你也可以用其他命名，但后续操作请做相应修改），创建完毕后，找到你的仓库ssh地址，备用

<kbd>
  <img src="img/assignment7/ass7-createrepo1.png">
</kbd>
<kbd>
  <img src="img/assignment7/ass7-createrepo2.png">
</kbd>

#### 2）在云主机上安装git，并配置一下，对应于自己水杉码园的用户名和邮箱

```
sudo yum install -y git
git config --global user.name "51255903039"
git config --global user.email "51255903039@stu.ecnu.edu.cn"
```

#### 3）生成云主机密钥，使用密钥访问水杉码园

```
ssh-keygen -t rsa -C '51255903039@stu.ecnu.edu.cn'
```
不用在提示符中输入任何内容，连摁回车，密钥即生成。可以在~/.ssh/下看到你生成的两个密钥，id_rsa是私钥，id_rsa.pub是公钥。如果你使用root账号，密钥在/root/.ssh/目录下。接下来我们要把公钥给码园，以后从这台云主机访问码园，云主机会把私钥提供给码园进行身份验证。

#### 4）打印并复制公钥的全部内容

```
cat ~/.ssh/id_rsa.pub
```

复制屏幕上出现的公钥内容

#### 5）在码园中创建公钥，并粘贴上述公钥内容

<kbd>
  <img src="img/assignment7/ass7-key1.png">
</kbd>
<kbd>
  <img src="img/assignment7/ass7-key2.png">
</kbd>
<kbd>
  <img src="img/assignment7/ass7-key3.png">
</kbd>


#### 6）在云主机上运行如下命令，取消码园密码访问

```
eval 'ssh-agent -s'
exec ssh-agent bash
ssh-add ~/.ssh/id_rsa
ssh -T git@gitea.shuishan.net.cn
```

如果你看到类似如下输出，说明密钥访问设置成功

<kbd>
  <img src="img/assignment7/ass7-key4.png">
</kbd>

#### 7）将mnist仓库下载到文件储存

```
cd /mnt
sudo mkdir mnist
sudo chown xuesong:xuesong mnist   //更改mnist文件夹拥有者（即你的云主机登录账号）。假如你使用root账号，这步不需要
cd mnist
git init
git pull git@gitea.shuishan.net.cn:luxuesong_dase_ecnu_edu_cn/mnist.git   //将pull后面的内容替换成你仓库的ssh地址
```

#### 8）在mnist下面新建三个目录code，data，output，下一个步骤中会使用。创建完毕后，你的mnist文件夹应该有如下结构。

<kbd>
  <img src="img/assignment7/ass7-tree.png">
</kbd>

在步骤四）和五）中，我们将代码放在code文件夹中，数据放在data中，模型放在output中


## `**************作业3：请在mnist目录下运行ls -la命令并截图，插入实验报告***************`


### 四）使用docker拉取python镜像，并进入容器运行python
#### 1）运行docker，登录ucloud的镜像仓库，输入ucloud密码
```
docker login uhub.service.ucloud.cn -u 707661163@qq.com  //换成你的ucloud登录邮箱
```

#### 2）拉取python镜像
```
docker pull uhub.service.ucloud.cn/cloud_computing/python:latest
```

#### 3）运行容器，并进入bash
```
docker run -it uhub.service.ucloud.cn/cloud_computing/python /bin/bash
```

#### 4）测试python环境，查看已有包
```
python -V
pip list
```

## `**************作业4：请将进入容器测试python页面截图，插入实验报告***************`


### 五）使用python容器训练MNIST识别模型，最后将所有内容同步到水杉码园

#### 1）按ctrl+d可退出容器，进入主机/mnt/mnist/data目录，下载mnist数据集。

```
wget https://storage.googleapis.com/tensorflow/tf-keras-datasets/mnist.npz
```

#### 2）此时让我们先把UFS中的这些文件push一把，同步到码园中。在/mnt/mnist下，运行

```
git remote add origin git@gitea.shuishan.net.cn:luxuesong_dase_ecnu_edu_cn/mnist.git   //替换成你的码园仓库
git add .
git commit -m "xuesong's first commit"
git push origin master
```

没有报错则成功push，去水杉码园查看你的仓库验证。

#### 3）下载mnist训练代码[mnist.py](file/assignment7/mnist.py)，把它放在/mnt/mnist/code/目录下。

#### 4）进入python容器依次安装训练所需的包。
```
pip install -U numpy==1.16.4 -i https://pypi.tuna.tsinghua.edu.cn/simple
pip install matplotlib -i https://pypi.tuna.tsinghua.edu.cn/simple
pip install tensorflow==1.14.0 -i https://pypi.tuna.tsinghua.edu.cn/simple
```

#### 5）准备好数据、代码、环境，开始训练。因为/mnt/mnist目录在容器外部，所以运行时需要将此目录与容器内部的目录进行映射。
```
docker run -v /mnt/mnist:/home/mnist -it uhub.service.ucloud.cn/cloud_computing/python /bin/bash
```

## `**************作业5：上述代码训练的模型，在测试集上精度较低（如下图），请把测试集上的精度提升到95%以上（即运行model.evaluate(x_test,  y_test)后，accurray在95%以上），将运行结果截图并插入实验报告***************`

> 提示：你可以尝试增加epoch，也可以尝试更换优化器，其他优化器有Adagrad, RMSprop, Adam等


#### 6）保存训练代码，并push到水杉码园中。

```
git add .
git commit -m "commit source code and model"
git push origin master
```
