
# 500px 图片下载器（学习/研究版） 🖼️

![GitHub stars](https://img.shields.io/github/stars/Adminstorter/500px-downloader?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/Adminstorter/500px-downloader?style=for-the-badge)
![GitHub license](https://img.shields.io/github/Adminstorter/500px-downloader?style=for-the-badge)

---

> ⚠️ **免责声明**
>
> 本程序仅用于 **学习和研究目的**，禁止用于下载受版权保护的图片进行商业或公开传播。  
> 作者不对因使用本程序造成的任何版权纠纷负责。

---

## 🎨 项目截图

<div align="center">
<img src="screenshot.png" alt="程序截图" width="600">
</div>

---

## ✨ 功能特性

- **粘贴链接自动添加**：从剪贴板粘贴 500px 图片链接，自动添加到下载列表  
- **批量下载**：多张图片同时下载，自动去重避免覆盖  
- **下载进度**：下载过程中显示进度条  
- **文件命名**：根据图片 ID 自动生成文件名，避免冲突  

---

## 🛠️ 使用教程

### 1️⃣ 克隆仓库

```bash
git clone httAdminstrator/Adminstorter/500px-downloader.git
````

### 2️⃣ 运行程序

* 使用 Java IDE（IntelliJ IDEA / Eclipse）打开项目
* 运行 `Downloader500pxGUI.java`

### 3️⃣ 下载图片

1. 粘贴 500px 图片链接到输入框
2. 点击 **“粘贴”** 按钮添加链接
3. 点击 **“下载”** 开始批量下载
4. 下载完成后，图片将保存在 `downloads` 文件夹

---

## 💻 使用示例

```java
// 添加链接
urlList.add("https://500px.com/photo/1115951985/example");

// 开始下载
new DownloadTask(url).execute();
```

下载后的文件命名示例：

```
1115951985_夜色_by_yoohoo.jpg
1115951985_夜色_by_yoohoo_1.jpg
```

---

## ⚠️ 安全提醒

> * 本程序仅限学习、研究和技术交流用途
> * 不得用于下载受版权保护的图片用于商业或公开传播
> * 使用过程中请遵守 500px 网站的服务条款

---

## 🛠 技术栈

* Java 17+
* Swing GUI
* JSON 解析 (`org.json`)

---

## 📄 开源许可

本项目遵循 **MIT 许可证**（MIT License），允许学习、修改和个人使用，但不保证可用于商业用途。

---

## 💡 提示

* 请勿用于爬取大量图片，否则可能违反 500px 服务条款
* 建议仅用于个人学习、调试和研究代码

