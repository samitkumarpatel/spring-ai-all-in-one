# spring-ai

- [Reference Example](https://spring.io/blog/2025/05/20/your-first-spring-ai-1)

- [Reference](https://docs.spring.io/spring-ai/reference/)

Spring AI is a framework (in spring eco system) designed to integrate artificial intelligence capabilities into Java applications, particularly those built with the Spring ecosystem. Here are some key points about Spring AI and its purpose:


- **Seamless Integration with Spring Ecosystem:** Spring AI extends the Spring framework, allowing developers to incorporate AI functionalities like chat, embeddings, and function calling using familiar Spring concepts such as dependency injection and annotations.

- **Support for Multiple AI Providers:** It offers a portable API that supports major AI model providers, including OpenAI, Anthropic, Microsoft, Amazon, and Google, enabling flexibility in choosing and switching between different AI services.

- **Vector Store Integration:** Spring AI integrates with various vector databases like PostgreSQL/PGVector, Redis, and Milvus, facilitating efficient storage and retrieval of embeddings for applications such as semantic search and Retrieval Augmented Generation (RAG).

- **Tool/Function Calling:** It allows AI models to invoke user-defined functions or tools, enabling dynamic interactions with external systems and real-time data processing within AI workflows.

- **Simplified AI Integration:** By leveraging Spring Boot's auto-configuration and starter dependencies, Spring AI simplifies the process of adding AI capabilities to applications without requiring extensive configuration or new paradigms.

Support for Generative AI Patterns: Spring AI provides features to address common challenges in generative AI applications, such as context management, structured output, and evaluation of AI-generated content to mitigate issues like hallucinations.

Overall, Spring AI aims to make AI integration more accessible and manageable for Java developers by providing a cohesive set of tools and abstractions that align with the established practices of the Spring framework.


## ollama
- https://ollama.com/
- https://hub.docker.com/r/ollama/ollama

```shell
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama

docker exec -it ollama ollama run llama3

```
