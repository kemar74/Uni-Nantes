###########################################################################
# la fonction codage()  A bouchier INRA
############################################################################
codage<-function(nom)
#découpage en 3 classes d'effectifs égaux
{
#calcul des bornes
bornes<-quantile(nom, probs = c(0, 1/3,2/3,1), na.rm = TRUE,names = TRUE)
#description des bornes et effectifs
Amax<-aggregate(nom,list(Nom=cut(nom,bornes,include.lowest=T,label=F)),max)
Amin<-aggregate(nom,list(Nom=cut(nom,bornes,include.lowest=T,label=F)),min)
Afreq<-as.matrix(summary(as.factor(cut(na.omit(nom),bornes, include.lowest=T,
label=F))))
limites<-as.data.frame(cbind(Amin[,1],Amin[,2],Amax[,2],Afreq))
names(limites)<-c("Classe","Mini","Maxi","Effectif")
#calcul du nombre de valeurs manquantes
manques<-length(nom)-length(na.omit(nom))
#impression des bornes
cat(paste("Découpage de la variable ",deparse(substitute(nom))," - Nb de valeurs
manquantes : ", manques, "\n"))
print(limites)
#découpage de la variable
varfac<-cut(nom,bornes,include.lowest=T,label=F)
#transformation en facteur
as.factor(varfac)
}