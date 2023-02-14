/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.edjuniorcoder.view;

import br.com.edjuniorcoder.core.ConectionSpliMail;
import br.com.edjuniorcoder.model.DadosLogin;
import br.com.edjuniorcoder.model.ResponseConection;
import br.com.edjuniorcoder.model.SettingsTemplate;
import com.google.common.collect.Lists;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DarkStar;
import java.applet.Applet;
import java.awt.SystemColor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jerry
 */
public class Home extends javax.swing.JFrame {

    private static String titleForm;
    private int totalThreads;
    private int totalList;
    private int totalLive;
    private int totalDie;
    private int totalRetrie;
    private int totalToCheck;
    private int totalTestado;
    private int salvarRetestar = 5;
    private List<String> listaLogin = new ArrayList<String>();
    private List<String> listaProxy = new ArrayList<String>();
    private boolean start;
    private ExecutorService executor;
    private static final Object lockLive = new Object();
    private static final Object lockDie = new Object();
    private static final Object lockRetrie = new Object();
    private static final Object lockToCheck = new Object();
    private static final Object lock = new Object();

    private DefaultTableModel valStatus;
    private DefaultTableModel valTableProxy;
    DefaultTableModel valDie;
    DefaultTableModel valLive;
    DefaultTableModel valRetrie;
    DefaultTableModel valToCheck;

    private Object o = new Object();
    private volatile boolean suspended = false;
    private Properties prop;
    private Properties propProxyList;
    private Properties configsProxy;
    private Properties configsValidacoes;
    int validaPosProxy;

    /**
     * Creates new form Home
     */
    public Home() {
        initComponents();
        formatarForm();
        titleForm = getTitle();

        valStatus = (DefaultTableModel) tableStatus.getModel();
        valLive = (DefaultTableModel) tableLive.getModel();
        valDie = (DefaultTableModel) tableDie.getModel();
        valRetrie = (DefaultTableModel) tableRetrie.getModel();
        valToCheck = (DefaultTableModel) tableToCheck.getModel();

        totalThreads = jBots.getValue();
        
        
        configsValidacoes = new Properties();
        setPropriedadesValidacoes();
    }
    
     private void setPropriedadesValidacoes() {

        configsValidacoes = getPropriedadesValidacoes();

        try {

            cbPorta1.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbPorta1")));
            cbPorta2.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbPorta2")));
            cbPorta3.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbPorta3")));

            cbServer1.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbServer1")));
            cbServer2.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbServer2")));
            cbServer3.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbServer3")));
            cbServer4.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbServer4")));

            cbValidarEmail.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbValidarEmail")));
            cbMX.setSelected(Boolean.parseBoolean(configsValidacoes.getProperty("config.cbMX")));

        } catch (Exception ex) {

        }
    }

    public static Properties getPropriedadesValidacoes() {
        Properties prop = new Properties();

        try {

            new File("./configs").mkdirs();
            String apiName = "./configs/configs.Validacoes" + titleForm;
            new FileWriter(apiName, true);
            InputStream input = new FileInputStream("./configs/configs.Validacoes" + titleForm);

            // load a properties file
            prop.load(input);

        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }

        return prop;
    }

    private void formatarForm() {

        PlasticLookAndFeel.setPlasticTheme(new DarkStar());
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {
        }

        SwingUtilities.updateComponentTreeUI(this);

    }

    private void zerarContadores() {

        //Reseta os contadores
        totalLive = 0;
        totalDie = 0;
        totalRetrie = 0;
        totalToCheck = 0;
        totalTestado = 0;

        //Reseta os jLabels
        this.txtDies.setText("0");
        this.txtLives.setText("0");
        this.txtRetries.setText("0");
        this.txtToCheck.setText("0");
        this.txtTotalList.setText(String.valueOf(totalList));

        //reseta as tabelas
        ((DefaultTableModel) tableLive.getModel()).setNumRows(0);
        ((DefaultTableModel) tableDie.getModel()).setNumRows(0);
        ((DefaultTableModel) tableRetrie.getModel()).setNumRows(0);
        ((DefaultTableModel) tableToCheck.getModel()).setNumRows(0);
        ((DefaultTableModel) tableStatus.getModel()).setNumRows(0);

        btAddList.setEnabled(false);
    }

    private void atualizarContadores() {

        new Thread(new Runnable() {
            public void run() {

                while (!executor.isTerminated()) {
                    jProgress.setText("PROGRESS: " + totalTestado + " / " + totalList);
                    txtRetries.setText(String.valueOf(totalRetrie));
                    txtLives.setText(String.valueOf(totalLive));
                    txtDies.setText(String.valueOf(totalDie));
                    txtToCheck.setText(String.valueOf(totalToCheck));
                }
            }
        }).start();

    }

    private void checkStatusBoots() {

        new Thread(new Runnable() {
            public void run() {

                while (!executor.isTerminated()) {
                }

                btStart.setText("START");
                btStop.setEnabled(false);
                btAddList.setEnabled(true);

            }
        }).start();

    }

    public void resume() {
        suspended = false;
        synchronized (o) {
            o.notifyAll();
        }
    }

