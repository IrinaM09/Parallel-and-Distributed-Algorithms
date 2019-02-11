MITOCARU IRINA
334CB

														  _______                   ___  
														 |__   __|                 |__ \ 
														    | | ___ _ __ ___   __ _   ) |
														    | |/ _ \ '_ ` _ \ / _` | / / 
														    | |  __/ | | | | | (_| |/ /_ 
														    |_|\___|_| |_| |_|\__,_|____|
														                                 
--------------------------
CommunicationChannel.java:

1. 	Pentru a preveni ca mai multe threaduri sa acceseze in acelasi timp structurile in care se adauga mesajele de la mineri, 
	respectiv vrajitori, am folosit o coada blocanta (LinkedBlockingQueue a interfei BlockingQueue) la ambele structuri, care
	asigura:
	  - o capacitate nelimitata;
	  - wait() pana cand lista nu mai este goala.

2.	Pentru ca mesajele trimise de vrajitori sa nu se intercaleze, am folosit un semafor pe post de lock. Acesta e initializat cu 1,
	iar in momentul in care se trimite primul mesaj dintr-un set de mesaje, se face acquire(), deci permits va fi 0. Doar la primul mesaj
	dintr-un set de mesaje se face acquire(). In cazul in care un alt thread incearca sa adauge un mesaj in structura, e obligat sa faca acquire(),
	deci permits va fi tot negativ si astfel threadul este blocat pana in momentul in care primul thread face release().
	Release() se face doar in momentul in care vrajitorul a terminat trimiterea setului de mesaje, adica cand se trimite (-1,"END").


Structuri folosite:
  --   LinkedBlockingQueue<Message> minerQueue - structura in care minerii adauga si din care vrajitorii citesc;
  --   LinkedBlockingQueue<Message> wizardQueue - structura in care vrajitorii adauga si din care minerii citesc;
  --   Semaphore cu 1 permit - folosit pentru ca alti vrajitori sa nu adauge mesaje;
  --   LinkedList<String> firstMessage - structura in se salveaza ID-ul cananalului de comunicatie, pentru a sti al carui
  										 thread este primul mesaj din set.


-----------
Miner.java:

1.	Pentru ca mai multi mineri sa nu citeasca mesajele intercalate (unul sa citeasca primul mesaj si al doilea celalalt mesaj), am folosit
	un semafor cu 1 permit, astfel incat cele doua mesaje sunt citite doar daca threadul trece de acquire, deci numarul de permits este > 0.
	Cand se termina citirea celor 2 mesaje, se face release() si alt thread (miner) are voie sa citeasca 2 mesaje.


2.	Doar primul mesaj extras va putea primi (-1, NO_PARENT) sau (-1, EXIT). Mesajele (-1, END) nu sunt adaugate in structura, ci
	sunt folosite doar pentru a evita intercalarea mesajelor.
	Mereu se vor extrage cate doua mesaje consecutive, al doilea fiind cel care indica camera care urmeaza a fi rezolvata.
	Pentru a evita rezolvarea unei camere de mai multe ori, se verifica in setul solved daca aceasta exista. Daca nu exista, se rezolva
	si se trimite vrajitorilor camera rezolvata impreuna cu raspunsul, altfel se sare.
