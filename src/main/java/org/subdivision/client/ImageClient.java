package org.subdivision.client;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.subdivision.DivideStrategy;
import org.subdivision.Image;
import org.subdivision.RenderMethod;
import org.subdivision.RenderProperties;
import org.subdivision.tool.Configuration;
import org.subdivision.tool.ImageDisplayComponent;
import org.subdivision.tool.Util;

public class ImageClient {

    Image image;

    public static void main(String[] args) throws Exception {
        RenderProperties renderProperties = new RenderProperties(
                0.4f, 
                1, 
                RenderMethod.STROKE, 
                DivideStrategy.CENTER,
                false, 
                false, 
                true);
        progressiveImageGeneration("dc2.jpg", renderProperties);
    }

    private static void progressiveImageGeneration(String name, RenderProperties renderProperties) throws IOException {

        Image img = new Image(Configuration.getInstance().getFullInputImageFileName(name), renderProperties);

        generateImage(img, name);
    }

    private static void generateImage(Image img, String name) {
        
        ImageDisplayComponent id = new ImageDisplayComponent(img);
        ImageClient d = new ImageClient(img);
        d.display(id);

        Thread t = new Thread(() -> {
            while (img.render(id))
                ;
            id.repaint();
            Util.saveImageTo(img, Configuration.getInstance().getFullOutputDir(name), name);
            System.out.println("Finished");
        });
        t.start();
    }

    public ImageClient(Image image) {
        super();
        this.image = image;
    }

    public void display(ImageDisplayComponent id) {
        JFrame w = new JFrame();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        System.out.println(screenBounds);
        w.setBounds(0, 0, (int) Math.min(screenBounds.getWidth(), image.width),
                (int) Math.min(screenBounds.getHeight() * 0.1, image.height));

        JScrollPane jsp = new JScrollPane(id, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        w.getContentPane().add(jsp, BorderLayout.CENTER);
        w.pack();
        w.setVisible(true);
    }

}
