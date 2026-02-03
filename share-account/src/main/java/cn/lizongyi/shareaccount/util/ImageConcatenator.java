package cn.lizongyi.shareaccount.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageConcatenator {

    /**
     * 水平拼接三张图片
     *
     * @param imagePaths 图片文件路径数组
     * @param outputFilePath 输出文件路径
     * @throws IOException 如果读取或写入图片时发生错误
     */
    public static void concatImagesHorizontally(String[] imagePaths, String outputFilePath) throws IOException {
        if (imagePaths.length != 3) {
            throw new IllegalArgumentException("需要提供三张图片");
        }

        // 读取三张图片
        BufferedImage[] images = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            images[i] = ImageIO.read(new File(imagePaths[i]));
        }

        // 获取每张图片的高度和宽度
        int height = images[0].getHeight();
        int totalWidth = 0;
        for (BufferedImage img : images) {
            totalWidth += img.getWidth();
        }

        // 创建一个新的BufferedImage来存储拼接后的图片
        BufferedImage concatenatedImage = new BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_ARGB);

        // 将三张图片拼接到新的BufferedImage中
        int x = 0;
        for (BufferedImage img : images) {
            concatenatedImage.getGraphics().drawImage(img, x, 0, null);
            x += img.getWidth();
        }

        // 保存拼接后的图片
        File outputFile = new File(outputFilePath);
        ImageIO.write(concatenatedImage, "jpeg", outputFile);
    }

    public static void main(String[] args) {
        try {
            String[] imagePaths = {
                    "/Users/lizongyi/Desktop/test/1.jpeg",
                    "/Users/lizongyi/Desktop/test/2.jpeg",
                    "/Users/lizongyi/Desktop/test/3.jpeg"
            };
            String outputFilePath = "/Users/lizongyi/Desktop/test/网站建设方案书-扫描.jpeg";
            concatImagesHorizontally(imagePaths, outputFilePath);
            System.out.println("图片拼接完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}