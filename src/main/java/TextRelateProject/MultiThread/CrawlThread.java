package TextRelateProject.MultiThread;

import TextRelateProject.BingApi.TextExtractor;
import TextRelateProject.DocumentRanking.RetrievedDocument;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Yair on 08/01/2017.
 */
public class CrawlThread implements Runnable {
    private ConcurrentLinkedQueue<RetrievedDocument> jobs;
    private  ConcurrentLinkedQueue<RetrievedDocument> done;

    public CrawlThread(ConcurrentLinkedQueue<RetrievedDocument> jobs, ConcurrentLinkedQueue<RetrievedDocument> done) {
        this.jobs = jobs;
        this.done = done;
    }

    @Override
    public void run() {
        RetrievedDocument mydoc = jobs.poll();
        while (mydoc != null) {
            Document document = TextExtractor.getDocument(mydoc.getURL());
            if (document != null) {
                mydoc.setWeb_document(document);
                mydoc.retrieveNameFromDoc();
                done.add(mydoc);
            }
            mydoc = jobs.poll();
        }
    }

    private static Thread[] createCrawlThreads(int thread_num, ConcurrentLinkedQueue<RetrievedDocument> jobs,
                                               ConcurrentLinkedQueue<RetrievedDocument> done) {
        Thread[] threads = new Thread[thread_num];
        for (int i=0; i<thread_num; i++) {
            threads[i] = new Thread(new CrawlThread(jobs, done));
        }
        return threads;
    }

    private static void runThreads(Thread[] threads) {
        for (int i=0; i<threads.length; i++) {
            threads[i].start();
        }
        for (int i=0; i<threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {}
        }
    }

    public static ArrayList<RetrievedDocument> crawl(int thread_num, ArrayList<RetrievedDocument> docs) {
        ConcurrentLinkedQueue<RetrievedDocument> jobs = new ConcurrentLinkedQueue<>();
        jobs.addAll(docs);
        ConcurrentLinkedQueue<RetrievedDocument> done = new ConcurrentLinkedQueue<>();
        runThreads(createCrawlThreads(thread_num, jobs, done));
        ArrayList<RetrievedDocument> newDocumentList = new ArrayList<>();
        newDocumentList.addAll(done);
        return newDocumentList;
    }
}