    public void suspend() {
        suspended = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jGuiasPrincipal = new javax.swing.JTabbedPane();
        jHome = new javax.swing.JPanel();
        jHeader = new javax.swing.JPanel();
        jProgressBar = new javax.swing.JProgressBar();
        jProgress = new javax.swing.JLabel();
        jBots = new javax.swing.JSlider();
        txtBots = new javax.swing.JLabel();
        jToolBar5 = new javax.swing.JToolBar();
        btAddList = new javax.swing.JButton();
        btStart = new javax.swing.JButton();
        btStop = new javax.swing.JButton();
        jBody = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableStatus = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableLive = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableDie = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableRetrie = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableToCheck = new javax.swing.JTable();
        jFooter = new javax.swing.JPanel();
        jToolBar8 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        txtTotalList = new javax.swing.JLabel();
        jToolBar9 = new javax.swing.JToolBar();
        jLabel3 = new javax.swing.JLabel();
        txtLives = new javax.swing.JLabel();
        jToolBar10 = new javax.swing.JToolBar();
        jLabel4 = new javax.swing.JLabel();
        txtDies = new javax.swing.JLabel();
        jToolBar11 = new javax.swing.JToolBar();
        jLabel5 = new javax.swing.JLabel();
        txtRetries = new javax.swing.JLabel();
        jToolBar12 = new javax.swing.JToolBar();
        jLabel6 = new javax.swing.JLabel();
        txtToCheck = new javax.swing.JLabel();
        jSetting = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        panelValidacoes = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        cbPorta1 = new javax.swing.JCheckBox();
        cbPorta2 = new javax.swing.JCheckBox();
        cbPorta3 = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        cbServer3 = new javax.swing.JCheckBox();
        cbServer1 = new javax.swing.JCheckBox();
        cbServer2 = new javax.swing.JCheckBox();
        cbServer4 = new javax.swing.JCheckBox();
        cbMX = new javax.swing.JCheckBox();
        cbValidarEmail = new javax.swing.JCheckBox();
        jToolBar2 = new javax.swing.JToolBar();
        jButton7 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        btSettingsMail = new javax.swing.JButton();
        panelProxy = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tableProxy = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        cbUseProxy = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        btSalvarProxy = new javax.swing.JButton();
        panelCaptcha = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        cbCaptcha = new javax.swing.JCheckBox();
        jButton8 = new javax.swing.JButton();
        btSalvarCaptcha = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        txtIdCaptcha = new javax.swing.JTextField();
        cbCapcha = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        txtPortCaptcha = new javax.swing.JTextField();
        txtServerCaptcha = new javax.swing.JTextField();
        btSaldo2Captcha = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lbServer = new javax.swing.JLabel();
        lbPort = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SplitListMailServer - By Edson Júnior");

        jHome.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jHeader.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jProgressBar.setForeground(new java.awt.Color(51, 153, 0));
        jProgressBar.setStringPainted(true);

        jProgress.setText("PROGRESS: ");

        jBots.setMaximum(2000);
        jBots.setMinimum(1);
        jBots.setValue(100);
        jBots.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jBotsStateChanged(evt);
            }
        });

        txtBots.setText("Threads: 100");

        jToolBar5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar5.setRollover(true);

        btAddList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-adicionar-24.png"))); // NOI18N
        btAddList.setText("ADD LIST");
        btAddList.setToolTipText("Format: Email:Password");
        btAddList.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btAddList.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btAddList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btAddListMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btAddListMouseExited(evt);
            }
        });
        btAddList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddListActionPerformed(evt);
            }
        });
        jToolBar5.add(btAddList);

        btStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play_24x24.png"))); // NOI18N
        btStart.setText("START");
        btStart.setToolTipText("Start");
        btStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btStart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btStartMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btStartMouseExited(evt);
            }
        });
        btStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btStartActionPerformed(evt);
            }
        });
        jToolBar5.add(btStart);

        btStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop_24x24.png"))); // NOI18N
        btStop.setText("STOP");
        btStop.setToolTipText("Stop");
        btStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btStop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btStopMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btStopMouseExited(evt);
            }
        });
        btStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btStopActionPerformed(evt);
            }
        });
        jToolBar5.add(btStop);

        javax.swing.GroupLayout jHeaderLayout = new javax.swing.GroupLayout(jHeader);
        jHeader.setLayout(jHeaderLayout);
        jHeaderLayout.setHorizontalGroup(
            jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jHeaderLayout.createSequentialGroup()
                        .addGroup(jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jHeaderLayout.createSequentialGroup()
                                .addComponent(jProgress)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jHeaderLayout.createSequentialGroup()
                        .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jHeaderLayout.createSequentialGroup()
                                .addComponent(jBots, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jHeaderLayout.createSequentialGroup()
                                .addComponent(txtBots)
                                .addGap(127, 127, 127))))))
        );
        jHeaderLayout.setVerticalGroup(
            jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jHeaderLayout.createSequentialGroup()
                .addGroup(jHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jHeaderLayout.createSequentialGroup()
                        .addComponent(txtBots)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBots, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jProgress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jBody.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tableStatus.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Data", "Status"
            }
        ));
        jScrollPane1.setViewportView(tableStatus);

        tableLive.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User", "Pass", "Server"
            }
        ));
        jScrollPane2.setViewportView(tableLive);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Live", jPanel2);

        tableDie.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User", "Pass", "Status"
            }
        ));
        jScrollPane3.setViewportView(tableDie);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Die", jPanel1);

        tableRetrie.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User", "Pass", "Message"
            }
        ));
        jScrollPane4.setViewportView(tableRetrie);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Retrie", jPanel3);

        tableToCheck.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User", "Pass", "Message"
            }
        ));
        jScrollPane5.setViewportView(tableToCheck);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("ToCheck", jPanel4);

        javax.swing.GroupLayout jBodyLayout = new javax.swing.GroupLayout(jBody);
        jBody.setLayout(jBodyLayout);
        jBodyLayout.setHorizontalGroup(
            jBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBodyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jBodyLayout.setVerticalGroup(
            jBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jBodyLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jFooter.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jToolBar8.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar8.setRollover(true);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Total:");
        jToolBar8.add(jLabel2);

        txtTotalList.setText("0");
        jToolBar8.add(txtTotalList);

        jToolBar9.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar9.setRollover(true);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 204, 0));
        jLabel3.setText("Lives:");
        jToolBar9.add(jLabel3);

        txtLives.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtLives.setForeground(new java.awt.Color(51, 204, 0));
        txtLives.setText("0");
        jToolBar9.add(txtLives);

        jToolBar10.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar10.setRollover(true);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 51, 0));
        jLabel4.setText("Dies:");
        jToolBar10.add(jLabel4);

        txtDies.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtDies.setForeground(new java.awt.Color(204, 51, 0));
        txtDies.setText("0");
        jToolBar10.add(txtDies);

        jToolBar11.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar11.setRollover(true);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(204, 204, 0));
        jLabel5.setText("Retrie:");
        jToolBar11.add(jLabel5);

        txtRetries.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtRetries.setForeground(new java.awt.Color(204, 204, 0));
        txtRetries.setText("0");
        jToolBar11.add(txtRetries);

        jToolBar12.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar12.setRollover(true);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 204, 102));
        jLabel6.setText("ToCheck:");
        jToolBar12.add(jLabel6);

        txtToCheck.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtToCheck.setForeground(new java.awt.Color(255, 204, 102));
        txtToCheck.setText("0");
        jToolBar12.add(txtToCheck);

        javax.swing.GroupLayout jFooterLayout = new javax.swing.GroupLayout(jFooter);
        jFooter.setLayout(jFooterLayout);
        jFooterLayout.setHorizontalGroup(
            jFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFooterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jFooterLayout.setVerticalGroup(
            jFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFooterLayout.createSequentialGroup()
                .addComponent(jToolBar8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jHomeLayout = new javax.swing.GroupLayout(jHome);
        jHome.setLayout(jHomeLayout);
        jHomeLayout.setHorizontalGroup(
            jHomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jFooter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jBody, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jHomeLayout.setVerticalGroup(
            jHomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jHomeLayout.createSequentialGroup()
                .addComponent(jHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBody, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jGuiasPrincipal.addTab("Home", jHome);

        cbPorta1.setText("993");
        cbPorta1.setEnabled(false);

        cbPorta2.setText("143");
        cbPorta2.setEnabled(false);

        cbPorta3.setText("443");
        cbPorta3.setEnabled(false);

        jLabel11.setText("Test Ports:");
        jLabel11.setEnabled(false);

        jLabel12.setText("Teste Servers:");
        jLabel12.setEnabled(false);

        cbServer3.setText("host");
        cbServer3.setToolTipText("exemplo: gmail.com");
        cbServer3.setEnabled(false);

        cbServer1.setText("imap.host");
        cbServer1.setToolTipText("exemplo: imap.gmail.com");
        cbServer1.setEnabled(false);

        cbServer2.setText("mail.host");
        cbServer2.setToolTipText("exemplo: mail.gmail.com");
        cbServer2.setEnabled(false);

        cbServer4.setText("webmail.host");
        cbServer4.setToolTipText("exemplo: webmail.gmail.com");
        cbServer4.setEnabled(false);

        cbMX.setText("valid servers MX");

        cbValidarEmail.setSelected(true);
        cbValidarEmail.setText("valid formatt email");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(cbValidarEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbMX))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbServer1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbServer2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbServer3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbServer4))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbPorta1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbPorta2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbPorta3)))))
                .addContainerGap(554, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbValidarEmail)
                    .addComponent(cbMX))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbPorta1)
                    .addComponent(cbPorta2)
                    .addComponent(cbPorta3)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(cbServer3)
                    .addComponent(cbServer1)
                    .addComponent(cbServer2)
                    .addComponent(cbServer4))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        jToolBar2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar2.setRollover(true);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-vassoura-24.png"))); // NOI18N
        jButton7.setText("Clear");
        jButton7.setToolTipText("Limpar lista");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton7);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-verificar-todos-os-24.png"))); // NOI18N
        jButton5.setText("Select All");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButton5);

        btSettingsMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-salvar-e-fechar-24.png"))); // NOI18N
        btSettingsMail.setText("Save");
        btSettingsMail.setToolTipText("Salvar");
        btSettingsMail.setFocusable(false);
        btSettingsMail.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btSettingsMail.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btSettingsMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSettingsMailActionPerformed(evt);
            }
        });
        jToolBar2.add(btSettingsMail);

        javax.swing.GroupLayout panelValidacoesLayout = new javax.swing.GroupLayout(panelValidacoes);
        panelValidacoes.setLayout(panelValidacoesLayout);
        panelValidacoesLayout.setHorizontalGroup(
            panelValidacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidacoesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelValidacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelValidacoesLayout.setVerticalGroup(
            panelValidacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelValidacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(424, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Configs Conections", panelValidacoes);

        jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tableProxy.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Host", "Port", "User", "Pass"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableProxy.setEnabled(false);
        jScrollPane8.setViewportView(tableProxy);

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setRollover(true);

        cbUseProxy.setText("Active Proxy");
        cbUseProxy.setEnabled(false);
        cbUseProxy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cbUseProxy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cbUseProxy.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbUseProxyStateChanged(evt);
            }
        });
        jToolBar1.add(cbUseProxy);

        jButton1.setText("Add");
        jButton1.setToolTipText("Adicionar Proxy");
        jButton1.setEnabled(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setText("Delete");
        jButton2.setToolTipText("Deletar proxy selecionado");
        jButton2.setEnabled(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setText("Import");
        jButton3.setToolTipText("Importar a partir de arquivo");
        jButton3.setEnabled(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton6.setText("Clear");
        jButton6.setToolTipText("Limpar lista");
        jButton6.setEnabled(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton4.setText("Test Proxy");
        jButton4.setEnabled(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        btSalvarProxy.setText("Save");
        btSalvarProxy.setEnabled(false);
        btSalvarProxy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btSalvarProxy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btSalvarProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarProxyActionPerformed(evt);
            }
        });
        jToolBar1.add(btSalvarProxy);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 953, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout panelProxyLayout = new javax.swing.GroupLayout(panelProxy);
        panelProxy.setLayout(panelProxyLayout);
        panelProxyLayout.setHorizontalGroup(
            panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProxyLayout.createSequentialGroup()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelProxyLayout.setVerticalGroup(
            panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProxyLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Proxy Http", panelProxy);

        jToolBar3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar3.setRollover(true);

        cbCaptcha.setText("Active Captcha");
        cbCaptcha.setEnabled(false);
        cbCaptcha.setFocusable(false);
        cbCaptcha.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cbCaptcha.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(cbCaptcha);

        jButton8.setText("Clear");
        jButton8.setToolTipText("Limpar lista");
        jButton8.setEnabled(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar3.add(jButton8);

        btSalvarCaptcha.setText("Save");
        btSalvarCaptcha.setEnabled(false);
        btSalvarCaptcha.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btSalvarCaptcha.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btSalvarCaptcha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarCaptchaActionPerformed(evt);
            }
        });
        jToolBar3.add(btSalvarCaptcha);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setEnabled(false);

        txtIdCaptcha.setEnabled(false);

        cbCapcha.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2Captcha", "Custom2Captcha" }));
        cbCapcha.setEnabled(false);
        cbCapcha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCapchaActionPerformed(evt);
            }
        });

        jLabel9.setText("Seu saldo é:");
        jLabel9.setEnabled(false);

        txtPortCaptcha.setEnabled(false);

        txtServerCaptcha.setEnabled(false);

        btSaldo2Captcha.setText("Checar Saldo");
        btSaldo2Captcha.setEnabled(false);

        jLabel7.setText("Service Captcha");
        jLabel7.setEnabled(false);

        jLabel8.setText("2Capcha API:");
        jLabel8.setEnabled(false);

        lbServer.setText("Server:");
        lbServer.setEnabled(false);

        lbPort.setText("Port:");
        lbPort.setEnabled(false);
        lbPort.setInheritsPopupMenu(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(btSaldo2Captcha))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbCapcha, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtIdCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(13, 13, 13)
                        .addComponent(lbServer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtServerCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbPort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPortCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbCapcha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtIdCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtServerCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPortCaptcha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbServer)
                    .addComponent(lbPort))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSaldo2Captcha)
                    .addComponent(jLabel9))
                .addContainerGap(140, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCaptchaLayout = new javax.swing.GroupLayout(panelCaptcha);
        panelCaptcha.setLayout(panelCaptchaLayout);
        panelCaptchaLayout.setHorizontalGroup(
            panelCaptchaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCaptchaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCaptchaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCaptchaLayout.createSequentialGroup()
                        .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelCaptchaLayout.setVerticalGroup(
            panelCaptchaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCaptchaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(314, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Captcha", panelCaptcha);

        javax.swing.GroupLayout jSettingLayout = new javax.swing.GroupLayout(jSetting);
        jSetting.setLayout(jSettingLayout);
        jSettingLayout.setHorizontalGroup(
            jSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );
        jSettingLayout.setVerticalGroup(
            jSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSettingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jGuiasPrincipal.addTab("Settings", jSetting);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jGuiasPrincipal)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jGuiasPrincipal)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btAddListMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btAddListMouseEntered
        // TODO add your handling code here:
        btAddList.setBackground(ColorUIResource.BLACK);
    }//GEN-LAST:event_btAddListMouseEntered

    private void btAddListMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btAddListMouseExited
        // TODO add your handling code here:
        btAddList.setBackground(SystemColor.GRAY);
    }//GEN-LAST:event_btAddListMouseExited

    private void btAddListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddListActionPerformed
        // TODO add your handling code here:

        new Thread(new Runnable() {

            public void run() {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Adicionar lista");
                //fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
                fileChooser.setCurrentDirectory(new java.io.File("."));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                FileNameExtensionFilter filter = new FileNameExtensionFilter("texto", "txt");
                fileChooser.setFileFilter(filter);

                Applet applet = new Applet(); //Cria um novo view para o chooser

                int retorno = fileChooser.showOpenDialog(applet);

                // 0 para endereço selecionado, 1 quando fecha sem selecionar nada
                if (retorno == 0) {
                    File file = fileChooser.getSelectedFile();

                    try {
                        BufferedReader in = new BufferedReader(new FileReader(file));

                        while (in.ready()) {
                            String dados = in.readLine();
                            listaLogin.add(new String(dados));
                        }
                        in.close();

                        totalList = listaLogin.size();
                        btStart.setEnabled(true);

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, "Adicione a lista a ser Testada");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex);

                    }

                }

            }
        }).start();
    }//GEN-LAST:event_btAddListActionPerformed

    private void btStartMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btStartMouseEntered
        // TODO add your handling code here:
        btStart.setBackground(ColorUIResource.BLACK);
    }//GEN-LAST:event_btStartMouseEntered

    private void btStartMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btStartMouseExited
        // TODO add your handling code here:
        btStart.setBackground(SystemColor.GRAY);
    }//GEN-LAST:event_btStartMouseExited

    private void btStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btStartActionPerformed
        // TODO add your handling code here:

        if (listaLogin.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Add List!");
        } /*else if (txtIdCaptcha.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Configure a API 2Captcha atráves do menu Settings");

        }*/ else {
            if (btStart.getText().equals("START")) {

                btStart.setText("PAUSE");
                btStop.setEnabled(true);
                zerarContadores();
                resume();

                //Inicia pools de threads
                executor = Executors.newFixedThreadPool(totalThreads);

                atualizarContadores();

                valStatus = (DefaultTableModel) tableStatus.getModel();

                jProgressBar.setMaximum(totalList);

                for (int i = 0; i < totalThreads; i++) {

                    //Cria a rotina
                    int numThread = i;

                    valStatus.addRow(new String[]{String.valueOf(i + 1), null, null});

                    executor.submit(new Runnable() {

                        @Override
                        public void run() {

                            ResponseConection responseCon = new ResponseConection();

                            while (!Thread.currentThread().isInterrupted()) {

                                SettingsTemplate settingsTemplate = new SettingsTemplate();

                                if (listaLogin.size() >= 1) {
                                    String linha = "";

                                    if (responseCon.getCodResponse() != 2) {
                                        synchronized (listaLogin) {
                                            linha = listaLogin.get(0);
                                            System.out.println("thread " + numThread + " " + linha);
                                            listaLogin.remove(0);
                                        }
                                    }

                                    //Inicia
                                    valStatus.setValueAt(linha, numThread, 1);
                                    valStatus.setValueAt("Start", numThread, 2);

                                    settingsTemplate.setUseProxy(cbUseProxy.isSelected());

                                    if (cbUseProxy.isSelected()) {
                                        String proxyLinha = getProxy(validaPosProxy);
                                        settingsTemplate.setValueProxyList(proxyLinha);
                                    }

                                    //Set configs ports
                                    settingsTemplate.setPortEmail1(cbPorta1.isSelected());
                                    settingsTemplate.setPortEmail2(cbPorta2.isSelected());
                                    settingsTemplate.setPortEmail3(cbPorta3.isSelected());

                                    //Set configs servers
                                    settingsTemplate.setServerEmail2(cbServer1.isSelected());
                                    settingsTemplate.setServerEmail3(cbServer2.isSelected());
                                    settingsTemplate.setServerEmail1(cbServer3.isSelected());
                                    settingsTemplate.setServerEmail4(cbServer4.isSelected());

                                    //Set port and server do front-end
                                    settingsTemplate.setPortText1(cbPorta1.getText());
                                    settingsTemplate.setPortText2(cbPorta2.getText());
                                    settingsTemplate.setPortText3(cbPorta3.getText());

                                    //Verificar servidores MX
                                    settingsTemplate.setVerifyMx(cbMX.isSelected());

                                    //Verificar formato email
                                    settingsTemplate.setVerifyFormattEmail(cbValidarEmail.isSelected());

                                    DadosLogin dadosLogin = new DadosLogin();

                                    String[] arrayLinha = null;
                                    if (linha.contains(":")) {
                                        arrayLinha = linha.split(":");
                                        
                                        if(arrayLinha.length == 1){
                                            dadosLogin.setUsername(arrayLinha[0]);
                                        }
                                        
                                        if(arrayLinha.length >= 2){
                                            dadosLogin.setUsername(arrayLinha[0]);
                                            dadosLogin.setPass(arrayLinha[1]);
                                        }
                                        
                                    }else{
                                        dadosLogin.setUsername(linha);
                                    }
                                    

                                    ConectionSpliMail con = new ConectionSpliMail();
                                    responseCon = con.conectar(dadosLogin, settingsTemplate);

                                //    System.out.println("responseCon.getCodResponse(): "+responseCon.getCodResponse());
                                    switch (responseCon.getCodResponse()) {
                                        case 0:
                        synchronized (lockLive) {
                                                valLive.addRow(new String[]{dadosLogin.getUsername(), dadosLogin.getPass(), responseCon.getStatusResponse()});
                                                valStatus.setValueAt("Live", numThread, 2);

                                                atualizaLives(linha, responseCon.getStatusResponse());
                                            }
                                            break;
                                        case 1:
                        synchronized (lockDie) {
                                                valDie.addRow(new String[]{dadosLogin.getUsername(), dadosLogin.getPass(), responseCon.getStatusResponse()});
                                                valStatus.setValueAt("Die", numThread, 2);
                                                atualizaDies(linha, responseCon.getStatusResponse());
                                            }
                                            break;
                                        case 2:
                        synchronized (lockRetrie) {
                                                valRetrie.addRow(new String[]{dadosLogin.getUsername(), dadosLogin.getPass(), responseCon.getStatusResponse()});
                                                valStatus.setValueAt("Retrie", numThread, 2);
                                                atualizaRetries();
                                            }

                                            break;
                                        case 3:
                        synchronized (lockToCheck) {
                                                valToCheck.addRow(new String[]{dadosLogin.getUsername(), dadosLogin.getPass(), responseCon.getStatusResponse()});
                                                valStatus.setValueAt("ToCheck", numThread, 2);
                                                atualizaToCheck(linha, responseCon.getStatusResponse());
                                            }

                                            break;

                                    }
                                } else {
                                    Thread.currentThread().interrupt();
                                    valStatus.setValueAt("", numThread, 1);
                                    valStatus.setValueAt("Finish", numThread, 2);
                                    break;

                                }

                            }

                        }
                    });
                }

                executor.shutdown();
                

                //Aguarda finalizar para resetar botões
                checkStatusBoots();

            } else if (btStart.getText().equals("PAUSE")) {
                btStart.setText("RESUME");
                suspend();

            } else if (btStart.getText().equals("RESUME")) {
                btStart.setText("PAUSE");
                resume();

            } else {
                btStart.setText("START");
                resume();
            }

        }
    }//GEN-LAST:event_btStartActionPerformed

    private String getProxy(int posLista) {
        String retorno = "";
        if (posLista < listaProxy.size()) {
            retorno = listaProxy.get(posLista);
        } else {
            validaPosProxy = 0;
            retorno = listaProxy.get(0);
        }

        return retorno;

    }

    private void btStopMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btStopMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_btStopMouseEntered

    private void btStopMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btStopMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_btStopMouseExited

    private void btStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btStopActionPerformed
        // TODO add your handling code here:

        btStart.setEnabled(true);
        btStop.setEnabled(false);

        btStart.setText("START");
        suspend();

        salvarRetestar = JOptionPane.showConfirmDialog(null, "Save not test?");

        if (salvarRetestar == 0) {
            resume();
        }
    }//GEN-LAST:event_btStopActionPerformed

    private void jBotsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jBotsStateChanged
        // TODO add your handling code here:
        totalThreads = jBots.getValue();
        txtBots.setText("BOTS: " + totalThreads);
    }//GEN-LAST:event_jBotsStateChanged

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:

        cbValidarEmail.setSelected(false);
        cbMX.setSelected(false);

        cbPorta1.setSelected(false);
        cbPorta2.setSelected(false);
        cbPorta3.setSelected(false);

        cbServer1.setSelected(false);
        cbServer2.setSelected(false);
        cbServer3.setSelected(false);
        cbServer4.setSelected(false);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        cbValidarEmail.setSelected(true);
        cbMX.setSelected(true);

        cbPorta1.setSelected(true);
        cbPorta2.setSelected(true);
        cbPorta3.setSelected(true);

        cbServer1.setSelected(true);
        cbServer2.setSelected(true);
        cbServer3.setSelected(true);
        cbServer4.setSelected(true);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void btSettingsMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSettingsMailActionPerformed
        // TODO add your handling code here:

        try (OutputStream output = new FileOutputStream("./configs/configs.Validacoes" + titleForm)) {

            configsValidacoes.setProperty("config.cbPorta1", String.valueOf(cbPorta1.isSelected()));
            configsValidacoes.setProperty("config.cbPorta2", String.valueOf(cbPorta2.isSelected()));
            configsValidacoes.setProperty("config.cbPorta3", String.valueOf(cbPorta3.isSelected()));

            configsValidacoes.setProperty("config.cbServer1", String.valueOf(cbServer1.isSelected()));
            configsValidacoes.setProperty("config.cbServer2", String.valueOf(cbServer2.isSelected()));
            configsValidacoes.setProperty("config.cbServer3", String.valueOf(cbServer3.isSelected()));
            configsValidacoes.setProperty("config.cbServer4", String.valueOf(cbServer4.isSelected()));

            configsValidacoes.setProperty("config.cbValidarEmail", String.valueOf(cbValidarEmail.isSelected()));
            configsValidacoes.setProperty("config.cbMX", String.valueOf(cbMX.isSelected()));

            // save properties to project root folder
            configsValidacoes.store(output, null);

        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }//GEN-LAST:event_btSettingsMailActionPerformed

    private void cbUseProxyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbUseProxyStateChanged
        // TODO add your handling code here:

        if (cbUseProxy.isSelected()) {
            tableProxy.setEnabled(true);
        } else {
            tableProxy.setEnabled(false);
        }
    }//GEN-LAST:event_cbUseProxyStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        valTableProxy.addRow(new String[]{});
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

        if (tableProxy.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione a linha a ser excluída!");
        } else if (tableProxy.getSelectedRow() >= 0) {
            valTableProxy.removeRow(tableProxy.getSelectedRow());
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:

        new Thread(new Runnable() {

            public void run() {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Adicionar lista");
                //fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
                fileChooser.setCurrentDirectory(new java.io.File("."));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                FileNameExtensionFilter filter = new FileNameExtensionFilter("texto", "txt");
                fileChooser.setFileFilter(filter);

                Applet applet = new Applet(); //Cria um novo view para o chooser

                int retorno = fileChooser.showOpenDialog(applet);

                // 0 para endereço selecionado, 1 quando fecha sem selecionar nada
                if (retorno == 0) {
                    File file = fileChooser.getSelectedFile();

                    try {
                        BufferedReader in = new BufferedReader(new FileReader(file));

                        while (in.ready()) {
                            String dados = in.readLine();

                            if (dados.contains(":")) {
                                String[] arrayLinha = dados.split(":");

                                if (arrayLinha.length == 4) {
                                    valTableProxy.addRow(new String[]{arrayLinha[0], arrayLinha[1], arrayLinha[2], arrayLinha[3]});
                                } else if (arrayLinha.length == 4) {
                                    valTableProxy.addRow(new String[]{arrayLinha[0], arrayLinha[1], arrayLinha[2], arrayLinha[3]});
                                } else {
                                    JOptionPane.showMessageDialog(null, "Padrão incorreto. Considere adicionar lista nos formatos abaixo: \n\n"
                                            + "host;port;user;pass \n"
                                            + "host;port");
                                }

                            } else {
                                JOptionPane.showMessageDialog(null, "Padrão incorreto. Considere adicionar lista nos formatos abaixo: \n\n"
                                        + "host:port:user:pass \n"
                                        + "host:port");
                            }

                        }
                        in.close();

                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, "Adicione a lista a ser Testada");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex);

                    }

                }

            }
        }).start();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        ((DefaultTableModel) tableProxy.getModel()).setNumRows(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:

        if (tableProxy.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione a linha a ser excluída!");
        } else if (tableProxy.getSelectedRow() >= 0) {

            int row = tableProxy.getSelectedRow();

            String host = tableProxy.getValueAt(row, 0).toString();
            String port = tableProxy.getValueAt(row, 1).toString();
            String user = tableProxy.getValueAt(row, 2).toString();
            String pass = tableProxy.getValueAt(row, 3).toString();

            //    PingProxy ping = new PingProxy(host, port, user, pass);
            //    ping.checkPing();
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void btSalvarProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarProxyActionPerformed
        // TODO add your handling code here:

        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < tableProxy.getRowCount(); i++) {
            String host = "";
            String port = "";
            String user = "";
            String pass = "";

            try {
                host = tableProxy.getValueAt(i, 0).toString();
            } catch (Exception ex) {
            }

            try {
                port = tableProxy.getValueAt(i, 1).toString();
            } catch (Exception ex) {
            }

            try {
                user = tableProxy.getValueAt(i, 2).toString();
            } catch (Exception ex) {
            }

            try {
                pass = tableProxy.getValueAt(i, 3).toString();
            } catch (Exception ex) {
            }

            if (!host.isEmpty() && !port.isEmpty() && !user.isEmpty() && !pass.isEmpty()) {
                listaProxy.add(host + ":" + port + ":" + user + ":" + pass);
                strBuilder.append(host + ":" + port + ":" + user + ":" + pass + ";");
            } else if (!host.isEmpty() && !port.isEmpty()) {
                listaProxy.add(host + ":" + port);
                strBuilder.append(host + ":" + port + ";");
            } else {
                System.out.println("host vazio");
            }
        }

        try (OutputStream output = new FileOutputStream("./configs/configs.Proxy" + titleForm)) {

            configsProxy.setProperty("config.cbUseProxy", String.valueOf(cbUseProxy.isSelected()));
            configsProxy.setProperty("config.proxy", strBuilder.toString());

            // save properties to project root folder
            configsProxy.store(output, null);

        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
    }//GEN-LAST:event_btSalvarProxyActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:

        cbCaptcha.setSelected(false);

        txtIdCaptcha.setText(null);
        txtServerCaptcha.setText(null);
        txtPortCaptcha.setText(null);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void btSalvarCaptchaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarCaptchaActionPerformed
        // TODO add your handling code here:
        try (OutputStream output = new FileOutputStream("./configs/configs.prop" + titleForm)) {

            prop.setProperty("config.cbCaptcha", String.valueOf(cbCaptcha.isSelected()));
            if (cbCapcha.getSelectedIndex() == 0) {

                // set the properties value
                prop.setProperty("config.pos", String.valueOf(cbCapcha.getSelectedIndex()));
                prop.setProperty("config.name", "2Captcha");
                prop.setProperty("config.token", txtIdCaptcha.getText());

                // save properties to project root folder
                prop.store(output, null);

                txtServerCaptcha.setText("");
                txtPortCaptcha.setText("");

            } else if (cbCapcha.getSelectedIndex() == 1) {

                // set the properties value
                prop.setProperty("config.pos", String.valueOf(cbCapcha.getSelectedIndex()));
                prop.setProperty("config.name", "Custon2Captcha");
                prop.setProperty("config.token", txtIdCaptcha.getText());
                prop.setProperty("config.server", txtServerCaptcha.getText());
                prop.setProperty("config.port", txtPortCaptcha.getText());

                // save properties to project root folder
                prop.store(output, null);

            }

        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }

    }//GEN-LAST:event_btSalvarCaptchaActionPerformed

    private void cbCapchaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCapchaActionPerformed
        // TODO add your handling code here:

        int bcSelected = cbCapcha.getSelectedIndex();

        if (bcSelected == 0) {
            lbServer.setVisible(false);
            lbPort.setVisible(false);
            txtServerCaptcha.setVisible(false);
            txtPortCaptcha.setVisible(false);

        } else if (bcSelected == 1) {
            lbServer.setVisible(true);
            lbPort.setVisible(true);
            txtServerCaptcha.setVisible(true);
            txtPortCaptcha.setVisible(true);
        }
    }//GEN-LAST:event_cbCapchaActionPerformed

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
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddList;
    private javax.swing.JButton btSaldo2Captcha;
    private javax.swing.JButton btSalvarCaptcha;
    private javax.swing.JButton btSalvarProxy;
    private javax.swing.JButton btSettingsMail;
    private javax.swing.JButton btStart;
    private javax.swing.JButton btStop;
    private javax.swing.JComboBox<String> cbCapcha;
    private javax.swing.JCheckBox cbCaptcha;
    private javax.swing.JCheckBox cbMX;
    private javax.swing.JCheckBox cbPorta1;
    private javax.swing.JCheckBox cbPorta2;
    private javax.swing.JCheckBox cbPorta3;
    private javax.swing.JCheckBox cbServer1;
    private javax.swing.JCheckBox cbServer2;
    private javax.swing.JCheckBox cbServer3;
    private javax.swing.JCheckBox cbServer4;
    private javax.swing.JCheckBox cbUseProxy;
    private javax.swing.JCheckBox cbValidarEmail;
    private javax.swing.JPanel jBody;
    private javax.swing.JSlider jBots;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JPanel jFooter;
    private javax.swing.JTabbedPane jGuiasPrincipal;
    private javax.swing.JPanel jHeader;
    private javax.swing.JPanel jHome;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jProgress;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JPanel jSetting;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar10;
    private javax.swing.JToolBar jToolBar11;
    private javax.swing.JToolBar jToolBar12;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar5;
    private javax.swing.JToolBar jToolBar8;
    private javax.swing.JToolBar jToolBar9;
    private javax.swing.JLabel lbPort;
    private javax.swing.JLabel lbServer;
    private javax.swing.JPanel panelCaptcha;
    private javax.swing.JPanel panelProxy;
    private javax.swing.JPanel panelValidacoes;
    private javax.swing.JTable tableDie;
    private javax.swing.JTable tableLive;
    private javax.swing.JTable tableProxy;
    private javax.swing.JTable tableRetrie;
    private javax.swing.JTable tableStatus;
    private javax.swing.JTable tableToCheck;
    private javax.swing.JLabel txtBots;
    private javax.swing.JLabel txtDies;
    private javax.swing.JTextField txtIdCaptcha;
    private javax.swing.JLabel txtLives;
    private javax.swing.JTextField txtPortCaptcha;
    private javax.swing.JLabel txtRetries;
    private javax.swing.JTextField txtServerCaptcha;
    private javax.swing.JLabel txtToCheck;
    private javax.swing.JLabel txtTotalList;
    // End of variables declaration//GEN-END:variables

    private void atualizaTestado() {
        totalTestado++;
        jProgressBar.setValue(totalTestado);
    }

    private void atualizaLives(String str, String str2) {
        totalTestado++;
        jProgressBar.setValue(totalTestado);
        totalLive++;

        try {
            new File("./Resultados/" + titleForm + "/").mkdirs();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./Resultados/" + titleForm + "/" + str2 + ".txt", true)));
            out.println(str);
            out.close();
            out.flush();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar login: " + ex);
        }
    }

    private void atualizaDies(String str, String str2) {
        totalTestado++;
        jProgressBar.setValue(totalTestado);
        totalDie++;

        try {
            new File("./Resultados/" + titleForm + "/").mkdirs();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./Resultados/" + titleForm + "/" + str2 + ".txt", true)));
            out.println(str);
            out.close();
            out.flush();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar login: " + ex);
        }

    }

    private void atualizaRetries() {
        totalRetrie++;
    }

    private void atualizaToCheck(String str, String str2) {
        totalTestado++;
        jProgressBar.setValue(totalTestado);
        totalToCheck++;

        try {

            new File("./Resultados/" + titleForm + "/").mkdirs();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./Resultados/" + titleForm + "/" + str2 + ".txt", true)));
            out.println(str);
            out.close();
            out.flush();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar login: " + ex);
        }

    }

}
