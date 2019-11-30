import urllib.request, json, datetime
import colorama
import socket

def haveWifi():
  try:
    host = socket.gethostbyname("www.google.com")
    s = socket.create_connection((host, 80), 2)
    s.close()
    return True
  except:
     pass
  return False

def mergeClasses(classes):
	myClasses = []
	for i, course in enumerate(classes):
		sameAsLast = False
		# try:
		if i > 0:
			sameAsLast = (course == myClasses[len(myClasses) - 1])
		# except:
		# 	pass

		if not sameAsLast:
			myClasses.append(course)
		else:
			myClasses[len(myClasses) - 1].end = course.end

	return myClasses

class Cours:
	def __init__(self, start, end, name, teacher, category, location, groups, notes):
		self.start = datetime.datetime.fromisoformat(start)
		self.end = datetime.datetime.fromisoformat(end)
		self.name = name
		self.teacher = teacher
		self.category = category
		self.location = location	
		self.groups = groups.split(' ; ')
		self.groups.sort()
		self.groups = str(' ; ').join(self.groups)
		self.notes = notes

	def __repr__(self):
		return F"{colorama.Style.BRIGHT}{colorama.Fore.RED}({self.location}) {colorama.Fore.BLUE}{self.name}{colorama.Fore.RESET} ({self.start.strftime('%d/%m %H:%M')} - {self.end.strftime('%d/%m %H:%M')}) {colorama.Fore.GREEN}[{self.category}]{colorama.Fore.RESET} with {self.teacher}"
	def __eq__(self, other):
		return self.name == other.name and self.location == other.location and self.start.day == other.start.day and self.groups == other.groups


colorama.init(autoreset=True)
classID = 22964
today = datetime.date.today()
if today.weekday() < 5:
	startDate = today - datetime.timedelta(days=today.weekday())
else:
	startDate = today - datetime.timedelta(days=today.weekday() - 7)
endDate = startDate + datetime.timedelta(days=5)

requestURL = F"https://edt-v2.univ-nantes.fr/events?start={startDate}&end={endDate}&timetables[0]={classID}"
allClasses = []

timetableName = F"timetable_nantes_{startDate}_{endDate}_{classID}.json"
if haveWifi() :
	with urllib.request.urlopen(requestURL) as url:
		data = json.loads(url.read().decode())
		file = open(timetableName, "w+")
		json.dump(data, file)
		print("---------------------------- Online timetable ----------------------------")
else:
	try:
		data = json.load(open(timetableName, "r"))
		print("---------------------------- Offline timetable ----------------------------")
	except:
		print(colorama.Style.BRIGHT + colorama.Style.BOLD + colorama.Fore.RED + "Pas de fichier trouvé...")
		exit()

for item in data:
	# try:
	allClasses.append(Cours(item["start_at"], item["end_at"], item["modules_for_item_details"], 
	 item["teachers_for_item_details"], item["categories"], item["rooms_for_item_details"], item["educational_groups_for_item_details"], item["notes"]))
	# except:
	# 	print("Un élément non-pris en compte : ", item)

allClasses = mergeClasses(allClasses)
dayTags = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"]

dayWidth = 10
hourHeight = 0

startingHourInDay = datetime.time(hour=7)
endingHourInDay = datetime.time(hour=19)

coursesDays = [[], [], [], [], []]

nextShowed = False
for item in allClasses:
	if "M1ATAL" in item.groups:
		if ("GROUPE 2" in item.notes.upper() or "GROUPE 1" not in item.notes.upper()) and ("GROUPE B" in item.notes.upper() or "GROUPE A" not in item.notes.upper()):
			coursesDays[item.start.weekday()].append(item)
			if item.start <= datetime.datetime.now(item.start.tzinfo) <= item.end:
				print(colorama.Style.BRIGHT + '' + colorama.Back.MAGENTA + str(item))
			elif not nextShowed and item.start > datetime.datetime.now(item.start.tzinfo):
				print(colorama.Back.MAGENTA + str(item))
				nextShowed = True
			else:
				print(item)