package com.github.idelstak.metadata.components;

import javafx.scene.image.*;

import java.nio.*;

public class ImageComparator {

    private final Image img1;
    private final Image img2;

    public ImageComparator(Image img1, Image img2) {
        this.img1 = img1;
        this.img2 = img2;
    }

    public boolean equals() {
        if (img1 == img2) {
            return true;
        }

        if (img1 == null || img2 == null) {
            return false;
        }

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }

        PixelReader reader1 = img1.getPixelReader();
        PixelReader reader2 = img2.getPixelReader();

        int width = (int) img1.getWidth();
        int height = (int) img1.getHeight();

        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        byte[] buffer1 = new byte[width * 4];
        byte[] buffer2 = new byte[width * 4];

        for (int y = 0; y < height; y++) {
            reader1.getPixels(0, y, width, 1, format, buffer1, 0, width * 4);
            reader2.getPixels(0, y, width, 1, format, buffer2, 0, width * 4);

            for (int x = 0; x < width * 4; x++) {
                if (buffer1[x] != buffer2[x]) {
                    return false;
                }
            }
        }

        return true;
    }
}