from fastapi import FastAPI, UploadFile, File, HTTPException, Security, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.responses import FileResponse
from pathlib import Path
import shutil
import jwt

app = FastAPI(title="Secure File Service")

STORAGE_DIR = Path("/data")
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

PUBLIC_KEY_PATH = Path("/run/secrets/jwt-public.pem")

try:
    PUBLIC_KEY = PUBLIC_KEY_PATH.read_text()
except Exception as e:
    PUBLIC_KEY = None

security = HTTPBearer()

def verify_jwt(credentials: HTTPAuthorizationCredentials = Security(security)):
    if not PUBLIC_KEY:
        raise HTTPException(status_code=500, detail="Server public key not configured")
    
    token = credentials.credentials
    try:
        payload = jwt.decode(token, PUBLIC_KEY, algorithms=["RS256"])
        return payload
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except jwt.InvalidTokenError as e:
        print(f"JWT validation failed: {type(e).__name__}: {e}")
        raise HTTPException(
            status_code=401,
            detail="Could not validate credentials"
        )

def get_file_path(filename: str) -> Path:
    filename = filename.replace("\\", "/").split("/")[-1]

    if not filename or filename in (".", ".."):
        raise HTTPException(status_code=400, detail="Invalid filename")

    file_path = (STORAGE_DIR / filename).resolve()

    if file_path.parent != STORAGE_DIR.resolve():
        raise HTTPException(status_code=400, detail="Invalid file path")

    return file_path

@app.post("/files")
def upload_file(
    file: UploadFile = File(...), 
    user: dict = Depends(verify_jwt)
):
    file_path = get_file_path(file.filename)

    if file_path.exists():
        raise HTTPException(status_code=409, detail="File already exists")

    try:
        with file_path.open("xb") as destination:
            shutil.copyfileobj(file.file, destination)

    except Exception:
        file_path.unlink(missing_ok=True)
        raise HTTPException(status_code=500, detail="File upload failed")

    finally:
        file.file.close()

    return {
        "message": "File uploaded successfully",
        "filename": file_path.name,
        "uploaded_by": user.get("sub")
    }

@app.get("/files")
def list_files(user: dict = Depends(verify_jwt)):
    files = []

    for file_path in STORAGE_DIR.iterdir():
        if file_path.is_file() and not file_path.is_symlink():
            files.append({
                "filename": file_path.name,
                "size": file_path.stat().st_size
            })

    return {
        "files": files,
        "total": len(files)
    }

@app.get("/files/{filename}")
def get_file(
    filename: str, 
    user: dict = Depends(verify_jwt)
):
    file_path = get_file_path(filename)

    if not file_path.is_file() or file_path.is_symlink():
        raise HTTPException(status_code=404, detail="File not found")

    return FileResponse(
        path=file_path,
        filename=file_path.name
    )

@app.delete("/files/{filename}")
def delete_file(
    filename: str, 
    user: dict = Depends(verify_jwt)
):
    file_path = get_file_path(filename)

    if not file_path.is_file() or file_path.is_symlink():
        raise HTTPException(status_code=404, detail="File not found")

    file_path.unlink()

    return {
        "message": "File deleted successfully",
        "filename": filename
    }