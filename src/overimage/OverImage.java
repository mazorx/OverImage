package overimage;

import com.sun.glass.events.KeyEvent;
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Buda
 */
public class OverImage extends javax.swing.JFrame {

    int difx = 0;
    int dify = 0;
    boolean scalingx = false;
    boolean scalingy = false;
    boolean scaledragx = false;
    boolean scaledragy = false;
    boolean draging = false;
    int prevx = 0;
    int prevy = 0;
    int clickedx = 0;
    int clickedy = 0;
    int currentcursor = 0;
    Image originalimage;
    Image showingimage;
    Double zoom = 1.0;
    Double scaleimagepercent = 1.0;
    int fliph = 1;
    int flipv = 1;
    static int bordersize = 20;
    static Double bigzoomstep = 0.15;
    static Double zoomstep = 0.02;
    static Double minzoom = 0.01;
    int imageX = 0;
    int imageY = 0;
    int clickedimageX = 0;
    int clickedimageY = 0;
    NumberFormat formatter = new DecimalFormat("#0.00");
    boolean defaultsize = false;
    float imgalpha = 1f;
    float alphastep = 0.1f;

    /**
     * Creates new form OverImage
     */
    public OverImage() {
        initComponents();
        holder.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    //evt.acceptDrop(DnDConstants.ACTION_COPY);
                    evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    imageX = 0;
                    imageY = 0;
                    originalimage = ImageIO.read(droppedFiles.get(0));
                    setimgtosize();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBorder = new javax.swing.JPanel();
        holder = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocation(new java.awt.Point(200, 200));
        setUndecorated(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        panelBorder.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        holder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        holder.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        holder.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                holderMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                holderMouseMoved(evt);
            }
        });
        holder.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                holderMouseWheelMoved(evt);
            }
        });
        holder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                holderMousePressed(evt);
            }
        });

        javax.swing.GroupLayout panelBorderLayout = new javax.swing.GroupLayout(panelBorder);
        panelBorder.setLayout(panelBorderLayout);
        panelBorderLayout.setHorizontalGroup(
            panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(holder, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
        );
        panelBorderLayout.setVerticalGroup(
            panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(holder, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBorder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBorder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if (evt.isControlDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_V) {
                try {
                    imageX = 0;
                    imageY = 0;
                    originalimage = getImageFromClipboard();
                    setimgtosize();
                } catch (Exception e) {
                }
            }
        } else {
            if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                draging = true;
                currentcursor = 13;
                this.setCursor(currentcursor);
            }
            if (evt.getKeyCode() == KeyEvent.VK_PLUS) {
                if (imgalpha + alphastep <= 1) {
                    imgalpha += alphastep;
                } else {
                    imgalpha = 1;
                }
            }
            if (evt.getKeyCode() == KeyEvent.VK_MINUS) {
                if (imgalpha - alphastep >= 0.01) {
                    imgalpha -= alphastep;
                } else {
                    imgalpha = 0.01f;
                }
            }
            if (evt.getKeyCode() == KeyEvent.VK_F) {
                try {
                    fliph *= -1;
                    setimgtosize();
                } catch (Exception e) {
                }
            }
            if (evt.getKeyCode() == KeyEvent.VK_V) {
                try {
                    flipv *= -1;
                    setimgtosize();
                } catch (Exception e) {
                }
            }
            if (evt.getKeyCode() == KeyEvent.VK_T) {
                try {
                    int mx = ((Double) MouseInfo.getPointerInfo().getLocation().getX()).intValue();
                    int my = ((Double) MouseInfo.getPointerInfo().getLocation().getY()).intValue();
                    if (mx - this.getX() > bordersize * 2) {
                        this.setSize(mx + 5 - this.getX(), this.getHeight());
                    } else {
                        this.setLocation(mx - (bordersize * 2), this.getY());
                        this.setSize(mx + 5 - this.getX(), this.getHeight());
                    }
                    if (my - this.getY() > bordersize * 2) {
                        this.setSize(this.getWidth(), my + 5 - this.getY());
                    } else {
                        this.setLocation(this.getX(), my - (bordersize * 2));
                        this.setSize(this.getWidth(), my + 5 - this.getY());
                    }
                } catch (Exception e) {
                }
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_D) {
                imageX = 0;
                imageY = 0;
                defaultsize = !defaultsize;
                setimgtosize();
            }
        }
    }//GEN-LAST:event_formKeyPressed

    public Image getImageFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            } catch (Exception e) {
                // handle this as desired
                e.printStackTrace();
            }
        } else if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                java.util.List list = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                return ImageIO.read((File) list.get(0));
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("getImageFromClipboard: That wasn't an image!");
            }
        }
        return null;
    }

    private void holderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_holderMousePressed
        difx = evt.getXOnScreen() - this.getX();
        dify = evt.getYOnScreen() - this.getY();
        prevx = this.getWidth();
        prevy = this.getHeight();
        clickedx = evt.getXOnScreen();
        clickedy = evt.getYOnScreen();
        clickedimageX = imageX;
        clickedimageY = imageY;
    }//GEN-LAST:event_holderMousePressed

    private void holderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_holderMouseDragged
        if (draging) {
            imageX = clickedimageX + (evt.getXOnScreen() - clickedx);
            imageY = clickedimageY + (evt.getYOnScreen() - clickedy);
            setimgtosize();
        } else if (scalingx | scalingy) {
            int movingx = (evt.getXOnScreen() - clickedx);
            int movingy = (evt.getYOnScreen() - clickedy);
            if (scaledragx) {
                this.setLocation(evt.getXOnScreen() - difx, this.getY());
                movingx *= -1;
            }
            if (scaledragy) {
                this.setLocation(this.getX(), evt.getYOnScreen() - dify);
                movingy *= -1;
            }

            if (scalingx & prevx + movingx > bordersize * 2) {
                this.setSize(prevx + movingx, this.getHeight());
            } else if (scalingx) {
                this.setSize(bordersize * 2 + 1, this.getHeight());
            }
            if (scalingy & prevy + movingy > bordersize * 2) {
                this.setSize(this.getWidth(), prevy + movingy);
            } else if (scalingy) {
                this.setSize(this.getWidth(), bordersize * 2 + 1);
            }
            setimgtosize();
        } else {
            this.setLocation(evt.getXOnScreen() - difx, evt.getYOnScreen() - dify);
        }
    }//GEN-LAST:event_holderMouseDragged

    private void holderMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_holderMouseMoved
        setnormalcursor();
    }//GEN-LAST:event_holderMouseMoved

    private void setnormalcursor() {
        try {
            if (!draging) {
                if (this.getMousePosition().getX() < bordersize) {
                    if (this.getMousePosition().getY() < bordersize) {
                        currentcursor = 6;
                        scalingy = true;
                        scaledragx = true;
                        scaledragy = true;
                    } else if (this.getMousePosition().getY() > (this.getHeight() - bordersize)) {
                        currentcursor = 7;
                        scalingy = true;
                        scaledragx = true;
                        scaledragy = false;
                    } else {
                        currentcursor = 10;
                        scalingy = false;
                        scaledragx = true;
                        scaledragy = false;
                    }
                    scalingx = true;
                } else if (this.getMousePosition().getX() > (this.getWidth() - bordersize)) {
                    if (this.getMousePosition().getY() < bordersize) {
                        currentcursor = 7;
                        scalingy = true;
                        scaledragx = false;
                        scaledragy = true;
                    } else if (this.getMousePosition().getY() > (this.getHeight() - bordersize)) {
                        currentcursor = 6;
                        scalingy = true;
                        scaledragx = false;
                        scaledragy = false;
                    } else {
                        currentcursor = 10;
                        scalingy = false;
                        scaledragx = false;
                        scaledragy = false;
                    }
                    scalingx = true;
                } else if (this.getMousePosition().getY() < bordersize) {
                    currentcursor = 8;
                    scalingy = true;
                    scalingx = false;
                    scaledragx = false;
                    scaledragy = true;
                } else if (this.getMousePosition().getY() > (this.getHeight() - bordersize)) {
                    currentcursor = 8;
                    scalingy = true;
                    scalingx = false;
                    scaledragx = false;
                    scaledragy = false;
                } else {
                    currentcursor = 0;
                    scalingx = false;
                    scalingy = false;
                    scaledragx = false;
                    scaledragy = false;
                }
                this.setCursor(currentcursor);
            }
        } catch (Exception e) {

        }
    }

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            draging = false;
        }
    }//GEN-LAST:event_formKeyReleased

    private void holderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_holderMouseWheelMoved
        if (evt.getWheelRotation() < 0) {
            if (evt.isControlDown()) {
                if (imgalpha + alphastep <= 1) {
                    imgalpha += alphastep;
                } else {
                    imgalpha = 1;
                }
            } else if ((originalimage.getWidth(null) * zoom) * scaleimagepercent < originalimage.getWidth(null)) {
                if (evt.isShiftDown()) {
                    zoom += bigzoomstep;
                } else {
                    zoom += zoomstep;
                }
            } else {
                zoom = (originalimage.getWidth(null) / scaleimagepercent) / originalimage.getWidth(null);
            }
        } else {
            if (evt.isControlDown()) {
                if (imgalpha - alphastep >= 0.01) {
                    imgalpha -= alphastep;
                } else {
                    imgalpha = 0.01f;
                }
            }else if (zoom > 0.01 & (zoom - (evt.isShiftDown() ? bigzoomstep : zoomstep) > zoomstep)) {
                if (evt.isShiftDown()) {
                    zoom -= bigzoomstep;
                } else {
                    zoom -= zoomstep;
                }
            } else {
                zoom = minzoom;
            }
        }
        setimgtosize();
    }//GEN-LAST:event_holderMouseWheelMoved

    private void setimgtosize() {
        panelBorder.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.01f));
        this.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.01f));
        if (defaultsize) {
            scaleimagepercent = 1.0;
            zoom = 1.0;
        } else {
            boolean continuar = true;
            Double imagezsp = originalimage.getWidth(null) * zoomstep;
            if (originalimage.getWidth(null) > originalimage.getHeight(null)) {
                while (continuar) {
                    if (originalimage.getWidth(null) * (scaleimagepercent + zoomstep) > this.getWidth() + imagezsp * 2) {
                        scaleimagepercent -= zoomstep;
                    } else if (originalimage.getWidth(null) * (scaleimagepercent - zoomstep) < this.getWidth() - imagezsp * 2) {
                        scaleimagepercent += zoomstep;
                    } else {
                        continuar = false;
                    }
                }
                if ((originalimage.getWidth(null) * zoom) * scaleimagepercent > originalimage.getWidth(null)) {
                    zoom = (originalimage.getWidth(null) / scaleimagepercent) / originalimage.getWidth(null);
                }
                if (this.getWidth() > originalimage.getWidth(null)) {
                    zoom = (originalimage.getWidth(null) / scaleimagepercent) / originalimage.getWidth(null);
                }
            } else {
                while (continuar) {
                    if (originalimage.getHeight(null) * (scaleimagepercent + zoomstep) > this.getHeight() + imagezsp * 2) {
                        scaleimagepercent -= zoomstep;
                    } else if (originalimage.getHeight(null) * (scaleimagepercent - zoomstep) < this.getHeight() - imagezsp * 2) {
                        scaleimagepercent += zoomstep;
                    } else {
                        continuar = false;
                    }
                }
                if ((originalimage.getHeight(null) * zoom) * scaleimagepercent > originalimage.getHeight(null)) {
                    zoom = (originalimage.getHeight(null) / scaleimagepercent) / originalimage.getHeight(null);
                }
                if (this.getHeight() > originalimage.getHeight(null)) {
                    zoom = (originalimage.getHeight(null) / scaleimagepercent) / originalimage.getHeight(null);
                }
            }
        }
        showingimage = getimgtoshow();
        holder.setIcon(new ImageIcon(showingimage));
    }

    private Image getimgtoshow() {
        return getScaledImage(originalimage, ((Double) ((originalimage.getWidth(null) * zoom) * scaleimagepercent)).intValue(), ((Double) ((originalimage.getHeight(null) * zoom) * scaleimagepercent)).intValue());
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        int wid = ((Double) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth()).intValue() + this.getWidth();
        int hei = ((Double) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()).intValue() + this.getHeight();
        BufferedImage resizedImg = new BufferedImage(wid, hei, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        int wwid = this.getWidth();
        int hhei = this.getHeight();
        int dif = 3;
        int bs = 2;
        g2.setComposite(AlphaComposite.SrcOver.derive(imgalpha));
        g2.drawImage(srcImg, (fliph > 0 ? imageX : imageX + w), (flipv > 0 ? imageY : imageY + h), w * fliph, h * flipv, null);
        g2.setColor(this.getBackground());
        g2.fillRect(0, 0, wwid, hhei - dif);
        g2.setColor(new Color(0f, 0f, 0f, 1f));
        g2.setStroke(new BasicStroke(bs));
        g2.drawRect(0, 0, wwid - dif, hhei - dif);
        g2.setColor(new Color(1f, 1f, 1f, 1f));
        g2.drawRect(bs, bs, wwid - dif - bs * 2, hhei - dif - bs * 2);
        g2.setFont(new Font("Arial Black", Font.PLAIN, 25));
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.dispose();
        return resizedImg;
    }

    private Image makeColorTransparent(BufferedImage image, int alpha) {
        ImageFilter filter = new RGBImageFilter() {
            public final int filterRGB(int x, int y, int rgb) {
                return (rgb << 8) & alpha;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OverImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OverImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OverImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OverImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OverImage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel holder;
    private javax.swing.JPanel panelBorder;
    // End of variables declaration//GEN-END:variables
}
