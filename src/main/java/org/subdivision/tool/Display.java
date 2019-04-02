package org.subdivision.tool;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.AWTUtil;
import org.subdivision.DivideStrategy;
import org.subdivision.Image;
import org.subdivision.RenderMethod;
import org.subdivision.RenderProperties;

public class Display {

    Image image;

    public static void main(String[] args) throws Exception {
        RenderProperties renderProperties = new RenderProperties(0.4f, 1, RenderMethod.STROKE, DivideStrategy.CENTER,
                false, false, true);
        progressiveImageGeneration("dc2.jpg", renderProperties);
        // iterativeImageGeneration(0.9f, 0.1f, 100, 1, 20);
        // movieGeneration("jedi.mp4");

    }

    private static void movieGeneration(String fileName) throws FileNotFoundException, IOException, JCodecException {

        ImageDisplayComponent id = null;
        Display d = null;

        String videoFileName = Configuration.getInstance().getFullVideoFileName(fileName);
        File file = new File(videoFileName);

        var ch = NIOUtils.readableFileChannel(videoFileName);
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
        DemuxerTrack video_track = demuxer.getVideoTrack();
        System.out.println("video_duration: " + video_track.getMeta().getTotalFrames());
        System.out.println("video_duration: " + video_track.getMeta().getTotalDuration());

        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

        double startSec = 20;
        int frameCount = video_track.getMeta().getTotalFrames();

        // grab.seekToSecondPrecise(startSec);

        float treshold = 0.3f;
        int detail = 16;
        RenderProperties renderProperties = new RenderProperties(treshold, (int) detail, RenderMethod.STROKE,
                DivideStrategy.CENTER, false, false, true);

        String videoOutputFileName = getFileNameFrom(fileName) + "_" + getDateString() + ".MP4";

        String fullVideoOutputFileName = Configuration.getInstance().getFullOutputDir(fileName) + "/"
                + videoOutputFileName;

        Image img = null;

        SeekableByteChannel out = null;
        BufferedImage toOutput = null;
        AWTSequenceEncoder encoder = null;
        int frame = 0;
        try {

            TreeMap<Double, BufferedImage> reorderBuffer = new TreeMap<Double, BufferedImage>();

            out = NIOUtils.writableFileChannel(fullVideoOutputFileName);

            encoder = new AWTSequenceEncoder(out, Rational.R(25, 1));

            long startTime = System.currentTimeMillis();

            do {
                var picture = grab.getNativeFrameWithMetadata();
                // for JDK (jcodec-javase)
                if (picture != null) {
                    BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture.getPicture());
                    reorderBuffer.put(picture.getTimestamp(), bufferedImage);
                    // System.out.println("reorderbuffer:" + reorderBuffer);

                    // System.out.println(picture.getTimestamp() + ", " + picture.getDuration());
                }

                // System.out.println(picture.getPicture().getWidth() + "x" +
                // picture.getPicture().getHeight() + " " + picture.getPicture().getColor());

                // Allow for 5 frames in reorder buffer, that will cover the most common case of
                // I-B-B-B-P
                if (picture == null || reorderBuffer.size() >= 5) {
                    double leastPts = reorderBuffer.keySet().iterator().next();
                    toOutput = reorderBuffer.remove(leastPts);

                    frame++;

                    img = new Image(toOutput, renderProperties);

                    if (id == null) {
                        id = new ImageDisplayComponent(img);
                        d = new Display(img);
                        d.display(id);
                    }
                    id.setImage(img);

                    // g2.drawString("Hello",10,10);
                    displayImage(id);
                    /*
                     * Graphics2D g2 = (Graphics2D) id.image.output.getGraphics(); //g2.setFont(new
                     * Font("TimesRoman", Font.PLAIN, 20)); g2.setColor(Color.WHITE);
                     * //g2.fillRect(0,0,500,500); //g2.setColor(Color.RED);
                     * //g2.drawImage(toOutput, 0, 0, null); g2.drawString("Time:"+leastPts,20,20);
                     */
                    id.repaint();

                    // Generate the image, for Android use Bitmap
                    BufferedImage outputImage = id.getImage().output;
                    // Encode the image
                    encoder.encodeImage(outputImage);
                    long currentTime = System.currentTimeMillis();

                    float ellapsedTime = (float) (currentTime - startTime) / 1000.0f;
                    float ellapsedFrames = (float) frame;

                    float OneFrameTime = ellapsedTime / ellapsedFrames;

                    // System.out.println("oneframetime:"+OneFrameTime+", ellapsedtime:
                    // "+ellapsedTime +", ellapsedFrames:"+ellapsedFrames);

                    int remainingFrames = frameCount - frame;

                    float remainingTime = remainingFrames * OneFrameTime;

                    Duration remainingDuration = Duration.ofSeconds((long) remainingTime);

                    System.out.format("Frame: %d of %d (%2.2f%%, remaining: %s)\n", frame, frameCount,
                            ((float) frame / (float) frameCount) * 100.0f, remainingDuration);

                    // System.out.println("Frame " + i + " encoded");
                }
            } while (!reorderBuffer.isEmpty());

            // Finalize the encoding, i.e. clear the buffers, write the header, etc.
            encoder.finish();
            System.out.println("Encoding finished!");

        } finally {

            NIOUtils.closeQuietly(out);
        }

    }

    private static void progressiveImageGeneration(String name, RenderProperties renderProperties) throws IOException {

        Image img = new Image(Configuration.getInstance().getFullInputImageFileName(name), renderProperties);

        generateImage(img, name);
    }

    private static void iterativeImageGeneration(float startTreshold, float endTreshold, float startDetail,
            float endDetail,

            int steps

    ) throws IOException {
        String[] fileNames = { "model.jpg", "bridge.jpg", "dog.jpg", "night.jpg", "mona.jpg", "test.png",
                "area_subdivision.png", "lake.jpg", "Botticelli_Venus.jpg", "eifel2.jpg", "daddario.jpg", "pearl.jpg",
                "12.jpg", "3.jpg", "jedi.png" };

        ImageDisplayComponent id = null;
        Display d = null;

        float tresholdDelta = (endTreshold - startTreshold) / (float) steps;
        float treshold = startTreshold;

        float detail = startDetail;
        float detailDelta = (endDetail - startDetail) / (float) steps;
        System.out.println("detail delta:" + detailDelta);

        RenderProperties renderProperties = new RenderProperties(treshold, (int) detail, RenderMethod.STROKE,
                DivideStrategy.CENTER, false, false, true);

        String fileName = fileNames[14];

        String prefix = getFileNameFrom(fileName) + "_" + getDateString();

        Image img = new Image(Configuration.getInstance().getFullInputImageFileName(fileName), renderProperties);

        for (int i = 0; i < steps; i++) {

            if (id == null) {
                id = new ImageDisplayComponent(img);
                d = new Display(img);
                d.display(id);
            }

            displayAndSaveImage(id, fileName, prefix);

            detail += detailDelta;
            treshold = treshold + tresholdDelta;
            img.reset();

            renderProperties.treshold = treshold;
            renderProperties.minAreaToDivide = (int) detail;
            // img.setRenderProperties(renderProperties);

            System.out.format("\ntreshold: %f, detail: %f", treshold, detail);
        }
    }

    private static String getFileNameFrom(String fileNameWithExtension) {
        String[] tokens = fileNameWithExtension.split("\\.(?=[^\\.]+$)");
        String[] fileNamePath = Util.splitPath(tokens[0]);
        return fileNamePath[fileNamePath.length - 1];
    }

    private static void saveImageTo(Image image, String path, String fileName) {

        String dateStr = getDateString();

        String fileNameWithoutExt = getFileNameFrom(fileName);

        String outputFileName = path + "/" + fileNameWithoutExt + "_" + dateStr + ".png";

        File f = new File(outputFileName);
        f.getParentFile().mkdirs();

        image.saveImage(outputFileName);
    }

    private static String getDateString() {
        var sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String dateStr = sdf.format(new Date());
        return dateStr;
    }

    private static void displayImage(ImageDisplayComponent id) {
        while (id.getImage().render(null))
            ;
        id.repaint();
    }

    private static void displayAndSaveImage(ImageDisplayComponent id, String fileName, String prefix) {

        while (id.getImage().render(null))
            ;
        id.repaint();

        String path = Configuration.getInstance().getFullOutputDir(fileName) + "/" + prefix;
        saveImageTo(id.getImage(), path, fileName);

    }

    private static void generateImage(Image img, String name) {
        // Image img = new Image("C:\\workspace\\SubDivision\\src\\12.jpg", 50, 2);
        ImageDisplayComponent id = new ImageDisplayComponent(img);
        Display d = new Display(img);
        d.display(id);

        Graphics2D g2 = (Graphics2D) id.getImage().output.getGraphics();
        // g2.setColor(new Color(original.color));
        // g2.fillRect(original.bounds.a.x, original.bounds.a.y,
        // original.bounds.getWidth(), original.bounds.getHeight());

        // g2.drawImage(d.image.input, 0, 0, null);
        // id.repaint();

        Thread t = new Thread(() -> {
            while (img.render(id))
                ;
            id.repaint();
            saveImageTo(img, Configuration.getInstance().getFullOutputDir(name), name);
            System.out.println("Finished");
        });
        t.start();
    }

    public Display(Image image) {
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

        // w.setLocationRelativeTo(null);
        JScrollPane jsp = new JScrollPane(id, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // jsp.setPreferredSize(new Dimension(100,100));
        w.getContentPane().add(jsp, BorderLayout.CENTER);
        w.pack();
        w.setVisible(true);
    }

}
