FROM registry.bst-1.cns.bstjpc.com:5000/openjdk:8

WORKDIR workdir

RUN curl -LO http://minio.gcloud.srcb.com/download-center/spc-cli/0.3.0.tp4/spc && \
  chmod +x spc && \
  mv ./spc /usr/local/bin/spc

COPY clouddriver-web/build clouddriver-web/build

RUN dpkg -i ./clouddriver-web/build/distributions/*.deb \
&& rm -rf ./*

CMD ["/opt/clouddriver/bin/clouddriver"]
