(ns aula.example_1_schemas
  (:require [datomic.client.api :as d]))

(def schema [
             ;; genre
             {:db/ident :genre/id
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc "Genre's id"
              :db/unique :db.unique/identity}
             {:db/ident :genre/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "Genre's name"}
             
             ;; albums
             {:db/ident :albums/id
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc "Album's id"
              :db/unique :db.unique/identity}
             {:db/ident :albums/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "Album's name"}
             {:db/ident :albums/date-release
              :db/valueType :db.type/instant
              :db/cardinality :db.cardinality/one
              :db/doc "Album's date release"}
             {:db/ident :albums/artist
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc "Album's artist id"}
             {:db/ident :albums/genre
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc "Album's genre id"}
             
             ;; artists
             {:db/ident :artists/id
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc "Artist's id"
              :db/unique :db.unique/identity}
             {:db/ident :artists/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "Artist's name"}
             ])

(comment
  (do
    (def client (d/client {:server-type :dev-local
                           :system "dev"}))
    (d/delete-database client {:db-name "entertainment"})
    (d/create-database client {:db-name "entertainment"})
    (def conn (d/connect client {:db-name "entertainment"}))
    (d/transact conn {:tx-data schema}))

  (d/transact conn {:tx-data [;; add genre
                              {:genre/name "Alternative"
                               :genre/id 1}
                              {:genre/name "Pop Punk"
                               :genre/id 2}

                              ;; add artists
                              {:artists/name "Paramore"
                               :artists/id 1}
                              {:artists/name "My Chemical Romance"
                               :artists/id 2}]})

  ; Busca todos os gêneros com todos os atributos
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :genre/id]] (d/db conn))
  
  ; Busca um único departamento com todos os atributos
  (d/pull (d/db conn) '[*] [:artists/id 1]) ; lookup ref

  ; Busca todos os artistas com os atributos especificados dentro do pull
  (d/q '[:find (pull ?entidade
                     [:artists/name
                      :artists/id])
         :where [?entidade :artists/id]] (d/db conn))


  ;; add albums
  (d/transact conn {:tx-data [{:albums/id 1
                               :albums/name "RIOT!"
                               :albums/date-release #inst "2007-06-12T00:00:00.000-00:00"
                               :albums/artist [:artists/id 1]
                               :albums/genre [:genre/id 1]}
                              
                              {:albums/id 2
                               :albums/name "The Black Parade"
                               :albums/date-release #inst "2006-10-23T00:00:00.000-00:00"
                               :albums/artist [:artists/id 2]
                               :albums/genre [:genre/id 2]}]})

  ; Busca todos os albums
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :albums/id]] (d/db conn))

  (d/q '[:find (pull ?entidade
                     [:albums/id
                      :albums/name])
         :where [?entidade :albums/id]] (d/db conn))
  )