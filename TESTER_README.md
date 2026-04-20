# Quality Alternative: instrukcja testowania Android alpha

To jest instrukcja dla osób testujących wewnętrzną wersję alpha aplikacji `Quality Alternative`.

Aplikacja zatrzymuje automatyczne wejście do wybranych rozpraszających aplikacji i pokazuje jedną lepszą alternatywę do przeczytania, dwa zapasowe wybory oraz opcję świadomego wejścia mimo wszystko.

## 1. Pobierz APK

Wejdź w najnowszy release:

[Quality Alternative Releases](https://github.com/dkacz/qualityalternative/releases/latest)

Pobierz plik:

`quality-alternative-v0.1.0-alpha-debug.apk`

Opcjonalnie możesz pobrać też plik `.sha256`, ale do zwykłego testu nie jest potrzebny.

## 2. Zainstaluj aplikację

1. Otwórz pobrany plik APK na telefonie z Androidem.
2. Jeśli Android zapyta o zgodę na instalowanie z tej aplikacji, wejdź w ustawienia i włącz `Zezwalaj z tego źródła`.
3. Wróć do instalatora i zainstaluj `Quality Alternative`.
4. Otwórz aplikację po instalacji.

Nazwy ustawień mogą się lekko różnić zależnie od producenta telefonu. Na polskim Androidzie szukaj najczęściej: `Instaluj nieznane aplikacje`, `Nieznane źródła` albo `Zezwalaj z tego źródła`.

## 3. Przejdź onboarding

W aplikacji wybierz:

1. Co najmniej trzy aplikacje, które chcesz traktować jako rozpraszające.
2. Co najmniej trzy tematy, które cię interesują.
3. Preferowaną długość sesji czytania.
4. Starter packi z treściami.

Wersja alpha obsługuje lokalne editorial starter packi. Nie ma jeszcze dodawania własnych linków ani PDF-ów.

## 4. Włącz Accessibility

Interception działa przez usługę Dostępności Androida.

Na polskim Androidzie najczęściej znajdziesz to tutaj:

1. Otwórz `Ustawienia`.
2. Wejdź w `Dostępność`.
3. Znajdź sekcję typu `Zainstalowane aplikacje` albo `Pobrane aplikacje`.
4. Wybierz `Quality Alternative`.
5. Włącz usługę.
6. Potwierdź ostrzeżenie systemowe.

Aplikacja nie potrzebuje w tej alphie logowania ani backendu. Dane testowe, historia i opóźnienia są lokalne na telefonie.

## 5. Wybierz aplikacje do testu

Wróć do `Quality Alternative` i upewnij się, że wybrane są aplikacje, które masz na telefonie.

Aktualna lista wspierana w alphie:

- YouTube
- X
- Facebook
- Instagram
- Reddit
- TikTok

Najlepiej zacząć od YouTube, X albo Facebooka, bo te przepływy były już potwierdzone na realnym urządzeniu.

## 6. Sprawdź podstawowy flow

1. Zamknij `Quality Alternative`.
2. Otwórz jedną z wybranych aplikacji, na przykład YouTube.
3. Powinien pojawić się ekran interwencji `Quality Alternative`.
4. Sprawdź, czy widzisz jedną główną rekomendację i dwie alternatywy.
5. Kliknij `Read now` i sprawdź, czy otwiera się czytnik.
6. Wróć i sprawdź `Open anyway`, czyli świadome wejście do oryginalnej aplikacji.
7. Sprawdź `Delay for 15 minutes`.
8. Po wybraniu opóźnienia otwórz tę samą aplikację ponownie i upewnij się, że pełna interwencja nie pojawia się natychmiast drugi raz.

Właśnie ten moment jest najważniejszy w alphie: aplikacja ma zatrzymać impuls, zaproponować skończoną alternatywę i nie zamienić się w kolejny feed.

## 7. Co raportować po teście

Wyślij proszę krótką informację:

- model telefonu
- wersja Androida
- jakie aplikacje testowałeś
- czy interwencja pojawiła się po otwarciu aplikacji
- czy `Open anyway` działa bez zapętlenia
- czy `Delay for 15 minutes` faktycznie wstrzymuje ponowną interwencję
- czy aplikacja crashuje, zawiesza się albo pokazuje pusty ekran
- czy rekomendacje treści wyglądają sensownie

Przykładowy raport:

```text
Telefon: Samsung S24
Android: 15
Testowane: YouTube, X
Interwencja: działa na obu
Open anyway: działa
Delay: działa, nie pyta ponownie od razu
Problemy: brak / opis problemu
```

## 8. Znane ograniczenia alpha

- To jest build debug/internal alpha, nie produkcyjna wersja ze sklepu.
- Działa tylko na Androidzie.
- Wymaga włączenia Accessibility.
- Nie ma jeszcze iOS.
- Nie ma jeszcze własnych linków, PDF-ów ani synchronizacji w chmurze.
- Nie blokuje pojedynczych powierzchni typu Shorts/Reels; przechwytuje całą wybraną aplikację.
- Testy real-device były dotąd potwierdzone na jednym Samsungu dla YouTube, X i Facebooka.

## 9. Jak odinstalować

Jeśli chcesz zakończyć test:

1. Wejdź w `Ustawienia`.
2. Otwórz `Aplikacje`.
3. Znajdź `Quality Alternative`.
4. Wybierz `Odinstaluj`.

Jeśli Android nie pozwala odinstalować aplikacji, najpierw wyłącz ją w `Ustawienia -> Dostępność -> Quality Alternative`, a potem spróbuj ponownie.
