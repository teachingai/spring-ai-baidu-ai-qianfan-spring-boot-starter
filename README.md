# spring-ai-baidu-ai-qianfan-spring-boot-starter

 > 基于 [百度千帆大模型平台](https://cloud.baidu.com/product/wenxinworkshop.html) 和 Spring AI 的 Spring Boot Starter 实现

### 百度千帆大模型平台

> 百度智能云千帆大模型平台（以下简称千帆或千帆大模型平台）是面向企业开发者的一站式大模型开发及服务运行平台。千帆不仅提供了包括文心一言底层模型和第三方开源大模型，还提供了各种AI开发工具和整套开发环境，方便客户轻松使用和开发大模型应用。

支持数据管理、自动化模型SFT以及推理服务云端部署的一站式大模型定制服务，助力各行业的生成式AI应用需求落地。

- 官网地址：[https://techday.sensetime.com](https://techday.sensetime.com)
- API文档：[https://platform.sensenova.cn/doc?path=/chat/GetStarted/APIList.md](https://platform.sensenova.cn/doc?path=/chat/GetStarted/APIList.md)
- 模型更新: [https://platform.sensenova.cn/release?path=/release-202404.md](https://platform.sensenova.cn/release?path=/release-202404.md)
- 体验中心: [https://platform.sensenova.cn/trialcenter](https://platform.sensenova.cn/trialcenter)

#### 支持的功能包括：

- 支持文本生成（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持函数调用（Function Calling）：用户传入各类自定义工具，自动选择并调用工具，准确度达到99%
- 支持文本嵌入（Embeddings）
- 支持图片生成（Image Generation API）
- 支持助手API（Assistants API）
- 支持 code interpreter功能：自动生成Python代码解决数学问题，降低直接数值计算错误，提升数学解题能力。在公开数学测评数据集上逼近GPT-4 Turbo的水平

#### 资源

- [查看模型列表](https://platform.sensenova.cn/doc?path=/chat/Models/GetModelList.md)
- [平台动态](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Dlfmc9dxj)
- [API 介绍](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/flfmc9do2)

#### 模型

百度智能云千帆大模型平台 支持对话Chat、续写Completions、向量Embeddings、模型管理、模型服务、模型调优等调用。

##### [对话 Chat](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/xlmokikxe)

百度智能云千帆大模型平台提供了对话Chat相关模型API SDK，支持单轮对话、多轮对话、流式等调用。

##### [续写Completions](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/vlmokjd30)

千帆 SDK 支持调用续写Completions相关API，支持非流式、流式调用。

##### [向量 Embeddings](https://cloud.baidu.com/doc/WENXINWORKSHOP/s/hlmokk9qn)

千帆 SDK 支持调用千帆大模型平台中的模型，将输入文本转化为用浮点数表示的向量形式。转化得到的语义向量可应用于文本检索、信息推荐、知识挖掘等场景。

| 模型            |  描述 |
|---------------| ------------ |
| Embedding-V1	 |   |
| bge-large-zh	 |   |
| bge-large-en  |   |
| tao-8k |   |

### Maven

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>spring-ai-baidu-ai-qianfan-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```


### Sample

使用示例请参见 [Spring AI Examples](https://github.com/TeachingAI/spring-ai-examples)

### License

[Apache License 2.0](LICENSE)
