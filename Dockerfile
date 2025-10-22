# =========================
# 🧠 Base: Python + Java runtime
# =========================
FROM continuumio/miniconda3 AS runtime

# 设置工作目录
WORKDIR /app

# 拷贝并创建 Python 环境（一次性完成，减少层数）
# environment.yml 示例：
# name: bankModel
# dependencies:
#   - python=3.10
#   - geopandas
#   - shapely
#   - json
#   - argparse
COPY environment.yml /tmp/environment.yml

# 创建 conda 环境
RUN conda env create -f /tmp/environment.yml && \
    conda clean -afy

# 设置 PATH，默认激活 bankModel 环境
ENV PATH /opt/conda/envs/bankModel/bin:$PATH

# 安装 OpenJDK 17
RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-17-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 复制 Spring Boot 打包文件和 Python 脚本
COPY backEnd-0.0.1-SNAPSHOT.jar /app/
COPY scripts /app/scripts

# 设置默认激活的 Spring Boot 配置文件
ENV SPRING_PROFILES_ACTIVE=deploy

# 暴露端口
EXPOSE 9876

# 启动 Spring Boot 应用
ENTRYPOINT ["bash", "-c", "java -jar backEnd-0.0.1-SNAPSHOT.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
