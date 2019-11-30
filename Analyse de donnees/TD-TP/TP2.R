library(psych)
library(ade4)

cidres<-read.table("cidre.txt", header=T)
summary(cidres)

pairs.panels(cidres)

round(cor(cidres) * (abs(cor(cidres)) > 0.70 & cor(cidres) != 1), 2)

acp<-dudi.pca(cidres,center=T,scale=T,scannf=F)
round(acp$eig,2) 
round(cumsum(acp$eig*10),2) 