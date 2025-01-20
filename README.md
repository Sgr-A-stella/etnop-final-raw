# etnop-final-raw
(Funkcionálisan kész, de sok tekintetben befejezetlen állapot)

# Fejlesztői dokumentáció
Jelen fejlesztői dokumentáció eligazítási célú, tapasztalt fejlesztők számára készült, így bizonyos szinten triviális információkat nem tartalmaz.  

## Feladat

### Képtranszformáció és titkosítás - Backend feladat leírás

A projekted célja egy olyan webalkalmazás backendjének kialakítása, amely képes képek lokális feltöltésére, átméretezésére, titkosítására, és ezeket titkosítva, adatbázisban tárolja. Az alkalmazás Java17 és Spring Boot használatával készüljön el és REST végpontok segítségével kommunikáljon.

#### Feladatok:

##### Képek feltöltése és kezelése (api/files):
Készíts REST végpontokat képek feltöltéséhez, amelyek egyszerre több képet is fogadhatnak.
Csak PNG és JPG formátumokat fogadj el, és korlátozd a képméretet, hogy ne legyen nagyobb, mint 5000x5000 pixel.
A backend oldalon legyen egy konfiguráció, amely két átméretezési paramétert tartalmaz: maximum szélesség és magasság. Ezek egyike sem kötelezően megadandó. Ha a kép valamelyik mérete nagyobb, mint az adott paraméter, akkor méretezd át a képet megtartva az arányát. A képek átméretezéséhez használj külső 3rd party alkalmazást, amelyet backendről tudsz vezérelni (pl.: ImageMagick vagy GraphicsMagick). Az átméretezett képeket AES algoritmus segítségével titkosítva mentsd el az adatbázisba. A titkosításhoz szükséges kulcsot (secretKey fájl) egy külön erre a célra írt Java util osztályban generáld le, és mentsd ki a fájlrendszerre.

##### Képek letöltése(api/file/{fileName}):
Implementálj egy végpontot, amely lehetővé teszi egy adott fájlnév alapján a titkosított kép visszafejtését és letöltését a böngészőben.
Egy másik végponton (api/files) keresztül biztosíts lehetőséget az összes kép visszafejtésére és ZIP állományban történő letöltésére a böngészőben.

Úgy tervezd meg az alkalmazást, hogy a képkezelő alkalmazás (pl.: ImageMagick vagy GraphicsMagick) könnyen cserélhető legyen anélkül, hogy az alkalmazás fő logikáját módosítani kellene!

#### Követelmények:
A projekt Maven segítségével legyen létrehozva, biztosítva ezzel a könnyű függőségkezelést és projektkonfigurációt. A backend szolgáltatások fejlesztéséhez használd a Java 17 verzióját, valamint a Spring Boot keretrendszert. Az adatbáziskapcsolatok kezelésére alkalmazz JPA-t. Adatbázisnak PostgreSQL használata javallott.

Az alkalmazásnak RESTful API-t kell kínálnia, amely lehetővé teszi a fájlok feltöltését és lekérdezését. Az API végpontok dokumentálásához használj Swagger-t.

Teszteld az alkalmazást, készíts integrációs teszteket.

#### Verziókövetés:
A projektet egy publikus git alapú repository-ban oszd meg, például GitHubon vagy GitLabon.
Készíts egy fordítási és futtatási útmutatót, amelyet a README.md fájlban helyezz el a projekt gyökérmappájában. Biztosíts dokumentációt a kódhoz, amely segít más fejlesztőknek megérteni és bővíteni a projektet a jövőben.


### Megvalósítás
A megvalósítás, egyetlen (ajánlott) pont kivételével a fenti feladatkiírásnak megfelelően történt:
* Maven build eszközre épülő SpringBoot projekt, Hibernate JPA provider és PostgreSQL adatbázis-kiszolgálón alapuló perzisztenciával
* Beágyazott webkiszolgálón futó, Spring REST controller végpontokon elérhető végfelhasználói (feladatkiírásban igényelt teljes) funkcionalitás
* Parancssorból futtatható AES kulcsállomány-generáló utility (SecretKeyGenerator)
* Az application.properties állomány megadható alkalmazás konfigurációs paraméterek
* Néhány egység és integrációs teszt
* Git verziókezelőben publikálva

