/*
 * Project:  droidAtScreen
 * File:     ScreenShotCommand.java
 * Modified: 2011-10-04
 *
 * Copyright (C) 2011, Ribomation AB (Jens Riboe).
 * http://blog.ribomation.com/
 *
 * You are free to use this software and the source code as you like.
 * We do appreciate if you attribute were it came from.
 */

package com.ribomation.droidAtScreen.cmd;

import com.ribomation.droidAtScreen.Application;
import com.ribomation.droidAtScreen.dev.AndroidDevice;
import com.ribomation.droidAtScreen.dev.ScreenImage;
import com.ribomation.droidAtScreen.gui.DeviceFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Arrays;

/**
 * Takes a screen-shot of the current device image.
 *
 * @user jens
 * @date 30 september 2011, 14:19
 */
public class ScreenShotCommand extends Command {
    private File lastDir = null;
    private int count = 1;

    public ScreenShotCommand() {
        setLabel("Capture");
        setIcon("camera");
        setTooltip("Takes a screen-shot of the current device and saves it as a PNG file.");
        setEnabledOnlyWithDevice(true);
    }

    @Override
    protected void doExecute(final Application app) {
        final DeviceFrame device = app.getSelectedDevice();
        if (device == null) return;

        File suggestedFile = new File(String.format("droidAtScreen-%d.%s", count++, app.getSettings().getImageFormat().toLowerCase()));

        final JFileChooser chooser = new JFileChooser(lastDir);
        chooser.setCurrentDirectory(null);
        chooser.setSelectedFile(suggestedFile);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", app.getSettings().getImageFormats()));

        int rc = chooser.showSaveDialog(app.getAppFrame());
        if (rc != JFileChooser.APPROVE_OPTION) return;


        final File imageFile = chooser.getSelectedFile();
        lastDir = imageFile.getParentFile();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (imageFile.exists()) {
                        int rc = JOptionPane.showConfirmDialog(app.getAppFrame(),
                                "File '" + imageFile + "' already exist. Do you want to overwrite?",
                                "Overwrite file",
                                JOptionPane.YES_NO_OPTION);
                        if (rc != JOptionPane.YES_OPTION) return;
                    }

                    ScreenImage screenShot = device.getLastScreenshot();
                    ImageIO.write(screenShot.toBufferedImage(), getFormat(imageFile), imageFile);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(app.getAppFrame(),
                            "Failed to save file " + imageFile + ". " + e.getMessage(),
                            "Failure",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            String getFormat(File f) {
                final String name = f.getName();
                final int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    String ext = name.substring(dot + 1).toUpperCase();
                    if (Arrays.asList(app.getSettings().getImageFormats()).contains(ext)) {
                        return ext;
                    }
                }

                throw new RuntimeException("Invalid extension: " + name);
            }
        });

    }
}
