package application.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class ImageDownloadTask extends Task<Void> {

	private StringProperty fileName;
	private StringProperty fileSize;
	private StringProperty directory;
	private StringProperty url;

	private UrlScanTask urlScanTask;

	private long size = 0;

	private int retry = 3;
	
	private Object lock = new Object();

	/**
	 * Default constructor.
	 */
	public ImageDownloadTask() {
		this(null, null, null, null);
	}

	/**
	 * Constructor with some initial data.
	 * 
	 */
	public ImageDownloadTask(String fileName, String directory, String url,
			UrlScanTask urlScanTask) {
		this.fileName = new SimpleStringProperty(fileName);
		this.directory = new SimpleStringProperty(directory);
		this.url = new SimpleStringProperty(url);
		this.urlScanTask = urlScanTask;

		this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
		this.updateMessage("等待排程");
	}

	/**
	 * Constructor with some initial data.
	 * 
	 */
	public ImageDownloadTask(String fileName, String directory, String url) {
		this.fileName = new SimpleStringProperty(fileName);
		this.directory = new SimpleStringProperty(directory);
		this.url = new SimpleStringProperty(url);

		this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
		this.updateMessage("等待排程");
	}

	public String getFileName() {
		return fileName.get();
	}

	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}

	public StringProperty getFileNameProperty() {
		if (fileName == null)
			this.fileName = new SimpleStringProperty(this, "fileName");
		return fileName;
	}

	public String getFileSize() {
		return fileSize.get();
	}

	public void setFileSize(String firstName) {
		getFileSizeProperty().set(firstName);
	}

	public StringProperty getFileSizeProperty() {
		if (fileSize == null)
			this.fileSize = new SimpleStringProperty(this, "fileSize");
		return fileSize;
	}

	public String getDirectory() {
		return directory.get();
	}

	public void setDirectory(String directory) {
		this.directory.set(directory);
	}

	public StringProperty getDirectoryProperty() {
		if (directory == null)
			this.directory = new SimpleStringProperty(this, "directory");
		return directory;
	}

	public String getUrl() {
		return url.get();
	}

	public void setUrl(String url) {
		this.url.set(url);
	}

	public StringProperty getUrlProperty() {
		if (url == null)
			this.url = new SimpleStringProperty(this, "url");
		return url;
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub

		this.updateMessage("下載中");

		// check Folder for picture
		File dir = new File(getDirectory());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = null;

		File imageFile = new File(getDirectory() + "/" + getFileName());
		if (imageFile.exists()) {
			Thread.sleep(100);

			size = imageFile.length();

			return null;
		}

		URL url = null;
		try {
			url = new URL(getUrl());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpURLConnection conn = null;
		do {
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
			} catch (SocketTimeoutException e) {
				System.out.println(getFileName() + " : Socket Timeout");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (retry-- > 0)
					continue;
				else {
					throw new Exception();
				}
			} catch (IOException e) {
				System.out.println(getFileName() + " : URL Connection fail");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (retry-- > 0)
					continue;
				else {
					throw new Exception();
				}
			}

			size = conn.getContentLength();
			if (size > 0)
				break;
			else {
				System.out.println(getFileName() + " file size can't get.");
				if (retry-- <= 0)
					break;
				if (conn != null)
					conn.disconnect();
				Thread.sleep(1000);
			}
		} while (true);

		DataInputStream dis = null;
		DataOutputStream dos = null;

		while (true) {
			try {
				dis = new DataInputStream(new BufferedInputStream(
						conn.getInputStream()));
				dos = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(imageFile)));
				break;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(getFileName() + " get InputSream fail.");
				conn.disconnect();
				try {
					Thread.sleep(1000);
					conn = (HttpURLConnection) url.openConnection();
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		while (true) {

			byte b = 0;

			try {

				while (true) {
					b = dis.readByte();
					dos.writeByte(b);
				}

			} catch (EOFException e) {

				try {
					dos.writeByte(b);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (imageFile.length() < size) {
					// System.out.println("The file is downloaded incompletely.");
					// retry++;
					continue;
				}

				break;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(e.toString() + " : " + getFileName());
			}
		}
		
		dis.close();
		dos.close();
		conn.disconnect();

		if (size < 0) {
			size = imageFile.length();
		}
		
		final CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(()->{
			try{
				setFileSize(String.format("%.1f KB",
					(size > 0) ? ((float) size / 1024) : 0));
				updateProgress(1, 1);
				updateMessage("下載完成");
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				latch.countDown();
			}			
		});
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void failed() {
		// TODO Auto-generated method stub
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				updateMessage("下載失敗");
			}
		});
		super.failed();
	}

	@Override
	protected void succeeded() {
		// TODO Auto-generated method stub	
		urlScanTask.increaseDownloaded();
		super.succeeded();
	}
}
