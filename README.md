# Animal War

Game strategi jalur dua markas untuk tugas akhir Pemrograman Berorientasi Objek. Pemain memilih hewan untuk menyerang Tower musuh; Tower juga mengirim hewan secara berkala. Hewan bergerak otomatis, bertarung, dan memberi money serta EXP saat mengalahkan lawan.

## Menjalankan demo

Pastikan JDK dan Maven terpasang, lalu jalankan dari folder proyek:

```bash
mvn clean package
java -jar target/animal-war-1.0.0.jar
```

Jika belum memakai Maven, kompilasi langsung dari PowerShell:

```powershell
$sources = Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object FullName
javac -d out $sources
java -cp out lamana.Main
```

Demo menggunakan tampilan Swing. Klik tombol hewan untuk spawn, gunakan EXP untuk upgrade Base, dan lindungi Base sampai Tower musuh hancur.

## Mekanik demo

- Semua hewan bergerak dengan movement speed yang sama.
- Unit berjalan berbaris dengan jarak aman; hewan ranged dapat menyerang dari belakang jika range-nya cukup.
- Serangan kedua tim terjadi bersamaan setiap satu detik.
- Kill memberi money dan EXP.
- Pemain mendapat passive income setiap detik; income bertambah saat Base naik level.
- Base pemain dan Tower musuh masing-masing memiliki turret; level lebih tinggi meningkatkan damage dan mempercepat tembakan.
- Tower musuh naik level dan spawn hewan otomatis tiap 5/4/3 detik sesuai level.
- HP Tower atau Base mencapai 0 menentukan menang atau kalah.
