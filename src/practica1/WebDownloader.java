package practica1;

//import com.intellij.codeInsight.template.postfix.templates.SoutPostfixTemplate;
/*
import com.intellij.codeInsight.template.postfix.templates.SoutPostfixTemplate;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.thoughtworks.xstream.mapper.Mapper;

*/
import org.jsoup.Jsoup;

import javax.sound.midi.Soundbank;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class WebDownloader {

	private final int nThreads;
	private final int maxDownloads;
	private final String outputFolder;
	private final File folder;
	private static BufferedReader bReader;
	private static BufferedWriter bError;
	private static FileReader fileR;

	private static Semaphore semReadLine;
	private static Semaphore semMaxDownloads;
	private static Semaphore semWriteError;

	private static int filesDownload;

	public WebDownloader(String outputFolder, int nThreads, int maxDownloads) throws IOException {
		this.outputFolder = outputFolder;
		this.nThreads = nThreads;
		this.maxDownloads = maxDownloads;
		filesDownload = 0;
		
		bReader = null;

		//Sem para leer las lineas los Threads y maxDownloads
		semReadLine = new Semaphore(1);
		semMaxDownloads = new Semaphore(maxDownloads);
		semWriteError = new Semaphore(1);
		//Create directory to save the files
		this.folder = new File(outputFolder);
		folder.mkdir();

		bError = null;

	}

	public String readUrl(BufferedReader bReader) throws IOException, InterruptedException {
		String url = null;
		if(WebDownloader.bReader != null){
			//Exclusion mutua buffer
			semReadLine.acquire();
			url = WebDownloader.bReader.readLine();
			System.out.println("Leyendo url "+url);

			System.out.println("Me duermo 50ms");
			semReadLine.release();
			//END Exclusion mutua buffer
			//DESCARGA
		}else{
			fileR.close();
		}
		return url;

	}

	public static String extractData(String url) throws IOException, InterruptedException {
		try {
			System.out.println("Extrayendo url "+url);
			org.jsoup.Connection conn = Jsoup.connect(url);
			// Performs the connection and retrieves the response
			org.jsoup.Connection.Response resp = conn.execute();
			// If the response is different from 200 OK,
			// the website is not reachable
			if (resp.statusCode() != 200) {
				System.out.println("Error: "+resp.statusCode());
				//writeError(url);
				return "ERROR DE CONEXION EN ESTA URL";
			} else {
				System.out.println("Todo bien");
				String html = conn.get().html();
				filesDownload++;

				System.out.println();
				return html;

			}
		} catch (IOException e) {

			System.out.println("No se puede conectar a "+ url);
			return url;
			/*Si se produce un error asociado a una URL (no es una URL correcta, no es posible
					conectarse a esa web, etc.), se continuarÃ¡ con la siguiente web, sin crear el fichero
			asociado. La web que no se ha podido descargar se escribirÃ¡ en un fichero
			error_log.txt. */
		}catch (Exception e){
			System.out.println("adios");
			return null;
		}

	}

	public void writeHtml(String url, String html) throws IOException {
		String[] fileCut = url.split("\\.");
		String nameFile = fileCut[1];

		File ofile = new File(this.folder+"\\"+nameFile + ".html");
		BufferedWriter bw;

		bw = new BufferedWriter(new FileWriter(ofile));
		bw.write(html);
		bw.close();
		System.out.println("Llevo "+filesDownload+" ficheros descargados ");
	}

	public static void writeError(String url) throws IOException, InterruptedException {
		
		semWriteError.acquire();

		System.out.println("ERROOOOR");
		File errorFile = new File("error_log.txt");
		bError = new BufferedWriter(new FileWriter(errorFile,true));

		bError.write(url + "\n");
		bError.close();

		semWriteError.release();
	}

	public void process(String pathToFile) throws IOException {
		File file = new File(pathToFile);
		fileR =  new FileReader(file);
		bReader = new BufferedReader(fileR);

        //Creamos listado de Threads
        List<Thread> ths = new ArrayList<Thread>();

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				System.out.println();
				
				System.out.println("Web descargadas en este momento: "+ filesDownload);
				System.out.println();
			}
		};
		timer.schedule(task, 10, 3000);

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
            for(Thread th:ths){
            	System.out.println("Ha pulsado ENTER");
                th.interrupt();                
            }
        }).start();;

		for(int i = 0; i<nThreads; i++){
			int ilocal = i;
			//METER ESTRUCTURA DE DATOS CONCURRENTE CON LAS LINEAS DEL FICHERO PARA QUE TENGA EXCL MUTUA AUTOMÃ�TICA.
			ths.add(new Thread(() -> {
				String url = "aaa" ;
				while(url != null){
					try {
						System.out.println("Soy Thread: " + ilocal +" voy a leer");
						url = readUrl(bReader);
						//Thread.sleep(50);
						//Lo pongo aquÃ­ ya que dice que la descarga Â¡, es decir a partir de la lectura
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						semMaxDownloads.acquire();
						System.out.println("Extrayendo datos Thread: "+ilocal );
						String html = extractData(url);
						if(html != null){
							//Thread.sleep(600);
							semMaxDownloads.release();
							writeHtml(url, html);
						}else{
							semMaxDownloads.release();
							timer.cancel();
						}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("He acabado de ejecutar "+ ilocal);

			},"Thread "+(ilocal)));
		}

		for(Thread th:ths){
			th.start();
		}




		//Funcion cuando pulse intro haga ths.interrupted.


	}


}


