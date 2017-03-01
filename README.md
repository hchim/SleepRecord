#About

This is the SleepRecord Android Application.

#How to Add a Module

Checkout the package in the modules directory and Add the module to the project.

#Generate API key

* Sign the APK with the keystore.
* Unzip the signed apk file and use keytool to print the signatures of the .RSA file. `$keytool -printcert -file CERT.RSA`
* Use the package name, KeyStore file, KeyStore password, certificate alias and the SHA256 signature to generate the API key by invoking the generateAndroidAPIKey method of APIKeyEncoder.
* Put the apikey string to assets file 'apikey'.
* Use any md5 tool to get the md5 hash of the signature part (second part) of the API key file (payload.signature).
* Put the md5 hash to apikey_decoder.cpp as the value of the APIKEY_SIG_MD5 constant.
* Rebuild the project and sign the generated APK.