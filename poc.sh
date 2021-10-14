#!/usr/bin/env bash

cookiejar=$(mktemp)
curl -s --cookie-jar "${cookiejar}" --cookie "${cookiejar}" 'https://www.sundhed.dk/borger/guides/find-behandler/?Page=2&Informationskategori=Praktiserende%20l%C3%A6ge'

curl  --cookie-jar "${cookiejar}" --cookie "${cookiejar}" \
      'https://www.sundhed.dk/api/core/startupsettings' \
      -H 'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0' \
      -H 'Accept: application/json, text/plain, */*' \
      -H 'Accept-Language: en-GB,en-US;q=0.7,en;q=0.3' \
      -H 'x-queueit-ajaxpageurl: https%3A%2F%2Fwww.sundhed.dk%2Fborger%2Fguides%2Ffind-behandler%2F%3FInformationskategori%3DPraktiserende%2520l%25C3%25A6ge' \
      -H 'Referer: https://www.sundhed.dk/borger/guides/find-behandler/?Informationskategori=Praktiserende%20l%C3%A6ge'

curl -v --cookie-jar "${cookiejar}" --cookie "${cookiejar}" \
     'https://www.sundhed.dk/app/findbehandler/api/v1/findbehandler/search?Page=1&Pagesize=99999&RegionId=0&MunicipalityId=0&Sex=0&AgeGroup=0&Informationskategori=Praktiserende%20l%C3%A6ge&InformationsUnderkategori=&DisabilityFriendlyAccess=false&GodAdgang=false&EMailConsultation=false&EMailAppointmentReservation=false&EMailPrescriptionRenewal=false&TakesNewPatients=false&TreatmentAtHome=false&WaitTime=false&Name=&Latitude=null&Longitude=null&Address=null&SearchId=288054ce-3e1e-44bd-aba4-d46c9594567e' \
     -H 'Accept: application/json, text/plain, */*' \
     -H 'action_identifier: app.findbehandler.searchresults' \
     -H 'Referer: https://www.sundhed.dk/borger/guides/find-behandler/?Informationskategori=Praktiserende%20l%C3%A6ge'

curl \
     'https://www.sundhed.dk/api/core/organisation/61685' \
     -H 'Accept: application/json, text/plain, */*' \
     -H 'action_identifier: app.findbehandler.searchresults' \
     -H 'Referer: https://www.sundhed.dk/borger/guides/find-behandler/?orgId=61685'
