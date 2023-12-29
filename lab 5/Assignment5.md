# Assignment 5 指南

## `******注意，实验结束请立即删除云主机和数据仓库，节省费用******` 

## `******注意2，实验未结束且短期内不会继续实验，也请删除云主机和数据仓库。下次实验时重新创建******` 



##  实验内容

- 创建Clickhouse数据仓库实例: `实验步骤 一)` 
- 登录数据仓库，并练习建表、插入、查找等基本操作：`实验步骤 二)`
- 使用clickhouse完成简单的机器学习任务: `实验步骤  三)`

## 实验要求（仔细看）

- 完成所有步骤，并在实验报告（[模板下载](file/assignment5/学号-实验五.docx))中完成穿插在本指南中的作业1～作业4（只需要截图）。实验报告上传至：https://send2me.cn/ynNfA6Q0/RFC4OpEdpqIcNg
- 实验报告上传deadline： `10月27日23:59`

## 使用UCloud产品 

云数据仓库UDW、云主机uhost、私有网络vpc、基础网络unet

## 需要权限

云数据仓库UDW、云主机uhost、基础网络unet


## 基础知识（本实验详细知识会在数据仓库相关课程中学习，这里我们仅简单熟悉一下相关操作，你也可以去Google上Baidu一下相关知识 :ghost: :ghost: :ghost:）


`数据仓库:` data warehouse（缩写DW），是用于报告和数据分析的数据管理系统。

`和SQL数据库的区别:` 传统的关系型数据库（如实验四中的MySQL）主要应用于基本的、日常的事务处理（Transaction Processing），例如银行交易，淘宝购物，订购车票等。而数据仓库系统主要应用于生产数据的整理和分析（Analytical Processing），通过扩展数据分析和可视化等工具，支持业务决策，例如流量分析、用户画像、统计建模等。

举个简单例子，淘宝前端（网页端、app端）每天的交易事务，都是用关系型数据库支持的。而每天的交易数据，会定时导入到后端的数据仓库（俗称ETL，Extract, Transform, Load），并根据特定业务逻辑重新整理，从而方便后端各个团队分析数据和做出业务决策。

<kbd>
  <img src="img/assignment5/ass5-etl.jpg">
</kbd>


`Clickhouse数据仓库:` 是由俄罗斯公司Yandex开发的一种列式数据库管理系统，也可以用作数据仓库。它以高性能、高并发和高可扩展性而闻名，能够处理PB级别的数据集，并支持实时查询和分析。

与传统的关系型数据库管理系统（如MySQL和PostgreSQL）不同，Clickhouse采用列式存储方式，将每个列单独存储在硬盘上，而不是按行存储。这种存储方式使得Clickhouse在数据压缩、查询性能、批量插入等方面具有优势，适用于大数据量的实时查询和分析场景。

Clickhouse支持多种查询语言，包括SQL、HiveQL和ODBC等，并且可以与各种数据源集成，如Hadoop、Kafka、Elasticsearch等。同时，Clickhouse还具有高度的可扩展性，可以通过添加节点来实现水平扩展，以满足不断增长的数据处理需求。

ClickHouse也内置了许多机器学习算法，例如基于梯度下降的线性回归、逻辑回归、SVM、决策树、随机森林、PCA等。这些算法都是使用ClickHouse的SQL语言编写的，并且可以通过简单的SQL查询进行调用和使用。除了内置的算法，ClickHouse还提供了一些用于机器学习的函数和工具，例如数组函数、向量函数、一些聚合函数等。这些函数可以用于数据清洗、特征工程、数据转换等任务。


闲话少说，我们开始创建一个Clickhouse数据仓库玩玩。:ghost: :ghost: :ghost:

## 实验步骤

### 一）创建Clickhouse数据仓库实例

#### 1）在产品中选择云数据仓库UDW Clickhouse，然后点击创建集群
<kbd>
  <img src="img/assignment5/create1.jpg">
</kbd>

#### 2）配置中，选择版本22.3LTS，云盘容量选择200G，节点个数选择2个节点。子网设成"DefaultNetwork"，记住DB名称，端口，管理员用户名，设置管理员密码。选择按时付费，立即购买。
<kbd>
  <img src="img/assignment5/create2.jpg">
</kbd>

<kbd>
  <img src="img/assignment5/create3.jpg">
</kbd>

<kbd>
  <img src="img/assignment5/create4.jpg">
</kbd>

<kbd>
  <img src="img/assignment5/create5.jpg">
</kbd>

#### 3）支付之后，数仓创建时间从几分钟到十几分钟不等，等状态显示“运行中”，则创建完毕。
<kbd>
  <img src="img/assignment5/create6.jpg">
</kbd>

