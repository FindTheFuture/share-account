package cn.lizongyi.shareaccount.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizer {

    // 目标文件大小范围（单位：字节）
    private static final int MIN_SIZE = 250 * 1024; // 250KB
    private static final int MAX_SIZE = 300 * 1024; // 300KB

    // 图片质量（0.0 - 1.0）
    private static final float QUALITY = 0.8f;

    public static void resizeImage(File inputFile, File outputFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);

        // 初始缩放比例
        double scale = 1.0;
        long fileSize;

        do {
            // 计算新的尺寸
            int newWidth = (int) (originalImage.getWidth() * scale);
            int newHeight = (int) (originalImage.getHeight() * scale);

            // 创建缩放后的图片
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // 将图片写入临时文件以检查大小
            File tempFile = File.createTempFile("temp", ".jpg");
            ImageIO.write(resizedImage, "jpg", tempFile);

            // 获取文件大小
            fileSize = tempFile.length();

            // 调整缩放比例
            if (fileSize > MAX_SIZE) {
                scale *= 0.9; // 缩小10%
            } else if (fileSize < MIN_SIZE) {
                scale *= 1.1; // 放大10%
            }

            // 删除临时文件
            tempFile.delete();

        } while (fileSize < MIN_SIZE || fileSize > MAX_SIZE);

        // 最终写入输出文件
        BufferedImage finalImage = new BufferedImage(
                (int) (originalImage.getWidth() * scale),
                (int) (originalImage.getHeight() * scale),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = finalImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, finalImage.getWidth(), finalImage.getHeight(), null);
        g.dispose();

        ImageIO.write(finalImage, "jpg", outputFile);
    }

    public static void main(String[] args) {
        try {
            File inputFile = new File("/Users/lizongyi/Desktop/2.jpg");
            File outputFile = new File("/Users/lizongyi/Desktop/22.jpg");
            resizeImage(inputFile, outputFile);
            System.out.println("图片已成功缩小并保存到: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}