library(cluster)
library(fpc)

veni1 <- function(miara_odl, algorytm_c,sciezka,plik_csv, ile)
{
   file_path = paste(sciezka, plik_csv, sep = "")
   plik=read.csv(file=file_path, sep=";")
   plik=plik[,-1]
   macierz=matrix(0,2,ile)
   odleglosc=dist(plik,method=miara_odl)
   c=hclust(odleglosc,method=algorytm_c)
   odleglosc=as.matrix(odleglosc)
   for(i in 2:ile)
   {
      klastry=cutree(c,i)
      macierz[1,i-1]=i
      macierz[2,i-1]=cluster.stats(odleglosc, klastry, silhouette=TRUE)$ch
   }
   return(macierz)
}
