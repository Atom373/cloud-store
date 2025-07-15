# Cloud Storage

This project represents a Google Drive like cloud file storage.

## Technologies used

- Minio (for file storing)
- Postgresql (for user data)
- Redis (for caching), 
- Spring (MVC, Data JPA, Security)

## How to run this app on your PC

NOTE: if you want to use Google Login, you have to set up Google OAuth Client Secret variable.

```bash
git clone https://github.com/Atom373/cloud-store.git
cd cloud-store
docker-compose up
```

Now you can access the app via this [link](http://localhost:8080)
