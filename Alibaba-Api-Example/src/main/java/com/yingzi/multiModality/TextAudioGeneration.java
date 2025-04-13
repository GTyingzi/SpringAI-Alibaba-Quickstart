package com.yingzi.multiModality;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author yingzi
 * @date 2025/4/1:10:43
 */
public class TextAudioGeneration {


    static class Message {
        String role;
        Content[] content;
        public Message(String role, Content[] contents) {
            this.role = role;
            this.content = contents;
        }
    }

    static class Content {
        String type;
        String text;
        InputAudio input_audio;
        public Content(String type, String text, InputAudio inputAudio) {
            this.type = type;
            this.text = text;
            this.input_audio = inputAudio;
        }
        public Content(String type, String text) {
            this.type = type;
            this.text = text;
        }
        public Content(String type, InputAudio inputAudio) {
            this.type = type;
            this.input_audio = inputAudio;
        }
    }

    static class InputAudio {
        String data;
        String format;
        public InputAudio(String data, String format) {
            this.data = data;
            this.format = format;
        }
    }
    static class StreamOptions {
        boolean include_usage;
        public StreamOptions(boolean includeUsage) {
            this.include_usage = includeUsage;
        }
    }

    static class Audio {
        String voice;
        String format;
        public Audio(String voice, String format) {
            this.voice = voice;
            this.format = format;
        }
    }

    static class RequestBody {
        String model;
        Message[] messages;
        boolean stream;
        StreamOptions stream_options;
        List<String> modalites;
        Audio audio;
        public RequestBody(String model, Message[] messages, boolean stream, StreamOptions streamOptions, List<String> modalites, Audio audio) {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
            this.stream_options = streamOptions;
            this.modalites = modalites;
            this.audio = audio;
        }

    }

    private static final String URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen-omni-turbo";
    private static final String MP3_URL = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";


    public static void main(String[] args) throws IOException {
        RequestBody requestBody = new RequestBody(
                MODEL,
                new Message[] {
                        new Message(
                                "system",
                                new Content[] {
                                        new Content("text", "You are a helpful assistant.")
                                }
                        ),
                        new Message(
                                "user",
                                new Content[] {
                                        new Content("input_audio", new InputAudio(
                                                MP3_URL,
                                                "mp3"
                                        )),
                                        new Content("text", "这段音频在说什么")
                                }
                        )
                },
                true,
                new StreamOptions(true),
                List.of("text", "audio"),
                new Audio("Cherry", "way")
        );

        // 将请求体转换为 JSON
        Gson gson = new Gson();
        String jsonInputString = gson.toJson(requestBody);

        // 创建URL对象
        URL obj = new URL(URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法为POST
        con.setRequestMethod("POST");

        // 若没有配置环境变量，请用百炼API Key将下行替换为：String apiKey = "sk-xxx";
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        String auth = "Bearer " + apiKey;

        // 设置请求头
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", auth);
        con.setRequestProperty("Cookie", "acw_tc=01a132c7-e8a9-91d1-b0ca-1fae2e7d5ec012a37177dee494164ef6685578571380");

        // 启用输入输出流
        con.setDoOutput(true);

        // 写入请求体
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应体
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                System.out.println(responseLine);
            }
        }

    }
}