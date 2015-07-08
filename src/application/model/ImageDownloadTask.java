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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class ImageDownloadTask extends Task<Void> {

	private StringProperty fileName;
	private StringProperty fileSize;
	private StringProperty directory;
	private StringProperty url;

	private static UrlScanTask urlScanTask = null;

	private long size = 0;

	private int retry = 3;

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
		if(urlScanTask == null) this.urlScanTask = urlScanTask;

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
		this.fileSize.set(firstName);
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
	protected Void call() {
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
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			size = imageFile.length();
			this.setFileSize(String.format("%.1f KB",
					(size > 0) ? ((float) size / 1024) : 0));

			urlScanTask.increaseDownloaded();
			urlScanTask.updateTotalProgressBar();
			Platform.runLater(new Runnable() {
		        @Override
		        public void run() {
					updateProgress(1, 1);
					updateMessage("下載完成");
		        }
		      });
			
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
				if(retry-- > 0)	continue;
				else {
					Platform.runLater(new Runnable() {
				        @Override
				        public void run() {
							updateMessage("下載失敗");
				        }
				      });
					return null;
				}
			}catch (IOException e) {
				System.out.println(getFileName() + " : URL Connection fail");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(retry-- > 0)	continue;
				else {
					Platform.runLater(new Runnable() {
				        @Override
				        public void run() {
							updateMessage("下載失敗");
				        }
				      });
					return null;
				}
			}
			
			size = conn.getContentLength();
			if (size > 0)
				break;
			else {
				try {
					System.out.println(getFileName()
							+ " file size can't get.");
					if(retry-- <= 0) break;
					if (conn != null)
						conn.disconnect();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} while (true);

		DataInputStream dis = null;
		DataOutputStream dos = null;
		byte b = 0;
		int tatolBytes = 0;

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
			try {

				// FileUtils.copyURLToFile(url, imageFile,
				// 3000, 3000);

				while (true) {
					b = dis.readByte();
					dos.writeByte(b);
					updateProgress((++tatolBytes) / size, 1);
				}

			} catch (EOFException e) {

				try {
					dos.writeByte(b);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				updateProgress((++tatolBytes) / size, 1);

				if (imageFile.length() < size) {
					// System.out.println("The file is downloaded incompletely.");
					// retry++;
					continue;
				}

				Platform.runLater(new Runnable() {
			        @Override
			        public void run() {
						updateMessage("下載完成");
			        }
			      });
				System.out.println(getFileName()
						+ " download succeess.");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(e.toString() + " : "
						+ getFileName());
			}
		}

		try {
			dis.close();
			dos.close();
			conn.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// while (true) {
		// try {
		// FileUtils.copyURLToFile(url, imageFile, 3000, 3000);
		// updateMessage("下載完成");
		// updateProgress(1, 1);
		// break;
		// } catch (IOException e) {
		// System.out.println(getUrl().substring(
		// getUrl().lastIndexOf("/") + 1)
		// + " time out.");
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// continue;
		// }
		// }

		if (size < 0) {
			size = imageFile.length();
		}
		this.setFileSize(String.format("%.1f KB",
				(size > 0) ? ((float) size / 1024) : 0));

		return null;
	}

	@Override
	protected void succeeded() {
		// TODO Auto-generated method stub
		super.succeeded();
		urlScanTask.increaseDownloaded();
		urlScanTask.updateTotalProgressBar();
	}

}
