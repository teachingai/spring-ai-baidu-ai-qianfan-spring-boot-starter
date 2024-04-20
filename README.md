# spring-ai-baidu-ai-qianfan-spring-boot-starter

Spring Boot Starter For Spring AI Implementation Base On 百度千帆大模型平台

### 说明


 > 基于 百度千帆大模型平台 和 Spring AI 的 Spring Boot Starter 实现

### Baidu AI

百度智能云千帆大模型平台（以下简称千帆或千帆大模型平台）是面向企业开发者的一站式大模型开发及服务运行平台。千帆不仅提供了包括文心一言底层模型和第三方开源大模型，还提供了各种AI开发工具和整套开发环境，方便客户轻松使用和开发大模型应用。

支持数据管理、自动化模型SFT以及推理服务云端部署的一站式大模型定制服务，助力各行业的生成式AI应用需求落地。

### Maven

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>spring-ai-baidu-ai-qianfan-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

### Sample

```java

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

}

```

