package com.pdf.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

@RestController
public class PdfController {
    @RequestMapping("/pdf")
    public void pdf(HttpServletResponse response) {
        String pdfUrl = "http://www.1.com/1.pdf";
        String imageType = "PNG";//可改成JPG
        OutputStream sos = null;
        try {
            sos = response.getOutputStream();
            PDFToImg(sos, pdfUrl, 1, imageType);
            sos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sos.close();
            } catch (IOException e) {
                System.out.println("关闭输出流异常: " + e.getMessage());
            }
        }
    }

    /**
     * PDF转图片 根据页码一页一页转
     *
     * @throws IOException imgType:转换后的图片类型 jpg,png
     */
    private void PDFToImg(OutputStream sos, String fileUrl, int page, String imgType) throws Exception {
        PDDocument pdDocument = null;
        /* dpi越大转换后越清晰，相对转换速度越慢 */
        int dpi = 200;
        try {
            pdDocument = getPDDocument(fileUrl);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            int pages = pdDocument.getNumberOfPages();
            if (page <= pages && page > 0) {
                BufferedImage image = renderer.renderImageWithDPI(page - 1, dpi);
                ImageIO.write(image, imgType, sos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF转换异常");
        } finally {
            if (pdDocument != null) {
                pdDocument.close();
            }
        }

    }

    private PDDocument getPDDocument(String fileUrl) throws IOException {
        InputStream inputStream = getInputStreamByUrl(fileUrl);
        return PDDocument.load(inputStream);
    }


    /**
     * 根据url地址 获取输入流
     *
     * @param url
     * @return
     * @throws IOException
     */
    private InputStream getInputStreamByUrl(String url) throws IOException {
        URL httpurl = new URL(URLDecoder.decode(url, "UTF-8"));
        InputStream is;
        HttpURLConnection httpConn = (HttpURLConnection) httpurl.openConnection();
        httpConn.setDoOutput(true);// 使用 URL 连接进行输出
        httpConn.setDoInput(true);// 使用 URL 连接进行输入
        httpConn.setUseCaches(false);// 忽略缓存
        httpConn.setRequestMethod("GET");// 设置URL请求方法
        //可设置请求头
        httpConn.setRequestProperty("Content-Type", "application/octet-stream");
        httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
        httpConn.setRequestProperty("Charset", "UTF-8");
        httpConn.connect();
        if (httpConn.getResponseCode() >= 400) {
            is = httpConn.getErrorStream();
        } else {
            is = httpConn.getInputStream();
        }
        return is;
    }
}
