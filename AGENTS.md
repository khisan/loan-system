# Loan System

REST API backend untuk manajemen pinjaman. Spring Boot 4.1.1, Java 25, Gradle 9.7.1 (wrapper), PostgreSQL.

## Build & run

Selalu pakai wrapper dari root project (`/mnt/NewVolume/Learning/loan-system`):

- Dev server: `./gradlew bootRun`
- Test: `./gradlew test` — jalankan sebelum bilang pekerjaan selesai
- Build penuh: `./gradlew build`

## Layout kode

Package root: `com.loan.loan_system` (pakai **underscore**, bukan tanda hubung — lihat `HELP.md`).

- `controller/` — REST endpoint
- `service/` — logika bisnis
- `repository/` — Spring Data JPA
- `entity/` — entitas JPA
- `dto/request/`, `dto/response/` — DTO request/response (termasuk `ApiResponse`)
- `enums/` — status pinjaman, jaminan, dll.
- `security/` — `SecurityConfig`, `JwtAuthenticationFilter`
- `config/` — konfigurasi + `DataSeeder`
- `exception/` — `GlobalExceptionHandler`

Pola umum: controller → service → repository. Tambahkan validasi dengan anotasi Jakarta Validation pada DTO, dan lempar error yang ditangani `GlobalExceptionHandler` agar response konsisten.

## Dependency penting

- Spring WebMVC, Data JPA, Security, Validation
- PostgreSQL (`org.postgresql:postgresql`)
- Lombok — pakai untuk boilerplate, jangan tulis getter/setter manual
- JJWT 0.11.5 — autentikasi bearer token
- `springboot4-dotenv` (developmentOnly) — memuat nilai dari `.env`

## Konfigurasi & rahasia

- `src/main/resources/application.properties` berisi `spring.datasource.*`, `jwt.secret`, `jwt.expiration`, `server.port`, `server.address`, dan `logging.level.*`. Nilai sensitif berasal dari `.env`.
- `.env` sudah masuk `.gitignore`. **Jangan pernah baca, tampilkan, atau kirim isi `.env`, jwt.secret, atau kredensial DB ke chat/log/commit.** Kalau butuh mengubah konfigurasi, edit `.env` langsung atau minta pengguna yang melakukannya.
- Jangan commit `build/`, `.gradle/`, `bin/`.

## Catatan lingkungan

- Proyek berada di volume eksternal `/mnt/NewVolume/Learning/loan-system`. Pastikan volume ter-mount dan bisa ditulis sebelum menjalankan Gradle.
- `hoppscotch` MCP server terdaftar di `.mcp.json` untuk eksplorasi API.
