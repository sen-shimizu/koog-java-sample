package com.example.koog;

import ai.koog.agents.core.agent.AIAgent;
import ai.koog.agents.core.agent.AIAgentBuilder;
import ai.koog.agents.core.tools.ToolRegistry;
import ai.koog.prompt.executor.llms.all.SimplePromptExecutorsKt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MultiToolAgent {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String WEATHER_API_KEY = System.getenv("OPENWEATHERMAP_API_KEY");
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) throws Exception {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
            System.err.println("OPENAI_API_KEY が設定されていません。環境変数を設定してください。");
            return;
        }

        if (WEATHER_API_KEY == null || WEATHER_API_KEY.isBlank()) {
            System.err.println("OPENWEATHERMAP_API_KEY が設定されていません。環境変数を設定してください。");
            return;
        }

        ToolRegistry toolRegistry = ToolRegistry.builder()
                .tool(getStaticMethod("getWeather", String.class), null, "getWeather", "指定された都市の天気を取得します")
                .tool(getStaticMethod("translate", String.class), null, "translate", "テキストを指定言語に翻訳します。入力例: Hello, ja")
                .tool(getStaticMethod("calculate", String.class), null, "calculate", "数式を計算します。入力例: 25*12")
                .build();

        AIAgent<String, String> agent = new AIAgentBuilder()
                .promptExecutor(SimplePromptExecutorsKt.simpleOpenAIExecutor(OPENAI_API_KEY))
                .toolRegistry(toolRegistry)
                .systemPrompt("あなたは必要に応じて天気・翻訳・計算のツールを呼び出して応答するマルチツールAIエージェントです。")
                .build();

        try {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("マルチツールAIエージェントへようこそ！（終了するには 'exit' と入力）");

                while (true) {
                    System.out.print("あなた: ");
                    String userInput = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(userInput.trim())) {
                        break;
                    }

                    String response = agent.run(userInput);
                    System.out.println("エージェント: " + response);
                }
            }
        } finally {
            Continuation<Unit> closeContinuation = new Continuation<Unit>() {
                @Override
                public CoroutineContext getContext() {
                    return EmptyCoroutineContext.INSTANCE;
                }

                @Override
                public void resumeWith(Object result) {
                    // close の完了を待たず、例外も無視します
                }
            };
            agent.close(closeContinuation);
        }
    }

    private static Method getStaticMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return MultiToolAgent.class.getMethod(name, parameterTypes);
    }

    public static String getWeather(String city) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = WEATHER_BASE_URL
                    + "?q=" + URLEncoder.encode(city, StandardCharsets.UTF_8)
                    + "&appid=" + WEATHER_API_KEY
                    + "&units=metric&lang=ja";

            HttpGet request = new HttpGet(url);
            String json = client.execute(request, response -> EntityUtils.toString(response.getEntity()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root.has("main") && root.has("weather")) {
                double temp = root.get("main").get("temp").asDouble();
                String desc = root.get("weather").get(0).get("description").asText();
                return String.format("%sの現在の気温は%.1f℃、天気は%sです。", city, temp, desc);
            } else {
                return "天気情報を取得できませんでした。";
            }
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }

    public static String translate(String input) {
        String[] parts = input.split(",", 2);
        if (parts.length < 2) {
            return "入力形式: <テキスト>,<言語コード>";
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String encodedText = URLEncoder.encode(parts[0].trim(), StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q="
                    + encodedText
                    + "&langpair=auto|"
                    + parts[1].trim();

            HttpGet request = new HttpGet(url);
            String json = client.execute(request, response -> EntityUtils.toString(response.getEntity()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode translatedText = root.path("responseData").path("translatedText");
            if (translatedText.isTextual()) {
                return translatedText.asText();
            }
            return "翻訳失敗";
        } catch (Exception e) {
            return "翻訳エラー: " + e.getMessage();
        }
    }

    public static String calculate(String expression) {
        try {
            double result = new org.mariuszgromada.math.mxparser.Expression(expression).calculate();

            if (Double.isNaN(result)) {
                return "計算できませんでした。";
            }

            return expression + " = " + result;
        } catch (Exception e) {
            return "計算エラー: " + e.getMessage();
        }
    }
}