### 二）登录数据仓库。Clickhouse数据仓库的登录有很多方式，比如用clickhouse客户端登录，用JDBC（在Java中使用）或者ODBC（在C/C++中使用）访问，用clickhouse-driver（在Python中使用）访问。本实验我们完成clickhouse客户端和clickhouse-driver两种方式。其余方式同学们以后可以自行尝试。

#### 1）使用Clickhouse客户端登录。创建一个最低配置的Centos云主机，选择按流量计费，20M带宽，云主机按小时付费。登录云主机以后运行以下命令，下载Clickhouse安装包。

因为网络原因，我们使用阿里云的镜像安装

```
wget https://mirrors.aliyun.com/clickhouse/rpm/lts/clickhouse-client-22.3.6.5.noarch.rpm
wget https://mirrors.aliyun.com/clickhouse/rpm/lts/clickhouse-common-static-22.3.6.5.x86_64.rpm
```
##### 安装
```
rpm -ivh clickhouse-common-static-22.3.6.5.x86_64.rpm
rpm -ivh clickhouse-client-22.3.6.5.noarch.rpm
```
##### 1.1）安装完毕后运行以下命令登录数据仓库。将相关参数替换成你的数据仓库参数。
```
clickhouse-client --host=<下图任一节点IP地址> --port=9000 --user=admin --password=<创建集群时设置的密码>
```
<kbd>
  <img src="img/assignment5/adress.jpg">
</kbd>

> 登录后将看到如下界面
<kbd>
  <img src="img/assignment5/login.jpg">
</kbd>
## `**************作业1：请将登录命令和登录成功界面截图，并插入实验报告***************`

##### 1.2）让我们运行几个SQL代码来实现建表，插入，查询等操作。
> 创建数据库`ck_test`

```sql
CREATE DATABASE IF NOT EXISTS ck_test ON CLUSTER ck_cluster;
```

注：`on cluster ck_cluster`表示在双副本模式下建库/建表

> 创建表`regression`
```
CREATE TABLE ck_test.regression ON CLUSTER ck_cluster 
(
   id Int32,
   y Int32,
   x1 Int32,
   x2 Int32
)
ENGINE = ReplicatedMergeTree(
  '/clickhouse/ck_test/tables/{layer}-{shard}/regression',
  '{replica}'
) ORDER BY (id);
```
> ClickHouse支持各种数据类型，包括基本数据类型、日期时间类型、文本类型、枚举类型等，还支持自定义数据类型，以满足特定的需求。

> 接着让我们在regression表中插入一些值
```
INSERT INTO ck_test.regression VALUES 
   (1,  5, 2, 3),
   (2, 10, 7, 2),
   (3,  6, 4, 1),
   (4,  8, 3, 4);
```

> 让我们查询一下regression表中的数据。你应该看到如下输出。
```
SELECT * FROM ck_test.regression;
```

<kbd>
  <img src="img/assignment5/select_result.jpg">
</kbd>

##### 1.3）运行\q退出clickhouse客户端。

## `**************作业2：请如上图一样将regression表的查询命令、输出结果和退出clickhouse以后的界面截图，并插入实验报告***************`


#### 2）使用`Python`+`clickhouse-driver`模块访问。很多时候我们需要在程序中访问数据仓库，比如用Python读取DW中的数据，然后进一步操作。clickhouse-driver是一个开源的Python模块，它提供了一个Python DB API 2.0兼容的接口，可以通过Python程序连接和操作ClickHouse数据库。
运行以下命令安装python环境
```
sudo yum install python3
sudo yum install python3-pip
```
安装clickhouse-driver，这里我们需要用阿里云镜像安装
```
pip3 install --user -i https://mirrors.aliyun.com/pypi/simple clickhouse-driver
```

可能会报未找到 gcc 命令的错误，运行 `sudo yum install -y gcc` 就好~

##### 2.1）安装完毕后运行python3，然后import clickhouse_driver，若没有报错，则说明clickhouse_driver安装成功。
<kbd>
  <img src="img/assignment5/run_python.jpg">
</kbd>

##### 2.2）quit()退出python3命令行，让我们运行几个Python代码来实现建表，插入，查询等操作。

