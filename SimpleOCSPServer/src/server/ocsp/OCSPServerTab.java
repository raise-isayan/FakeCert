package server.ocsp;

import burp.BurpExtender;
import burp.IExtensionStateListener;
import burp.ITab;
import extend.util.SwingUtil;
import extend.util.Util;
import java.awt.Component;
import java.awt.TrayIcon;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.BindException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import server.ocsp.OCSPProperty.CACertificateType;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author isayan
 */
public class OCSPServerTab extends javax.swing.JPanel 
        implements ITab, IExtensionStateListener, UncaughtExceptionHandler {

    /**
     * Creates new form OCSPServerTab
     */
    public OCSPServerTab() {
        initComponents();
        customizeComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpCACertificate = new javax.swing.ButtonGroup();
        spnListenPort = new javax.swing.JSpinner();
        btnServerStart = new javax.swing.JToggleButton();
        lblListenPort = new javax.swing.JLabel();
        pnlCaCertificate = new javax.swing.JPanel();
        rdoBurpCA = new javax.swing.JRadioButton();
        rdoCustomCA = new javax.swing.JRadioButton();
        pnlCustomCA = new javax.swing.JPanel();
        txtCAFile = new javax.swing.JTextField();
        btnSelectCustomCA = new javax.swing.JButton();
        txtCApassword = new javax.swing.JPasswordField();
        lblPassword = new javax.swing.JLabel();
        chkNonMaskPassword = new javax.swing.JCheckBox();
        chkAutoStart = new javax.swing.JCheckBox();

        spnListenPort.setModel(new javax.swing.SpinnerNumberModel(8888, 1024, 65535, 1));
        spnListenPort.setEditor(new javax.swing.JSpinner.NumberEditor(spnListenPort, "#0"));
        spnListenPort.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnListenPortStateChanged(evt);
            }
        });

        btnServerStart.setText("Start");
        btnServerStart.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnServerStartStateChanged(evt);
            }
        });
        btnServerStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnServerStartActionPerformed(evt);
            }
        });

        lblListenPort.setText("Listen port:");

        pnlCaCertificate.setBorder(javax.swing.BorderFactory.createTitledBorder("CA Certificate"));

        btnGrpCACertificate.add(rdoBurpCA);
        rdoBurpCA.setSelected(true);
        rdoBurpCA.setText("Burp suite default CA");
        rdoBurpCA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rdoBurpCAStateChanged(evt);
            }
        });

        btnGrpCACertificate.add(rdoCustomCA);
        rdoCustomCA.setText("Use custom CA file (pkcs12)");
        rdoCustomCA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rdoCustomCAStateChanged(evt);
            }
        });

        btnSelectCustomCA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/server/ocsp/resources/folder_image.png"))); // NOI18N
        btnSelectCustomCA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectCustomCAActionPerformed(evt);
            }
        });

        txtCApassword.setToolTipText("");

        lblPassword.setText("password:");

        chkNonMaskPassword.setText("non mask password ");
        chkNonMaskPassword.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkNonMaskPasswordStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlCustomCALayout = new javax.swing.GroupLayout(pnlCustomCA);
        pnlCustomCA.setLayout(pnlCustomCALayout);
        pnlCustomCALayout.setHorizontalGroup(
            pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCustomCALayout.createSequentialGroup()
                .addGroup(pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlCustomCALayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(txtCAFile))
                    .addGroup(pnlCustomCALayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkNonMaskPassword)
                            .addComponent(txtCApassword, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSelectCustomCA)
                .addGap(66, 66, 66))
        );
        pnlCustomCALayout.setVerticalGroup(
            pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCustomCALayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCAFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectCustomCA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCustomCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCApassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkNonMaskPassword)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlCaCertificateLayout = new javax.swing.GroupLayout(pnlCaCertificate);
        pnlCaCertificate.setLayout(pnlCaCertificateLayout);
        pnlCaCertificateLayout.setHorizontalGroup(
            pnlCaCertificateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCaCertificateLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCaCertificateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCustomCA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCaCertificateLayout.createSequentialGroup()
                        .addComponent(rdoBurpCA, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlCaCertificateLayout.createSequentialGroup()
                        .addComponent(rdoCustomCA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(273, 273, 273))))
        );
        pnlCaCertificateLayout.setVerticalGroup(
            pnlCaCertificateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCaCertificateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoBurpCA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdoCustomCA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCustomCA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        chkAutoStart.setText("Automatic start at loading");
        chkAutoStart.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkAutoStartStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCaCertificate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblListenPort, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnListenPort, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnServerStart, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkAutoStart)))
                        .addGap(0, 342, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkAutoStart)
                    .addComponent(btnServerStart))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblListenPort)
                    .addComponent(spnListenPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCaCertificate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public KeyStore loadKeyStore() throws IOException, KeyStoreException {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            String password = getCAPassword();
            if (this.rdoBurpCA.isSelected()) {
                Preferences prefs = Preferences.userNodeForPackage(burp.IBurpExtender.class);
                byte[] caCartByte = Base64.decode(prefs.get("caCert", ""));
                ks.load(new ByteArrayInputStream(caCartByte), password.toCharArray());
                return ks;
            } else {
                File pkcs_ca = new File(this.txtCAFile.getText());
                ks.load(new FileInputStream(pkcs_ca), password.toCharArray());
                return ks;
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        } catch (CertificateException ex) {
            throw new IOException(ex);
        }
    }

    public String getCAPassword() {
        if (this.rdoBurpCA.isSelected()) {
            return "/burp/media/ps.p12";
        } else {
            return String.valueOf(this.txtCApassword.getPassword());
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof BindException) {
            this.btnServerStart.setSelected(false);
            JOptionPane.showMessageDialog(this, e.getMessage(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
            BurpExtender.issueAlert(getTabCaption(), e.getMessage(), TrayIcon.MessageType.ERROR);
        } else {
            BurpExtender.issueAlert(getTabCaption(), e.getMessage(), TrayIcon.MessageType.ERROR);
        }

    }

    private SimpleOCSPServer.ThreadWrap thredServer = null;

    private void btnServerStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnServerStartActionPerformed
        if (this.btnServerStart.isSelected()) {
            if (this.thredServer == null || (this.thredServer != null && !this.thredServer.isRunning())) {
                try {
                    KeyStore ks = this.loadKeyStore();
                    String password = this.getCAPassword();
                    String alias = OCSPUtil.getFirstAlias(ks);
                    PrivateKey issuerPrivateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
                    X509Certificate issuerCert = (X509Certificate) ks.getCertificate(alias);
                    this.thredServer = new SimpleOCSPServer.ThreadWrap(issuerPrivateKey, issuerCert, (int) this.spnListenPort.getValue());
                    this.thredServer.setUncaughtExceptionHandler(this);
                    this.thredServer.startServer();
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(this, "File not found:" + this.txtCAFile.getText(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
                    BurpExtender.issueAlert(getTabCaption(), Util.getStackTraceMessage(ex), TrayIcon.MessageType.ERROR);
                    this.btnServerStart.setSelected(false);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
                    BurpExtender.issueAlert(getTabCaption(), Util.getStackTraceMessage(ex), TrayIcon.MessageType.ERROR);
                    this.btnServerStart.setSelected(false);
                    Logger.getLogger(OCSPServerTab.class.getName()).log(Level.SEVERE, null, ex);
                } catch (KeyStoreException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
                    BurpExtender.issueAlert(getTabCaption(), Util.getStackTraceMessage(ex), TrayIcon.MessageType.ERROR);
                    this.btnServerStart.setSelected(false);
                    Logger.getLogger(OCSPServerTab.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
                    BurpExtender.issueAlert(getTabCaption(), Util.getStackTraceMessage(ex), TrayIcon.MessageType.ERROR);
                    this.btnServerStart.setSelected(false);
                    Logger.getLogger(OCSPServerTab.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnrecoverableKeyException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), getTabCaption(), JOptionPane.ERROR_MESSAGE);
                    BurpExtender.issueAlert(getTabCaption(), Util.getStackTraceMessage(ex), TrayIcon.MessageType.ERROR);
                    this.btnServerStart.setSelected(false);
                    Logger.getLogger(OCSPServerTab.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            this.stopThreadServer();
        }

    }//GEN-LAST:event_btnServerStartActionPerformed
    
    private void spnListenPortStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnListenPortStateChanged
        this.firePropertyChange(IOptionProperty.OCSP_PROPERTY, null, this.getOCSPProperty());
    }//GEN-LAST:event_spnListenPortStateChanged

    private void rdoBurpCAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rdoBurpCAStateChanged
        this.firePropertyChange(IOptionProperty.OCSP_PROPERTY, null, this.getOCSPProperty());
    }//GEN-LAST:event_rdoBurpCAStateChanged

    private void rdoCustomCAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rdoCustomCAStateChanged
        SwingUtil.setContainerEnable(this.pnlCustomCA, this.rdoCustomCA.isSelected());
        this.firePropertyChange(IOptionProperty.OCSP_PROPERTY, null, this.getOCSPProperty());
    }//GEN-LAST:event_rdoCustomCAStateChanged

    private void chkAutoStartStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkAutoStartStateChanged
        this.firePropertyChange(IOptionProperty.OCSP_PROPERTY, null, this.getOCSPProperty());
    }//GEN-LAST:event_chkAutoStartStateChanged

    private void btnServerStartStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnServerStartStateChanged
        if (this.btnServerStart.isSelected()) {
            this.btnServerStart.setText("Stop");
        } else {
            this.btnServerStart.setText("Start");
        }
    }//GEN-LAST:event_btnServerStartStateChanged

    final static FileFilter FILTER_PKCS12 = new FileNameExtensionFilter("certificate file(*.p12;*.pfx)", "p12", "pfx");

    private void btnSelectCustomCAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectCustomCAActionPerformed
        JFileChooser filechooser = new JFileChooser();
        filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        filechooser.addChoosableFileFilter(FILTER_PKCS12);
        filechooser.setFileFilter(FILTER_PKCS12);
        filechooser.setSelectedFile(new File(this.txtCAFile.getText()));
        int selected = filechooser.showOpenDialog(this);
        if (selected == JFileChooser.APPROVE_OPTION) {
            File file = filechooser.getSelectedFile();
            this.txtCAFile.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_btnSelectCustomCAActionPerformed

    private void chkNonMaskPasswordStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkNonMaskPasswordStateChanged
        if (this.chkNonMaskPassword.isSelected()) {
            this.txtCApassword.setEchoChar((char) 0);
        } else {
            this.txtCApassword.setEchoChar('*');
        }
    }//GEN-LAST:event_chkNonMaskPasswordStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGrpCACertificate;
    private javax.swing.JButton btnSelectCustomCA;
    private javax.swing.JToggleButton btnServerStart;
    private javax.swing.JCheckBox chkAutoStart;
    private javax.swing.JCheckBox chkNonMaskPassword;
    private javax.swing.JLabel lblListenPort;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JPanel pnlCaCertificate;
    private javax.swing.JPanel pnlCustomCA;
    private javax.swing.JRadioButton rdoBurpCA;
    private javax.swing.JRadioButton rdoCustomCA;
    private javax.swing.JSpinner spnListenPort;
    private javax.swing.JTextField txtCAFile;
    private javax.swing.JPasswordField txtCApassword;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getTabCaption() {
        return "OCSP Server";
    }

    @Override
    public Component getUiComponent() {
        return this;
    }

    private void customizeComponents() {
        IOptionProperty option = BurpExtender.getInstance().getProperty();
        OCSPProperty ocsp = option.getOCSPProperty();
        if (ocsp.isAutoStart()) {
            this.btnServerStart.doClick();
        }
        SwingUtil.setContainerEnable(this.pnlCustomCA, this.rdoCustomCA.isSelected());
    }

    public void setOCSPProperty(OCSPProperty ocspProperty) {
        this.chkAutoStart.setSelected(ocspProperty.isAutoStart());
        this.spnListenPort.setValue(ocspProperty.getListenPort());
        if (ocspProperty.getCACertificateType() == CACertificateType.BurpCA) {
            this.rdoBurpCA.setSelected(true);
        } else {
            this.rdoCustomCA.setSelected(true);
            this.txtCAFile.setText(String.valueOf(ocspProperty.getCaFile()));
            this.txtCApassword.setText(ocspProperty.getPassword());
        }
    }

    public OCSPProperty getOCSPProperty() {
        OCSPProperty ocspProperty = new OCSPProperty();
        ocspProperty.setAutoStart(this.chkAutoStart.isSelected());
        ocspProperty.setListenPort((int) this.spnListenPort.getValue());
        if (this.rdoBurpCA.isSelected()) {
            ocspProperty.setCACertificateType(CACertificateType.BurpCA);
        } else {
            ocspProperty.setCACertificateType(CACertificateType.CustomCA);
        }
        ocspProperty.setCaFile(new File(this.txtCAFile.getText()));
        ocspProperty.setPassword(String.valueOf(this.txtCApassword.getPassword()));
        return ocspProperty;
    }

    protected void stopThreadServer() {
        if (this.thredServer != null) {
            this.thredServer.stopServer();
            this.thredServer = null;
        }    
    }
        
    @Override
    public void extensionUnloaded() {
        this.stopThreadServer();
    }

}
