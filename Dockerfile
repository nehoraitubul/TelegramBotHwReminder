FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . /app

RUN javac -cp "lib/*" -d . $(find . -name "*.java")

CMD ["java", "src.main.java.TelegramBotAAC.Main"]