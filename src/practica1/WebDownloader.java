package practica1;

//import com.intellij.codeInsight.template.postfix.templates.SoutPostfixTemplate;
/*
import com.intellij.codeInsight.template.postfix.templates.SoutPostfixTemplate;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.thoughtworks.xstream.mapper.Mapper;

*/
import clojure.lang.IFn;
import org.jsoup.Jsoup;

import javax.sound.midi.Soundbank;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WebDownloader {

	private final int nThreads;
	private final int maxDownloads;
	private final String outputFolder;
	private final File folder;
	private static BufferedReader bReader;
	private static BufferedWriter bError;
	private static FileReader fileR;

	//private static Semaphore semReadLine;
	//private static Semaphore semWriteError;
	private static Semaphore semMaxDownloads;

	private static Lock lockWriteError;
	private static Lock lockReadLine;
	private static Lock lockFilesDowoad;
	private static Lock lockAuxPulsarTecla;
	private static Lock lockInterruptPrueba;
	private static Lock lockStringBuilder;

	private static int filesDownload;

	private static boolean interruptPrueba;
	private static boolean auxPulsarTecla;
	//ESTAS DOS STATIC ATOMIC BOOLEAN

	private static StringBuilder stringBuilder;

	public WebDownloader(String outputFolder, int nThreads, int maxDownloads) throws IOException {
		this.outputFolder = outputFolder;
		this.nThreads = nThreads;
		this.maxDownloads = maxDownloads;
		filesDownload = 0;

		bReader = null;

		//Sem para leer las lineas los Threads y maxDownloads
		//semReadLine = new Semaphore(1);
		//semWriteError = new Semaphore(1);

		semMaxDownloads = new Semaphore(maxDownloads);

		lockWriteError = new ReentrantLock();
		lockReadLine = new ReentrantLock();
		lockFilesDowoad = new ReentrantLock();
		lockAuxPulsarTecla = new ReentrantLock();
		lockInterruptPrueba = new ReentrantLock();
		lockStringBuilder = new ReentrantLock();
		//Create directory to save the files
		this.folder = new File(outputFolder);
		folder.mkdir();

		bError = null;

		this.interruptPrueba = false;
		this.auxPulsarTecla = false;



		stringBuilder = new StringBuilder();
	}

	public String readUrl(BufferedReader bReader) throws IOException, InterruptedException {
		String url = null;
		if(WebDownloader.bReader != null){
			//Exclusion mutua buffer

			lockReadLine.lock();
			url = WebDownloader.bReader.readLine();
			lockReadLine.unlock();

			//END Exclusion mutua buffer
			//DESCARGA
		}else{
			fileR.close();
		}
		return url;

	}

	public static String extractData(String url) throws IOException, InterruptedException {
		try {
			org.jsoup.Connection conn = Jsoup.connect(url);
			// Performs the connection and retrieves the response
			org.jsoup.Connection.Response resp = conn.execute();
			// If the response is different from 200 OK,
			// the website is not reachable
			if (resp.statusCode() != 200) {
				System.out.println("Error: "+resp.statusCode());
				writeError(url);
				return "ERROR";
			} else {
				String html = conn.get().html();
				lockFilesDowoad.lock();
				filesDownload++;
				lockFilesDowoad.unlock();
				System.out.println();
				return html;
			}
		} catch (IOException e) {
			System.out.println("No se puede conectar a "+ url);
			writeError(url);
			return "ERROR";
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

	}

	public static void writeError(String url) throws IOException, InterruptedException {


		lockWriteError.lock();
		File errorFile = new File("error_log.txt");
		bError = new BufferedWriter(new FileWriter(errorFile,true));

		bError.write(url + "\n");
		bError.close();

		//No es necesario pero se puede usar el lock
		lockStringBuilder.lock();
		stringBuilder.append("La web "+url+" no se ha podido descargar" +"\n");
		lockStringBuilder.unlock();

		lockWriteError.unlock();

	}

	public boolean isAuxPulsarTecla(){
		boolean aux;
		lockAuxPulsarTecla.lock();
		aux = auxPulsarTecla;
		lockAuxPulsarTecla.unlock();
		return aux;
	}

	public boolean isInterruptPrueba(){
		boolean aux;
		lockInterruptPrueba.lock();
		aux = interruptPrueba;
		lockInterruptPrueba.unlock();
		return aux;
	}


	public void process(String pathToFile) throws IOException {
		File file = new File(pathToFile);
		fileR =  new FileReader(file);
		bReader = new BufferedReader(fileR);
		boolean interrup = true;
        //Creamos listado de Threads

        List<Thread> ths = new ArrayList<Thread>();

        //Timer para mostrar las descargas cada 3s
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("------------------------------");
				System.out.println("");
				System.out.println("WEB DESCARGADAS HASTA ESTE MOMENTO -->: "+ filesDownload);
				System.out.println("");
				System.out.println("------------------------------");
			}
		};
		timer.schedule(task, 10, 3000);


		//Interrupcion con ENTER
        new Thread(() -> {
			try {
				while((System.in.available() <1) && (!isAuxPulsarTecla()) ){
                    Thread.sleep(500);
                }
			} catch (IOException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			//CUANDO SE PULSA LA TECLA
			//Pongo esta condición para que si acaba el fichero no ponga nada a true ni haga cancel, ya que los Threads ya han acabado.
			if(!isAuxPulsarTecla()){
				lockInterruptPrueba.lock();
				this.interruptPrueba = true;
				lockInterruptPrueba.unlock();
                timer.cancel();
				System.out.println("STRING BUILDER: ");
				System.out.println(stringBuilder);
			}else{
				System.out.println("STRING BUILDER: ");
				System.out.println("Han acabo todos los Threads y NO se ha pulsado Enter" );
				System.out.println(stringBuilder);
			}
		}, "ThreadStop").start();

        //Creamos Threads
      	for(int i = 0; i<nThreads; i++){
			int ilocal = i;
			//METER ESTRUCTURA DE DATOS CONCURRENTE CON LAS LINEAS DEL FICHERO PARA QUE TENGA EXCL MUTUA AUTOMÃ�TICA.
			ths.add(new Thread(() -> {

				String url = "aaa" ;
					while((url != null) && (!isInterruptPrueba())){
						try {
							url = readUrl(bReader);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							semMaxDownloads.acquire();
							System.out.println("Extrayendo datos Thread: "+ilocal+" de URL: "+ url );
							String html = extractData(url);
							if((html != "ERROR") &&(url != null)){
								lockStringBuilder.lock();
								stringBuilder.append("La web "+url+" se ha descargado correctamente" +"\n");
								lockStringBuilder.unlock();
								semMaxDownloads.release();
								writeHtml(url, html);
							}else{
								semMaxDownloads.release();
							}

						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

				}
				lockAuxPulsarTecla.lock();
				this.auxPulsarTecla = true;
				lockAuxPulsarTecla.unlock();
				System.out.println("He acabado de ejecutar "+ ilocal);
				timer.cancel();

			},"Thread "+(ilocal)));

		}

		for(Thread th:ths){
			th.start();
		}
	}
}


