library(cluster)
library(amap)

veni1 <- function(miara_odl,algorytm_c,sciezka,plik_csv,ile)
{
   file_path = paste(sciezka, plik_csv, sep = "")
   plik=read.csv(file=file_path, sep=";")
   plik=plik[,-1]
   odleglosc=Dist(plik,method=miara_odl)
   klastry=hcluster(plik,method=miara_odl,link=algorytm_c)
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","dendrogram.pdf",sep="")
   pdf(file_path, width=170, height=170, bg = "white")
   plot(klastry,hang=-1)
   dev.off()
   file_path = paste(sciezka,algorytm_c,"_",miara_odl,"_","clusters",".pdf",sep="")
   pdf(file_path, width=10, height=200, bg = "white")
   for(i in 2:ile)
   {
      sil <- silhouette(cutree(klastry,i),odleglosc)
      sortSilhouette(sil)
      plot(silhouette(cutree(klastry,i),odleglosc))
      print(summary(sil))
   }
   dev.off()
}
