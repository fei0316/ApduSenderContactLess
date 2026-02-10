# ApduSenderContactLess
A fork of application with the same name developed by Jose Maria ARROYO, 
available at https://github.com/jmarroyo/ApduSenderContactLess under the GPLv3 license. 
Therefore, this fork is also available under GPLv3.

This application was modified to serve one single purpose only: 
read transit cards issued in Macau (MacauPass) for further analysis.
You likely will not find this application useful.


## Disclaimer 免責聲明
This application is a simple APDU command sender designed for READING unprotected/unencrypted data
from a transit card ONLY. It is no different from any balance checker applications.

While it can be used to issue APDU commands that write data or read from protected areas, such 
actions will fail without mutual authentication, which requires private keys not available to public
and NOT shipped with this app. 

Therefore, this application CANNOT be used for malicious purposes such as modifying wallet balance. 

This application is NOT AFFILIATED with any entities and NOT GUARANTEED to work with MacauPass 
products. It is PURELY for my personal use and I am sharing it PURELY out of my desire to share.  

Tl;dr: don't sue me :P


本應用程式僅為一個簡單的 APDU 指令發送工具，設計用於從交通卡讀取未經加密或受保護的資料。它與任何查閱餘額的應用程式無異。

雖然本程式可用於發送寫入資料或讀取受保護區域的 APDU 指令，但若缺乏雙向認證，有關操作均會失敗；而雙向認證所需的私鑰並不公開，亦不會隨本應用程式提供。

因此，本應用程式無法用於惡意用途（如篡改電子錢包餘額）。

本應用程式與任何機構均無關聯，亦不保證支援澳門通產品。本程式純粹供本人個人使用，本人選擇公開源碼純粹出於分享意願。

長話短說：別告我 :P