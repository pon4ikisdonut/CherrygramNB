FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_HOME=/android-sdk
ENV ANDROID_SDK_ROOT=/android-sdk
ENV PATH=$PATH:$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/36.0.0

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    git \
    bash \
    curl \
    unzip \
    python3 \
    python3-pip \
    imagemagick \
    build-essential \
    file \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

CMD ["bash", "build_and_publish.sh"]
