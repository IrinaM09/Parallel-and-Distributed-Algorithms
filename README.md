MITOCARU IRINA
334CB

														  _______                   ___  
														 |__   __|                 |__ \ 
														    | | ___ _ __ ___   __ _   ) |
														    | |/ _ \ '_ ` _ \ / _` | / / 
														    | |  __/ | | | | | (_| |/ /_ 
														    |_|\___|_| |_| |_|\__,_|____|
														                                 
														                                 
Pentru a preveni ca mai multe threaduri sa acceseze in acelasi timp structurile in care se adauga mesajele de la mineri, 
respectiv vrajitori, am folosit o coada blocanta (LinkedBlockingQueue a interfei BlockingQueue) la ambele structuri, care
asigura:
	- o capacitate nelimitata;
	- wait() pana cand lista nu mai este goala.

Pentru a preveni intercalarea mesajelor trimise vrajitori, am folosit un semafor pe post de lock. Acesta e initializat cu 1,
iar in momentul in care se trimite primul mesaj dintr-un set de mesaje, se face acquire(), deci permits va fi 0. Doar la primul mesaj
dintr-un set de mesaje se face acquire(). In cazul in care un alt thread incearca sa adauge un mesaj in structura, e obligat sa faca acquire(),
deci permits va fi tot negativ si astfel threadul este blocat pana in momentul in care primul thread face release().
Release() se face doar in momentul in care vrajitorul a terminat trimiterea setului de mesaje, adica cand se trimite (-1,"END").

--------------------------
CommunicationChannel.java:

Structuri folosite:
  --   LinkedBlockingQueue<Message> minerQueue - structura in care minerii adauga si din care vrajitorii citesc;
  --   LinkedBlockingQueue<Message> wizardQueue - structura in care vrajitorii adauga si din care minerii citesc;
  --   Semaphore cu 1 permit - folosit pentru ca alti vrajitori sa nu adauge mesaje;
  --   LinkedList<String> firstMessage - structura in se salveaza ID-ul cananalului de comunicatie, pentru a sti al carui
  										 thread este primul mesaj din set.


-----------
Miner.java:

Doar primul mesaj extras va putea primi (-1, NO_PARENT) sau (-1, EXIT). Mesajele (-1, END) nu sunt adaugate in structura, ci
sunt folosite doar pentru a evita intercalarea mesajelor.
Mereu se vor extrage cate doua mesaje consecutive, al doilea fiind cel care indica camera care urmeaza a fi rezolvata.
Pentru a evita rezolvarea unei camere de mai multe ori, se verifica in setul solved daca aceasta exista. Daca nu exista, se rezolva
si se trimite vrajitorilor camera rezolvata impreuna cu raspunsul, altfel se sare.
