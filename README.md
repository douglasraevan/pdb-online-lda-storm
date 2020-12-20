# pdb-online-lda-storm

## Instalasi dan Eksekusi di Local

1. Git clone project ini dengan command `git clone https://github.com/douglasraevan/pdb-online-lda-storm.git`
2. Di dalam folder `online-lda-storm/resources`, jalankan command untuk copy file Config.
```
cp config.sample.properties config.properties
``` 

3. Agar bisa terkoneksi dengan Twitter, daftarkan akun di [Developer Twitter](https://developer.twitter.com)
4. Isi parameter di `config.properties`
```
# Properties for using Twitter API. Dev account OAuth and creds.
twitter.dev.consumer.key = <ISI_CONSUMER_KEY>
twitter.dev.consumer.secret = <ISI_CONSUMER_SECRET>
twitter.dev.oauth_access_token = <ISI_ACCESS_TOKEN>
twitter.dev.oauth_access_secret = <ISI_ACCESS_SECRET>
twitter.account.name = <tidak usah diisi>
twitter.account.password = <tidak usah diisi>
```
5. Import Project online-lda-storm dengan IntelliJ. Import pom.xml.
