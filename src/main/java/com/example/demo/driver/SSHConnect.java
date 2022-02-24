package com.example.demo.driver;

import org.apache.commons.io.IOUtils;
import com.example.demo.entity.base.SshUser;
import com.jcraft.jsch.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author: yd
 * @date: 2022-02-15
 * @version: 1.0
 * @description: ssh链接
 * @modifiedBy:
 */
@Component
@Log4j2
public class SSHConnect {
    /**
     * 使用方法：
     * 1、先调用createSshConnect() 方法
     * 2、executeCommand() 执行shell 命令
     * 3、最后调用disconnect() 关闭连接
     */

    @Autowired
    SshUser sshUser;
    private Session session;
    /**
     *     static Channel getChannel(String type) {
     *         if (type.equals("session")) {
     *             return new ChannelSession();
     *         } else if (type.equals("shell")) {
     *             return new ChannelShell();
     *         } else if (type.equals("exec")) {
     *             return new ChannelExec();
     *         } else if (type.equals("x11")) {
     *             return new ChannelX11();
     *         } else if (type.equals("auth-agent@openssh.com")) {
     *             return new ChannelAgentForwarding();
     *         } else if (type.equals("direct-tcpip")) {
     *             return new ChannelDirectTCPIP();
     *         } else if (type.equals("forwarded-tcpip")) {
     *             return new ChannelForwardedTCPIP();
     *         } else if (type.equals("sftp")) {
     *             return new ChannelSftp();
     *         } else {
     *             return type.equals("subsystem") ? new ChannelSubsystem() : null;
     *         }
     *     }
     */
    // ChannelExec负责通过SSH执行Shell命令
//    private final String type = "exec";


    public void createSshConnect(String ip) {
        final JSch jSch = new JSch();
        sshUser.setServerIP(ip);

        if(sshUser.getPort() == 0){
            sshUser.setPort(22);
        }
        try {
            session = jSch.getSession(sshUser.getUserName(), sshUser.getServerIP(), sshUser.getPort());log.debug("ssh session created!");
            if(ObjectUtils.isEmpty(session)){
                log.info("ssh链接失败, 参数：{}", sshUser.toString());
                return;
            }
            session.setPassword(sshUser.getPassword());

            Properties config = new Properties();
            //跳过RSA key fingerprint输入yes/no
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config); // 为Session对象设置properties
            session.setTimeout(30000); // 设置timeout时间
            session.connect(); // 通过Session建立链接
            log.debug("Session connected.");
        } catch (JSchException e) {
            log.error("ssh 链接失败，详细信息：{}", e.getMessage());
            disconnect();
        }
    }

    /**
     * ssh远程执行 shell命令
     * @param serverIP 需要连接的服务器ip
     * @param command 命令
     */
    public void executeCommand(String serverIP, String command){
        if (session != null) {
            disconnect();
            session = null;
        }
        createSshConnect(serverIP);
        executeCommand(command);
        disconnect();
    }
    public void executeCommand(String command){
        ChannelExec execChannel = null;

        if(session == null){
            log.error("ssh 执行命令失败，详细信息：命令：{}， 请先建立ssh链接！", command);
            return;
        }

        try {
            execChannel = (ChannelExec) session.openChannel("exec");
            InputStream outputStream = execChannel.getInputStream();
            execChannel.setCommand(command);
            execChannel.connect();
            String s = IOUtils.toString(outputStream, StandardCharsets.UTF_8);
            System.out.println(s);
        } catch (JSchException ex) {
            log.error("ssh 执行命令失败，详细信息：{}", ex.getMessage());
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (execChannel != null) {
                execChannel.disconnect();
            }
        }

    }

    public void disconnect(){
        if(null != session){
            session.disconnect();
            log.debug("closed connected");
        }
    }

}
