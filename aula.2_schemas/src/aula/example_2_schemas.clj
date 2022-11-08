(ns aula.example_2_schemas
  (:require [datomic.client.api :as d]))

(def schema [
             ;; employee
             {:db/ident :employee/name
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc "Employee's name"}
               {:db/ident :employee/job-title
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc "Employee's job's title"}
               {:db/ident :employee/phone-number
                :db/valueType :db.type/long
                :db/cardinality :db.cardinality/many
                :db/doc "Employee's phone number"}
               {:db/ident :employee/department
                :db/valueType :db.type/ref
                :db/cardinality :db.cardinality/one
                :db/doc "Employee's department"}
               {:db/ident :employee/project
                :db/valueType :db.type/ref
                :db/cardinality :db.cardinality/one
                :db/doc "Employee's project"}
             
               ;; department
               {:db/ident       :department/name
                :db/valueType   :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc         "Department's name"} 
               {:db/ident       :department/id
                :db/valueType   :db.type/string
                :db/cardinality :db.cardinality/one
                :db/unique      :db.unique/identity
                :db/doc         "Department's id"}
               
                ;; project
               {:db/ident       :project/name
                :db/valueType   :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc         "Project's name"} 
               {:db/ident       :project/id
                :db/valueType   :db.type/string
                :db/cardinality :db.cardinality/one
                :db/unique      :db.unique/identity
                :db/doc         "Project's id"}])

(comment
  (do
    (def client (d/client {:server-type :dev-local
                         :system "dev"}))
    (d/delete-database client {:db-name "enterprise"})
    (d/create-database client {:db-name "enterprise"})
    (def conn (d/connect client {:db-name "enterprise"}))
    (d/transact conn {:tx-data schema})) 
  
  (d/transact conn {:tx-data [
                              ;; add departments
                              {:department/name "BU Authorizer"
                               :department/id "ABC"}
                              {:department/name "BU Marketplace"
                               :department/id "MKT"}
                              
                              ;; add projects
                              {:project/name "Authorizer"
                               :project/id "DEF"}
                              {:project/name "Marketplace"
                               :project/id "MPL"}]})
  
  ; Busca todos os departamentos com todos os atributos
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :department/name]] (d/db conn))
  
  ; Busca um único departamento com todos os atributos
  (d/pull (d/db conn) '[*] 87960930222162) ; db/id
  (d/pull (d/db conn) '[*] [:department/id "ABC"]) ; lookup ref

  ; Busca todos os projetos com os atributos especificados dentro do pull
  (d/q '[:find (pull ?entidade 
                     [:project/name
                      :project/id])
         :where [?entidade :project/id]] (d/db conn))
  

  ;; add employees
  (d/transact conn {:tx-data [{:employee/name "Daniel Rangel"
                               :employee/job-title "Associate Software Engineer"
                               :employee/phone-number 987654321
                               :employee/department [:department/id "ABC"]
                               :employee/project [:project/id "DEF"]}
                              
                              {:employee/name "Juliana Ribeiro"
                               :employee/job-title "Associate Software Engineer"
                               :employee/phone-number 987654321
                               :employee/department [:department/id "ABC"]
                               :employee/project [:project/id "DEF"]}]})
  
  ; Busca todos os funcionários
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :employee/name]] (d/db conn))
  
  (d/q '[:find (pull ?entidade
                     [:employee/name
                      :employee/job-title])
         :where [?entidade :employee/name]] (d/db conn))
  )