package com.aosgi.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.framework.util.Util;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

public class Launcher extends Application {

    /**
     * The configuration directory
     */
    private static final String DIR_CONF = "conf";

    /**
     * The bundle cache directory
     */
    private static final String DIR_CACHE = "cache";

    /**
     * The bundle deploy directory
     */
    private static final String DIR_DEPLOY = "bundle";

    private static final String BUNDLE_DIR_SWITCH = "-b";

    private static final String FELIX_JAR = "felix.jar";

    private static final String CONFIG_PROPERTIES = "config.properties";

    private static final String FELIX_SYSTEM_PROPERTIES = "felix.system.properties";

    private static final String FELIX_CONFIG_PROPERTIES = "felix.config.properties";

    private static final String SYSTEM_PROPERTIES = "system.properties";

    private static final String PROP_FELIX_SHUTDOWN_HOOK = "felix.shutdown.hook";

    private static final String PROP_FELIX_AUTO_DEPLOY_DIR = "felix.auto.deploy.dir";

    private static final String TAG = "AOSGi";

    private static Launcher instance = null;

    /**
     * The system bundle
     */
    private Framework framework = null;

    public static Launcher getInstance() {
        return instance;
    }

    private static void loadSystemProperties() {
        final String custom = System.getProperty(FELIX_SYSTEM_PROPERTIES);

        URL propURL = null;

        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Load system properties error", e);
                return;
            }
        } else {
            final File conf;
            final String classpath = System.getProperty("java.class.path");
            final int index = classpath.indexOf(FELIX_JAR);
            final int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;

            if (index >= start) {
                final String jarLocation = classpath.substring(start, index);
                final File jar = new File(jarLocation);
                conf = new File(jar.getParent(), DIR_CONF);
            } else {
                conf = new File(System.getProperty("user.dir"), DIR_CONF);
            }

            try {
                propURL = new File(conf, SYSTEM_PROPERTIES).toURI().toURL();
            } catch (MalformedURLException e) {
                Log.e(TAG, "Load system properties error", e);
                return;
            }
        }

        final Properties props = new Properties();

        InputStream is = null;

        try {
            is = propURL.openConnection().getInputStream();
            props.load(is);
        } catch (FileNotFoundException e) {
            Log.v(TAG, "System properties not found");
        } catch (Exception e) {
            Log.e(TAG, "Error loading system properties from " + propURL, e);
            return;
        } finally {
            try {
                if (null != is) {
                    is.close();
                    is = null;
                }
            } catch (IOException ex) {
            }
        }

        for (final Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            final String name = String.valueOf(e.nextElement());
            System.setProperty(name, Util.substVars(props.getProperty(name), name, null, null));
        }
    }

    private static Map<String, String> loadConfigProperties() {
        final String custom = System.getProperty(FELIX_CONFIG_PROPERTIES);

        URL propURL = null;

        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                Log.e(TAG, "Load config properties error", ex);
                return null;
            }
        } else {
            final File conf;
            final String classpath = System.getProperty("java.class.path");
            final int index = classpath.indexOf(FELIX_JAR);
            final int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;

            if (index >= start) {
                final File jar = new File(classpath.substring(start, index));
                conf = new File(jar.getParent(), DIR_CONF);
            } else {
                conf = new File(System.getProperty("user.dir"), "conf");
            }

            try {
                propURL = new File(conf, CONFIG_PROPERTIES).toURI().toURL();
            } catch (MalformedURLException e) {
                Log.e(TAG, "Log config properties error", e);
                return null;
            }

        }

        final Properties props = new Properties();
        InputStream is = null;
        try {
            is = propURL.openConnection().getInputStream();
            props.load(is);
        } catch (Exception e) {
            Log.e(TAG, "Log config properties error ", e);
            return null;
        } finally {
            try {
                if (null != is) {
                    is.close();
                    is = null;
                }
            } catch (IOException ex) {
            }
        }

        final Map<String, String> map = new HashMap<String, String>();

        for (final Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            final String name = String.valueOf(e.nextElement());
            map.put(name, Util.substVars(props.getProperty(name), name, null, props));
        }

        return map;
    }

    private static void copySystemProperties(Map<String, String> configProps) {
        final Enumeration<?> e = System.getProperties().propertyNames();
        while (e.hasMoreElements()) {
            final String key = String.valueOf(e.nextElement());
            if ((key.startsWith("felix.")) || (key.startsWith("org.osgi.framework."))) {
                configProps.put(key, System.getProperty(key));
            }
        }
    }

    public Launcher() {
        Launcher.instance = this;
    }

    public Framework getFramework() {
        return this.framework;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final File root = getFilesDir();
        final File conf = new File(root, DIR_CONF);
        final File cache = new File(root, DIR_CACHE);
        final File deploy = new File(root, DIR_DEPLOY);
        final String[] args = { BUNDLE_DIR_SWITCH, deploy.getAbsolutePath(), cache.getAbsolutePath() };

        if (!conf.exists()) {
            conf.mkdirs();
        }

        if (!cache.exists()) {
            cache.mkdirs();
        }

        if (!deploy.exists()) {
            deploy.mkdirs();
        }

        this.setup(conf);
        this.launch(args);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (FrameworkEvent.STOPPED_UPDATE == framework.waitForStop(0L).getType()) {
                        framework.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error occurred", e);
                }
            }

        });
    }

    /**
     * Copy asset to the specified path
     * 
     * @param dest
     *            Destination path
     * @param asset
     *            Asset file name
     */
    private void copyAsset(final File dest, final String asset) {
        final byte[] buffer = new byte[1024 * 1024];
        final AssetManager am = getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }

            is = am.open(asset);
            os = new FileOutputStream(dest);

            for (int n = 0; -1 != (n = is.read(buffer));) {
                os.write(buffer, 0, n);
            }

            os.flush();
        } catch (IOException e) {
            Log.e(TAG, "Copy asset from " + asset + " to " + dest + " error", e);
        } finally {
            if (null != os) {
                try {
                    os.close();
                    os = null;
                } catch (IOException e) {
                }
            }

            if (null != is) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Setup configuration files
     * 
     * @param conf
     *            The configuration directory
     */
    private void setup(File conf) {
        final File cfg = new File(conf, FELIX_CONFIG_PROPERTIES);
        final Uri uri = Uri.fromFile(cfg);

        this.copyAsset(cfg, FELIX_CONFIG_PROPERTIES);
        System.setProperty(FELIX_CONFIG_PROPERTIES, uri.toString());
    }

    /**
     * felix [-b &lt;bundle-deploy-dir&gt;] [&lt;bundle-cache-dir&gt];
     * 
     * @param args
     * @throws Exception
     */
    private void launch(String[] args) {
        String bundleDir = null;
        String cacheDir = null;
        boolean expectBundleDir = false;

        for (int i = 0; i < args.length; i++) {
            if (BUNDLE_DIR_SWITCH.equals(args[i])) {
                expectBundleDir = true;
            } else if (expectBundleDir) {
                bundleDir = args[i];
                expectBundleDir = false;
            } else {
                cacheDir = args[i];
            }
        }

        if ((args.length > 3) || ((expectBundleDir) && (bundleDir == null))) {
            Log.i(TAG, "Usage: [-b <bundle-deploy-dir>] [<bundle-cache-dir>]");
            System.exit(0);
            return;
        }

        loadSystemProperties();

        Map<String, String> configProps = loadConfigProperties();

        if (configProps == null) {
            Log.e(TAG, "No " + CONFIG_PROPERTIES + " found.");
            configProps = new HashMap<String, String>();
        }

        copySystemProperties(configProps);

        if (bundleDir != null) {
            configProps.put(PROP_FELIX_AUTO_DEPLOY_DIR, bundleDir);
        }

        if (cacheDir != null) {
            configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir);
        }

        if (!"false".equalsIgnoreCase(configProps.get(PROP_FELIX_SHUTDOWN_HOOK))) {
            Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {

                @Override
                public void run() {
                    try {
                        if (framework != null) {
                            framework.stop();
                            framework.waitForStop(0L);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error stopping framework", e);
                    }
                }

            });
        }

        final FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();

        try {
            this.framework = factory.newFramework(configProps);
            this.framework.init();

            // register Android application context
            final BundleContext ctx = this.framework.getBundleContext();
            final Context context = new RuntimeContext(this);
            final Hashtable<String, ?> props = new Hashtable<String, Object>();
            ctx.registerService(Context.class, context, props);

            AutoProcessor.process(configProps, ctx);

            this.framework.start();
        } catch (Exception e) {
            Log.e(TAG, "Could not create framework", e);
            System.exit(0);
        }
    }

}
