# Segmentation Service

Сервис для управления сегментами пользователей.

### Запуск сервиса

1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/team-eight-brown/segmentaion-back
   cd segmentaion-back
   ```
2. Соберите

   ```bash
   ./gradlew clean build'
   ```

3. Запустите Docker Compose
   ```bash
   docker-compose up --build
    ```

4. Запустите сервис одним из способов:
- В среде разработки (например, IntelliJ IDEA): запустите основной класс приложения.
- Через консоль: выполните команду
   ```bash
   java -jar build/libs/segmentation-0.0.1-SNAPSHOT.jar 
   ```

После успешного запуска проекта документация API будет доступна в Swagger UI по адресу:

[http://localhost:8090/swagger-ui/index.html#/](http://localhost:8090/swagger-ui/index.html#/)
