# nerka3d

# Uruchamianie skryptu do konwersji
Plik z danymi *mat* musi znajdować się w folderze **data**. Skrypt konwertuje go na kilka plików *json* w folderze **data/output**. Przed uruchomieniem należy upewnić się, że w skrypcie zawarta jest prawidłowa nazwa pliku z danymi. Ze względu na duży rozmiar danych, jeden plik odpowiada jednej warstwie skanu nerki. Konwertowane dane dotyczą wyłącznie nerki, bez nowotworu.

Wynikowe dane mają postać tablicy 4000x6000 zawierająca liczby 1 (nerka) lub 0 (nie nerka) dla jednej warstwy.
