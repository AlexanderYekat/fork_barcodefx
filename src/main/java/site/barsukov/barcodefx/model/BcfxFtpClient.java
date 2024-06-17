package site.barsukov.barcodefx.model;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BcfxFtpClient {
    private String server;
    private int port;
    private String user;
    private String password;
    private FTPClient ftp;
    private boolean active;

    // constructor

    public BcfxFtpClient(PickingServer pickingServer) {
        this.server = pickingServer.getPath();
        this.port = pickingServer.getPort();
        this.user = pickingServer.getLogin();
        this.password = pickingServer.getPassword();
        this.active = pickingServer.isActive();
    }

    public boolean testConnection() {
        try {
            open();
            close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void open() throws IOException {
        ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, port);

        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        if (!active) {
            ftp.enterLocalPassiveMode();
        }
        ftp.login(user, password);
    }

    public void close() throws IOException {
        ftp.disconnect();
    }

    public boolean sendFile(File file) throws IOException {
        return ftp.storeFile(FilenameUtils.getName(file.getAbsolutePath()), new FileInputStream(file));
    }

    public boolean downloadFile(String source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(destination);
        return ftp.retrieveFile("/" + source, out);
    }

    public List<String> getFilesList() throws IOException {
        FTPFile[] files = ftp.listFiles("");
        return Arrays.stream(files)
            .map(FTPFile::getName)
            .collect(Collectors.toList());
    }

    public void deleteFile(String remoteFileName) throws IOException {
        ftp.deleteFile(remoteFileName);
    }
}
