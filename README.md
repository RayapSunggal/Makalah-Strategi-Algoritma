# ♠️♥️ Bot Bridge Greedy Murni (Visual GUI) ♦️♣️

Sebuah aplikasi asisten bot interaktif berbasis **Java Swing** untuk permainan kartu Bridge. Bot ini mengimplementasikan algoritma **Greedy Murni** (*Pure Greedy*) untuk membantu Anda mengambil keputusan terbaik di atas meja pada *trick* yang sedang berjalan, mulai dari fase *bidding* hingga ke-13 *trick* selesai.

Aplikasi ini dikemas dalam **satu file Java tunggal** (`BotGUI.java`) lengkap dengan antarmuka (GUI) visual papan 52 kartu interaktif, sehingga Anda tidak perlu lagi mengetik input kartu satu per satu secara manual.

---

## ✨ Fitur Utama

* **Papan Kartu Visual (52 Cards Grid):** Antarmuka responsif berbentuk *checkbox* bergambar (♠, ♥, ♦, ♣) untuk memilih kartu Anda, kartu Dummy, dan kartu yang dimainkan lawan dengan satu kali klik.
* **Sistem *Bidding* Cerdas:** Otomatis menghitung *High Card Points* (HCP) dan menganalisis *Suit* terpanjang/terkuat Anda untuk memberikan rekomendasi *Bidding* yang logis (Pass, Minimum Response, Invite, atau Game Forcing).
* **Mode Bermain Ganda:** Mendukung simulasi permainan baik saat tim Anda **Menyerang** (*Declarer* + *Dummy*) maupun **Bertahan** (*Defender*).
* **Logika Anti-Tabrak Teman (*Partner Protection*):** Jika teman (*partner*) dipastikan sedang memenangkan *trick* saat itu, bot tidak akan menyarankan Anda membuang kartu besar (tidak "nabrak"), melainkan menyarankan membuang kartu terkecil (*discard/follow suit* terendah).
* **Visualisasi Saran (*Highlighting*):** Saat giliran Anda atau Dummy, bot akan otomatis menyoroti (mewarnai latar belakang menjadi **Kuning**) kartu terbaik yang direkomendasikan untuk dikeluarkan.
* **Otomatisasi Filter Kartu:** Papan bermain akan secara cerdas menyembunyikan kartu yang tidak mungkin dimiliki oleh musuh (kartu yang ada di tangan Anda, Dummy, atau yang sudah terbuang).

---

## 🛠️ Prasyarat (Prerequisites)

Untuk menjalankan aplikasi ini, komputer Anda hanya memerlukan:
* **Java Development Kit (JDK):** Versi 8 atau yang lebih baru.

---

## 🚀 Cara Menjalankan Aplikasi

1.  Unduh atau salin seluruh kode program dan simpan ke dalam file bernama `BotGUI.java`.
2.  Buka terminal atau *Command Prompt* (CMD) / PowerShell.
3.  Arahkan direktori (`cd src`).
4.  Kompilasi program dengan perintah:
    ```bash
    javac BotGUI.java
    ```
5.  Jalankan program dengan perintah:
    ```bash
    java BotGUI
    ```

---

## 🎮 Panduan Penggunaan (Alur Permainan)

Permainan dibagi menjadi beberapa fase yang diatur secara otomatis oleh bot:

### Fase 1: Input Tangan Anda
Papan akan menampilkan 52 kartu aktif. Klik (centang) tepat **13 kartu** yang Anda pegang di tangan. Setelah selesai, klik tombol **"Konfirmasi / Lanjut"** di kanan bawah.

### Fase 2: Fase *Bidding* (Tawar-Menawar)
Sistem akan menampilkan jumlah poin HCP dan *Suit* terbaik Anda di layar Log.
* Bot akan meminta Anda mengetikkan *bid* terakhir dari teman/partner Anda (Misalnya ketik: `PASS`, `1H`, `1NT`, dll) di kotak teks bagian bawah.
* Klik Konfirmasi, dan bot akan menyarankan *bid* yang harus Anda teriakkan di meja.

### Fase 3: Penentuan Kontrak Final
Di kotak teks, masukkan data kontrak akhir:
1.  Ketik `1` jika Tim Kita menang *bidding* (Menyerang), atau `2` jika Bertahan.
2.  Ketik huruf lambang *Trump* yang disepakati (Ketik `C`, `D`, `H`, `S`, atau `N` untuk *No-Trump*).

*(Jika Anda memilih peran Menyerang, papan kartu akan terbuka kembali untuk meminta Anda mencentang 13 kartu Dummy milik teman Anda).*

### Fase 4: Bermain (*Trick Phase*)
* **Menentukan *Leader*:** Masukkan angka `0` (Anda), `1` (Lawan Kiri), `2` (Teman/Dummy), atau `3` (Lawan Kanan) untuk orang yang jalan pertama kali.
* **Giliran Lawan:** Papan visual akan menyisakan kartu yang tidak diketahui. Klik 1 kartu yang dikeluarkan oleh lawan.
* **Giliran Anda/Dummy:** Bot akan menyoroti satu kartu dengan **warna Kuning**. Itulah saran dari algoritma. Anda cukup mengklik kartu tersebut (atau kartu lain pilihan Anda) dan tekan Konfirmasi.
* Aplikasi akan melacak skor *trick* secara *real-time* hingga 13 *trick* selesai.

---
