package overimage;

import com.sun.awt.AWTUtilities;
import com.sun.glass.events.KeyEvent;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.util.Timer;
import java.util.TimerTask;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

/**
 *
 * @author Buda
 */
public class OverImage extends javax.swing.JFrame {

    boolean scalingx = false;
    boolean scalingy = false;
    boolean scaledragx = false;
    boolean scaledragy = false;
    boolean draging = false;
    boolean defaultsize = false;
    boolean hvisible = true;
    boolean rendering = false;
    boolean shiftdown = false;
    boolean fasterclickthrough = false;
    boolean locksize = false;
    static int bordersize = 20;
    static Double bigzoomstep = 0.85;
    static Double zoomstep = 0.95;
    static Double minzoom = 0.001;
    int mx;
    int my;
    int prevmx;
    int prevmy;
    int difx = 0;
    int dify = 0;
    int prevx = 0;
    int prevy = 0;
    int clickedx = 0;
    int clickedy = 0;
    int currentcursor = 0;
    int fliph = 1;
    int flipv = 1;
    int imageX = 0;
    int imageY = 0;
    int showingX = 0;
    int showingY = 0;
    int clickedimageX = 0;
    int clickedimageY = 0;
    int skiprender = 0;
    int defaultwl;
    float imgalpha = 1f;
    float alphastep = 0.1f;
    Image originalimage;
    Image sizeimage;
    Image holeimage;
    JFrame frame = this;
    JLabel display;
    JLabel holder;
    Double zoom = 1.0;
    Double scaleimagepercent = 1.0;
    NumberFormat formatter = new DecimalFormat("#0.00");
    Robot bot;
    Timer timer;
    Timer stimer;
    JLabel anotherhole = new JLabel();
    int holeradius = 4;

