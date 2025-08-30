FROM chainguard/jre:latest-dev

USER 1000
WORKDIR /app

ARG JAR_NAME
RUN echo ${JAR_NAME}
# Copy as the user (this works if build context has proper permissions)
COPY --chown=1000:1000 ${JAR_NAME} /app/atlas-guide-backend.jar

ENTRYPOINT ["java", "-jar", "/app/atlas-guide-backend.jar"]