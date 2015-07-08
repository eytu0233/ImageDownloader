package application.model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

public class UrlScanTask extends Thread {

	private static Object lock = new Object();

	private String url;
	private String dirPath;
	private ObservableList<ImageDownloadTask> imageDownloadTasks;
	private ProgressBar totalProgressBar;
	private Label totalProgressBarPercentage;

	private double total = 0, downloaded = 0;

	public synchronized void increaseTotal(){
		total++;
	}
	
	public synchronized void increaseDownloaded(){
		downloaded++;
	}
	
	public synchronized void updateTotalProgressBar() {
		double percentage = downloaded / total;
		System.out.println(percentage);
		Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	totalProgressBar.setProgress(percentage);
	    		totalProgressBarPercentage.setText(String.format("%.1f%%", percentage));
	        }
	      });
	}

	public UrlScanTask(String url, String dirPath,
			ObservableList<ImageDownloadTask> imageDownloadTasks,
			ProgressBar totalProgressBar, Label totalProgressBarPercentage) {
		// TODO Auto-generated constructor stub
		super();
		this.url = url;
		this.dirPath = dirPath;
		this.imageDownloadTasks = imageDownloadTasks;
		this.totalProgressBar = totalProgressBar;
		this.totalProgressBarPercentage = totalProgressBarPercentage;
	}

	private ImageDownloadTask analyzeURL() {
		String[] pieces = url.split("/");
		for (String s : pieces) {
			if (s.equals("g.e-hentai.org")) {
				// System.out.println("e-hentai");
				return null;
			} else if (s.equals("pururin.com")) {
				// System.out.println("pururin");
				return null;
			} else if (s.equals("eyny.com")) {
				// System.out.println("pururin");
				return null;
			}
		}

		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String nowURL = "", nextURL = url;
		Document doc = null;
		Elements links = null, images = null;
		boolean reconnection = false;
		int index = 1;

		ExecutorService executor = Executors.newScheduledThreadPool(3,
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setDaemon(true);
						return t;
					}
				});

		do {
			try {
				reconnection = false;
				nowURL = nextURL;
				doc = Jsoup.connect(nowURL).get();
				links = doc.select("a[href]");
				for (Element e : links) {
					if (e.id().equals("next")) {
						nextURL = e.attr("href");
						break;
					}
				}
				images = doc.select("img[src]");
				for (Element e : images) {
					if (e.id().equals("img")) {
						ImageDownloadTask imageDownloader = new ImageDownloadTask(
								String.format("%03d.jpg", index++), dirPath,
								e.attr("src"), this);
						imageDownloadTasks.add(imageDownloader);
						this.increaseTotal();
						this.updateTotalProgressBar();
//						executor.execute(imageDownloader);
					}
				}
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				nextURL = nowURL;
				reconnection = true;
				System.out.println("Reconnection : " + nowURL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!nextURL.equals(nowURL) || reconnection);

		for (ImageDownloadTask task : imageDownloadTasks) {
			if (!task.isDone()) {
				executor.execute(task);
			}
		}
	}

}
