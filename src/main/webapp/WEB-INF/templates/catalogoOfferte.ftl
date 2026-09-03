<#--
  Importa la macro condivisa che gestisce layout, filtri,
  impaginazione e carrello per tutti i cataloghi
-->
<#import "common/catalogoSemplice.ftl" as cs>

<#--
  Richiama la macro passando i parametri specifici per la rotta delle offerte.
-->
<@cs.renderCatalogoSemplice
titolo="Offerte Speciali"
subpage="offerte"
urlBase="${base_url}/offerte"
prodotti=prodotti
filtri=filtri
paginaCorrente=paginaCorrente
totalePagine=totalePagine
query=query!""
breadcrumbs=breadcrumbs />