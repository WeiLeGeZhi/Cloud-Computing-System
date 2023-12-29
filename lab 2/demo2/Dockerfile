# 拉取python镜像作为基本环境
FROM python:latest

# 设置额外信息
LABEL description="Dockerfile Demo for ECNU"

# 在container中运行命令
RUN pip install flask -i https://pypi.tuna.tsinghua.edu.cn/simple

# 将本地目录拷贝到container并且设置为工作目录
ADD ./app.py /opt
WORKDIR /opt

# 将5000端口暴露出来
EXPOSE 5000

# 容器启动的执行命令
CMD ["/bin/bash", "-c", "python app.py"]
