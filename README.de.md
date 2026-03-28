# Der Aqualizer

Dieses Schulprojekt entstand auf Grundlage des UN-Nachhaltigkeitsziels **„Leben unter Wasser“ (Life Below Water)**. Im Rahmen des Projekts haben wir uns intensiv mit verschiedenen Problemfeldern beschäftigt, darunter Überfischung, Übersäuerung von Gewässern sowie Wasserverschmutzung.

Ein damit verbundenes Problem ist, dass die tatsächlichen Fischbestände in Gewässern oft nicht exakt bekannt sind. Deshalb haben wir uns mit der grundlegenden Frage beschäftigt:

> **Kann Fisch _x_ in Habitat _y_ (über)leben?**

Dabei bleibt zunächst offen, um welche Art von Gewässer es sich handelt. Möglich sind beispielsweise Aquarium, Teich, Fluss, See oder Ozean.

---

# Ziel der Anwendung

Die Anwendung soll es ermöglichen:

- **Wasserdaten zu erfassen oder zu importieren**
- **Toleranzbereiche verschiedener Fischarten zu definieren**
- **die Kompatibilität zwischen Fischarten und Gewässerbedingungen auszuwerten**

Eine Übersicht über diese Funktionen wird im zugehörigen Komponentendiagramm dargestellt:

![komponenten](komponenten.png)

---

# Funktionen der Anwendung

Die Anwendung bietet folgende Funktionen:

1. Fischarten mit ihren jeweiligen Toleranzbereichen erfassen
2. Wassermessungen manuell erfassen
3. Wasserdaten aus einer CSV-Datei importieren
4. Wassermessungen suchen und filtern
5. Fischart auswählen
6. Kompatibilität zwischen Fischart und Wassermessungen berechnen
7. Auswertungsergebnisse als CSV-Datei exportieren

---

# Messbare Wasserparameter

Die Anwendung unterstützt die Erfassung der folgenden Wasserparameter:

- **Wassertemperatur**
- **Sauerstoffgehalt**
- **Salzgehalt**
- **pH-Wert**

Für jede Fischart können Toleranzbereiche für diese vier Parameter definiert werden.

---

# Bewertung der Wasserqualität

Jede Fischart besitzt einen bestimmten Toleranzbereich, in dem sie überleben kann. Werden die Grenzen dieses Bereichs überschritten, kann der Fisch langfristig nicht überleben.

Um zu bewerten, wie gut die Umweltbedingungen für eine Fischart geeignet sind, orientiert sich die Berechnung am Mittelwert des jeweiligen Toleranzbereichs.

![qualität](qualität.png)

Beispiel:

Liegt der gemessene pH-Wert bei 7, wird überprüft, wie nah dieser Wert am optimalen Bereich (Mittelwert) des Toleranzbereichs der Fischart liegt. Daraus ergibt sich die Bewertung von **gut** bis **kritisch** in folgender Reihenfolge:

- **gut**
- **ok**
- **riskant**
- **schlecht**
- **kritisch**