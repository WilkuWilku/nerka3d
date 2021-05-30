# nerka3d

# Etapy przetwarzania danych w celu wizualizacji nerki

1. [Konwersja danych z plików .mat na pliki .ctl](#konwersja)
2. [Załadowanie plików .ctl przez aplikację backendową](#ladowanie-backend)
3. [Wyodrębnienie obrysu nerki dla każdej z warstw](#obrys)
4. [Zaindeksowanie w kolejności punktów obrysu nerki](#kolejnosc)
5. [Zredukowanie liczby zaindeksowanych punktów](#redukcja)
6. [Przeprowadzenie triangulacji dla sąsiednich warstw](#triangulacja)
7. [Zwrócenie listy trójkątów do wyświetlenia](#zwrot-na-frontend)
8. [Narysowanie trójkątów przez aplikację frontendową](#rendering)

## <a name="konwersja">Konwersja danych z plików .mat na pliki .ctl</a>

Ze względu na to, że pliki wejściowe pochodzą z Matlaba, musimy je
przekonwertować na format zrozumiały dla aplikacji napisanej w Javie.

### Uruchamianie skryptu do konwersji

Pliki z danymi *mat* muszą znajdować się w folderze **data**. Skrypt
konwertuje plik z danymi nerki na kilka plików *ctl (Computed Tomography
Layer)* w folderze **data/output**. Jeden plik odpowiada jednemu skanowi
dla danej warstwy. Pliki wyjściowe nazywane są według schematu
**nazwapliku**\_**typ**\_**x**\-**y**.ctl, gdzie **nazwapliku** to nazwa
pliku wejściowego *mat* z danymi, **typ** to nazwa obiektu, którego dane
dotyczą (kidney / cancer), **x** to numer warstwy, a **y** to numer
skanu dla danej warstwy i danego obiektu. Drugi plik wejściowy powinien
zawierać informacje o przesunięciach między warstwami. Skrypt zawiera
informacje dotyczące danej warstwy w odpowiednim pliku .ctl.

Przykładowe wykonanie funkcji:

```convertCT('ct16b.mat', 'ct16align.mat')```

Wynikowe dane mają postać skompresowanej tablicy 4000x6000 zawierającej
liczby 1 (obiekt) lub 0 (otoczenie) dla jednego skanu danej warstwy.
Poprzedza je nagłówek z informacją o nazwie i numerze warstwy oraz
przesunięciach w osi X, Y i Z.

### Kompresja danych w plikach ctl

Ze względu na dużą liczbę powtarzalnych wartości, dane wynikowe należało
skompresować.

Dane wejściowe miały dość charakterystyczne cechy:
- składały się tylko z dwóch różnych wartości \- 0 i 1
- występowały bardzo długie serie tej samej wartości
- zmiany serii wartości były bardzo rzadkie


Z tego względu idealnie sprawdza się tu prosty algorytm kompresji
bezstratnej *RLE (Run-Length Encoding)*. Polega on na zapisaniu długości
serii danej wartości zamiast całego ciągu. Przy kompresowaniu z góry
ustalamy, że najpierw podawana jest liczba zer, następnie liczba jedynek
i dalej na zmianę. Długości oddzielane są średnikiem. Koniec wiersza
zaznaczany jest przejściem do nowej linijki (*\\n*). Za kompresję
pojedynczego wiersza wartości odpowiada funkcja *rleCompressRow.mat*.
Poniżej przedstawiono przykłady kompresji danych z jej pomocą:

```
rleCompressRow('00001111110001011000000111111100000000')
result: "4;6;3;1;1;2;6;7;8"
```

```
rleCompressRow('1111111111100000000000000111111110000100')
result: "0;11;14;8;4;1;2"
```

```
rleCompressRow('000011111111111111111111000000111111111')
result: "4;20;6;9"
```

W ten sposób te same dane z bardzo ciężkich plików json, ważących
łącznie ponad 1 GB udało się zapisać w odchodzonych plikach
skompresowanych, ważących łącznie niecały 1 MB.


## <a name="ladowanie-backend">Załadowanie plików .ctl przez aplikację backendową</a>

Po otrzymaniu requestu z aplikacji frontendowej, aplikacja backendowa
pobiera pliki ctl i rozpoczyna przetwarzanie. Odczytuje informacje o
nazwie i numerze warstwy oraz dekompresuje dane wejściowe.

## <a name="obrys">Wyodrębnienie obrysu nerki dla każdej z warstw</a>

Do wizualizacji nerki absolutnie nie są potrzebne wszystkie punkty
wewnątrz, a wyłącznie sam obrys nerki. Z tego powodu należy usunąć
wszystkie punkty, które nie są na krawędzi nerki. Wykorzystaliśmy do
tego operację erozji dla sąsiedztwa ośmiospójnego. W wyniku jej
działania mapa 4000x6000 zawiera wyłącznie kilka tysięcy zaznaczonych
punktów krawędziowych zamiast setek tysięcy punktów tworzących przekrój
nerki.

## <a name="kolejnosc">Zaindeksowanie w kolejności punktów obrysu nerki</a>

Do dalszego przetwarzania konieczna jest umiejętność "przemierzania"
obrysu nerki. Zatem dla każdego punktu powinna istnieć referencja do
punktu następnego. W tym celu należy wyznaczyć pierwszy punkt na obrysie
i po kolei szukać kolejnych punktów.

### Wyznaczanie pierwszego punktu

Do wyznaczenia pierwszego punktu opracowaliśmy dwa typy algorytmów:
1. **Algorytm losowy** generuje pary współrzędnych X i Y dla danej
   warstwy do momentu aż znajdzie punkt znajdujący się na obrysie nerki.
   Jest to metoda najszybsza w implementacji, ale niestety ze względu na
   wykorzystanie generatora liczb pseudolosowych \- również
   niedeterministyczna. Przy każdym wywołaniu liczba podjętych prób do
   znaleznienia pierwszego punktu jest inna, a przez to także czas
   wykonania metody.
2. **Algorym czterostronn**y bazuje na jednej ogólnej metodzie w
   czterech odmianach. Mapa jest "przeczesywana" wiersz po wierszu
   szukając pierwszego, najbardziej skrajnego, punktu od określonej
   strony. Przykładowo - szukając "od góry" algorytm sprawdza wiersze od
   góry do dołu aż napotka na punkt krawędziowy nerki. Natomiast dla
   wariantu "prawego" algorytm sprawdza kolumny od prawej do lewej
   strony w poszukiwaniu pierwszego punktu krawędziowego. Analogicznie
   pozostałe wersje algorytmu to "dolny" i "lewy".

### Wyznaczanie kolejnych punktów

Mając określony punkt pierwszy można przystąpić do wyszukiwnia kolejnych
punktów. W tym celu, mając ustalony konkretny punkt, wyszukujemy jego
sąsiada w sąsiedztwie czterospójnym. Gdy go znajdziemy, oznaczamy do
jako "kolejny" punkt i szukamy jego sąsiada. W ten sposób przemierzamy
cały obrys do momentu aż dojdziemy do pierwszego wyznaczonego punktu. W
zdecydowanej większości przypadków każdy punkt ma dwóch sąsiadów.
Natomiast zdarzają się sporadyczne przypadki, gdy obryz nerki ma "ostre
krawędzie" i wówczas punkt może mieć więcej niż dwóch sąsiadów. Takie
przypadki wymagają specjalnego przetwarzania i nie zawsze udaje się je
prawidłowo zaindeksować.

## <a name="redukcja">Zredukowanie liczby zaindeksowanych punktów</a>

Surowe obrysy nerki składają się z kilku tysięcu punktów jeden obok
drugiego, a tak duża ich liczba w triangulacji bardziej przeszkadza niż
pomaga. Dlatego też ważnym krokiem jest zredukowanie liczny punktów
krawędziowych. Odpowiada za to prosty algorytm wybierający odpowiednie
indeksy z listy na podstawie określonego stopnia redukcji. Przykładowo
dla redukcji 10% z listy kolejnych punktów krawędziowych zostanie
wybrany co dziesiąty z nich. Dla wartości bardziej niestandardowych, jak
np. 4,19% wyliczone indeksy zostaną zaokrąglone do wartości całkowitych.
W ten sposób otrzymujemy z góry zadaną liczbę wyjściowych punktów
krawędziowych równo oddalonych od siebie.

## <a name="triangulacja">Przeprowadzenie triangulacji dla sąsiednich warstw</a>

Proces triangulacji składa się z kilku etapów, których wynikiem jest
lista trójkątów tworzących powierzchnię zewnętrzną dwóch sąsiednich
warstw. Pierwszym etapem jest wyznaczenie pierwszej pary punktów
odpowiadających \- są to punkty (po jednym na warstwę), które znajdują
się po tej samej stronie warstwy i które można ze sobą połączyć. Drugim
etapem jest dobór kolejnego punktu na warstwie górnej lub dolnej, który
z dwoma pierwszymi utworzy trójkąt. Wybrany punkt będzie stanowić nowe
odniesienie dla wyznaczenia kolejnego punktu. Proces odbywa się w pętli
dopóki nie zostaną przetworzone wszystkie punktu na obu warstwach.

### Wyznaczenie punktów odpowiadających

Do wyznaczania punktów odpowiadających na dwóch sąsiednich warstwach
opracowaliśmy dwa algorytmy:
1. **Algorytm czterostronny** \- działa on na podobnej zasadzie do
   algorytmu czterostronnego do wyznaczenie pierwszego punktu na
   obrysie. Główną różnicą jest fakt, że działa on nie na mapie punktów
   4000x6000, ale na liście zredukowanych zaindeksowanych punktów.
   Znajduje on cztery punkty skrajne w czterech stronach (ekstremalne
   wartości X, Y, \-X i \-Y) na każdą warstwę. Mając cztery pary takich
   punktów wybierana jest taka para, której punkty są najmniej oddalone
   w drugiej osi (tzn. jeżeli znaleziono punkty o skrajnych wartościach
   \-X lub X, to obliczana jest różnica współrzędnych Y).
2. **Algorytm ćwiartkowy** \- jest on rozszerzeniem powyższego algorytmu
   czterostronnego. Po wyznaczeniu czterech par punktów na obu
   warstwach, wyszukiwane są dodatkowe punkty z otoczenia. Dla każdego
   punktu skrajnego wyszukiwana jest określona liczba punktów po prawej
   i po lewej stronie. Analogicznie postępujemy dla odpowiadającego
   punktu na drugiej warstwie. Znajdując przykładowo sześć dodatkowych
   punktów dla punktu skrajnego, na obu warstwach mamy juz 14 punktów na
   jedną skrajną stronę. Wówczas następuje obliczanie odległości w
   przestrzeni 3D dla każdej kombinacji punktu z górnej i dolnej warstwy
   w ramach jednej strony. Tak samo postępujemy dla pozostałych trzech
   stron i wybieramy taką parę punktów, której odległość jest
   najmniejsza. Dodatkowe wyszukiwanie punktów jest pomocne w przypadku,
   gdy kształt obrysu jest mocno nieregularny i para punktów wyznaczona
   przez algorytm czterostronny jest mimo wszystko mocno oddalona od
   siebie. Stosując algorytm ćwiartkowy sprawdzamy znacznie więcej
   potencjalnych pierwszych par punktów, zatem dopasowanie jest lepsze.
   Oczywiście kosztem większej liczby obliczeń.


### Wyznaczanie kolejnego punktu do nowego trójkąta

Mając dwa połączone ze sobą punkty na sąsiednich warstwach, należy
znaleźć odpowiedni trzeci punkt, aby utworzyć trójkąt. Do wyboru tego
punktu opracowaliśmy trzy proste algorytmy, które kierują się różnymi
kryteriami. Każdy z nich porównuje dwa potencjalne nowe trójkąty
(aktualna para punktów + potencjalny nowy punkt górny lub dolny).

1. **Algorytm najmniejszego największego kąta** oblicza największy kąt
   dla każdego z trójkątów. Wybierany jest trójkąt o najmniejszym z
   największych kątów \- ogólnie mówiąc wybiera on najbardziej
   ostrokątny trójkąt.
2. **Algorytm najmniejszej sumy długości krawędzi** oblicza sumę
   długości krawędzi dla każdego z trójkątów. Wybierany jest trójkąt o
   najmniejszej sumie. Algorytm dąży do wyboru najmniejszego i "najmniej
   rozciągniętego" trójkąta.
3. **Algorytm najkrótszej nowej krawędzi (TODO)** oblicza długość nowej
   krawędzi w trójkącie (aktualny dolny punkt z potencjalnym górnym lub
   aktualny górny punkt z potencjalnym dolnym). Wybierany jest trójkąt o
   najkrótszej nowej krawędzi. Po wyznaczeniu nowego punktu tworzona
   jest nowa aktualna krawędź, z której wyszukiwany będzie nowy punkt.

Trójkąty wyznaczane są do momentu, aż wszystkie punkty na obu warstwach
zostaną przetworzone.

## <a name="zwrot-na-frontend">Zwrócenie listy trójkątów do wyświetlenia</a>

Po wyznaczeniu wszystkich trójkątów powinny one trafić do aplikacji
frontendowej w formie listy trójkątów dla każdej warstwy. Rozdzielenie
tych danych na każdą warstwę umożliwi w przyszłości manipulację
wizualizowanymi warstwami (np. wyłączenie dowolnej z nich).

## <a name="rendering">Narysowanie trójkątów przez aplikację frontendową</a>

Aplikacja frontendowa otrzymuje listę trójkątów do narysowania. Każdy
trójkąt renderowany jest jako powierzchnia odbijająca światło, przez co
całość wygląda bardzo realistycznie.


