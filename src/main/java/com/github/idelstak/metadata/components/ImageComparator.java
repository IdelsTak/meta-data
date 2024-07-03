/*
 * MIT License
 *
 * Copyright (c) 2024 Hiram K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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