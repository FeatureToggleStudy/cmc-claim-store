ARG APP_INSIGHTS_AGENT_VERSION=2.3.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

LABEL maintainer="https://github.com/hmcts/cmc-claim-store"

COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/claim-store.jar /opt/app/

EXPOSE 4400
CMD [ "claim-store.jar" ]
