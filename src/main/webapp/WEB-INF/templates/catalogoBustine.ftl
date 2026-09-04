<#import "common/catalogoSemplice.ftl" as cs>
<@cs.renderCatalogoBase
titolo="Bustine"
subpage="bustine"
urlBase="${base_url}/catalogo/bustine"
prodotti=prodotti
filtri=filtri
paginaCorrente=paginaCorrente
totalePagine=totalePagine
query=query!""
breadcrumbs=breadcrumbs />