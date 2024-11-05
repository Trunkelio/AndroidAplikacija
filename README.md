Description lang.: Slovenian --> translate if needed

Funkcionalnosti
Prepoznavanje rastlin v realnem času: Prepoznavanje rastlinskih vrst z uporabo modela strojnega učenja.
Analiza zdravja in diagnosticiranje bolezni: Diagnostika zdravja rastline in odkrivanje bolezni na podlagi posnetka.
Uporabniku prijazen vmesnik: Android aplikacija s preprostim in intuitivnim vmesnikom za zajemanje in nalaganje slik.
Razširljiv zaledni sistem: Flask API s podporo v oblaku za skalabilno in zanesljivo delovanje.


Arhitektura aplikacije
Aplikacija je sestavljena iz dveh glavnih komponent:

Android aplikacija - Zagotavlja uporabniški vmesnik za zajemanje slik, prepoznavanje rastlin in analizo zdravja.
Flask Backend API - Obdeluje slike, izvaja klasifikacijo vrst in bolezni ter vrača rezultate aplikaciji.
Uporabljene tehnologije

Android (Java/Kotlin): Android aplikacija uporablja CameraX za zajem slik, Retrofit za komunikacijo HTTP in Glide za prikaz slik.
Flask (Python): Zaledje je implementirano v Flasku, uporablja TorchVision za obdelavo slik in Orange Data Mining za strojno učenje.
Oracle Cloud: Gosti Flask API, kar zagotavlja skalabilno okolje za obdelavo zahtev.


Glavne funkcionalnosti
Zajem slike in izbira iz galerije:

CameraActivity: Uporablja CameraX API za zajemanje slik rastlin in omogoča uporabnikom izbiro slik iz galerije.
ConfirmActivity: Prikaže zajeto ali izbrano sliko za potrditev, preden nadaljuje z analizo.
Nalaganje podatkov in analiza:

LoadingActivity: Po potrebi zmanjša velikost slike in jo naloži na Flask API za analizo zdravja in vrste. Prejme rezultate in jih obdela za prikaz.
Prikaz rezultatov in diagnostika bolezni:

ActivityOutput: Prikaže rezultate analize (ime vrste, stopnjo zaupanja, zdravstveno stanje) in omogoča dodatno diagnostiko bolezni preko stikala.

Potek dela

Zagon aplikacije: Uporabnik odpre aplikacijo in podeli dovoljenja za dostop do kamere in shranjevanja.
Zajem ali izbira slike: Uporabnik zajame novo sliko ali izbere obstoječo iz galerije.
Potrditev slike: Uporabnik potrdi sliko za analizo ali jo ponovno zajame.
Nalaganje in analiza: Potrjena slika se pošlje na Flask API, ki vrne rezultate prepoznavanja vrste in analize zdravja.
Prikaz rezultatov: Prikazani so rezultati, uporabnik pa lahko po želji aktivira dodatno diagnostiko bolezni.

Flask Backend API
API končne točke
/health (POST): Analizira zdravje rastline na podlagi naložene slike.
/species (POST): Prepozna vrsto rastline in vrne njeno ime s stopnjo zaupanja.
/sickness (POST): Najprej prepozna vrsto rastline in nato preveri prisotnost bolezni ter vrne podatke o bolezni, če so zaznane.
Obdelava slik in klasifikacija
SqueezeNet: Izvleče značilne lastnosti iz slik, ki se nato uporabijo kot vhod za modele strojnega učenja.
Orange Data Mining modeli: Vnaprej trenirani modeli za klasifikacijo vrst in zdravja, optimizirani za natančnost in hitro odzivnost.

Potek dela
API prejme sliko iz Android aplikacije.
Slika se obdeluje in zmanjša na 256x256 pikslov za kompatibilnost z modeli.
Izvlečenje značilk: SqueezeNet pridobi vektorsko predstavitev slike za klasifikacijo.
Klasifikacija: Modeli napovedo vrsto rastline, zdravstveno stanje in morebitne bolezni.
Rezultati se vrnejo Android aplikaciji v formatu JSON.

Namestitev in zagon

Android aplikacija
Klonirajte repozitorij in odprite Android projekt v Android Studiu.
Poskrbite, da imate nameščene vse potrebne odvisnosti, kot so Retrofit, CameraX in Glide.
Povežite Android napravo ali zaženite emulator.
Zaženite aplikacijo, da jo namestite na napravo/emulator.

Flask Backend
Klonirajte repozitorij in se premaknite v direktorij flask_app.
Namestite potrebne pakete:
bash
Copy code
pip install -r requirements.txt
Zaženite Flask API strežnik lokalno:

bash
Copy code
python app.py

Za produkcijsko uporabo konfigurirajte Flask aplikacijo na Oracle Cloud ali drugem ponudniku oblačnih storitev glede na vaše zahteve.
