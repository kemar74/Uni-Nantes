csp <- read.table("csp.txt")
summary(csp)
ki <- chisq.test(csp)

L <- csp/apply(csp, 1, sum)
C <- t(t(csp)/apply(t(csp),1,sum))

library(ade4)
afc <- dudi.coa(csp, nf=2, scannf = F)

# Individus
inertie <-inertia.dudi(afc, row.inertia=TRUE)
round(afc$li,2)
round(inertie$row.abs/100)
round(inertie$row.re/100)

# Variables
round(inertie$row.re/100)
round(afc$co,2)
round(inertie$col.abs/100)
round(inertie$col.re/100)

s.label(afc$li,xax=1,yax=2)
s.label(afc$co,xax=1,yax=2,add.plot=T,boxes=F) 

pro <-as.factor(c("MA","EM","CA","MA","EM","CA","MA","EM","CA","MA","EM","CA"))
nb <-as.factor(c(2,2,2,3,3,3,4,4,4,5,5,5))
s.class(afc$li,xax=1,yax=2,fac=pro)
s.class(afc$li,xax=1,yax=2,fac=nb) 

library(FactoMineR)
afc <- FactoMineR::CA(csp)