    /**
     * Creates new form OverImage
     */
    public OverImage() {
        initComponents();
        try {
            bot = new Robot();
        } catch (Exception e) {
        }

        BufferedImage buraco = new BufferedImage(holeradius, holeradius, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buraco.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(0f, 0f, 0f, 0.5f));
        g.fillOval(-holeradius, -holeradius, -holeradius * 2, -holeradius * 2);
        g.dispose();
        anotherhole.setIcon(new ImageIcon(buraco));
        anotherhole.setLocation(holeradius, holeradius);
        anotherhole.setSize(holeradius, holeradius);
        panelBorder.add(anotherhole);

        display = new JLabel();
        display.setSize(this.getWidth(), this.getHeight());
        display.setLocation(0, 0);
        display.setHorizontalAlignment(SwingConstants.LEFT);
        display.setVerticalAlignment(SwingConstants.TOP);
        display.setVisible(true);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!hvisible) {
//                    setdisplayhole();
                }
            }
        }, 30, 30);

        stimer = new Timer();
        stimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!hvisible) {
                    try {
                        mx = ((Double) frame.getMousePosition().getX()).intValue();
                        my = ((Double) frame.getMousePosition().getY()).intValue();
                        drawHole();
                        prevmx = mx;
                        prevmy = my;
                    } catch (Exception e) {
                    }
                }
            }
        }, 1, 1);

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nke) {
                if (nke.getKeyCode() == NativeKeyEvent.VC_W) {
                    unfreezePane();
                }
                return;
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nke) {
                return;
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nke) {
                return;
            }
        });

        GlobalScreen.addNativeMouseMotionListener(new NativeMouseInputListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent nme) {
                mx = nme.getX() - frame.getX();
                my = nme.getY() - frame.getY();
                drawHole();
                prevmx = mx;
                prevmy = my;
                return;
            }

            @Override
            public void nativeMousePressed(NativeMouseEvent nme) {
                mx = nme.getX() - frame.getX();
                my = nme.getY() - frame.getY();
                drawHole();
                prevmx = mx;
                prevmy = my;
                return;
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent nme) {
                mx = nme.getX() - frame.getX();
                my = nme.getY() - frame.getY();
                drawHole();
                prevmx = mx;
                prevmy = my;
                return;
            }

            @Override
            public void nativeMouseMoved(NativeMouseEvent nme) {
                mx = nme.getX() - frame.getX();
                my = nme.getY() - frame.getY();
                if (skiprender > 1) {
                    drawHole();
                    skiprender = 0;
                } else {
                    skiprender++;
                }
                prevmx = mx;
                prevmy = my;
                return;
            }

            @Override
            public void nativeMouseDragged(NativeMouseEvent nme) {
                mx = nme.getX() - frame.getX();
                my = nme.getY() - frame.getY();
                drawHole();
                prevmx = mx;
                prevmy = my;
                return;
            }
        });

        holder = new JLabel();
        panelBorder.add(holder);
        panelBorder.add(display);
        holder.setSize(this.getWidth(), this.getHeight());
        holder.setLocation(0, 0);
        holder.setHorizontalAlignment(SwingConstants.LEFT);
        holder.setVerticalAlignment(SwingConstants.TOP);
        holder.setVisible(true);
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
        this.setSize(200, 200);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBorder = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setLocation(new java.awt.Point(200, 200));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }

            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        panelBorder.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout panelBorderLayout = new javax.swing.GroupLayout(panelBorder);
        panelBorder.setLayout(panelBorderLayout);
        panelBorderLayout.setHorizontalGroup(
                panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 333, Short.MAX_VALUE)
        );
        panelBorderLayout.setVerticalGroup(
                panelBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 229, Short.MAX_VALUE)
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

    private void setTransparent(Component w) {
        WinDef.HWND hwnd = getHWnd(w);
        int wl = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
        defaultwl = wl;
        wl = wl | WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wl);
    }

    private void unsetTransparent(Component w) {
        WinDef.HWND hwnd = getHWnd(w);
        User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, defaultwl);
    }

    /**
     * Get the window handle from the OS
     */
    private static HWND getHWnd(Component w) {
        HWND hwnd = new HWND();
        hwnd.setPointer(Native.getComponentPointer(w));
        return hwnd;
    }

    public void freezePane() {
        setTransparent(this);
        hvisible = false;
        holder.setVisible(false);
        this.setCursor(0);
        this.repaint();
    }

    public void unfreezePane() {
        if (hvisible == false) {
            unsetTransparent(this);
            hvisible = true;
            holder.setVisible(true);
            this.repaint();
        }
    }

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
            if (evt.getKeyCode() == KeyEvent.VK_Q) {
                freezePane();
            }

            if (evt.getKeyCode() == KeyEvent.VK_E) {
                locksize = !locksize;
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                if (hvisible) {
                    draging = true;
                    currentcursor = 13;
                    this.setCursor(currentcursor);
                }
            }
            if (evt.getKeyCode() == KeyEvent.VK_Z) {
                if (evt.isShiftDown()) {
                    zoom /= bigzoomstep;
                } else {
                    zoom /= zoomstep;
                }
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_X) {;
                if (zoom * (evt.isShiftDown() ? bigzoomstep : zoomstep) > minzoom) {
                    if (evt.isShiftDown()) {
                        zoom *= bigzoomstep;
                    } else {
                        zoom *= zoomstep;
                    }
                } else {
                    zoom = minzoom;
                }
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_PLUS | evt.getKeyCode() == KeyEvent.VK_ADD | evt.getKeyCode() == KeyEvent.VK_S) {
                if (imgalpha + alphastep <= 1) {
                    imgalpha += alphastep;
                } else {
                    imgalpha = 1;
                }
                AWTUtilities.setWindowOpacity(this, imgalpha);
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_MINUS | evt.getKeyCode() == KeyEvent.VK_SUBTRACT | evt.getKeyCode() == KeyEvent.VK_A) {
                if (imgalpha - alphastep >= 0.01) {
                    imgalpha -= alphastep;
                } else {
                    imgalpha = 0.01f;
                }
                AWTUtilities.setWindowOpacity(this, imgalpha);
                setimgtosize();
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
            if (evt.getKeyCode() == KeyEvent.VK_R) {
                try {
                    int x = ((Double) MouseInfo.getPointerInfo().getLocation().getX()).intValue();
                    int y = ((Double) MouseInfo.getPointerInfo().getLocation().getY()).intValue();
                    this.setLocation(x - (this.getWidth() / 2), y - (this.getHeight() / 2));
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
                scaleimagepercent = 1.0;
                zoom = 1.0;
                setThisSize(originalimage.getWidth(this), originalimage.getHeight(this));
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_1) {
                if(!locksize){
                    scaleimagepercent = 0.05;
                }
                zoom = 1.0;
                int futurew = ((Double) ((originalimage.getWidth(this) * zoom) * scaleimagepercent)).intValue();
                int futureh = ((Double) ((originalimage.getHeight(this) * zoom) * scaleimagepercent)).intValue();
                setThisSize(futurew, futureh);
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_2) {
                if(!locksize){
                    scaleimagepercent = 0.2;
                }
                zoom = 1.0;
                int futurew = ((Double) ((originalimage.getWidth(this) * zoom) * 0.2)).intValue();
                int futureh = ((Double) ((originalimage.getHeight(this) * zoom) * 0.2)).intValue();
                setThisSize(futurew, futureh);
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_3) {
                if(!locksize){
                    scaleimagepercent = 0.5;
                }
                zoom = 1.0;
                int futurew = ((Double) ((originalimage.getWidth(this) * zoom) * 0.5)).intValue();
                int futureh = ((Double) ((originalimage.getHeight(this) * zoom) * 0.5)).intValue();
                setThisSize(futurew, futureh);
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_4) {
                if(!locksize){
                    scaleimagepercent = 0.8;
                }
                zoom = 1.0;
                int futurew = ((Double) ((originalimage.getWidth(this) * zoom) * 0.8)).intValue();
                int futureh = ((Double) ((originalimage.getHeight(this) * zoom) * 0.8)).intValue();
                setThisSize(futurew, futureh);
                setimgtosize();
            }
            if (evt.getKeyCode() == KeyEvent.VK_5) {
                if(!locksize){
                    scaleimagepercent = 1.0;
                }
                zoom = 1.0;
                int futurew = ((Double) ((originalimage.getWidth(this) * zoom) * 1)).intValue();
                int futureh = ((Double) ((originalimage.getHeight(this) * zoom) * 1)).intValue();
                setThisSize(futurew, futureh);
                setimgtosize();
            }
        }
    }//GEN-LAST:event_formKeyPressed

    private void setThisSize(int w, int h) {
        if (w < bordersize * 2) {
            w = bordersize * 2;
        }
        if (h < bordersize * 2) {
            h = bordersize * 2;
        }
        this.setSize(w, h);
    }

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

    private void holderMousePressed(java.awt.event.MouseEvent evt) {
        difx = evt.getXOnScreen() - this.getX();
        dify = evt.getYOnScreen() - this.getY();
        prevx = this.getWidth();
        prevy = this.getHeight();
        clickedx = evt.getXOnScreen();
        clickedy = evt.getYOnScreen();
        clickedimageX = imageX;
        clickedimageY = imageY;
    }

    private void holderMouseDragged(java.awt.event.MouseEvent evt) {
        if (draging) {
            imageX = ((Double) ((clickedimageX + (((evt.getXOnScreen() - clickedx) / scaleimagepercent) / zoom)))).intValue();
            imageY = ((Double) ((clickedimageY + (((evt.getYOnScreen() - clickedy) / scaleimagepercent) / zoom)))).intValue();
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
        } else {
            this.setLocation(evt.getXOnScreen() - difx, evt.getYOnScreen() - dify);
        }
        setimgtosize();
    }

    private void holderMouseMoved(java.awt.event.MouseEvent evt) {
        setnormalcursor();
    }

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
            if (!hvisible) {
                this.setCursor(0);
            }
        } catch (Exception e) {

        }
    }

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            draging = false;
        }
    }//GEN-LAST:event_formKeyReleased

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened

    private void holderMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        if (evt.getWheelRotation() < 0) {
            if (evt.isControlDown()) {
                if (imgalpha + alphastep <= 1) {
                    imgalpha += alphastep;
                } else {
                    imgalpha = 1;
                }
            } else if (evt.isShiftDown()) {
                zoom /= bigzoomstep;
            } else {
                zoom /= zoomstep;
            }
        } else if (evt.isControlDown()) {
            if (imgalpha - alphastep >= 0.01) {
                imgalpha -= alphastep;
            } else {
                imgalpha = 0.01f;
            }
        } else if (zoom * (evt.isShiftDown() ? bigzoomstep : zoomstep) > minzoom) {
            if (evt.isShiftDown()) {
                zoom *= bigzoomstep;
            } else {
                zoom *= zoomstep;
            }
        } else if (zoom != minzoom) {
            zoom = minzoom;
        }
        AWTUtilities.setWindowOpacity(this, imgalpha);
        setimgtosize();
    }

    private void setdisplayhole() {
        if (sizeimage != null) {
            holeimage = getHoleImage(sizeimage);
            display.setIcon(new ImageIcon(holeimage));
            this.repaint();
        }
    }

    private void setimgtosize() {
        if (originalimage != null) {
            int curw = ((Double) ((originalimage.getWidth(null) * zoom) * scaleimagepercent)).intValue();
            int curh = ((Double) ((originalimage.getHeight(null) * zoom) * scaleimagepercent)).intValue();
            panelBorder.setBackground(new Color(1f, 1f, 1f, 0f));
            this.setBackground(new Color(1f, 1f, 1f, 0f));
            Double ow = originalimage.getWidth(null) + 0d;
            Double tw = this.getWidth() + 0d;
            if (!locksize) {
                scaleimagepercent = tw / ow;
            }
            int center;
            if (originalimage.getWidth(null) > originalimage.getHeight(null)) {
                center = (((Double) ((this.getWidth() / 2) * zoom)).intValue() - (this.getWidth() / 2));
            } else {
                center = (((Double) ((this.getHeight() / 2) * zoom)).intValue() - (this.getHeight() / 2));
            }
            showingX = ((Double) ((imageX * zoom) * scaleimagepercent)).intValue() - center;
            showingY = ((Double) ((imageY * zoom) * scaleimagepercent)).intValue() - center;
            boolean continuar = true;
            Double imagezsp = originalimage.getWidth(null) * zoomstep;
            System.out.println(scaleimagepercent);
            holder.setVisible(false);
            display.setVisible(false);
            this.repaint();
            holder.setLocation(0, 0);
            holder.setSize(this.getSize());
            sizeimage = getScaleImage(originalimage, ((Double) ((originalimage.getWidth(null) * zoom) * scaleimagepercent)).intValue(), ((Double) ((originalimage.getHeight(null) * zoom) * scaleimagepercent)).intValue());
            holder.setIcon(new ImageIcon(sizeimage));

            display.setLocation(0, 0);
            display.setSize(this.getSize());
            display.setIcon(new ImageIcon(sizeimage));
        }
        holder.setLocation(0, 0);
        holder.setSize(this.getSize());
        holder.setVisible(true);
        display.setLocation(0, 0);
        display.setSize(this.getSize());
        this.repaint();
        display.setVisible(true);
        holder.setVisible(hvisible);
        this.repaint();
    }

    private void drawHole() {
//        if (holeimage != null) {
//            Graphics2D g = (Graphics2D) holeimage.getGraphics();
//            g.setComposite(AlphaComposite.Clear);
//            g.setColor(new Color(1f, 1f, 1f, 1f));
//            g.fillOval(mx - holeradius, my - holeradius, holeradius * 2, holeradius * 2);
//            g.fillOval(mx - holeradius + ((mx - prevmx)), my - holeradius + ((my - prevmy)), holeradius * 2, holeradius * 2);
//            g.dispose();
//        }
    }

    private Image getHoleImage(Image img) {
        if (img != null) {
            BufferedImage holeimg = new BufferedImage(img.getWidth(this), img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = holeimg.createGraphics();
            g2.drawImage(img, 0, 0, img.getWidth(this), img.getHeight(this), null);
            g2.setComposite(AlphaComposite.Clear);
            g2.setColor(new Color(1f, 1f, 1f, 1f));
            g2.fillOval(mx - holeradius, my - holeradius, holeradius * 2, holeradius * 2);
            g2.dispose();
            return holeimg;
        } else {
            return null;
        }
    }

    private Image getScaleImage(Image srcImg, int w, int h) {
        if (srcImg != null) {
            int wid = ((Double) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth()).intValue() + this.getWidth();
            int hei = ((Double) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()).intValue() + this.getHeight();
            BufferedImage resizedImg = new BufferedImage(wid, hei, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();
            int wwid = this.getWidth();
            int hhei = this.getHeight();
            int dif = 3;
            int bs = 1;
            g2.drawImage(srcImg, (fliph > 0 ? showingX : showingX + w), (flipv > 0 ? showingY : showingY + h), w * fliph, h * flipv, null);
            g2.setColor(new Color(1f, 1f, 1f, 0.01f));
            g2.fillRect(0, 0, wwid, hhei);
            g2.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
            g2.setStroke(new BasicStroke(bs));
            g2.drawRect(0, 0, wwid - dif, hhei - dif);
            g2.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
            g2.drawRect(bs, bs, wwid - dif - bs * 2, hhei - dif - bs * 2);

            //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.dispose();
            return resizedImg;
        } else {
            return null;
        }
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
    private javax.swing.JPanel panelBorder;
    // End of variables declaration//GEN-END:variables

}
