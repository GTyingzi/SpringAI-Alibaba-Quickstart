package com.yingzi.rag.etl.controller;

import com.yingzi.rag.etl.Constant;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yingzi
 * @date 2025/4/18:21:58
 */
@RestController
@RequestMapping("/reader")
public class ReaderController {


    @GetMapping("/text")
    public List<Document> readText() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.TEXT_FILE_PATH);
        TextReader textReader = new TextReader(resource); // 可以传任意类型数据
        return textReader.read();
    }

    @GetMapping("/json")
    public List<Document> readJson() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.JSON_FILE_PATH);
        JsonReader jsonReader = new JsonReader(resource); // 只可以传json格式文件
        return jsonReader.read();
    }

    @GetMapping("/pdf-page")
    public List<Document> readPdfPage() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource); // 只可以传pdf格式文件
        return pagePdfDocumentReader.read();
    }

    @GetMapping("/pdf-paragraph")
    public List<Document> readPdfParagraph() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.PDF_FILE_PATH);
        ParagraphPdfDocumentReader paragraphPdfDocumentReader = new ParagraphPdfDocumentReader(resource); // 有目录的pdf文件
        return paragraphPdfDocumentReader.read();
    }

    @GetMapping("/markdown")
    public List<Document> readMarkdown() {
        MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(Constant.MARKDOWN_FILE_PATH); // 只可以传markdown格式文件
        return markdownDocumentReader.read();
    }

    @GetMapping("/html")
    public List<Document> readHtml() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.HTML_FILE_PATH);
        JsoupDocumentReader jsoupDocumentReader = new JsoupDocumentReader(resource); // 只可以传html格式文件
        return jsoupDocumentReader.read();
    }

    @GetMapping("/tika")
    public List<Document> readTika() {
        Resource resource = new DefaultResourceLoader().getResource(Constant.HTML_FILE_PATH);
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource); // 可以传多种文档格式
        return tikaDocumentReader.read();
    }
}
