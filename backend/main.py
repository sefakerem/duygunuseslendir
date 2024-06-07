import uvicorn
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import librosa
import numpy as np
from tensorflow.keras.models import load_model
import io

app = FastAPI()

model = load_model('model.h5')


def predict_emotion(mfcc):
    input_data = np.expand_dims(mfcc, axis=0)
    input_data = np.expand_dims(input_data, axis=2)
    predictions = model.predict(input_data)[0]
    return predictions.tolist()


@app.post("/upload")
async def upload(file: UploadFile = File(...)):
    if not file:
        return JSONResponse(content={"error": "No file part"}, status_code=400)

    # Dosya uzantısını kontrol et
    if not file.filename.endswith(".wav"):
        return JSONResponse(content={"error": "Invalid file type. Please upload a WAV file."}, status_code=400)

    bytes_io = await file.read()

    try:
        # Dosya içeriğini kontrol et ve işleme al
        data, sampling_rate = librosa.load(io.BytesIO(bytes_io), sr=None)
        mfccs = np.mean(librosa.feature.mfcc(y=data, sr=sampling_rate, n_mfcc=40).T, axis=0)
        predicted_emotion = predict_emotion(mfccs)

        # Duygu etiketleri
        emotions = ["Kızgın", "Sakin", "Mutlu", "Üzgün"]
        emotion_results = {emotions[i]: f"{predicted_emotion[i] * 100:.2f}%" for i in range(len(emotions))}

        return JSONResponse(content=emotion_results)
    except Exception as e:
        return JSONResponse(content={"error": str(e)}, status_code=500)


if __name__ == '__main__':
    uvicorn.run(app, host='127.0.0.1', port=8000)