> 建立createTable.py文件（若忘记创建文件命令可参考[实验一](https://gitea.shuishan.net.cn/xslu_dase_ecnu_edu_cn/cloud-computing-course/src/branch/master/Assignment1.md)），并复制如下代码。其中username，password，hostIP，port都要替换成你的数据仓库参数。
```python
#!/usr/bin/env python 
# _*_ coding:utf-8 _*_

import clickhouse_driver

# 建立连接
conn = clickhouse_driver.connect(database="ck_test", user="admin", password="Linlinshe1", host="10.9.100.182", port="9000")

# 创建游标
cur = conn.cursor()

# 创建表
cur.execute('CREATE TABLE COMPANY ON CLUSTER ck_cluster \
																	(ID Int32 NOT NULL,\
                                   NAME String NOT NULL,\
                                   AGE Int16 NOT NULL,\
                                   ADDRESS String,\
                                   SALARY Float64, \
                                   PRIMARY KEY (ID))  \
           												 ENGINE = MergeTree()    \
																	 ORDER BY (ID);')
# 提交事务
conn.commit()

# 关闭连接
conn.close()
```

> 运行createTable.py，如果没有报错，则说明建表成功。
```
python3 createTable.py
```

> 向COMPANY表中添加一些记录。建立insertTable.py文件，复制如下代码并运行。
```python
#!/usr/bin/env python 
# _*_ coding:utf-8 _*_

import clickhouse_driver

# 建立连接
conn = clickhouse_driver.connect(database="ck_test", user="admin", password="Linlinshe1", host="10.9.100.182", port="9000")

cur = conn.cursor()
cur.execute("INSERT INTO COMPANY VALUES (1, 'Paul', 32, 'California', 20000.00 )");
cur.execute("INSERT INTO COMPANY VALUES (2, 'Allen', 25, 'Texas', 15000.00 )");
cur.execute("INSERT INTO COMPANY VALUES (3, 'Eric', 35, 'Florida', 25000.00 )");
conn.commit()
conn.close()
```

> 让我们查询一下刚刚建的表。建立selectTable.py文件，复制如下代码并运行。
```python
#!/usr/bin/env python 
# _*_ coding:utf-8 _*_

import clickhouse_driver

# 建立连接
conn = clickhouse_driver.connect(database="ck_test", user="admin", password="Linlinshe1", host="10.9.100.182", port="9000")
cur = conn.cursor()
cur.execute("SELECT ID, NAME, ADDRESS, SALARY  from COMPANY order by ID")
rows = cur.fetchall()
print ("ID      NAME      ADDRESS      SALARY")
for row in rows:
    print (row[0], "\t", row[1], "\t", row[2], "\t", row[3])
conn.close()
```

> 你应该会看到如下输出。

<kbd>
  <img src="img/assignment5/selectTabel.jpg">
</kbd>

> 以上只是一个简单的示例，clickhouse-driver还提供了更多的功能和选项，如使用参数化查询、执行多个查询、事务处理等。你可以在官方文档中了解更多信息：https://clickhouse-driver.readthedocs.io/en/latest/index.html。

## `**************作业3：请将运行selectTable.py的命令和输出结果截图，并插入实验报告***************`


### 三）使用clickhouse完成简单的机器学习任务（线性回归）

#### 1）前面我们说过，Clickhouse也可用于搭建机器学习模型。这里我们用二）中的regression表跑一下线性回归算法。请使用clickhouse或者clickhouse-driver登录数仓后，并运行如下命令。

##### 1.1）确认训练数据

```
select * from ck_test.regression
```
<kbd>
  <img src="img/assignment5/data.jpg">
</kbd>
> 这里我们看到数据量很少，因此`regression`同时是训练集和测试集。
> 使用了MADlib的线性回归模型linregr_train来对regression表中的数据进行训练，模型输入变量为x1, x2以及偏置项，输出变量为y。训练好的模型保存在regression_model表中。

### 2）训练线性回归模型，并将模型参数存入`linear_model`表中。

```
CREATE TABLE linear_model ENGINE = Memory AS SELECT stochasticLinearRegressionState(0.01, 0.1, 2, 'SGD')(y, x1, x2) AS state FROM ck_test.regression;
```

> 模型输出变量为y,输入变量为x1, x2。`stochasticLinearRegressionState`的四个参数分别为：

1. learning rate
2. l2 regularization coefficient
3. mini-batch size
4. method for updating weights

> 如果要查看训练好的模型参数，则使用以下方式训练模型

```
SELECT stochasticLinearRegression(0.01, 0.1, 2)(y, x1, x2) FROM ck_test.regression;
```

<kbd>
  <img src="img/assignment5/parameter.jpg">
</kbd>

### 3）最后让我们用这个模型去做预测（前提是已将模型参数保存在表中）。我们直接在训练集上进行预测。
```
 WITH (SELECT state FROM linear_model) AS model SELECT evalMLMethod(model, x1, x2) FROM ck_test.regression
```
> 你会看到如下输出，即模型对四组输入的预测结果
<kbd>
  <img src="img/assignment5/result.jpg">
</kbd>

### 4）运行\q退出clickhouse客户端。

## `**************作业4：请如上图一样把模型参数和预测结果的界面一起截图，并插入实验报告***************`
