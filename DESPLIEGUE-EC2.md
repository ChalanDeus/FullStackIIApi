# Despliegue en AWS EC2

Guía para ejecutar la API en una instancia EC2, tal como pide el trabajo.

---

## 1. Requisitos en AWS

| Elemento | Valor |
|---|---|
| AMI | Ubuntu Server 22.04 o 24.04 LTS |
| Tipo de instancia | `t2.micro` o `t3.micro` (capa gratuita) |
| Par de claves | `.pem` (guárdalo, lo necesitarás para conectarte) |

### Security Group — puertos de entrada

| Puerto | Protocolo | Origen | Uso |
|---|---|---|---|
| 22 | TCP | Tu IP | SSH |
| 8080 | TCP | 0.0.0.0/0 | API y Swagger UI |

---

## 2. Generar el `.jar` en tu PC

```powershell
cd C:\Users\ernes\Desktop\Duoc\FullStack\biblioteca-api
.\mvnw.cmd clean package
```

Se genera:

```
target\demo-0.0.1-SNAPSHOT.jar
```

---

## 3. Conectarse a la instancia

```powershell
ssh -i "C:\ruta\a\tu-llave.pem" ubuntu@IP_PUBLICA
```

> En Windows, si la llave da error de permisos:
> ```powershell
> icacls "C:\ruta\a\tu-llave.pem" /inheritance:r /grant:r "$($env:USERNAME):(R)"
> ```

---

## 4. Instalar Java en la instancia

```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless
java -version
```

---

## 5. Subir el `.jar`

Desde **tu PC** (no desde la instancia):

```powershell
scp -i "C:\ruta\a\tu-llave.pem" "C:\Users\ernes\Desktop\Duoc\FullStack\biblioteca-api\target\demo-0.0.1-SNAPSHOT.jar" ubuntu@IP_PUBLICA:/home/ubuntu/
```

---

## 6. Ejecutar la API

### Opción rápida (se corta al cerrar la sesión)

```bash
cd /home/ubuntu
java -jar demo-0.0.1-SNAPSHOT.jar
```

### Opción recomendada (queda corriendo en segundo plano)

```bash
cd /home/ubuntu
nohup java -jar demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

Ver logs:

```bash
tail -f app.log
```

Detenerla:

```bash
pkill -f demo-0.0.1-SNAPSHOT.jar
```

### Opción servicio systemd (arranca sola si la instancia reinicia)

```bash
sudo tee /etc/systemd/system/demo.service > /dev/null <<'EOF'
[Unit]
Description=API Biblioteca DSY1104
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/demo-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now demo
sudo systemctl status demo
```

---

## 7. Verificar

| Recurso | URL |
|---|---|
| Swagger UI | `http://IP_PUBLICA:8080/swagger-ui.html` |
| OpenAPI JSON | `http://IP_PUBLICA:8080/v3/api-docs` |
| Lista de libros | `http://IP_PUBLICA:8080/api/v1/libros` |

Si no responde, revisa:

1. Que el Security Group tenga el puerto **8080** abierto.
2. Que el proceso esté vivo: `ps -ef | grep demo`.
3. Los logs: `tail -f app.log` o `journalctl -u demo -f`.

---

## 8. Consideraciones

- **H2 es en memoria**: cada vez que se reinicia la aplicación, los datos vuelven a los 10 libros de `data.sql`. Para persistencia real habría que usar H2 en archivo o una base externa.
- **No expongas** la consola `/h2-console` a internet en producción.
- Para la entrega sirve **capturas** de Swagger UI y de Postman/Swagger ejecutando un POST y un GET sobre la IP pública.