Az eltérés a képátméretezés megoldása, amely két módszerrel került megvalósításra annak igazolására, hogy ennek a funkciónak a rugalmas, más üzleti logikát nem érintő módosítása a Spring keretrendszer és az ennek alapfilozófiáját képező dependency injection (DI) szellemének megfelelő.
Az átméretezési logika külön service komponensbe került saját üzleti interfésszal (ImageEditService), amelyet egy JDK-beli Graphics2D API-t és egy Imgscalr nevű 3rd-party library könyvtárat használó konkrét Spring service bean valósít meg. A képátméretezési funkciót használó, attól függő service (ImageService interfészt megvalósító ImageServiceImpl) @Qualifier annotációval pontosítja az aktuális megvalósítási függőséget, amely akár újrafordítás nélkül, külső xml állományban megvalósított konfigurációval felülbírálható.
(Itt a feladatkiírásban két, alapvetően parancssori segédprogram lett ajánlva, de konzultációs lehetőség hiányában feltételeztem, hogy nem a Java-ból történő /kissé szokatlan/ parancssori megoldás integrációját kell demonstrálnom, hanem a rugalmas cserélhetőséget, ezért saját hatáskörben döntöttem a fenti megoldás mellett.)

### Fejlesztői környezet

#### Java JDK
Java 17 JDK szükséges, de bármely megvalósításán fejleszthető, futtatható.

#### PostgreSQL
Futó PostgreSQL kiszolgálón létrehozandó egy adatbázis és az application.properties állomány a megfelelő datasource paraméterértékekkel frissítendő.
Az application.properties állományban a 'spring.jpa.hibernate.ddl-auto = update' sor biztosítja a tábla és objektumai létrehozását az alkalmazás indításakor, ezért további teendő nincs.

#### IDE projekt import
Maven projektként importálható a projekt gyökérkönyvtára és ezt követően buildelhető, az automatikus tesztek futtathatók felhasználóbarát módon, valamint a projekt továbbfejleszthető szokványos módon.

#### Tesztelési eszköz
Manuális teszteléshez a legegyszerűbb curl, Postman vagy esetleg soapUI használata. Alább (Tesztelés szekcióban) néhány curl parancssori példa is szerepel.

### Fordítás
Fordítás parancssorból a megfelelő Maven parancssori paraméterekkel a projekt gyökérkönyvtárában lehetséges.

Minden modern IDE (pl. Eclipse, IntelliJ IDEA) támogatja a Maven alapú projekteket, így ezekből a megfelelő menüpontok és/vagy gombok segítségével érhetők el a fordítási lehetőségek.
(IntelliJ IDEA esetén például a Maven nézetben.)


### Tesztelés
A csekély számú automatikus teszt futtatható Maven parancssori utasítással és IDE-ben a megfelelő menüpontokkal.

A kézi tesztelés curl parancssori HTTP-klienssel is megvalósítható. Néhány parancsori paraméterezési példa a fő tesztesetekre:
* Az összes tárolt kép letöltése egyetlen, all.zip nevű állományban: 
curl --output all.zip http://localhost:8080/api/files
* Egyetlen, JPG állomány feltöltée: 
curl -v -F file="@ngc4414_HubbleSdss_2069_forJDKGraphics2D.jpg" -X POST http://localhost:8080/api/files
* Több (egy JPG és egy PNG) állomány feltöltése:
curl -v -F file="@ngc4414_HubbleSdss_2069_forJDKGraphics2D.jpg" -F file="@24-cell.png" -X POST http://localhost:8080/api/files
* Egy adott nevő (24-cell.jpg) állomány letöltése és mentése donloaded-decrypted_2_24-cell.jpg néven:
curl --output donloaded-decrypted_2_24-cell.jpg http://localhost:8080/api/file/24-cell.jpg


### Hiányosságok, fejlesztendők, fejleszthetők
A jelen állapotú megoldás nem tekinthető produkciós szintű és minőségű megoldásnak (tekintettel a szűk határidőre), ezért az alábbi lista feltárja a hiányosságokat:
* Swagger végpont-dokumentáció
* További (kivételes lefutási ágakat tesztelő) integrációs tesztek
* Konzekvens logolás
* Korrekt és egységes hibakezelés (pl. )
* Statikus kódellenőrzésből kieső feladatok
* Clean code szigorúbb követése
* Kódtisztítás, egyszerűsítés
* Néhány helyen indokolt lehet tervezési minta használata (pl. képátméretezés)

A feladatkiírásban nem szereplő, de indokolt továbbfejlesztési lehetőségekre néhány példa:
* Titkosítási kulcs generáló utility felokosítása parancssori paraméterek feldolgozására és azok alapján történő kulcsállomány létrehozására (pl. kulcsállomány neve, kulcsméret, ...stb.)
* Jelszó alapú AES titkosítás támogatása
* Képtörlési, átnevezési, ...stb. funkciók