# spring-ai

- https://docs.spring.io/spring-ai/reference/

## ollama
- https://ollama.com/
- https://hub.docker.com/r/ollama/ollama

```shell
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama

docker exec -it ollama ollama run mistral
#OR
docker exec -it ollama ollama run llama3.1

```
**Run Application**

```shell
./mvnw clean spring-boot:run
```

**Test Application**

```shell
http :8080/1/hr-agent prompt=="get me details for user id 2"
```
