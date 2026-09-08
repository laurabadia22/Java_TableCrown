<#import "common/catalogoGestore.ftl" as catGestore>

<@catGestore.renderCatalogoGestore
    titolo="Gestione Giochi da Tavolo"
    subpage="catalogo_giochi"
    urlBase="${base_url}/gestore/catalogo/giochi-da-tavolo"
    urlNuovo="/gestore/crea/gioco-da-tavolo"
    prodotti=prodotti
    paginaCorrente=paginaCorrente
    totalePagine=totalePagine
    query=query
    breadcrumbs=breadcrumbs
/>