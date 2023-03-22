VERSION 0.7
FROM earthly/dind:ubuntu
WORKDIR /ctd-omega-services
RUN apt-get -y update
RUN apt-get -y install openjdk-11-jdk bash wget libsass-dev

sbt:
    #Scala
    # Defaults if not specified
    ARG sbt_version=1.8.2
    ARG sbt_home=/usr/local/sbt

    # Download and extract from archive
    RUN mkdir -pv "$sbt_home"
    RUN wget -qO - "https://github.com/sbt/sbt/releases/download/v$sbt_version/sbt-$sbt_version.tgz" >/tmp/sbt.tgz
    RUN tar xzf /tmp/sbt.tgz -C "$sbt_home" --strip-components=1
    RUN ln -sv "$sbt_home"/bin/sbt /usr/bin/

    # This triggers a bunch of useful downloads.
    RUN sbt -v sbtVersion

project-files:
    FROM +sbt
    COPY build.sbt ./
    COPY .scalafmt.conf ./
    COPY elasticmq.conf ./
    #COPY src/it/resources/bulk_corp_body_data.json ./
    COPY project project
    # Run sbt for caching purposes.
    #RUN --push --secret GITHUB_TOKEN
    RUN --push --secret GITHUB_TOKEN touch a.scala && sbt compile && rm a.scala
    #SAVE IMAGE --push earthly/examples:integration-project-files

integration-test:
    FROM +project-files
    COPY app app
    COPY conf conf
    COPY public public
    COPY test test
    COPY it it
    RUN mkdir -pv /var/opt/ctd-omega-services
    COPY docker-compose.yml ./
    #RUN --push --secret GITHUB_TOKEN
    WITH DOCKER --compose docker-compose.yml
       RUN --secret GITHUB_TOKEN sbt clean compile it:test
    END
