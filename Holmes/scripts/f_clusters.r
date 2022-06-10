library(cluster)

veni1 <- function(miara_odl,algorytm_c,sciezka,plik_csv,ile)
{
   file_path = paste(sciezka, plik_csv, sep = "")
   plik=read.csv(file=file_path, sep=";")
   plik=plik[,-1]
   odleglosc=dist(plik,method=miara_odl)
   klastry=hclust(odleglosc,method=algorytm_c)
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","dendrogram.pdf",sep="")
   pdf(file_path, width=170, height=170, bg = "white")
   plot(klastry,hang=-1)
   dev.off()
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","clusters",".pdf",sep="")
   pdf(file_path, width=10, height=200, bg = "white")
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","clusters",".txt",sep="")
   
   for(i in 2:ile)
   {
	
	sil <- silhouette(cutree(klastry,i),odleglosc)
	sortSilhouette(sil)
        plot(silhouette(cutree(klastry,i),odleglosc), cex.names = 0.5)
        #cat(paste(summary(sil)), file_path, append=TRUE)
	print(summary(sil))
   }

   dev.off()
}