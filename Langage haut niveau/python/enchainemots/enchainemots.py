
import unidecode, random, os.path, glob

listOfChar = 'ABCDEFGHIJKLMNOPQRSTUVWXYZÀÂÇÈÉÊÎÔÙÛ\'-'
def estLettreFr(cara):
	cara = cara.upper()
	return cara in listOfChar

def premiereLettreMajuscule(texte):
	return texte[0].upper() + texte[1:].lower()

class couplesmots:
	def __init__(self, func):
		self.isLetter = func
		self.mesCouples = []

	def cleanWord(self, word):
		#word = unidecode.unidecode(word)
		newWord = ""
		for l in word:
			if self.isLetter(l):
				newWord += l
		return newWord.upper()

	def afficher(self):
		for i in range(len(self.mesCouples)):
			print(F"{self.mesCouples[i][0].capitalize()} {self.mesCouples[i][1].capitalize()} : {self.mesCouples[i][2]} pts")

	def ajouterCouple(self, mot1, mot2):
		mot1 = self.cleanWord(mot1)
		mot2 = self.cleanWord(mot2)
		if(not mot1 or not mot2):
			return 0
		found = False
		points = 1
		for i in range(len(self.mesCouples)):
			if(mot1.upper() == self.mesCouples[i][0] and mot2.upper() == self.mesCouples[i][1]):
				points = self.mesCouples[i][2] +1
				self.mesCouples[i][2] = points
				found = True
				# print("mot ajouté!")
				break
		if not found:
			self.mesCouples.append([mot1.upper(), mot2.upper(), 1])
		return points

	def ajouterSuiteChaine(self, motprec, texte):
		if(len(texte.split()) == 0):
			return
		first = texte.split()[0]
		tail = texte[len(first)+1:]
		if(len(motprec) > 0):
			self.ajouterCouple(motprec, first)
		self.ajouterSuiteChaine(first, tail)

	def ajouterChaine(self, texte):
		self.ajouterSuiteChaine("", texte)
		return texte.split()[-1:]

	def ajouterFichier(self, nomfic, lineLimit = -1):
		lastWord = None
		with open(nomfic, 'r', encoding="utf-8") as fichier:
			line = " "
			iLine = 0
			while(line and (lineLimit < 0 or iLine <= lineLimit)):
				iLine += 1
				# print(F"Ligne {iLine} ... :")
				line = fichier.readline()
				for l in line:
					if(not self.isLetter(l)):
						l = " "
				lastWord = self.ajouterChaine(line)
		return lastWord

	def lesSuivOcc(self, mot):
		mot = self.cleanWord(mot)
		listePossible = []
		for couple in self.mesCouples:
			if(self.cleanWord(couple[0]) == mot):
				listePossible.append(couple)

		return sorted(listePossible, key= lambda x: x[2], reverse=True) 
	def lesSuivants(self, mot):
		listeOcc = self.lesSuivOcc(mot)
		liste = []
		for couple in listeOcc:
			liste.append(couple[1])

		return liste

	def unSuivant(self, mot):
		liste = self.lesSuivants(mot)
		return random.choice(liste)

	def meilleurSuivant(self, mot):
		listePossible = self.lesSuivants(mot)
		if listePossible:
			return listePossible[0]
		return None

	def phrase(self, mot, n):
		phrase = [self.cleanWord(mot)]
		for i in range(n):
			mot = self.meilleurSuivant(mot)
			phrase.append(mot)

		return " ".join(phrase)

	def phraseUnique(self, mot, n) :
		utilises = [self.cleanWord(mot)]
		phrase = [self.cleanWord(mot)]

		for i in range(n):
			mots = self.lesSuivants(mot)
			if(len(mots) == 0):
				break
			mot = mots[0]
			while(mots[0] in utilises):
				mots.pop(0)
			if(mots):
				mot = mots[0]
				utilises.append(mot)
				phrase.append(mot)

		return " ".join(phrase)

	def ajouterLangage(self, lang="fr"):
		if not os.path.exists(lang):
			print(F"'{lang}' n'est pas connu...")
			return False
		files = glob.glob(lang + "/*")
		for file in files:
			self.ajouterFichier(file)
		return True



enchainemots = couplesmots(estLettreFr)
enchainemots.ajouterLangage("fr")


print(premiereLettreMajuscule(enchainemots.phrase("Il", n=20)))
print(premiereLettreMajuscule(enchainemots.phraseUnique("Il", n=20)))