(ns petardo.scrape.findbehandler
  (:require [clj-http.client :as client]
            [clj-http.core]
            [clj-http.cookies]
            [clojure.edn]
            [clojure.string]))

(defn get-organizations-from-id
  [id]
  (:Organizations
   (:body
    (client/get (format "https://www.sundhed.dk/api/core/organisation/%d" id)
                {:as     :json
                 :accept :json}))))

(defn get-organization-people-names
  [organization]
  (->> organization
       :People
       flatten
       (map :Name)))

(defn format-organization
  [[id organizations]]
  (flatten (map (fn [{:keys [name people address zipcode city]}]
                  (map (fn [person-name]
                         (clojure.string/join "|" [(clojure.core/name id) name person-name (format "%s, %d %s" address zipcode city)]))
                       people))
                organizations)))

(defn -main
  []
  (let [results (atom {})
        cookie-store (clj-http.cookies/cookie-store)]

    (binding [clj-http.core/*cookie-store* cookie-store]
      ;; issue a first request to get a cookie
      (client/get "https://www.sundhed.dk/borger/guides/find-behandler/?Page=2&Informationskategori=Praktiserende%20l%C3%A6ge")
      (client/get "https://www.sundhed.dk/api/core/startupsettings"))

    (let [organizations
          (binding [clj-http.core/*cookie-store* cookie-store]
            (-> "https://www.sundhed.dk/app/findbehandler/api/v1/findbehandler/search?Page=1&Pagesize=99999&RegionId=0&MunicipalityId=0&Sex=0&AgeGroup=0&Informationskategori=Praktiserende%20l%C3%A6ge&InformationsUnderkategori=&DisabilityFriendlyAccess=false&GodAdgang=false&EMailConsultation=false&EMailAppointmentReservation=false&EMailPrescriptionRenewal=false&TakesNewPatients=false&TreatmentAtHome=false&WaitTime=false&Name=&Latitude=null&Longitude=null&Address=null"
                (client/get {:as :json})
                :body
                :Organizations))]

      ;; we use reduce to be a bit nice to sundhed.dk and run sequential
      ;; requests
      (reduce
       (fn [_ {:keys [Name OrganizationId WebAdresse]}]
         (try
           (binding [*out* *err*]
             (prn "fetching" OrganizationId))
           (let [organizations (get-organizations-from-id OrganizationId)]
             (->> organizations
                  (map (fn [{:keys [Address ZipCode City] :as organization}]
                         {:name    Name
                          :people  (get-organization-people-names organization)
                          :address Address
                          :zipcode ZipCode
                          :city    City
                          :website WebAdresse}))
                  (swap! results assoc OrganizationId)))
           (catch Exception ex
             (prn "failed" ex)
             nil))
         (Thread/sleep 200))
       nil
       organizations))
    (println
     (clojure.string/join "\n"
                          (cons
                           "Clinic ID|Clinic name|Doctor name|Address"
                           (flatten
                            (map format-organization @results)))))))
