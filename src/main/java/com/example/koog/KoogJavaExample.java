package com.example.koog;

import ai.koog.agents.core.agent.AIAgent;
import ai.koog.prompt.executor.clients.openai.OpenAIModels;
import ai.koog.prompt.executor.llms.all.SimplePromptExecutorsKt;

public class KoogJavaExample {
    public static void main(String[] args) {
        // APIキーを環境変数から取得（例: OPENAI_API_KEY）
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("OPENAI_API_KEY環境変数を設定してください。");
            return;
        }

        AIAgent<String, String> agent = AIAgent.builder()
                .promptExecutor(SimplePromptExecutorsKt.simpleOpenAIExecutor(apiKey))
                .systemPrompt("You are a helpful assistant. Answer user questions concisely.")
                .llmModel(OpenAIModels.Chat.GPT4o)
                .build();

        String response = agent.run("Hello Koog from Java!");
        System.out.println("Agent response: " + response);
    }
}