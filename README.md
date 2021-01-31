# nerka3d

# Uruchamianie skryptu do konwersji
Plik z danymi *mat* musi znajdować się w folderze **data**. Skrypt konwertuje go na kilka plików *json* w folderze **data/output**. Ze względu na duży rozmiar danych, jeden plik odpowiada jednemu skanowi dla danej warstwy. Pliki wyjściowe nazywane są według schematu **nazwapliku**\_**typ**\_**x**\-**y**.json, gdzie **nazwapliku** to nazwa pliku wejściowego *mat* z danymi, **typ** to nazwa obiektu, którego dane dotyczą (kidney / cancer), **x** to numer warstwy, a **y** to numer skanu dla danej warstwy i danego obiektu.

Przykładowe wykonanie funkcji:

```convertCT('ct16b.mat')```

Wynikowe dane mają postać tablicy 4000x6000 zawierającej liczby 1 (obiekt) lub 0 (otoczenie) dla jednego skanu danej warstwy.
