library(ggplot2)

# data :
Bodywt<-c( 10, 207, 62, 6.8, 52.2)
Brainwt<-c(115, 406, 1320, 179, 440)
Noms<-c("Potar monkey", "Gorilla", "Human", "Rhesus Monkey", "Chimp")

par(bg="aliceblue", col="red", mfcol=c(2, 2))
titre<-"Poids du cerveau / poids du corps"
labelX<-"Poids du corps"
labelY<-"Poids du cerveau"

plot(x=Bodywt, y=Brainwt, main=titre, xlab=labelX, ylab=labelY, pch=16, cex=3)
plot(x=Bodywt, y=Brainwt, main=titre, xlab=labelX, ylab=labelY, pch=16, cex=3)
plot(x=Bodywt, y=Brainwt, main=titre, xlab=labelX, ylab=labelY, pch=16, cex=3)

bledur<-read.table("bledur.txt", header=T, na.string="M",dec=",",sep=" ")
dim(bledur) ; dimnames(bledur)
# structure
str(bledur)
# n premières lignes
head(bledur, n=5)
# n dernières lignes
tail(bledur, n=5)
# retrouver des elts d’un tableau
bledur[1,1] ; bledur[9,6] ; bledur[,1] ; bledur[1,]
bledur[1:5,4:6] ; bledur[c(1,3),c(3,5)]
# exclure des elements
bledur[-1,] ; bledur[,-3]

par(pch=16, cex=5)
ggplot(bledur)+aes(x=bledur$Numero, y=bledur$ARG, color=bledur$PLM)+geom_point()

data("mtcars")
mtcars
ggplot(mtcars)+aes(x=mtcars$mpg)+geom_histogram()
ggplot(mtcars)+aes(x=mtcars$cyl)+geom_bar(width=.5)
ggplot(mtcars)+aes(x=mtcars$mpg, y=mtcars$disp, color=mtcars$cyl)+geom_point()+facet_grid(vs~mtcars$cyl)
