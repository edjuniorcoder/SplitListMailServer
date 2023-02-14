/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.edjuniorcoder.model;

/**
 *
 * @author jerry
 */
public class DadosLogin {

    private String username;
    private String pass;
    private String port;
    private String server;
    private String protocoloType;

    public DadosLogin() {
    }

    public String getProtocoloType() {
        return protocoloType;
    }

    public void setProtocoloType(String protocoloType) {
        this.protocoloType = protocoloType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String toString() {

        return username + ":" + pass + ":" + server + ":" + port + ":" + protocoloType;
    }

}
