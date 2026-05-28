package com.isoft.consultant.service;

import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用 Apache PDFBox 生成 AI 方案评审 PDF（支持中文嵌入字体）。
 */
@Service
public class PlanReviewPdfService {

    private static final Logger log = LoggerFactory.getLogger(PlanReviewPdfService.class);

    private static final float MARGIN = 48f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float BOTTOM = MARGIN;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final String FOOTER = "智选未来 · 高考志愿助手 · AI 方案评审";

    public byte[] generate(String title, String meta, String content) throws IOException {
        String safeTitle = (title == null || title.isBlank()) ? "AI 方案评审" : title.trim();
        String body = content == null ? "" : content.trim();
        if (body.isEmpty()) {
            throw new IllegalArgumentException("评审内容不能为空");
        }

        try (PDDocument doc = new PDDocument()) {
            PDType0Font font = loadChineseFont(doc);
            PageWriter writer = new PageWriter(doc, font);
            writer.writeBlock(safeTitle, 16, new Color(0x0f, 0x17, 0x2a), 8);
            if (meta != null && !meta.isBlank()) {
                writer.writeBlock(meta.trim(), 10, new Color(0x64, 0x74, 0x8b), 6);
            }
            writer.writeBlock(body, 12, new Color(0x33, 0x41, 0x55), 4);
            writer.writeFooter(9, new Color(0x94, 0xa3, 0xb8));
            writer.finish();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private PDType0Font loadChineseFont(PDDocument doc) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")) {
            if (in != null) {
                return PDType0Font.load(doc, in);
            }
        }
        Path simhei = Path.of("C:/Windows/Fonts/simhei.ttf");
        if (Files.isRegularFile(simhei)) {
            return PDType0Font.load(doc, simhei.toFile());
        }
        Path simsun = Path.of("C:/Windows/Fonts/simsun.ttc");
        if (Files.isRegularFile(simsun)) {
            PDType0Font font = loadFromTtc(doc, simsun.toFile(), "SimSun", "NSimSun");
            if (font != null) {
                return font;
            }
        }
        Path msyh = Path.of("C:/Windows/Fonts/msyh.ttc");
        if (Files.isRegularFile(msyh)) {
            PDType0Font font = loadFromTtc(doc, msyh.toFile(), "Microsoft YaHei", "MicrosoftYaHei");
            if (font != null) {
                return font;
            }
        }
        Path[] more = {
                Path.of("C:/Windows/Fonts/msyhbd.ttc"),
                Path.of("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"),
                Path.of("/System/Library/Fonts/PingFang.ttc"),
        };
        for (Path path : more) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            String name = path.toString().toLowerCase();
            if (name.endsWith(".ttf")) {
                return PDType0Font.load(doc, path.toFile());
            }
            PDType0Font font = loadFromTtc(doc, path.toFile());
            if (font != null) {
                return font;
            }
        }
        throw new IllegalStateException(
                "未找到可用的中文字体。请将 NotoSansSC-Regular.ttf 放入 Backend/src/main/resources/fonts/，"
                        + "或确保系统已安装黑体(simhei.ttf)或微软雅黑。");
    }

    private PDType0Font loadFromTtc(PDDocument doc, File ttcFile, String... preferredNames) throws IOException {
        try (TrueTypeCollection collection = new TrueTypeCollection(ttcFile)) {
            if (preferredNames != null) {
                for (String name : preferredNames) {
                    TrueTypeFont ttf = collection.getFontByName(name);
                    if (ttf != null) {
                        log.info("PDF 字体: {} 来自 {}", name, ttcFile.getName());
                        return PDType0Font.load(doc, ttf, true);
                    }
                }
            }
            final TrueTypeFont[] first = new TrueTypeFont[1];
            collection.processAllFonts(font -> {
                if (first[0] == null) {
                    first[0] = font;
                }
            });
            if (first[0] != null) {
                log.info("PDF 字体: 使用 {} 中首个字体", ttcFile.getName());
                return PDType0Font.load(doc, first[0], true);
            }
        } catch (IOException e) {
            log.warn("加载字体集合失败: {} - {}", ttcFile, e.getMessage());
        }
        return null;
    }

    private List<String> wrapLines(String text, PDType0Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        for (String paragraph : normalized.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < paragraph.length(); i++) {
                char ch = paragraph.charAt(i);
                String trial = current.toString() + ch;
                float width = font.getStringWidth(trial) / 1000f * fontSize;
                if (width > CONTENT_WIDTH && current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder(String.valueOf(ch));
                } else {
                    current.append(ch);
                }
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private final class PageWriter {
        private final PDDocument doc;
        private final PDType0Font font;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        PageWriter(PDDocument doc, PDType0Font font) throws IOException {
            this.doc = doc;
            this.font = font;
            newPage();
        }

        void writeBlock(String text, float fontSize, Color color, float gapAfter) throws IOException {
            List<String> lines = wrapLines(text, font, fontSize);
            float lineHeight = fontSize * 1.55f;
            for (String line : lines) {
                ensureSpace(lineHeight);
                stream.beginText();
                stream.setFont(font, fontSize);
                stream.setNonStrokingColor(color);
                stream.newLineAtOffset(MARGIN, y);
                if (!line.isEmpty()) {
                    stream.showText(line);
                }
                stream.endText();
                y -= lineHeight;
            }
            y -= gapAfter;
        }

        void writeFooter(float fontSize, Color color) throws IOException {
            ensureSpace(fontSize * 2);
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(MARGIN, BOTTOM);
            stream.showText(FOOTER);
            stream.endText();
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < BOTTOM + 20) {
                closeStream();
                newPage();
            }
        }

        private void newPage() throws IOException {
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        void finish() throws IOException {
            closeStream();
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
