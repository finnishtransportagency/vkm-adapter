# VKM-adapteri

AWS-lambdafunktio (Java 11 Corretto).

Funktio, jonka avulla Väyläviraston uutta viitekehysmuunninta (/viitekehysmuunnin) pystyy käyttämään kuten vanhaa (/vkm).

Funktio muuttaa vanhan vkm:n kaltaiset kutsut uuden vkm:n kutsuiksi, ja uuden vkm:n palautukset vanhan vkm:n kaltaisiksi palautuksiksi.

Huom. adapteri ei kata vielä kaikkia vanhan vkm:n toimintoja, ja rataosoitemuunnoksia se ei kata vielä lainkaan.