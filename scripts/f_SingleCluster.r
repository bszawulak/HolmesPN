library(cluster)

veni1 <- function(miara_odl,algorytm_c,sciezka,plik_csv,ile)
{
   file_path = paste(sciezka, plik_csv, sep = "")
   plik=read.csv(file=file_path, sep=";")
   plik=plik[,-1]
   odleglosc=dist(plik,method=miara_odl)
   klastry=hclust(odleglosc,method=algorytm_c)
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","dendrogram_ext_",ile,".pdf",sep="")
   pdf(file_path, width=170, height=170, bg = "white")
   plot(klastry,hang=-1)
   dev.off()
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","clusters_ext_",ile,".pdf",sep="")
   pdf(file_path, width=10, height=190, bg = "white")
   sil<-silhouette(cutree(klastry,ile),odleglosc)
   sortSilhouette(sil)
   plot(sil,nmax.lab = 8000, cex.names = 0.5)
   print(summary(sil))
   
   clusters.idx <- cutree(klastry,ile)
   clusters <- split(row.names(plik), clusters.idx)
   for(i in 1:ile) {print(clusters[[i]])}
   dev.off()
}
