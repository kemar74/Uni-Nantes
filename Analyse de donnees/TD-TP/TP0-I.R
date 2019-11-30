library(GGally)

data(iris) # Charger les donnees -> iris est une data.frame

selectedData <- iris  # On prend tout le jeu de donnees
selectedData <- subset.data.frame(iris, Species == "virginica") # Ou on se limite avec la condition de Species, par exemple

ggpairs(selectedData, mapping=aes(col=selectedData$Species))

colMeans(selectedData[,-5])

for(param in colnames(selectedData))
  print(paste(param, mean(selectedData[[param]])), sep=" ")

load("voit2005.Rdata")
nbParam <- length(voit2005)
nbVoitures <- length(voit2005[,1])
print(mean(voit2005$Longueur))
print(mean(voit2005$Largeur))
print(mean(voit2005$Surface))

print(sum(voit2005$Cylindree < 1000))

print(sqrt(var(voit2005$Longueur)))

print(paste("Moyenne : ", mean(voit2005$Longueur)))
print(paste("Mediane : ", median(voit2005$Longueur)))
print(paste("Ecart inter-quartile : ", IQR(voit2005$Longueur)))

persp(x=0:20, y=0:20, z=function(x){sin(x)})
      