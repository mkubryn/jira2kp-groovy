jira2kp-groovy
==============

Skrypt eksportujący raport czasu z Jira do wpisów KP (7bc).

---------------
Ficzery:

* Automatyczne pobieranie wpisów z Jira
* Grupowanie wpisów per-projekt
* Generowanie wpisów które można bezpośrednio wpisać do KP

---------------
Wymagania:
* Groovy 2.1+

---------------
Używanie (na posdstawie przykładu):

```
$ groovy jira2kp.groovy --help

    Usage: jira2kp [date from] [date to]
    Expml: jira2kp 23.11.2013 30.12.2013
    
$ groovy jira2kp.groovy 04.10.2013 29.10.2013
Jira login: 7bcusername
Jira password: 

[Portal-Faza4]

04.10.2013 -- 7bcusername -- 8h
  Technical task: PTRZY-1299 - testy - (7h)
  Improvement: PTRZY-1296 - code review - (1h)

08.10.2013 -- 7bcusername -- 7h
  Technical task: PTRZY-1299 - testy again.. - (7h)


[Portal-Prod]

24.10.2013 -- 7bcusername -- 1.75h
  Bug: PORTALPROD-769 - spike i rozmowy - (0,75h)
  Bug: PORTALPROD-762 - korespondecja z klientem - (1h)

28.10.2013 -- 7bcusername -- 3.5h
  Bug: PORTALPROD-769 - rozmowa z XYZ - (0,5h)
  Bug: PORTALPROD-772 - code review - (1,5h)
  Bug: PORTALPROD-770 -  - (1,5h)


[Portal-Utrzymanie]

29.10.2013 -- 7bcusername -- 0.5h
  Bug: PORTALMAINT-1407 - wtf? - (0,5h)
  
```
