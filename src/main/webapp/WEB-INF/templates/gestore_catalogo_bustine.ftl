<#import "common/catalogoGestore.ftl" as catGestore>

<@catGestore.renderCatalogoGestore
    titolo="Gestione Bustine"
    subpage="catalogo_bustine"
    urlBase="${base_url}/gestore/catalogo/bustine"
    urlNuovo="/gestore/crea/bustine"
    prodotti=prodotti
    paginaCorrente=paginaCorrente
    totalePagine=totalePagine
    query=query
    breadcrumbs=breadcrumbs
/>