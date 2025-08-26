import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class Downloader500pxGUI extends JFrame {
    private static final String API_URL = "https://api.500px.com/v1/photos/%s?image_size[]=2048";

    private JTextField urlField;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private List<String> urlList = new ArrayList<>();

    public Downloader500pxGUI() {
        setTitle("Download For 500Px Photo Website");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 输入区
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        urlField = new JTextField();
        JButton pasteButton = new JButton("粘贴");
        JButton batchDownloadButton = new JButton("下载");

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.add(pasteButton);
        buttonPanel.add(batchDownloadButton);

        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // 日志区
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        // 进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        add(topPanel, BorderLayout.NORTH);
        add(logScroll, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        // 事件绑定
        pasteButton.addActionListener(this::onPaste);
        batchDownloadButton.addActionListener(this::onBatchDownload);
    }

    // 粘贴并自动添加
    private void onPaste(ActionEvent e) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String text = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (text != null && !text.trim().isEmpty()) {
                urlList.add(text.trim());
                appendLog("[已添加] " + text.trim());
                urlField.setText("");
            } else {
                appendLog("[提示] 剪贴板为空或无效内容");
            }
        } catch (Exception ex) {
            appendLog("[错误] 无法读取剪贴板: " + ex.getMessage());
        }
    }

    private void onBatchDownload(ActionEvent e) {
        if (urlList.isEmpty()) {
            appendLog("[提示] 没有要下载的链接");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                for (String url : urlList) {
                    appendLog("[处理] " + url);
                    new DownloadTask(url).execute();
                    try {
                        Thread.sleep(500); // 批量下载间隔
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                appendLog("[批量下载完成]");
                urlList.clear();
            }
        }.execute();
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // 下载任务
    private class DownloadTask extends SwingWorker<Void, Void> {
        private final String pageUrl;

        public DownloadTask(String pageUrl) {
            this.pageUrl = pageUrl;
        }

        @Override
        protected Void doInBackground() {
            String[] info = fetchImageInfo(pageUrl);
            if (info == null) {
                appendLog("[警告] 未找到图片链接: " + pageUrl);
                return null;
            }

            String imgUrl = info[0];
            String imgName = info[1];
            appendLog("[找到] " + imgUrl);
            downloadImage(imgUrl, imgName, "downloads");
            return null;
        }

        private String extractPhotoId(String url) {
            Matcher m = Pattern.compile("/photo/(\\d+)").matcher(url);
            return m.find() ? m.group(1) : null;
        }

        private String[] fetchImageInfo(String pageUrl) {
            String photoId = extractPhotoId(pageUrl);
            if (photoId == null) {
                appendLog("[错误] 无法提取 photo_id");
                return null;
            }
            try {
                String apiUrl = String.format(API_URL, photoId);
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                if (conn.getResponseCode() != 200) {
                    appendLog("[失败] API 请求错误: " + conn.getResponseCode());
                    return null;
                }

                String jsonText;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    jsonText = reader.lines().reduce("", (a, b) -> a + b);
                }

                JSONObject data = new JSONObject(jsonText).getJSONObject("photo");
                String imgUrl = data.getJSONArray("images").getJSONObject(0).getString("url");
                String imgName = data.optString("name", photoId);

                return new String[]{imgUrl, photoId + "_" + imgName};
            } catch (Exception e) {
                appendLog("[错误] API 异常: " + e.getMessage());
                return null;
            }
        }

        private Path getUniqueFilename(String folder, String name) {
            String safeName = name.replaceAll("[\\\\/*?:\"<>|]", "_") + ".jpg";
            Path path = Paths.get(folder, safeName);
            int counter = 1;
            while (Files.exists(path)) {
                path = Paths.get(folder, name.replaceAll("[\\\\/*?:\"<>|]", "_") + "_" + counter + ".jpg");
                counter++;
            }
            return path;
        }

        private void downloadImage(String imgUrl, String imgName, String outFolder) {
            try {
                Files.createDirectories(Paths.get(outFolder));
                Path filePath = getUniqueFilename(outFolder, imgName);

                HttpURLConnection conn = (HttpURLConnection) new URL(imgUrl).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int contentLength = conn.getContentLength();
                progressBar.setValue(0);

                if (conn.getResponseCode() == 200 && contentLength > 0) {
                    try (InputStream in = conn.getInputStream();
                         OutputStream out = Files.newOutputStream(filePath)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        int downloaded = 0;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            downloaded += bytesRead;
                            int progress = (int) ((downloaded / (double) contentLength) * 100);
                            setProgress(progress);
                            progressBar.setValue(progress);
                        }
                    }
                    appendLog("[完成] " + filePath);
                } else {
                    appendLog("[失败] 下载错误 " + conn.getResponseCode());
                }
            } catch (Exception e) {
                appendLog("[错误] 下载失败: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Downloader500pxGUI().setVisible(true));
    }
}
