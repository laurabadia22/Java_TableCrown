<#import "common/catalogoGestore.ftl" as catGestore>

<@catGestore.renderCatalogoGestore
    titolo="Gestione Porta Dadi"
    subpage="catalogo_portadadi"
    urlBase="${base_url}/gestore/catalogo/porta-dadi"
    urlNuovo="/gestore/crea/porta-dadi"
    prodotti=prodotti
    paginaCorrente=paginaCorrente
    totalePagine=totalePagine
    query=query
    breadcrumbs=breadcrumbs
/>