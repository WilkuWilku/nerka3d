# nerka3d

# Uruchamianie skryptu do konwersji
Plik z danymi *mat* musi znajdować się w folderze **data**. Skrypt konwertuje go na kilka plików *ctl (Computed Tomography Layer)* w folderze **data/output**. Jeden plik odpowiada jednemu skanowi dla danej warstwy. Pliki wyjściowe nazywane są według schematu **nazwapliku**\_**typ**\_**x**\-**y**.json, gdzie **nazwapliku** to nazwa pliku wejściowego *mat* z danymi, **typ** to nazwa obiektu, którego dane dotyczą (kidney / cancer), **x** to numer warstwy, a **y** to numer skanu dla danej warstwy i danego obiektu.

Przykładowe wykonanie funkcji:

```convertCT('ct16b.mat')```

Wynikowe dane mają postać skompresowanej tablicy 4000x6000 zawierającej liczby 1 (obiekt) lub 0 (otoczenie) dla jednego skanu danej warstwy.

# Kompresja danych w plikach ctl
Ze względu na dużą liczbę powtarzalnych danych, pliki wynikowe należało skompresować.

Dane wejściowe miały dość charakterystyczne cechy:
- składały się tylko z dwóch różnych wartości \- 0 i 1
- występowały bardzo długie serie tej samej wartości
- zmiany serii wartości były bardzo rzadkie


Z tego względu idealnie sprawdza się tu prosty algorytm kompresji bezstratnej *RLE (Run-Length Encoding)*. Polega on na zapisaniu długości serii danej wartości zamiast całego ciągu. Przy kompresowaniu z góry ustalamy, że najpierw podawana jest liczba zer, następnie liczba jedynek i dalej na zmianę. Długości oddzielane są średnikiem. Koniec wiersza zaznaczany jest przejściem do nowej linijki (*\\n*). Za kompresję pojedynczego wiersza wartości odpowiada funkcja *rleCompressRow.mat*. Poniżej przedstawiono przykłady kompresji danych z jej pomocą:

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

W ten sposób te same dane z bardzo ciężkich plików json, ważących łącznie ponad 1 GB udało się zapisać w odchodzonych plikach skompresowanych, ważących łącznie niecały 1 MB

TODO: Implementacja dodatkowej kompresji po wierszach - w plikach ctl, gdzie obiekt zajmuje małą powierzchnię, występuje dużo powtarzających się wartości 6000. Tutaj również sprawdzi się algorytm RLE
