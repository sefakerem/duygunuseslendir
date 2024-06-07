# 🎉 Duyguları anlama App - README 📖

## 📱 Android Uygulaması

Bu uygulama, kullanıcıdan rastgele seçilmiş bir duyguyu ifade etmesini ister ve ses kaydını backend'e gönderir. Kullanıcı her bir duygu için 3 hakka sahiptir ve tüm duyguları seslendirdikten sonra bir skor alır.

### Gereksinimler

- Android Studio
- Bir Android cihaz veya emulator (API 21 veya üstü)
- İnternet bağlantısı

### Kurulum Adımları

1. **Proje Klasörünü Açın**: Android Studio'da proje klasörünü açın.
2. **Gerekli Bağımlılıkları Yükleyin**: `build.gradle` dosyasını kontrol edin ve gerekli bağımlılıkları yükleyin.
3. **Manifest Dosyasını Kontrol Edin**: `AndroidManifest.xml` dosyasında gerekli izinlerin bulunduğundan emin olun.
    ```xml
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    ```
4. **Uygulamayı Çalıştırın**: Bir cihaz veya emulator seçin ve uygulamayı çalıştırın.

### Kullanım

1. **Kayıt Başlat**: `Kayıt Başlat` butonuna basarak kaydı başlatın.
2. **Kayıt Durdur**: `Kayıt Durdur` butonuna basarak kaydı durdurun. Ses dosyası backend'e gönderilecektir.
3. **Sonuçları Görüntüleyin**: Duygu ve skoru ekranda görüntüleyin.

---

## 🌐 Backend - FastAPI

Bu backend, ses dosyasını alır ve duygu tahmini yapar.

### Gereksinimler

- Python 3.8 veya üstü
- Pip paket yöneticisi

### Kurulum Adımları

1. **Gerekli Kütüphaneleri Yükleyin**:
    ```bash
    pip install fastapi uvicorn tensorflow librosa numpy
    ```

2. **Model Dosyasını Ekleyin**: Eğitimli model dosyanızı (`model.h5`) projenizin kök dizinine ekleyin.


### Çalıştırma

1. **Uygulamayı Başlatın**:
    ```bash
    uvicorn main:app --reload
    ```

### API Kullanımı

- **Endpoint**: `/upload`
- **Yöntem**: `POST`
- **Parametreler**: Multipart form-data olarak `file` (WAV formatında ses dosyası)

### API Örneği

```bash
curl --location 'http://127.0.0.1:8000/upload' --form 'file=@"/path/to/file.wav"'
```
---
### 🚀 Projeyi Başlatma

#### 1. Backend'i Başlatın

Backend sunucusunu çalıştırmak için:

```bash
uvicorn main:app --reload
```

#### 2. Android Uygulamasını Başlatın

Android Studio'da projeyi çalıştırın ve talimatları izleyin.

---

### 💡 İpuçları ve Öneriler

- **Geliştirme Modu**: Backend'inizi geliştirme modunda (`--reload` bayrağı ile) çalıştırın, böylece kodda yaptığınız değişiklikler otomatik olarak yansır.
- **Emulator Kullanımı**: Android emulator kullanıyorsanız, backend sunucusuna bağlanmak için `10.0.2.2` IP adresini kullanın.
---
### 📊 Veri Seti

Bu proje için kullanılan veri seti:

- [TurEV-DB](https://github.com/Xeonen/TurEV-DB)
---

### 📚 Kaynaklar

- [FastAPI Belgeleri](https://fastapi.tiangolo.com/)
- [Retrofit Belgeleri](https://square.github.io/retrofit/)
- [Android Studio Kurulum Kılavuzu](https://developer.android.com/studio/install)

---

